"""
Salesforce SOQL query example using JWT auth.

Refactored to use shared utilities from shared/python/ to eliminate
duplicated secret retrieval and JWT auth code.
"""

import sys
sys.path.insert(0, "../..")

import requests
from shared.python.aws_utils import get_secret
from shared.python.salesforce_auth import load_private_key, authenticate

# === Configuration ===
CLIENT_ID = "YOUR_CONNECTED_APP_CLIENT_ID"
USERNAME = "salesforce_user@example.com"
LOGIN_URL = "https://login.salesforce.com"
PRIVATE_KEY_SECRET_NAME = "salesforce_jwt_private_key"


def query_salesforce(access_token, instance_url, soql):
    """Query Salesforce using SOQL."""
    headers = {
        "Authorization": f"Bearer {access_token}",
        "Content-Type": "application/json",
    }
    query_url = f"{instance_url}/services/data/v58.0/query"
    params = {"q": soql}

    response = requests.get(query_url, headers=headers, params=params)
    response.raise_for_status()
    return response.json()


# Authenticate
private_key_pem = get_secret(PRIVATE_KEY_SECRET_NAME)
private_key = load_private_key(private_key_pem)
access_token, instance_url = authenticate(CLIENT_ID, USERNAME, LOGIN_URL, private_key)

print("Access Token retrieved successfully.")

# Example SOQL query
soql = "SELECT Id, Name FROM Account LIMIT 10"
results = query_salesforce(access_token, instance_url, soql)

print("Queried Account Records:")
for record in results["records"]:
    print(record)
