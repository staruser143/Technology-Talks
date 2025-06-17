To allow an **AWS Glue Job** to authenticate with **Salesforce using the JWT Bearer Token Flow**, the Glue job's **IAM role** must have permissions to:

---

### ✅ 1. **Access Secrets in AWS Secrets Manager**
If you're storing the **private key** and **certificate** in Secrets Manager (as we did earlier), the Glue job needs permission to retrieve them.

#### **IAM Policy Example:**
```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "secretsmanager:GetSecretValue"
      ],
      "Resource": [
        "arn:aws:secretsmanager:<region>:<account-id>:secret:salesforce_jwt_private_key-*",
        "arn:aws:secretsmanager:<region>:<account-id>:secret:salesforce_jwt_certificate-*"
      ]
    }
  ]
}
```

---

### ✅ 2. **(Optional) Access to CloudWatch Logs**
To debug or monitor the Glue job execution.

```json
{
  "Effect": "Allow",
  "Action": [
    "logs:CreateLogGroup",
    "logs:CreateLogStream",
   }
```

---

### ✅ 3. **(Optional) Access to S3**
If your Glue job reads/writes data to S3.

```json
{
  "Effect": "Allow",
  "Action": [
    "s3:GetObject",
    "s3:PutObject",
    "s3:ListBucket"
  ],
  "Resource": [
    "arn:aws:s3:::your-bucket-name",
    "arn:aws:s3:::your-bucket-name/*"
  ]
}
```

---

### 🧠 Note:
- Salesforce itself doesn’t require IAM permissions — the Glue job just needs to be able to **construct and send the JWT** to Salesforce's token endpoint.
- Ensure the Glue job has **internet access** (via NAT Gateway or public subnet) to reach `https://login.salesforce.com`.

