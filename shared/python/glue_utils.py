"""
Shared AWS Glue + Spark utility functions.

Consolidates the duplicated initialization and data pipeline patterns from:
  - glue_salesforce/sf_bulk_opportunity.py (lines 14-19, 90-102)
  - glue_salesforce/salesforce_bulk_opportunity_etl_incremental.py (lines 14-19, 151-173)
  - glue_salesforce/salesforce_bulk_opportunity_etl_pagination.py (lines 14-18, 100-113)
"""

import json
import os
import tempfile

from pyspark.context import SparkContext
from awsglue.context import GlueContext
from awsglue.job import Job


def init_glue_job(job_name, args=None):
    """
    Initialize Spark, Glue context, and a Glue Job.

    Returns:
        tuple of (spark_session, glue_context, job)
    """
    if args is None:
        args = {}
    sc = SparkContext()
    glue_context = GlueContext(sc)
    spark = glue_context.spark_session
    glue_job = Job(glue_context)
    glue_job.init(job_name, args)
    return spark, glue_context, glue_job


def records_to_parquet(spark, records, output_s3_path):
    """
    Write a list of record dicts to S3 as Parquet via a Spark DataFrame.

    Uses a temp file as an intermediate step (matching the existing pattern).

    Returns:
        The Spark DataFrame (for further processing like extracting timestamps).
    """
    with tempfile.NamedTemporaryFile(delete=False, suffix=".json") as tmp:
        tmp.write(json.dumps(records).encode("utf-8"))
        tmp_path = tmp.name

    try:
        df = spark.read.json(tmp_path)
        df.write.mode("overwrite").parquet(output_s3_path)
        return df
    finally:
        os.remove(tmp_path)
