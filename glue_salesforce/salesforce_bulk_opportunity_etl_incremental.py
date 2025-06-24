
import boto3
import time
import requests
import json
import tempfile
import os
from datetime import datetime
from awsglue.context import GlueContext
from awsglue.job import Job
from pyspark.context import SparkContext
from pyspark.sql import SparkSession

# Initialize Spark and Glue contexts
sc = SparkContext()
glueContext = GlueContext(sc)
spark = glueContext.spark_session
job = Job(glueContext)
job.init("salesforce_bulk_opportunity_etl_incremental", {})

# Configuration
secret_id = 'your-secret-id'
bucket_name = 'your-output-bucket'
timestamp_key = 'salesforce/opportunity/last_run/timestamp.txt'
output_path = 's3://your-output-bucket/salesforce/opportunity/'

# Load secret from Secrets Manager
secrets_client = boto3.client('secretsmanager')
secret_value = secrets_client.get_secret_value(SecretId=secret_id)
secret = json.loads(secret_value['SecretString'])

private_key = secret['private_key']
client_id = secret['client_id']
username = secret['username']
audience = secret['audience']

# Generate JWT manually using RS256
import base64
from cryptography.hazmat.primitives import serialization
from cryptography.hazmat.primitives.asymmetric import padding
from cryptography.hazmat.primitives import hashes

def generate_jwt(client_id, username, audience, private_key_pem):
    header = {
        "alg": "RS256",
        "typ": "JWT"
    }
    payload = {
        "iss": client_id,
        "sub": username,
        "aud": audience,
        "exp": int(time.time()) + 300
    }

    def b64encode(data):
        return base64.urlsafe_b64encode(json.dumps(data).encode()).rstrip(b'=')

    header_b64 = b64encode(header)
    payload_b64 = b64encode(payload)
    message = header_b64 + b'.' + payload_b64

    private_key_obj = serialization.load_pem_private_key(
        private_key_pem.encode(),
        password=None
    )

    signature = private_key_obj.sign(
        message,
        padding.PKCS1v15(),
        hashes.SHA256()
    )

    jwt_token = message + b'.' + base64.urlsafe_b64encode(signature).rstrip(b'=')
    return jwt_token.decode()

jwt_token = generate_jwt(client_id, username, audience, private_key)

# Exchange JWT for access token
response = requests.post(
    f"{audience}/services/oauth2/token",
    data={
        "grant_type": "urn:ietf:params:oauth:grant-type:jwt-bearer",
        "assertion": jwt_token
    }
)

access_token = response.json().get("access_token")
instance_url = response.json().get("instance_url")

# Read last run timestamp from S3
s3_client = boto3.client('s3')
try:
    timestamp_obj = s3_client.get_object(Bucket=bucket_name, Key=timestamp_key)
    last_run_timestamp = timestamp_obj['Body'].read().decode('utf-8').strip()
except s3_client.exceptions.NoSuchKey:
    last_run_timestamp = '2000-01-01T00:00:00Z'  # default start time

# Create Bulk API v2 job with incremental SOQL
headers = {
    "Authorization": f"Bearer {access_token}",
    "Content-Type": "application/json"
}

soql_query = f"""
SELECT Id, Name, StageName, Amount, CloseDate, LastModifiedDate
FROM Opportunity
WHERE LastModifiedDate > {last_run_timestamp}
"""

job_payload = {
    "operation": "query",
    "query": soql_query,
    "object": "Opportunity",
    "contentType": "JSON"
}

job_response = requests.post(
    f"{instance_url}/services/data/v58.0/jobs/query",
    headers=headers,
    json=job_payload
)

job_id = job_response.json()["id"]

# Poll for job completion
status = "InProgress"
while status in ["InProgress", "UploadComplete"]:
    time.sleep(10)
    job_status_response = requests.get(
        f"{instance_url}/services/data/v58.0/jobs/query/{job_id}",
        headers=headers
    )
    status = job_status_response.json()["state"]
    print(f"Job status: {status}")

# Fetch all result chunks using locator
results_url = f"{instance_url}/services/data/v58.0/jobs/query/{job_id}/results"
params = {}
all_records = []

while True:
    response = requests.get(results_url, headers=headers, params=params)
    chunk = response.json()
    all_records.extend(chunk)

    locator = response.headers.get("Sforce-Locator")
    if not locator:
        break
    params["locator"] = locator

# Save results to temp file
with tempfile.NamedTemporaryFile(delete=False, suffix=".json") as tmp_file:
    tmp_file.write(json.dumps(all_records).encode('utf-8'))
    tmp_file_path = tmp_file.name

# Load into Spark DataFrame
df = spark.read.json(tmp_file_path)

# Write to S3 in Parquet format
df.write.mode("overwrite").parquet(output_path)

# Update last run timestamp in S3
if not df.rdd.isEmpty():
    max_timestamp = df.selectExpr("max(LastModifiedDate) as max_ts").collect()[0]["max_ts"]
    if max_timestamp:
        s3_client.put_object(
            Bucket=bucket_name,
            Key=timestamp_key,
            Body=max_timestamp.encode('utf-8')
        )

# Clean up
os.remove(tmp_file_path)
job.commit()
