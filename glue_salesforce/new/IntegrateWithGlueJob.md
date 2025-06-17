To integrate the `glue_dependencies.zip` file into your **AWS Glue Python shell job**, you need to reference it in the job's configuration using the `--extra-py-files` argument.

---

### ✅ If You're Using Terraform

Update your Glue job definition like this:

```hcl
resource "aws_glue_job" "salesforce_jwt_job" {
  name     = "your-glue-job-name"
  role_arn = aws_iam_role.glue_salesforce_jwt_role.arn

  command {
    name            = "pythonshell"
    python_version  = "3"
    script_location = "s3://your-script-location/glue_salesforce_jwt_query.py"
  }

  default_arguments = {
    "--TempDir"         = "s3://your-temp-dir/"
    "--job-language"    = "python"
    "--extra-py-files"  = "s3://your-bucket-name/glue_dependencies.zip"
  }

  max_capacity = 1.0

  execution_property {
    max_concurrent_runs = 1
  }

  glue_version = "2.0"
}
```

---

### ✅ If You're Using the AWS Console

1. Go to **AWS Glue → Jobs → Your Job → Edit**.
2. Under **Job parameters**, add:
   - **Key**: `--extra-py-files`
   - **Value**: `s3://your-bucket-name/glue_dependencies.zip`
3. Save and run the job.

---

