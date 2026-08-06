"""
Shared AWS utility functions.

Consolidates the duplicated Secrets Manager retrieval pattern from:
  - glue_salesforce/sf_bulk_opportunity.py (lines 22-24)
  - glue_salesforce/salesforce_bulk_opportunity_etl_incremental.py (lines 28-30)
  - glue_salesforce/salesforce_bulk_opportunity_etl_pagination.py (lines 22-24)
  - glue_salesforce/new/glue_salesforce_jwt_auth.py (lines 15-18)
  - glue_salesforce/new/python_salesforce_query.py (lines 14-17)
"""

import json
import boto3


def get_secret(secret_id, region_name="us-east-1"):
    """Retrieve a secret string from AWS Secrets Manager."""
    client = boto3.client("secretsmanager", region_name=region_name)
    response = client.get_secret_value(SecretId=secret_id)
    return response["SecretString"]


def get_secret_json(secret_id, region_name="us-east-1"):
    """Retrieve and JSON-parse a secret from AWS Secrets Manager."""
    raw = get_secret(secret_id, region_name)
    return json.loads(raw)


def read_s3_text(bucket, key):
    """Read a text object from S3, returning its content as a string."""
    s3 = boto3.client("s3")
    obj = s3.get_object(Bucket=bucket, Key=key)
    return obj["Body"].read().decode("utf-8").strip()


def write_s3_text(bucket, key, content):
    """Write a text string to an S3 object."""
    s3 = boto3.client("s3")
    s3.put_object(Bucket=bucket, Key=key, Body=content.encode("utf-8"))
