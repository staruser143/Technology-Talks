
variable "aws_region" {
  description = "AWS region to deploy resources"
  type        = string
}

variable "salesforce_client_id" {
  description = "Salesforce Connected App Client ID"
  type        = string
}

variable "salesforce_client_secret" {
  description = "Salesforce Connected App Client Secret"
  type        = string
  sensitive   = true
}

variable "salesforce_refresh_token" {
  description = "Salesforce OAuth Refresh Token"
  type        = string
  sensitive   = true
}

variable "salesforce_instance_url" {
  description = "Salesforce instance URL (e.g., https://yourInstance.salesforce.com)"
  type        = string
}

variable "subnet_id" {
  description = "Subnet ID for Glue connection"
  type        = string
}

variable "security_group_id" {
  description = "Security Group ID for Glue connection"
  type        = string
}
