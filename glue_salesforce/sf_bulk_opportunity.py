
import boto3
import jwt
import time
import requests
import json
import tempfile
import os
from awsglue.context import GlueContext
from awsglue.job import Job
from pyspark.context import SparkContext
from pyspark.sql import SparkSession

# Initialize Spark and Glue contexts
sc = SparkContext()
glueContext = GlueContext(sc)
spark = glueContext.spark_session
job = Job(glueContext)
job.init("salesforce_bulk_opportunity_etl", {})

# Load secret from Secrets Manager
secrets_client = boto3.client('secretsmanager')
secret_value = secrets_client.get_secret_value(SecretId='your-secret-id')
secret = json.loads(secret_value['SecretString'])

private_key = secret['private_key']
client_id = secret['client_id']
username = secret['username']
audience = secret['audience']

# Generate JWT
payload = {
    "iss": client_id,
    "sub": username,
    "aud": audience,
    "exp": int(time.time()) + 300
}
jwt_token = jwt.encode(payload, private_key, algorithm='RS256')

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

# Create Bulk API v2 job
headers = {
    "Authorization": f"Bearer {access_token}",
    "Content-Type": "application/json"
}

job_payload = {
    "operation": "query",
    "query": "SELECT Id, Name, StageName, Amount, CloseDate FROM Opportunity",
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

# Get result URLs
results_response = requests.get(
    f"{instance_url}/services/data/v58.0/jobs/query/{job_id}/results",
    headers=headers
)

# Save results to temp file
with tempfile.NamedTemporaryFile(delete=False, suffix=".json") as tmp_file:
    tmp_file.write(results_response.content)
    tmp_file_path = tmp_file.name

# Load into Spark DataFrame
df = spark.read.json(tmp_file_path)

# Write to S3 in Parquet format
df.write.mode("overwrite").parquet("s3://your-output-bucket/salesforce/opportunity/")

# Clean up
os.remove(tmp_file_path)
job.commit()
