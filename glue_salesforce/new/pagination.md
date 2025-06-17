The **Python shell job in AWS Glue** can handle moderate data volumes, but for **large datasets**, especially when querying from Salesforce, you **must implement pagination** to avoid:

- API limits
- Memory overflows
- Incomplete data retrieval

---

### 🔄 Why Pagination Is Necessary

Salesforce REST API responses are **limited to 2,000 records per query** by default. If your query returns more than that, Salesforce includes a `nextRecordsUrl` in the response.

---

### ✅ How to Handle Pagination in Your Script

Extend your query logic like this:

```python
def query_salesforce_paginated(access_token, instance_url, soql):
    headers = {
        'Authorization': f'Bearer {access_token}',
        'Content-Type': 'application/json'
    }
    query_url = f'{instance_url}/services/data/v58.0/query'
    params = {'q': soql}

    all_records = []
    response = requests.get(query_url, headers=headers, params=params)
    response.raise_for_status()
    data = response.json()
    all_records.extend(data['records'])

    # Follow pagination
    while not data.get('done', True):
        next_url = f"{instance_url}{data['nextRecordsUrl']}"
        response = requests.get(next_url, headers=headers)
        response.raise_for_status()
        data = response.json()
        all_records.extend(data['records'])

    return all_records
```

---

### 🧠 Tip:
- For very large datasets, consider **batching queries** using filters (e.g., by date or ID ranges).
- You can also use the **Salesforce Bulk API** for more efficient large-scale data extraction, but it requires a different setup.

