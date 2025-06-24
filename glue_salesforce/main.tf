
provider "aws" {
  region = "us-east-1"
}

# 1. Create AWS Secrets Manager secret for Salesforce JWT credentials
resource "aws_secretsmanager_secret" "salesforce_jwt" {
  name = "salesforce_jwt_secret"
}

resource "aws_secretsmanager_secret_version" "salesforce_jwt" {
  secret_id     = aws_secretsmanager_secret.salesforce_jwt.id
  secret_string = jsonencode({
    private_key = "<PEM formatted private key>",
    client_id   = "<Salesforce connected app client ID>",
    username    = "<Salesforce username>",
    audience    = "https://login.salesforce.com"
  })
}

# 2. Create S3 bucket to store the Glue script
resource "aws_s3_bucket" "glue_script_bucket" {
  bucket = "your-glue-script-bucket"
}

resource "aws_s3_bucket_object" "glue_script" {
  bucket = aws_s3_bucket.glue_script_bucket.bucket
  key    = "scripts/salesforce_bulk_opportunity_etl.py"
  source = "salesforce_bulk_opportunity_etl.py"
}

# 3. Create IAM role for the Glue job with necessary permissions
resource "aws_iam_role" "glue_role" {
  name = "glue_role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17",
    Statement = [
      {
        Effect = "Allow",
        Principal = {
          Service = "glue.amazonaws.com"
        },
        Action = "sts:AssumeRole"
      }
    ]
  })
}

resource "aws_iam_policy" "glue_policy" {
  name = "glue_policy"

  policy = jsonencode({
    Version = "2012-10-17",
    Statement = [
      {
        Effect = "Allow",
        Action = [
          "secretsmanager:GetSecretValue",
          "s3:PutObject",
          "s3:GetObject",
          "s3:ListBucket",
          "glue:*",
          "logs:*",
          "ec2:CreateNetworkInterface",
          "ec2:DescribeNetworkInterfaces",
          "ec2:DeleteNetworkInterface"
        ],
        Resource = "*"
      }
    ]
  })
}

resource "aws_iam_role_policy_attachment" "glue_role_policy" {
  role       = aws_iam_role.glue_role.name
  policy_arn = aws_iam_policy.glue_policy.arn
}

# 4. Create Glue job that runs the script using Salesforce Bulk API
resource "aws_glue_job" "salesforce_bulk_opportunity_etl" {
  name     = "salesforce_bulk_opportunity_etl"
  role_arn = aws_iam_role.glue_role.arn

  command {
    name            = "glueetl"
    script_location = "s3://${aws_s3_bucket.glue_script_bucket.bucket}/scripts/salesforce_bulk_opportunity_etl.py"
    python_version  = "3"
  }

  default_arguments = {
    "--TempDir"              = "s3://your-temp-dir/"
    "--job-language"         = "python"
    "--enable-continuous-cloudwatch-log" = "true"
  }

  max_retries = 1
}
