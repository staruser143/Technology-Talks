import boto3
import jwt
import time
import requests
from cryptography.hazmat.primitives import serialization

# === Configuration ===
REGION_NAME = 'us-east-1'  # Update to your region
PRIVATE_KEY_SECRET_NAME = 'salesforce_jwt_private_key'
CLIENT_ID = 'YOUR_CONNECTED_APP_CLIENT_ID'
USERNAME = 'salesforce_user@example.com'
LOGIN_URL = 'https://login.salesforce.com'  # or https://test.salesforce.com for sandbox

# === Retrieve secrets from AWS Secrets Manager ===
def get_secret(secret_name, region_name):
    client = boto3.client('secretsmanager', region_name=region_name)
    response = client.get_secret_value(SecretId=secret_name)
    return response['SecretString']

# === Load private key from PEM string ===
def load_private_key(pem_str):
    return serialization.load_pem_private_key(
        pem_str.encode('utf-8'),
        password=None,
    )

# === Create JWT Assertion ===
def create_jwt(client_id, username, login_url, private_key):
    issued_at = int(time.time())
    expiration_time = issued_at + 300  # 5 minutes

    payload = {
        'iss': client_id,
        'sub': username,
        'aud': login_url,
        'exp': expiration_time
    }

    return jwt.encode(payload, private_key, algorithm='RS256')

# === Request Access Token from Salesforce ===
def request_access_token(jwt_assertion, login_url):
    token_url = f'{login_url}/services/oauth2/token'
    headers = {'Content-Type': 'application/x-www-form-urlencoded'}
    data = {
        'grant_type': 'urn:ietf:params:oauth:grant-type:jwt-bearer',
        'assertion': jwt_assertion
    }

    response = requests.post(token_url, headers=headers, data=data)
    response.raise_for_status()
    return response.json()

# === Main Execution ===
def main():
    # Retrieve private key from Secrets Manager
    private_key_pem = get_secret(PRIVATE_KEY_SECRET_NAME, REGION_NAME)
    private_key = load_private_key(private_key_pem)

    # Create JWT
    jwt_assertion = create_jwt(CLIENT_ID, USERNAME, LOGIN_URL, private_key)

    # Authenticate with Salesforce
    token_response = request_access_token(jwt_assertion, LOGIN_URL)

    # Output access token and instance URL
    print("Access Token:", token_response['access_token'])
    print("Instance URL:", token_response['instance_url'])

if __name__ == "__main__":
    main()
