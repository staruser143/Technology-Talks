To query data from Salesforce using a Python shell job in AWS Glue, you can use the **Salesforce REST API** or the **`simple-salesforce`** Python library after authenticating with the JWT Bearer Token Flow.

Here’s how to extend your existing script to query data:

---

### ✅ Step-by-Step: Query Salesforce Using REST API

#### 1. **Authenticate and get access token** (already done in your script).

#### 2. **Make a SOQL query request**
```python
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
```

#### 3. **Use it in your main function**
```python
soql = "SELECT Id, Name FROM Account LIMIT 10"
results = query_salesforce(token_response['access_token'], token_response['instance_url'], soql)

for record in results['records']:
    print(record)
```

---

### 🧠 Notes:
- Replace `"SELECT Id, Name FROM Account LIMIT 10"` with your desired SOQL query.
- You can paginate through results using the `nextRecordsUrl` field in the response.

