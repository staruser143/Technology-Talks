Using a **Python Shell job in AWS Glue** with the `simple-salesforce` library is a lightweight and flexible alternative to the native Salesforce connector. It’s especially useful in certain scenarios.

---

## ✅ When to Use AWS Glue Python Shell + Simple Salesforce

| Use Case | Why It’s a Good Fit |
|----------|---------------------|
| **Lightweight data pulls** | Ideal for small to medium datasets or selective queries |
| **Custom API logic** | You need to call custom Salesforce endpoints or handle pagination manually |
| **No need for Glue DynamicFrames** | You prefer working with raw Python and `boto3` for S3 |
| **Avoiding Marketplace costs** | No need to pay for the Salesforce connector |
| **More control over authentication** | You want to manage OAuth manually or use Secrets Manager flexibly |

---

## 🔧 Required Setup

### 1. **Install Dependencies**
In the Glue job configuration:
- Set **Job type** to **Python Shell**
- Use **Python version 3.9+**
- Under **Python library path**, add:

```bash
--additional-python-modules simple-salesforce
```

You can also include `boto3` if you're writing to S3.

---

### 2. **Connection Details for Simple Salesforce**

You’ll need:

- **Username**
- **Password**
- **Security Token** (appended to password)
- OR use **OAuth 2.0** with:
  - `client_id`
  - `client_secret`
  - `refresh_token`
  - `access_token_url`

---

### 3. **Sample Code Using Username + Password + Token**

```python
from simple_salesforce import Salesforce
import boto3
import json

# Connect to Salesforce
sf = Salesforce(
    username='your_username',
    password='your_password',
    security_token='your_token'
)

# Query data
results = sf.query("SELECT Id, Name FROM Account")

# Write to S3
s3 = boto3.client('s3')
s3.put_object(
    Bucket='your-bucket-name',
    Key='salesforce/accounts.json',
    Body=json.dumps(results['records'])
)
```

---

### 4. **Using OAuth 2.0 (Optional)**

If you prefer OAuth:

```python
from simple_salesforce import Salesforce
import requests

# Get access token using refresh token
response = requests.post(
    'https://login.salesforce.com/services/oauth2/token',
    data={
        'grant_type': 'refresh_token',
        'client_id': 'your_client_id',
        'client_secret': 'your_client_secret',
        'refresh_token': 'your_refresh_token'
    }
)

access_token = response.json()['access_token']
instance_url = response.json()['instance_url']

# Connect using access token
sf = Salesforce(instance_url=instance_url, session_id=access_token)
```

---
The definition of a **"small to medium dataset"** can vary depending on the context, but in the case of **AWS Glue Python Shell jobs** and **Salesforce data extraction**, here’s a practical guideline:

---

## 📊 General Size Guidelines

| Dataset Size     | Record Count (approx.) | File Size (JSON/CSV) | Suitable for Python Shell? |
|------------------|------------------------|-----------------------|-----------------------------|
| **Small**        | < 10,000               | < 10 MB               | ✅ Yes                      |
| **Medium**       | 10,000 – 100,000       | 10 MB – 100 MB        | ✅ Yes (with care)          |
| **Large**        | 100,000+               | > 100 MB              | ⚠️ Not ideal                |

---

## 🧠 Why This Matters for Python Shell Jobs

- **Python Shell jobs** run on a **single node** with limited memory (up to 16 GB).
- They are **not distributed**, unlike Spark-based Glue jobs.
- Large datasets can lead to:
  - Memory errors
  - Long execution times
  - API rate limits from Salesforce

---

## ✅ Best Practices for Medium Datasets

- Use **pagination** when querying Salesforce (e.g., `nextRecordsUrl`)
- Write data to S3 in **batches**
- Monitor **API limits** (Salesforce has daily and per-user limits)
- Consider **splitting jobs** by object or time range (e.g., `LastModifiedDate`)

---

Here’s a **sample AWS Glue Python Shell job script** that:

- Connects to **Salesforce** using `simple-salesforce`
- Handles **pagination** using `nextRecordsUrl`
- Writes data to **Amazon S3** in **chunks**

---

## 🧾 Sample Script: Salesforce to S3 with Pagination

