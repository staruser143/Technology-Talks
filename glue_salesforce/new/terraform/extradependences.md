Here is your updated Terraform Glue job definition file:

📄 [Download `glue_job_definition_updated.tf`](blob:https://m365.cloud.microsoft/5fd2288a-a38b-46e7-b9c8-d95975c4ac61)

### 🔧 What's New:
- Added the `--extra-py-files` argument to include your packaged dependencies from S3:
  ```hcl
  "--extra-py-files" = "s3://your-bucket-name/glue_dependencies.zip"
  ```

Make sure to replace the placeholders (`your-bucket-name`, `your-script-location`, etc.) with your actual values.

