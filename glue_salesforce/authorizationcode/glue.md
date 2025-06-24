Here is the **AWS Glue job script** that performs **incremental data loading from Salesforce**, using a timestamp stored in S3:

📄 [Download the Glue Job Script](blob:https://m365.cloud.microsoft/c7b1d256-51f3-4427-8b32-376ae5bebc2a)

---

### 🧾 What the Script Does

1. **Reads the last run timestamp** from a JSON file in S3.
2. **Uses `pushDownPredicate`** to filter Salesforce records modified since that timestamp.
3. **Writes the filtered records** to S3 in Parquet format.
4. **Updates the timestamp file** with the current time after successful execution.

---

### 🧩 Required Job Parameters

When scheduling or running the Glue job, pass these parameters:

```json
{
  "--JOB_NAME": "your-glue-job-name",
  "--metadata_bucket": "your-metadata-bucket",
  "--metadata_key": "salesforce/last_run_metadata.json",
  "--output_bucket": "your-output-bucket",
  "--salesforce_connection_name": "salesforce-authorization-code-conn",
  "--salesforce_object": "Account"
}
```

---

