
resource "aws_glue_job" "salesforce_jwt_job" {
  name     = "your-glue-job-name"
  role_arn = aws_iam_role.glue_salesforce_jwt_role.arn

  command {
    name            = "pythonshell"
    python_version  = "3"
    script_location = "s3://your-script-location/script.py"
  }

  default_arguments = {
    "--TempDir" = "s3://your-temp-dir/"
    "--job-language" = "python"
  }

  max_capacity = 1.0

  execution_property {
    max_concurrent_runs = 1
  }

  glue_version = "2.0"
}
