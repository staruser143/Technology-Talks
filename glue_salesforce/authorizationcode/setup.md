In the context of provisioning an **AWS Glue connection to Salesforce using OAuth Authorization Code flow**, the **Terraform `aws_glue_connection` resource should reference the secret containing the refresh token**, not just the client secret.

---

### 🔐 Why the Refresh Token Is Required

AWS Glue uses the **refresh token** to obtain **access tokens** dynamically when executing jobs. The access token is what allows Glue to authenticate with Salesforce APIs. The client secret alone is insufficient — it’s only used in the token exchange process.

---

### ✅ What to Store in AWS Secrets Manager

Your secret should include:

```json
{
  "USER_MANAGED_CLIENT_APPLICATION_CLIENT_ID": "<your-client-id>",
  "USER_MANAGED_CLIENT_APPLICATION_CLIENT_SECRET": "<your-client-secret>",
  "REFRESH_TOKEN": "<your-refresh-token>"
}
```

This secret is referenced in Terraform like so:

```hcl
connection_properties = {
  INSTANCE_URL         = "https://your-instance.my.salesforce.com"
  ENVIRONMENT          = "PRODUCTION"
  AUTHENTICATION_TYPE  = "OAUTH2"
  OAUTH2_GRANT_TYPE    = "AUTHORIZATION_CODE"
  SECRET_ID            = "arn:aws:secretsmanager:<region>:<account-id>:secret:<your-secret-name>"
}
```

---

Great question! When using the **AWS Glue Salesforce connector**, you **cannot directly provide a SOQL query** in the Glue script. Instead, you specify the **Salesforce object name**, and optionally use **pushdown predicates** to filter the data.

---

### ❌ Not Supported
You **cannot** do this:
```python
connection_options={
    "connectionName": "salesforce-authorization-code-conn",
    "query": "SELECT Id, Name FROM Account WHERE CreatedDate > LAST_N_DAYS:30"
}
```

---

### ✅ Supported Approach

You can use **pushdown predicates** to filter data, which Glue translates into SOQL under the hood:

```python
connection_options={
    "connectionName": "salesforce-authorization-code-conn",
    "object": "Account",
    "enablePushdownPredicate": True,
    "pushDownPredicate": "CreatedDate >= '2024-06-01T00:00:00Z'"
}
```

This is the closest alternative to writing a custom SOQL query.

---

### 🧠 Workaround for Complex Queries

If you need to run **complex SOQL queries**, consider this approach:

1. **Use a Lambda function or custom script** to query Salesforce using SOQL.
2. Store the result in S3.
3. Use Glue to process the data from S3.

To implement **incremental data loading** from Salesforce to S3 using AWS Glue, you’ll need to track and filter records based on a timestamp field like `LastModifiedDate` or `SystemModstamp`. Here's how you can do it:

---

### ✅ Strategy for Incremental Loads

1. **Use a timestamp field** in Salesforce (e.g., `LastModifiedDate`) to identify new or changed records.
2. **Store the last successful run timestamp** in a metadata store (e.g., S3, DynamoDB, or Glue job parameters).
3. **Filter records** in the Glue job using `pushDownPredicate` based on that timestamp.
4. **Update the stored timestamp** after each successful run.

---

### 🧾 Sample Glue Script with Incremental Logic

```python
import sys
from datetime import datetime
from awsglue.utils import getResolvedOptions
from awsglue.context import GlueContext
from pyspark.context import SparkContext
from awsglue.dynamicframe import DynamicFrame

# Get job parameters
args = getResolvedOptions(sys.argv, ['JOB_NAME', 'last_run_time'])
last_run_time = args['last_run_time']  # e.g., '2025-06-23T00:00:00Z'

sc = SparkContext()
glueContext = GlueContext(sc)
spark = glueContext.spark_session
job = glueContext.create_job(args['JOB_NAME'])

# Read incrementally from Salesforce
salesforce_df = glueContext.create_dynamic_frame.from_options(
    connection_type="salesforce",
    connection_options={
        "connectionName": "salesforce-authorization-code-conn",
        "object": "Account",
        "enablePushdownPredicate": True,
        "pushDownPredicate": f"LastModifiedDate >= '{last_run_time}'"
    }
)

# Write to S3
glueContext.write_dynamic_frame.from_options(
    frame=salesforce_df,
    connection_type="s3",
    connection_options={"path": "s3://your-bucket/salesforce/incremental/"},
    format="parquet"
)

job.commit()
```

---

### 🗂️ Managing `last_run_time`

You can manage the `last_run_time` in several ways:

- **Glue Job Parameters**: Pass it as a parameter during job scheduling.
- **DynamoDB**: Store and retrieve the timestamp programmatically.
- **S3 File**: Read/write a small JSON file with the timestamp.

To manage the `last_run_time` for incremental Salesforce loads in AWS Glue, here's a robust and automated approach using **Amazon S3** as a lightweight metadata store.

---

### ✅ Overview of the Solution

1. **Store the last run timestamp** in a small JSON file in S3.
2. **Read the timestamp** at the start of the Glue job.
3. **Use it in the `pushDownPredicate`** to filter Salesforce records.
4. **Update the timestamp** at the end of the job with the current time.

---

### 🧾 Sample JSON File in S3

```json
{
  "last_run_time": "2025-06-23T00:00:00Z"
}
```

---

### 🧠 Glue Script Logic

Here’s what the script will do:

- Read the JSON file from S3.
- Extract `last_run_time`.
- Use it in the Salesforce query.
- After successful processing, update the file with the current timestamp.

---

Would you like me to generate the complete Glue script that includes:

- Reading and writing the timestamp from S3
- Using it in the Salesforce connector
- Writing the output to S3

