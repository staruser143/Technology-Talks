
provider "aws" {
  region = "us-east-1"  # Replace with your desired region
}

# IAM Role for AWS Glue
resource "aws_iam_role" "glue_salesforce_jwt_role" {
  name = "glue_salesforce_jwt_role"

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

# IAM Policy for accessing Secrets Manager, CloudWatch Logs, and S3
resource "aws_iam_policy" "glue_salesforce_jwt_policy" {
  name        = "glue_salesforce_jwt_policy"
  description = "Policy for Glue to access Secrets Manager, CloudWatch Logs, and S3"

  policy = jsonencode({
    Version = "2012-10-17",
    Statement = [
      {
        Effect = "Allow",
        Action = [
          "secretsmanager:GetSecretValue"
        ],
        Resource = [
          "arn:aws:secretsmanager:<region>:<account-id>:secret:salesforce_jwt_private_key-*",
          "arn:aws:secretsmanager:<region>:<account-id>:secret:salesforce_jwt_certificate-*"
        ]
      },
      {
        Effect = "Allow",
        Action = [
          "logs:CreateLogGroup",
          "logs:CreateLogStream",
          "logs:PutLogEvents"
        ],
        Resource = "*"
      },
      {
        Effect = "Allow",
        Action = [
          "s3:GetObject",
          "s3:PutObject",
          "s3:ListBucket"
        ],
        Resource = [
          "arn:aws:s3:::<your-bucket-name>",
          "arn:aws:s3:::<your-bucket-name>/*"
        ]
      }
    ]
  })
}

# Attach the policy to the IAM role
resource "aws_iam_role_policy_attachment" "glue_salesforce_jwt_attachment" {
  role       = aws_iam_role.glue_salesforce_jwt_role.name
  policy_arn = aws_iam_policy.glue_salesforce_jwt_policy.arn
}
