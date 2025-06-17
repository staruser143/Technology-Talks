
provider "aws" {
  region = "us-east-1"
}

provider "tls" {}

resource "tls_private_key" "jwt_key" {
  algorithm = "RSA"
  rsa_bits  = 2048
}

resource "tls_self_signed_cert" "jwt_cert" {
  key_algorithm   = "RSA"
  private_key_pem = tls_private_key.jwt_key.private_key_pem

  subject {
    common_name  = "Salesforce JWT Auth"
    organization = "MyCompany Inc."
    country      = "IN"
    province     = "Tamil Nadu"
    locality     = "Chennai"
  }

  validity_period_hours = 8760
  is_ca_certificate     = false
}

resource "aws_secretsmanager_secret" "private_key" {
  name = "salesforce_jwt_private_key"
}

resource "aws_secretsmanager_secret_version" "private_key_version" {
  secret_id     = aws_secretsmanager_secret.private_key.id
  secret_string = tls_private_key.jwt_key.private_key_pem
}

resource "aws_secretsmanager_secret" "certificate" {
  name = "salesforce_jwt_certificate"
}

resource "aws_secretsmanager_secret_version" "certificate_version" {
  secret_id     = aws_secretsmanager_secret.certificate.id
  secret_string = tls_self_signed_cert.jwt_cert.cert_pem
}

output "private_key_pem" {
  value     = tls_private_key.jwt_key.private_key_pem
  sensitive = true
}

output "certificate_pem" {
  value = tls_self_signed_cert.jwt_cert.cert_pem
}
