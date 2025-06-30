# AWS Glue Salesforce Connection using JWT Bearer Flow
resource "aws_glue_connection" "salesforce_jwt_connection" {
  name                = "salesforce-jwt-connection"
  description         = "Connection to Salesforce using OAuth2 JWT Bearer Flow"
  connection_type     = "SALESFORCE"
  connection_properties = {
    "CLIENT_ID"        = "YOUR_SALESFORCE_CONSUMER_KEY"
    "USERNAME"         = "your.salesforce.user@example.com"
    "AUTHORIZATION_TYPE" = "JWT_BEARER"
    # Reference the ARN of the Secrets Manager secret storing the private key
    "SECRET_ID"        = aws_secretsmanager_secret.salesforce_private_key_secret.arn # Changed this line
    "SF_AUTH_TYPE"     = "JWT"
    "CONNECTION_URL"   = "https://login.salesforce.com" # Or https://test.salesforce.com for sandbox
  }

  # ... (VPC configuration if any, same as before) ...
}
