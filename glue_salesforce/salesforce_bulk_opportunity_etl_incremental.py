"""
Salesforce Bulk API v2 ETL: Incremental export of Opportunity records to S3.

Refactored to use shared utilities from shared/python/ to eliminate
duplicated auth, bulk API, and Glue initialization code.
Retains the incremental timestamp tracking logic specific to this ETL variant.
"""

import sys
sys.path.insert(0, "..")

import boto3

from shared.python.aws_utils import get_secret_json, read_s3_text, write_s3_text
from shared.python.salesforce_auth import authenticate
from shared.python.salesforce_bulk_api import run_query
from shared.python.glue_utils import init_glue_job, records_to_parquet

# Configuration
SECRET_ID = "your-secret-id"
BUCKET_NAME = "your-output-bucket"
TIMESTAMP_KEY = "salesforce/opportunity/last_run/timestamp.txt"
OUTPUT_PATH = "s3://your-output-bucket/salesforce/opportunity/"

# Initialize Spark and Glue contexts
spark, glue_context, glue_job = init_glue_job("salesforce_bulk_opportunity_etl_incremental")

# Load secret from Secrets Manager
secret = get_secret_json(SECRET_ID)

# Authenticate with Salesforce
access_token, instance_url = authenticate(
    client_id=secret["client_id"],
    username=secret["username"],
    login_url=secret["audience"],
    private_key=secret["private_key"],
)

# Read last run timestamp from S3
try:
    last_run_timestamp = read_s3_text(BUCKET_NAME, TIMESTAMP_KEY)
except boto3.client("s3").exceptions.NoSuchKey:
    last_run_timestamp = "2000-01-01T00:00:00Z"

# Run incremental Bulk API query
soql = f"""
SELECT Id, Name, StageName, Amount, CloseDate, LastModifiedDate
FROM Opportunity
WHERE LastModifiedDate > {last_run_timestamp}
"""
records = run_query(instance_url, access_token, soql)

# Write to S3 in Parquet format
df = records_to_parquet(spark, records, OUTPUT_PATH)

# Update last run timestamp in S3
if not df.rdd.isEmpty():
    max_timestamp = df.selectExpr("max(LastModifiedDate) as max_ts").collect()[0]["max_ts"]
    if max_timestamp:
        write_s3_text(BUCKET_NAME, TIMESTAMP_KEY, max_timestamp)

glue_job.commit()
