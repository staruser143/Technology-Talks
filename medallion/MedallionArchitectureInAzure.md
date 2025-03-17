# Medallion Architecture Implementation in Azure
* Here's how Medallion Architecture can be implemented in Azure using Databricks (with Delta Lake for data versioning and transactional support).


## High-Level Flow of Medallion Architecture in Azure Databricks

### Data Ingestion into Bronze Layer

<table>
  <tr>
    <th>Source Systems:</th>
    <td>
        <ul>
            <li>Internal Claims System (CSV/JSON files)</li>
            <li>Third-Party Adjuster Reports (Excel, API)</li>
            <li>IoT Data (Telematics, Streaming)</li>
            <li>Partner APIs (Healthcare providers, etc.)</li>
       </ul>   
    </td>
  </tr>
   <tr>
    <th>Ingestion Mechanism:</th>
    <td>
        <ul> 
            <li><b>Batch ingestion: </b>b></li>
            <ul>
                <li><b>Azure Data Factory (ADF)</b> to move data into <b>Azure Data Lake Storage Gen2 (ADLS Gen2)</b>.</li>
            </ul>
             <li><b>Streaming ingestion: (Optional) </b>b></li>
                 <ul>
                     <li><b>Event Hubs</b> or <b>Kafka on HDInsight</b> connected to <b>Databricks Structured Streaming..</b></li>
                 </ul>
     </td>
  </tr>
 <tr>
    <th>Bronze Layer Storage:</th>
    <td>
        <ul>
            <li>Store raw data as-is in ADLS Gen2, partitioned by ingestion date.</li>
            <li>Access via <b>Databricks Delta Lake</b> for transaction and version control.</li>
         </ul>   
        <b>Example Path:</b> abfss://raw@storageaccount.dfs.core.windows.net/bronze/claims/yyyy/mm/dd/
    </td>
  </tr>
             <tr>
    <th>Tools:</th>
    <td>
        <ul>
            <li><b>ADF Pipelines, Databricks Auto Loader, or Streaming Jobs.</b></li>
       </ul>   
    </td>
  </tr>
</table>

### Data Transformation and Cleaning in Silver Layer
<table>
  <tr>
    <th>Processing in Databricks Notebooks/Jobs:</th>
    <td>
        <ul>
            <li>Read from Bronze Delta Tables.</li>
            <li>Clean/standardize (e.g., missing values, type casting, deduplication).</li>
            <ul>Enrich by Joining with:
                <li>Customer Master Data (stored in SQL DB of Delta Table</li>
                <li>PolicyHolder data (From Operational Systems)</li>
            </ul>
             <li>Apply Business Logic (e.g deriving claim severity, categorization</li>
       </ul>   
    </td>
  </tr>
  <tr>
    <th>Silver Layer Storage:</th>
    <td>
        <ul>
            <li>Store cleaned & enriched datasets as Delta tables, optimized with Z-ordering for faster queries.</li>
            <li>Partition data (e.g., by claim type, date)..</li>
       </ul>   
          <b>Example Path:</b> abfss://processed@storageaccount.dfs.core.windows.net/silver/claims/yyyy/mm/dd/
    </td>
  </tr>
</table>

### Business-Level Aggregations and Analytics in Gold Layer
<table>
  <tr>
    <th>Aggregation/Modeling in Databricks:</th>
    <td>
        <ul>
                <li>Summarize claims data (e.g., total claims per region, fraud score).</li>
                <li>Join with ML model outputs (e.g., fraud risk scores).</li>
                <li>Precompute KPIs for dashboards (e.g., claims processing time, adjuster performance)..</li>
     </ul>
     </td>
      </tr>
    <tr>
    <th>Gold Layer Storage:</th>
        <td>
     <ul>
                <li>Curated, business-ready Delta tables.</li>
                <li>Optimized for consumption by BI tools.</li>
            </ul>
       </ul>   
          <b>Example Path:</b> abfss://curated@storageaccount.dfs.core.windows.net/gold/claims/summary/
</td>
</tr>
  <tr>
    <th>Consumers:</th>
        <td>
     <ul>
            <li><b>Power BI/Synapse Analytics </b>(connect directly to Gold Delta tables or via Synapse Serverless SQL).</li>
            <li><b>Machine Learning models </b>(if used for retraining or insights).</li>
            </ul>
       </ul>   
        
</td>
</tr>
</table>
    
       
## End-to-End Architecture Diagram (Textual Representation):

```
Sources (Claims, Adjuster, IoT) 
    |
    v
Azure Data Factory / Event Hubs (Ingestion) 
    |
    v
[ Bronze Layer - Raw Data in ADLS + Delta Lake ]
    |
    v
Databricks Jobs (Cleaning, Enrichment, Joins)
    |
    v
[ Silver Layer - Cleaned Delta Tables in ADLS ]
    |
    v
Databricks Jobs (Aggregation, Modeling)
    |
    v
[ Gold Layer - Curated Business Data in ADLS ]
    |
    v
Power BI / Synapse SQL / ML Models
```


## Azure Services Used:

| Service   | Purpose  |
|------------|------------|
| **ADSL Gen2**  | Storage for Bronze,Silver and Gold Layers  |
| **Azure Databricks (with Delta Lake)**  | Data Processing, Transformation, Delta Tables  |
| **Azure Data Factory (ADF)**  | Orchestrate data ingestion pipelines   |
| **Event Hubs/Kafka (Optional)**  |Real-time Streaming Ingestion   |
| **Power BI/Synapse Analytics**  | Visualization , querying and reporting   |
| **Azure SQL DB )(Optional)**  | Storing Master Reference Data (e.g. policyholders   |
| **Azure Key Vault**  | Secure Secrets Management (e.g storage keys |


## Optional Add-ons for Production:
* **Delta Live Tables (DLT)**: Automate pipeline creation and data quality checks.
* **Unity Catalog**: For unified governance and security of Delta Tables.
* **Job Clusters**: Efficient, ephemeral compute clusters for cost-saving.

## Benefits of This Azure-Databricks Medallion Flow:
* **Governed Data Layers**: Raw to refined data with auditability.
* **Transactional and Scalable**: Delta Lake ACID properties.
* **Cost-effective and Optimized**: Partitioning, Z-ordering.
* **Secure and Compliant**: Managed access via Azure RBAC + Unity Catalog.
* **Real-time and Batch Flexibility**: Mix of streaming and batch pipelines.

