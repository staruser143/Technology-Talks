Here is your **complete Terraform module** for provisioning an AWS Glue job that performs **incremental Salesforce data loading** using OAuth Authorization Code flow:

---

### 📁 Download the Module Files

- [main.tf](blob:https://m365.cloud.microsoft/da622ca3-4a8a-450c-83d5-4412e8aff7c4) – Resources for IAM role, Glue connection, Glue job, and S3 metadata file
- [variables.tf](blob:https://m365.cloud.microsoft/6308accd-cfaa-42a4-98c8-4db7d8c66244) – Input variables for customization
- [outputs.tf](blob:https://m365.cloud.microsoft/29181728-3017-4497-b31e-4754b014a902) – Outputs for Glue job and connection names

---

### 🧩 What This Module Includes

- **IAM Role** with permissions for:
  - S3 (read/write metadata and output)
  - Secrets Manager (read Salesforce credentials)
  - CloudWatch Logs
- **Glue Connection** to Salesforce using OAuth2
- **Glue Job** with default arguments for metadata tracking
- **S3 Object** to store the initial `last_run_time` for incremental loads

---

### 🛠️ How to Use

1. Place the files in a directory (e.g., `modules/glue_salesforce_incremental/`).
2. In your root Terraform config, call the module:

```hcl
module "glue_salesforce_incremental" {
  source                  = "./modules/glue_salesforce_incremental"
  aws_region              = "us-east-1"
  prefix                  = "myproject"
  salesforce_instance_url = "https://your-instance.my.salesforce.com"
  salesforce_secret_arn   = "arn:aws:secretsmanager:us-east-1:123456789012:secret:your-secret"
  metadata_bucket         = "your-metadata-bucket"
  output_bucket           = "your-output-bucket"
  initial_last_run_time   = "2025-06-23T00:00:00Z"
  script_location         = "s3://your-script-bucket/scripts/glue_salesforce_incremental.py"
  security_group_ids      = ["sg-xxxxxxxx"]
  subnet_id               = "subnet-xxxxxxxx"
  availability_zone       = "us-east-1a"
}
```

---

