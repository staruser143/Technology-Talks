Salesforce Data Cloud and Amazon Redshift have **native integration options**.


## Integration Patterns

### 1. Zero-Copy Data Federation (Redshift → Data Cloud)

- This allows Salesforce Data Cloud to query data that remains in Amazon Redshift **without physically copying it into Data Cloud**. 
- Data stays in Redshift and is accessed on demand. [\[aws.amazon.com\]](https://aws.amazon.com/blogs/big-data/access-amazon-redshift-data-from-salesforce-data-cloud-with-zero-copy-data-federation/), [\[developer....sforce.com\]](https://developer.salesforce.com/docs/data/data-cloud-int/guide/c360-a-redshift-connector.html)

**Benefits**

* No ETL pipelines
* No duplicate storage
* Near real-time access
* Single source of truth in Redshift
* Lower data movement costs [\[aws.amazon.com\]](https://aws.amazon.com/blogs/big-data/access-amazon-redshift-data-from-salesforce-data-cloud-with-zero-copy-data-federation/), [\[aws.amazon.com\]](https://aws.amazon.com/blogs/big-data/harness-zero-copy-data-sharing-from-salesforce-data-cloud-to-amazon-redshift-for-unified-analytics-part-1/)

**Typical use case**

* Customer profiles in Salesforce
* Order, claims, product, or transaction data in Redshift
* Business users build customer 360 views in Data Cloud using both sources [\[aws.amazon.com\]](https://aws.amazon.com/blogs/big-data/access-amazon-redshift-data-from-salesforce-data-cloud-with-zero-copy-data-federation/)

***

### 2. Zero-Copy Data Sharing (Data Cloud → Redshift)

Salesforce also supports exposing Data Cloud data to Redshift via **bidirectional Zero-Copy integration**, enabling analytics teams to analyze unified customer profiles directly from Redshift. [\[aws.amazon.com\]](https://aws.amazon.com/blogs/big-data/harness-zero-copy-data-sharing-from-salesforce-data-cloud-to-amazon-redshift-for-unified-analytics-part-1/)

**Typical use case**

* Marketing data in Data Cloud
* Enterprise data warehouse analytics in Redshift
* Data scientists use Redshift for BI, ML, and reporting while consuming Data Cloud datasets. [\[aws.amazon.com\]](https://aws.amazon.com/blogs/big-data/harness-zero-copy-data-sharing-from-salesforce-data-cloud-to-amazon-redshift-for-unified-analytics-part-1/)

***

### 3. Traditional Data Ingestion (Redshift → Data Cloud)

Data Cloud can ingest Redshift data using its Amazon Redshift connector. Supported approaches include:

* Query Federation
* File Federation
* Data Share
* Zero-Copy mechanisms [\[developer....sforce.com\]](https://developer.salesforce.com/docs/data/data-cloud-int/guide/c360-a-redshift-connector.html)

***

### 4. Identity Provider-Based Authentication

A newer enhancement allows Salesforce Data Cloud to connect to Amazon Redshift using **Salesforce Identity Provider (IDP)** instead of long-lived database usernames and passwords. This improves security and reduces credential management overhead. [\[developer....sforce.com\]](https://developer.salesforce.com/blogs/2025/01/connect-data-cloud-to-amazon-redshift-using-salesforce-idp), [\[developer....sforce.com\]](https://developer.salesforce.com/docs/data/data-cloud-int/guide/c360-a-set-up-data-federation-redshift-connection.html)

***

