Here's the **complete AWS Glue script** for **incremental Salesforce data loading** using a timestamp stored in S3:

---

### ✅ Glue Script: Incremental Load with Timestamp Management

```python
import sys
import json
import boto3
from datetime import datetime, timezone
from awsglue.utils import getResolvedOptions
from pyspark.context import SparkContext
from awsglue.context import GlueContext

# Initialize Spark and Glue contexts
args = getResolvedOptions(sys.argv, ['JOB_NAME'])
sc = SparkContext()
glueContext = GlueContext(sc)
spark = glueContext.spark_session
job = glueContext.create_job(args['JOB_NAME'])

# S3 configuration
s3_bucket = "your-metadata-bucket"
s3_key = "salesforce/last_run_metadata.json"
s3_output_path = "s3://your-output-bucket/salesforce/incremental/"
metadata_path = f"s3://{s3_bucket}/{s3_key}"

# Read last run timestamp from S3
s3_client = boto3.client('s3')
response = s3_client.get_object(Bucket=s3_bucket, Key=s3_key)
metadata = json.loads(response['Body'].read().decode('utf-8'))
last_run_time = metadata.get("last_run_time", "2000-01-01T00:00:00Z")  # default if missing

# Read data from Salesforce incrementally
salesforce_df = glueContext.create_dynamic_frame.from_options(
    connection_type="salesforce",
    connection_options={
        "connectionName": "salesforce-authorization-code-conn",
        "object": "Account",
        "enablePushdownPredicate": True,
        "pushDownPredicate": f"LastModifiedDate >= '{last_run_time}'"
    }
)

# Write the data to S3 in Parquet format
glueContext.write_dynamic_frame.from_options(
    frame=salesforce_df,
    connection_type="s3",
    connection_options={"path": s3_output_path},
    format="parquet"
)

# Update the last run timestamp in S3
new_timestamp = datetime.now(timezone.utc).strftime("%Y-%m-%dT%H:%M:%SZ")
new_metadata = {"last_run_time": new_timestamp}
s3_client.put_object(
    Bucket=s3_bucket,
    Key=s3_key,
    Body=json.dumps(new_metadata),
    ContentType='application/json'
)

job.commit()
```

---

### 📁 Initial JSON File in S3

Before the first run, upload this file to `s3://your-metadata-bucket/salesforce/last_run_metadata.json`:

```json
{
  "last_run_time": "2025-06-23T00:00:00Z"
}
```

---

