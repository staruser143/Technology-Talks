"""
Salesforce Bulk API v2 ETL: Full export with pagination of Opportunity records to S3.

Refactored to use shared utilities from shared/python/ to eliminate
duplicated auth, bulk API, and Glue initialization code.
The pagination (Sforce-Locator) handling is now built into
salesforce_bulk_api.fetch_results().
"""

import sys
sys.path.insert(0, "..")

from shared.python.aws_utils import get_secret_json
from shared.python.salesforce_auth import authenticate
from shared.python.salesforce_bulk_api import run_query
from shared.python.glue_utils import init_glue_job, records_to_parquet

# Initialize Spark and Glue contexts
spark, glue_context, glue_job = init_glue_job("salesforce_bulk_opportunity_etl")

# Load secret from Secrets Manager
secret = get_secret_json("your-secret-id")

# Authenticate with Salesforce
access_token, instance_url = authenticate(
    client_id=secret["client_id"],
    username=secret["username"],
    login_url=secret["audience"],
    private_key=secret["private_key"],
)

# Run Bulk API query (pagination handled internally by run_query)
soql = "SELECT Id, Name, StageName, Amount, CloseDate FROM Opportunity"
records = run_query(instance_url, access_token, soql)

# Write to S3 in Parquet format
records_to_parquet(spark, records, "s3://your-output-bucket/salesforce/opportunity/")

glue_job.commit()
