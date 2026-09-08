AWS Glue Data Quality can validate any data source cataloged as a table within the [AWS Glue Data Catalog](https://docs.aws.amazon.com/prescriptive-guidance/latest/serverless-etl-aws-glue/aws-glue-data-catalog.html), as well as data moving through AWS Glue ETL pipelines. [1] 
It natively supports a broad ecosystem of modern storage architectures, transactional table formats, and traditional databases: [2] 
## 1. Modern Data Lakes & Storage Formats
AWS Glue Data Quality can run validation rules over tables pointed at files in Amazon S3. It supports standard data storage formats (like Parquet, JSON, CSV, and Avro), alongside modern high-performance table formats: [2] 

* 
* Amazon S3 Tables
* Apache Iceberg (including AWS Lake Formation managed Iceberg tables)
* Delta Lake
* Apache Hudi [2, 3, 4, 5] 
* 

## 2. Lakehouse Architectures
It validates unified storage layers that bridge data lakes and warehouses: [3, 6] 

* 
* Amazon SageMaker AI Lakehouse tables [3, 5] 
* 

## 3. Relational Databases & Data Warehouses (via JDBC)
Any relational or transactional database table mapped inside the Glue Data Catalog via a JDBC connection can be validated: [2] 

* 
* Amazon Redshift tables
* Amazon RDS tables (e.g., [PostgreSQL](https://www.postgresql.org/), [MySQL](https://www.mysql.com/), Oracle, SQL Server) [2, 7] 
* 

------------------------------
## How it Evaluates These Tables
When you point [AWS Glue Data Quality](https://aws.amazon.com/glue/features/data-quality/) at these tables, it executes validation using two main workflows: [8] 

   1. Data Catalog Evaluations (At Rest): Runs checks directly on the tables stored in your data lakes or warehouses before the data is queried or pushed to a Bedrock model. [1] 
   2. Glue ETL Pipelines (In Transit): Evaluates data streams programmatically using PySpark/Scala nodes inside a Glue ETL job as the data is being transformed or moved. [1, 9] 

(Note: While most table formats can be configured directly through the AWS Glue Console UI, newer or specialized tables like Amazon S3 Tables or SageMaker Lakehouse formats may require scheduling or evaluation execution via the AWS CLI or Glue APIs.) [10] 
------------------------------
## 🚀 Next Step
If you are planning your Bedrock validation pipeline, which of these table formats (e.g., standard S3 Parquet/JSONL, Apache Iceberg, or Amazon Redshift) are you currently using to store your foundation model training data or knowledge bases?

[1] [https://aws.amazon.com](https://aws.amazon.com/blogs/big-data/set-up-advanced-rules-to-validate-quality-of-multiple-datasets-with-aws-glue-data-quality/)
[2] [https://aws.amazon.com](https://aws.amazon.com/blogs/big-data/aws-glue-data-quality-is-generally-available/)
[3] [https://docs.aws.amazon.com](https://docs.aws.amazon.com/glue/latest/dg/glue-data-quality.html)
[4] [https://aws.amazon.com](https://aws.amazon.com/about-aws/whats-new/2025/07/aws-glue-data-quality-s3-tables-managed-iceberg/)
[5] [https://tutorialsdojo.com](https://tutorialsdojo.com/aws-glue-data-quality/)
[6] [https://tutorialsdojo.com](https://tutorialsdojo.com/aws-glue-data-quality/)
[7] [https://aws.amazon.com](https://aws.amazon.com/blogs/big-data/set-up-advanced-rules-to-validate-quality-of-multiple-datasets-with-aws-glue-data-quality/)
[8] [https://aws.amazon.com](https://aws.amazon.com/about-aws/whats-new/2026/07/aws-glue-data-quality-catalog-anomaly-detection-write-results/)
[9] [https://www.dremio.com](https://www.dremio.com/resources/guides/aws-glue/)
[10] [https://docs.aws.amazon.com](https://docs.aws.amazon.com/glue/latest/dg/data-quality-getting-started.html)
