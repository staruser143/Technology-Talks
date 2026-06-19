
terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}

provider "aws" {
  region = "us-east-1"
}

# Step 1: Create a secret in AWS Secrets Manager
resource "aws_secretsmanager_secret" "salesforce_oauth" {
  name = "salesforce/oauth"
}

variable "salesforce_client_id" {
  type      = string
  sensitive = true
}

variable "salesforce_client_secret" {
  type      = string
  sensitive = true
}

variable "salesforce_refresh_token" {
  type      = string
  sensitive = true
}

variable "salesforce_instance_url" {
  type    = string
  default = "https://yourInstance.salesforce.com"
}

resource "aws_secretsmanager_secret_version" "salesforce_oauth_version" {
  secret_id     = aws_secretsmanager_secret.salesforce_oauth.id
  secret_string = jsonencode({
    client_id     = var.salesforce_client_id
    client_secret = var.salesforce_client_secret
    refresh_token = var.salesforce_refresh_token
    instance_url  = var.salesforce_instance_url
  })
}

# Step 2: Read the secret values into Terraform
data "aws_secretsmanager_secret_version" "salesforce_oauth" {
  secret_id = aws_secretsmanager_secret.salesforce_oauth.id
}

locals {
  salesforce_secrets = jsondecode(data.aws_secretsmanager_secret_version.salesforce_oauth.secret_string)
}

# Step 3: Create the AWS Glue connection using the secret values
resource "aws_glue_connection" "salesforce_connection" {
  name = "salesforce-oauth-connection"

  connection_properties = {
    CONNECTION_TYPE = "Salesforce"
    CLIENT_ID       = local.salesforce_secrets.client_id
    CLIENT_SECRET   = local.salesforce_secrets.client_secret
    REFRESH_TOKEN   = local.salesforce_secrets.refresh_token
    TOKEN_URL       = "https://login.salesforce.com/services/oauth2/token"
    INSTANCE_URL    = local.salesforce_secrets.instance_url
  }

  physical_connection_requirements {
    availability_zone      = "us-east-1a"
    security_group_id_list = ["sg-xxxxxxxx"]
    subnet_id              = "subnet-xxxxxxxx"
  }
}