```python
import boto3
import json
from simple_salesforce import Salesforce
import os

# Salesforce credentials (replace with Secrets Manager retrieval in production)
sf = Salesforce(
    username='your_username',
    password='your_password',
    security_token='your_security_token'
)

# S3 setup
s3 = boto3.client('s3')
bucket_name = 'your-s3-bucket-name'
s3_prefix = 'salesforce/accounts/'

# Query Salesforce
query = "SELECT Id, Name, Industry FROM Account"
results = sf.query(query)

# Pagination loop
batch_num = 1
while True:
    records = results['records']
    # Remove attributes metadata
    for record in records:
        record.pop('attributes', None)

    # Write to S3
    s3.put_object(
        Bucket=bucket_name,
        Key=f"{s3_prefix}accounts_batch_{batch_num}.json",
        Body=json.dumps(records)
    )
    print(f"Uploaded batch {batch_num} with {len(records)} records.")

    # Check for next page
    if not results.get('nextRecordsUrl'):
        break

    results = sf.query_more(results['nextRecordsUrl'], True)
    batch_num += 1
```

---

## 🛡️ Best Practices

- Replace hardcoded credentials with **AWS Secrets Manager**.
- Use **try/except** blocks for error handling.
- Monitor **Salesforce API limits** using `sf.limits()`.

---

Here’s how you can securely use **AWS Secrets Manager** in your **AWS Glue Python Shell job** to retrieve Salesforce credentials and connect using `simple-salesforce`.

---

## 🔐 Step 1: Store the Secret in AWS Secrets Manager

Create a secret with the following JSON structure:

```json
{
  "username": "your_salesforce_username",
  "password": "your_salesforce_password",
  "security_token": "your_salesforce_security_token"
}
```

Name the secret something like: `salesforce/glue/credentials`

---

## 🧾 Step 2: Glue Python Shell Script Using Secrets Manager

```python
import boto3
import json
from simple_salesforce import Salesforce

# Load secret from AWS Secrets Manager
def get_salesforce_credentials(secret_name):
    client = boto3.client('secretsmanager')
    response = client.get_secret_value(SecretId=secret_name)
    secret = json.loads(response['SecretString'])
    return secret

# Retrieve credentials
secret_name = 'salesforce/glue/credentials'
creds = get_salesforce_credentials(secret_name)

# Connect to Salesforce
sf = Salesforce(
    username=creds['username'],
    password=creds['password'],
    security_token=creds['security_token']
)

# Example query
results = sf.query("SELECT Id, Name FROM Account")
print(f"Retrieved {len(results['records'])} records.")
```

---

## ✅ IAM Role Permissions

Make sure your Glue job’s IAM role has permission to access the secret:

```json
{
  "Effect": "Allow",
  "Action": "secretsmanager:GetSecretValue",
  "Resource": "arn:aws:secretsmanager:your-region:your-account-id:secret:salesforce/glue/credentials-*"
}
```

---

Here’s a complete **AWS Glue Python Shell job script** that:

- Retrieves **Salesforce credentials** from **AWS Secrets Manager**
- Connects to Salesforce using `simple-salesforce`
- Handles **pagination** using `nextRecordsUrl`
- Writes each batch of records to **Amazon S3** in JSON format

---

## 🧾 Full Script: Salesforce to S3 with Secrets Manager & Pagination

```python
import boto3
import json
from simple_salesforce import Salesforce

# Load secret from AWS Secrets Manager
def get_salesforce_credentials(secret_name):
    client = boto3.client('secretsmanager')
    response = client.get_secret_value(SecretId=secret_name)
    secret = json.loads(response['SecretString'])
    return secret

# Retrieve credentials
secret_name = 'salesforce/glue/credentials'
creds = get_salesforce_credentials(secret_name)

# Connect to Salesforce
sf = Salesforce(
    username=creds['username'],
    password=creds['password'],
    security_token=creds['security_token']
)

# S3 setup
s3 = boto3.client('s3')
bucket_name = 'your-s3-bucket-name'
s3_prefix = 'salesforce/accounts/'

# Query Salesforce
query = "SELECT Id, Name, Industry FROM Account"
results = sf.query(query)

# Pagination loop
batch_num = 1
while True:
    records = results['records']
    # Remove metadata
    for record in records:
        record.pop('attributes', None)

    # Write to S3
    s3.put_object(
        Bucket=bucket_name,
        Key=f"{s3_prefix}accounts_batch_{batch_num}.json",
        Body=json.dumps(records)
    )
    print(f"Uploaded batch {batch_num} with {len(records)} records.")

    # Check for next page
    if not results.get('nextRecordsUrl'):
        break

    results = sf.query_more(results['nextRecordsUrl'], True)
    batch_num += 1
```

---

## ✅ What You Need to Configure

- Replace `your-s3-bucket-name` with your actual S3 bucket name.
- Ensure your Glue job’s **IAM role** has:
  - `secretsmanager:GetSecretValue`
  - `s3:PutObject` permissions
- Add `simple-salesforce` to `--additional-python-modules` in the job config.

---


Here are some **common issues and pitfalls** you might encounter when using an **AWS Glue Python Shell job with `simple-salesforce` and Secrets Manager** to extract data from Salesforce and write to S3:

