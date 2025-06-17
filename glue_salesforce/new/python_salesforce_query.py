import jwt
import time
import requests
import boto3
from cryptography.hazmat.primitives import serialization

# === Configuration ===
CLIENT_ID = 'YOUR_CONNECTED_APP_CLIENT_ID'
USERNAME = 'salesforce_user@example.com'
LOGIN_URL = 'https://login.salesforce.com'  # Use test.salesforce.com for sandbox
PRIVATE_KEY_SECRET_NAME = 'salesforce_jwt_private_key'

# === Retrieve Private Key from AWS Secrets Manager ===
def get_secret(secret_name, region_name='us-east-1'):
    client = boto3.client('secretsmanager', region_name=region_name)
    response = client.get_secret_value(SecretId=secret_name)
    return response['SecretString']

private_key_pem = get_secret(PRIVATE_KEY_SECRET_NAME)

# === Load Private Key ===
private_key = serialization.load_pem_private_key(
    private_key_pem.encode(),
    password=None,
)

# === Create JWT Assertion ===
issued_at = int(time.time())
expiration_time = issued_at + 300  # 5 minutes

payload = {
    'iss': CLIENT_ID,
    'sub': USERNAME,
    'aud': LOGIN_URL,
    'exp': expiration_time
}

jwt_assertion = jwt.encode(payload, private_key, algorithm='RS256')

# === Request Access Token ===
token_url = f'{LOGIN_URL}/services/oauth2/token'
headers = {'Content-Type': 'application/x-www-form-urlencoded'}
data = {
    'grant_type': 'urn:ietf:params:oauth:grant-type:jwt-bearer',
    'assertion': jwt_assertion
}

response = requests.post(token_url, headers=headers, data=data)
response.raise_for_status()
token_response = response.json()

access_token = token_response['access_token']
instance_url = token_response['instance_url']

print("Access Token retrieved successfully.")

# === Query Salesforce Using SOQL ===
def query_salesforce(access_token, instance_url, soql):
    headers = {
        'Authorization': f'Bearer {access_token}',
        'Content-Type': 'application/json'
    }
    query_url = f'{instance_url}/services/data/v58.0/query'
    params = {'q': soql}

    response = requests.get(query_url, headers=headers, params=params)
    response.raise_for_status()
    return response.json()

# Example SOQL query
soql = "SELECT Id, Name FROM Account LIMIT 10"
results = query_salesforce(access_token, instance_url, soql)

# Print the results
print("Queried Account Records:")
for record in results['records']:
    print(record)
