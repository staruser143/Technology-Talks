To securely store the **generated RSA private key and certificate in AWS Secrets Manager** using Terraform, you can use the `aws_secretsmanager_secret` and `aws_secretsmanager_secret_version` resources.

Here’s a complete example that builds on the previous TLS setup:

---

### 🔐 **Terraform Configuration**

```hcl
provider "aws" {
  region = "us-east-1"  # Change to your preferred region
}

provider "tls" {}

# Generate RSA Private Key
resource "tls_private_key" "jwt_key" {
  algorithm = "RSA"
  rsa_bits  = 2048
}

# Generate Self-Signed Certificate
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

# Store Private Key in Secrets Manager
resource "aws_secretsmanager_secret" "private_key" {
  name = "salesforce_jwt_private_key"
}

resource "aws_secretsmanager_secret_version" "private_key_version" {
  secret_id     = aws_secretsmanager_secret.private_key.id
  secret_string = tls_private_key.jwt_key.private_key_pem
}

# Store Certificate in Secrets Manager
resource "aws_secretsmanager_secret" "certificate" {
  name = "salesforce_jwt_certificate"
}

resource "aws_secretsmanager_secret_version" "certificate_version" {
  secret_id     = aws_secretsmanager_secret.certificate.id
  secret_string = tls_self_signed_cert.jwt_cert.cert_pem
}
```

---

### 🧪 How Your Glue Job Can Use These Secrets

In your Glue script (Python), you can retrieve the secrets using **Boto3**:

```python
import boto3

client = boto3.client('secretsmanager')

private_key = client.get_secret_value(SecretId='salesforce_jwt_private_key')['SecretString']
certificate = client.get_secret_value(SecretId='salesforce_jwt_certificate')['SecretString']
```

---

