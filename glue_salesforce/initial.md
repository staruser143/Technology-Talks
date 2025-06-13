# How do I use AWS Glue Salesforce Connector to connect to Salesforce from AWS Glue
To use the **AWS Glue Salesforce Connector** to connect to Salesforce and extract data into an S3 bucket, follow these steps:

---

## ✅ Step-by-Step Guide

### **1. Set Up a Salesforce Connection in AWS Glue**
1. Go to the **AWS Glue Console**.
2. Navigate to **"Connections"** under **Data Catalog**.
3. Click **"Add connection"** and choose **"Salesforce"** as the connection type.
4. Fill in the required details:
   - **Connection name**
   - **Salesforce username**
   - **Password**
   - **Security token**
   - **Authentication type** (Basic or OAuth)
   - **Salesforce environment** (Production or Sandbox)

> 🔐 **Tip**: If using OAuth, you’ll need to register an app in Salesforce and provide the client ID and secret.

---

### **2. Create a Crawler (Optional but Recommended)**
1. Go to **"Crawlers"** in the Glue Console.
2. Create a new crawler to connect to Salesforce using the connection you just created.
3. Choose the **Salesforce objects** you want to crawl.
4. Set the output to a **Glue Data Catalog database**.

This step helps you **automatically catalog** the Salesforce schema.

---

### **3. Create a Glue Job**
1. Go to **"Jobs"** in the Glue Console.
2. Click **"Add job"** and configure:
   - **Name**
   - **IAM Role** (must have access to Salesforce and S3)
   - **Type**: Spark or Python Shell (choose Spark for Salesforce connector)
   - **Glue version**: Choose a version that supports Salesforce connector (e.g., Glue 3.0+)
3. In the script editor:
   - Use the Salesforce connection as the **source**
   - Use **S3** as the **target**
   - You can use **DynamicFrames** to transform and write data

---

### **4. Sample PySpark Script**
Here’s a basic example:

```python
import sys
from awsglue.transforms import *
from awsglue.utils import getResolvedOptions
from awsglue.context import GlueContext
from pyspark.context import SparkContext

args = getResolvedOptions(sys.argv, ['JOB_NAME'])
sc = SparkContext()
glueContext = GlueContext(sc)
spark = glueContext.spark_session

# Read from Salesforce
sf_data = glueContext.create_dynamic_frame.from_options(
    connection_type="salesforce",
    connection_options={
        "connectionName": "your-salesforce-connection",
        "object": "Account",  # Salesforce object
        "soqlQuery": "SELECT Id, Name, Industry FROM Account"
    }
)

# Write to S3
glueContext.write_dynamic_frame.from_options(
    frame=sf_data,
    connection_type="s3",
    connection_options={"path": "s3://your-bucket/salesforce-data/"},
    format="parquet"
)
```

---

### **5. Run the Job**
- Save and run the job.
- Monitor logs in **CloudWatch** for any errors or progress.

---


