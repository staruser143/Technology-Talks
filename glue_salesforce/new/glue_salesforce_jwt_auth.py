"""
Salesforce JWT authentication example.

Refactored to use shared utilities from shared/python/ to eliminate
duplicated secret retrieval and JWT auth code.
"""

import sys
sys.path.insert(0, "../..")

from shared.python.aws_utils import get_secret
from shared.python.salesforce_auth import load_private_key, authenticate

# === Configuration ===
REGION_NAME = "us-east-1"
PRIVATE_KEY_SECRET_NAME = "salesforce_jwt_private_key"
CLIENT_ID = "YOUR_CONNECTED_APP_CLIENT_ID"
USERNAME = "salesforce_user@example.com"
LOGIN_URL = "https://login.salesforce.com"


def main():
    private_key_pem = get_secret(PRIVATE_KEY_SECRET_NAME, REGION_NAME)
    private_key = load_private_key(private_key_pem)

    access_token, instance_url = authenticate(CLIENT_ID, USERNAME, LOGIN_URL, private_key)

    print("Access Token:", access_token)
    print("Instance URL:", instance_url)


if __name__ == "__main__":
    main()
