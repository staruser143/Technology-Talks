"""
Shared Salesforce JWT authentication utilities.

Consolidates the duplicated JWT generation + token exchange pattern from:
  - glue_salesforce/sf_bulk_opportunity.py (lines 31-50)
  - glue_salesforce/salesforce_bulk_opportunity_etl_incremental.py (lines 43-85)
  - glue_salesforce/salesforce_bulk_opportunity_etl_pagination.py (lines 31-49)
  - glue_salesforce/new/glue_salesforce_jwt_auth.py (lines 28-52)
  - glue_salesforce/new/python_salesforce_query.py (lines 27-50)
"""

import time
import jwt
import requests
from cryptography.hazmat.primitives import serialization


def load_private_key(pem_str):
    """Load an RSA private key from a PEM string."""
    return serialization.load_pem_private_key(
        pem_str.encode("utf-8"),
        password=None,
    )


def create_jwt_assertion(client_id, username, audience, private_key, ttl_seconds=300):
    """
    Create a signed JWT assertion for Salesforce OAuth.

    Args:
        client_id: Connected App consumer key
        username: Salesforce username
        audience: Login URL (e.g., https://login.salesforce.com)
        private_key: RSA private key (PEM string or key object)
        ttl_seconds: Token time-to-live in seconds (default 300)

    Returns:
        Encoded JWT string
    """
    if isinstance(private_key, str):
        private_key = load_private_key(private_key)

    payload = {
        "iss": client_id,
        "sub": username,
        "aud": audience,
        "exp": int(time.time()) + ttl_seconds,
    }
    return jwt.encode(payload, private_key, algorithm="RS256")


def exchange_jwt_for_token(jwt_assertion, login_url):
    """
    Exchange a JWT assertion for a Salesforce access token.

    Returns:
        dict with 'access_token' and 'instance_url'
    """
    response = requests.post(
        f"{login_url}/services/oauth2/token",
        data={
            "grant_type": "urn:ietf:params:oauth:grant-type:jwt-bearer",
            "assertion": jwt_assertion,
        },
    )
    response.raise_for_status()
    return response.json()


def authenticate(client_id, username, login_url, private_key):
    """
    Full authentication flow: create JWT -> exchange for access token.

    Returns:
        tuple of (access_token, instance_url)
    """
    assertion = create_jwt_assertion(client_id, username, login_url, private_key)
    token_data = exchange_jwt_for_token(assertion, login_url)
    return token_data["access_token"], token_data["instance_url"]
