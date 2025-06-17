Here’s a **sample Python script** that demonstrates how to implement the **JWT Bearer Token Flow** to authenticate with Salesforce from an AWS Glue Job (or any Python environment):

---

### ✅ **Requirements**
Install these Python packages in your Glue Job or local environment:
```bash
pip install pyjwt cryptography requests
```

---

### 🧾 **Sample Script**

```python
import jwt
import time
import requests
from cryptography.hazmat.primitives import serialization

# === Configuration ===
CLIENT_ID = 'YOUR_CONNECTED_APP_CLIENT_ID'
USERNAME = 'salesforce_user@example.com'
LOGIN_URL = 'https://login.salesforce.com'  # Use test.salesforce.com for sandbox
PRIVATE_KEY_PATH = 'path/to/your/private_key.pem'

# === Load Private Key ===
with open(PRIVATE_KEY_PATH, 'rb') as key_file:
    private_key = serialization.load_pem_private_key(
        key_file.read(),
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

if response.status_code == 200:
    access_token = response.json()['access_token']
    instance_url = response.json()['instance_url']
    print("Access Token:", access_token)
    print("Instance URL:", instance_url)
else:
    print("Failed to authenticate:", response.text)
```

---

### 🔐 Notes:
- Replace `PRIVATE_KEY_PATH` with the path to your `.pem` file (you can store this securely in AWS Secrets Manager or S3).
- Ensure the **Salesforce Connected App** has:
  - JWT Bearer Flow enabled.
  - The **public key** from your private key uploaded.
  - The user (`USERNAME`) is authorized to use the app.

---

