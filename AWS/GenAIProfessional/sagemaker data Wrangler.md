# Amazon Sagemaker Data Wrangler

- We should use Amazon SageMaker Data Wrangler when we need to radically accelerate data preparation and feature engineering for machine learning workflows. It is specifically engineered to bridge the gap between low-code exploration and production-grade MLOps pipelines.

The primary scenarios where SageMaker Data Wrangler is the ideal choice include:
## 1. Low-Code / No-Code Exploratory Data Analysis (EDA) 
* **Visual Data Profiling**: When we need a quick, deep dive into the data without writing manual pandas or matplotlib code.
* **Automated Data Quality Checking**: When we want to immediately generate a Data Quality and Insights Report to automatically detect missing values, anomalies, outliers, and duplicate rows.
* **Predictive Power Assessment**: When we want to quickly see how well the features correlate with a target variable via "Quick Model" approximations before actually training an expensive model.


## 2. Multi-Source Data Aggregation (Data Silos) 
* U**nified Ingestion**: When the data spans multiple different infrastructures and we want to join them in a single canvas. Data Wrangler natively pulls from:
* Amazon S3 and Amazon Athena
   * Amazon Redshift
   * [Snowflake](https://www.snowflake.com/en/) and [Databricks](https://www.databricks.com/)
   * Amazon SageMaker Feature Store

## 3. Rapid Feature Transformation at Scale
* **Pre-configured Math/Text Operations**: When we want to apply common transformations (like One-Hot Encoding, Min-Max Normalization, Imputation, or String Formatting) via a graphical point-and-click layout. It boasts over 300 built-in rules.
* H**ybrid Code Needs**: When we want the ease of a UI but still need the flexibility to write specialized chunks of custom Python, PySpark, or SQL code for advanced column mutations.

## 4. Seamless Transition from Sandbox to Production MLOps
* **Pipeline Exportation**: When we are done visually tweaking the data pipeline and need to export the workflow directly into a [SageMaker Pipeline](https://aws.amazon.com/sagemaker/pipelines/), an S3 bucket, or as automated Python code. Data Wrangler converts the visual steps into code under the hood, tracking exact data lineage for reproducibility.

## 5. Generative AI & LLM Data Prep

* **Vector Vectorization & RAG**:
  - When building Retrieval-Augmented Generation (RAG) systems.
   Data Wrangler can orchestrate the ingestion of text data, utilize a SageMaker-hosted model to convert text to vector embeddings, and push them cleanly into a database   engine like Amazon OpenSearch Service.

------------------------------
## Direct Comparison: When Not to Use Data Wrangler

| Requirement | Use SageMaker Data Wrangler | Use Alternative (SageMaker Processing / AWS Glue) |
|---|---|---|
| Interface | Visual UI / Studio Canvas | Pure programmatic API / Scripts (boto3) |
| Workflow | Iterative EDA & rapid prototyping | Code-heavy "lift and shift" of legacy workloads |
| Processing Style | Pushdown queries & visual steps | Headless job execution on custom containers |