---

## ⚠️ Common Issues and How to Handle Them

### 1. **API Limits Exceeded (Salesforce)**
- **Cause**: Salesforce enforces daily API call limits per org/user.
- **Symptoms**: `REQUEST_LIMIT_EXCEEDED` errors.
- **Solution**:
  - Use `sf.limits()` to monitor usage.
  - Optimize queries to reduce calls.
  - Use **bulk APIs** for large data loads (not supported by `simple-salesforce` directly).

---

### 2. **Pagination Not Handled Properly**
- **Cause**: Not using `nextRecordsUrl` to fetch all records.
- **Symptoms**: Only partial data retrieved.
- **Solution**: Always loop through `query_more()` until `nextRecordsUrl` is `None`.

---

### 3. **Secrets Manager Access Denied**
- **Cause**: Glue job’s IAM role lacks permission.
- **Symptoms**: `AccessDeniedException` when calling `get_secret_value`.
- **Solution**: Attach a policy with `secretsmanager:GetSecretValue` for the specific secret ARN.

---

### 4. **Incorrect or Expired Salesforce Credentials**
- **Cause**: Wrong password, token, or expired refresh token.
- **Symptoms**: `INVALID_LOGIN` or `invalid_grant` errors.
- **Solution**: Verify credentials in Secrets Manager. Regenerate token if needed.

---

### 5. **S3 Write Failures**
- **Cause**: Missing permissions or incorrect bucket/key.
- **Symptoms**: `AccessDenied` or `NoSuchBucket` errors.
- **Solution**: Ensure the IAM role has `s3:PutObject` permission and the bucket exists.

---

### 6. **Large Dataset Memory Issues**
- **Cause**: Python Shell jobs have limited memory (up to 16 GB).
- **Symptoms**: Job crashes or runs out of memory.
- **Solution**:
  - Process data in **batches** (as shown in the script).
  - Consider using **Glue Spark jobs** for large datasets.

---

### 7. **Missing Python Modules**
- **Cause**: `simple-salesforce` not included in job config.
- **Symptoms**: `ModuleNotFoundError`
- **Solution**: Add `--additional-python-modules simple-salesforce` in job parameters.

---

### 8. **Region Mismatch for Secrets Manager**
- **Cause**: Secret is in a different AWS region than the Glue job.
- **Symptoms**: `ResourceNotFoundException`
- **Solution**: Ensure the secret and Glue job are in the **same region** or use cross-region access.

---

Here’s a **checklist** to help you monitor and troubleshoot your **AWS Glue Python Shell job** that connects to **Salesforce using `simple-salesforce`** and **Secrets Manager**, and writes data to **S3**.

---

## ✅ AWS Glue + Salesforce Integration Checklist

### 🔐 **Secrets Manager Setup**
- [ ] Secret is created with correct JSON structure (`username`, `password`, `security_token`)
- [ ] Secret is in the **same region** as the Glue job
- [ ] Glue job IAM role has `secretsmanager:GetSecretValue` permission
- [ ] Secret name is correctly referenced in the script

---

### 🔗 **Salesforce Connection**
- [ ] Credentials are valid (test login manually if needed)
- [ ] Security token is appended correctly to the password (if required)
- [ ] Query is tested and returns expected results
- [ ] Pagination is handled using `nextRecordsUrl` and `query_more()`

---

### 📦 **Glue Job Configuration**
- [ ] Job type is set to **Python Shell**
- [ ] Python version is compatible (e.g., Python 3.9)
- [ ] `--additional-python-modules` includes `simple-salesforce`
- [ ] Job has sufficient memory (up to 16 GB for Python Shell)
- [ ] Logging is enabled (check CloudWatch for errors)

---

### 🪣 **S3 Write Operations**
- [ ] S3 bucket exists and is in the correct region
- [ ] IAM role has `s3:PutObject` permission
- [ ] S3 key prefix is correctly formatted
- [ ] Data is written in batches to avoid memory overload

---

### 📊 **Monitoring & Logging**
- [ ] CloudWatch logs are enabled and accessible
- [ ] Logs show successful connection and data retrieval
- [ ] Logs confirm successful S3 writes
- [ ] No `AccessDenied`, `INVALID_LOGIN`, or `REQUEST_LIMIT_EXCEEDED` errors

---

### 🧠 **Optional Enhancements**
- [ ] Use Secrets Manager for OAuth credentials if switching to OAuth flow
- [ ] Add retry logic for transient API failures
- [ ] Monitor Salesforce API usage with `sf.limits()`
- [ ] Validate data integrity post-write (e.g., record counts)

---

