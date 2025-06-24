
provider "aws" {
  region = var.aws_region
}

resource "aws_iam_role" "glue_role" {
  name = "${var.prefix}-glue-role"
  assume_role_policy = jsonencode({
    Version = "2012-10-17",
    Statement = [{
      Action = "sts:AssumeRole",
      Effect = "Allow",
      Principal = {
        Service = "glue.amazonaws.com"
      }
    }]
  })
}

resource "aws_iam_role_policy" "glue_policy" {
  name = "${var.prefix}-glue-policy"
  role = aws_iam_role.glue_role.id
  policy = jsonencode({
    Version = "2012-10-17",
    Statement = [
      {
        Effect = "Allow",
        Action = [
          "s3:GetObject",
          "s3:PutObject"
        ],
        Resource = "arn:aws:s3:::${var.metadata_bucket}/salesforce/last_run_metadata.json"
      },
      {
        Effect = "Allow",
        Action = [
          "s3:PutObject",
          "s3:GetObject"
        ],
        Resource = "arn:aws:s3:::${var.output_bucket}/*"
      },
      {
        Effect = "Allow",
        Action = [
          "secretsmanager:GetSecretValue"
        ],
        Resource = var.salesforce_secret_arn
      },
      {
        Effect = "Allow",
        Action = [
          "logs:CreateLogGroup",
          "logs:CreateLogStream",
          "logs:PutLogEvents"
        ],
        Resource = "*"
      }
    ]
  })
}

resource "aws_glue_connection" "salesforce_conn" {
  name             = "${var.prefix}-salesforce-conn"
  connection_type  = "SALESFORCE"

  connection_properties = {
    INSTANCE_URL         = var.salesforce_instance_url
    ENVIRONMENT          = "PRODUCTION"
    AUTHENTICATION_TYPE  = "OAUTH2"
    OAUTH2_GRANT_TYPE    = "AUTHORIZATION_CODE"
    SECRET_ID            = var.salesforce_secret_arn
  }

  physical_connection_requirements {
    security_group_id_list = var.security_group_ids
    subnet_id              = var.subnet_id
    availability_zone      = var.availability_zone
  }
}

resource "aws_s3_bucket_object" "last_run_metadata" {
  bucket = var.metadata_bucket
  key    = "salesforce/last_run_metadata.json"
  content = jsonencode({
    last_run_time = var.initial_last_run_time
  })
  content_type = "application/json"
}

resource "aws_glue_job" "salesforce_incremental" {
  name     = "${var.prefix}-salesforce-incremental-job"
  role_arn = aws_iam_role.glue_role.arn

  command {
    name            = "glueetl"
    script_location = var.script_location
    python_version  = "3"
  }

  default_arguments = {
    "--job-language"     = "python"
    "--metadata_bucket"  = var.metadata_bucket
    "--metadata_key"     = "salesforce/last_run_metadata.json"
    "--TempDir"          = "s3://${var.output_bucket}/temp/"
  }

  glue_version = "4.0"
  number_of_workers = 2
  worker_type       = "G.1X"
}
