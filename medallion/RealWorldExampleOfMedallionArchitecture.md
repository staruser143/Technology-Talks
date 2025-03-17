# Real-world example of applying Medallion Architecture in an insurance domain for claims processing and analytics:

<table>
  <tr>
    <th>Scenario</th>
    <td> Insurance Claims Analytics Platform</td>
  </tr>
  <tr>
    <th>Problem</th>
    <td>An insurance company wants to build a data platform that can:
      <ul>
      <li>Ingest raw claims data from multiple sources (e.g., claims management system, third-party adjusters, IoT devices, partner APIs).</li>
      <li>Clean, validate, and enrich this data.</li>
        <li>Provide actionable insights to various teams (claims adjusters, fraud detection, actuarial teams, executives)</li>.</td>
      </ul>
  </tr>
</table>


## Applying Medallion Architecture:
<table>
  <tr>
    <th>Bronze Layer (Raw Data)</th>
    <td> <b>Data Sources:</b>

<ul><li>Claims submitted via internal systems (CSV, JSON).</li>
<li>Third-party adjuster reports (Excel, PDFs converted to structured data).</li>
<li>IoT data (e.g., telematics data from vehicles).</li>
<li>APIs for partner data (e.g., healthcare providers, repair shops).</li>
</ul>


<b>Storage Example:</b>

<ul><li>/bronze/claims/2025/03/14/claims_batch_1.json</li>
<li>/bronze/adjuster_reports/2025/03/14/report_1.xlsx</li>
<li>/bronze/iot/2025/03/14/telematics_data_1.csv</li></ul>

<b>Characteristics:</b>
<ul>
  <li>Stored as-is, no transformations.</li>
  <li>Used for auditing and reprocessing in case of pipeline failure.</li>
  <li>Supports schema evolution for new data formats.</li>
</ul>
  </td></tr>

  <tr>
    <th>Silver Layer (Cleaned and Enriched Data)</th>
    <td> <b>Processing Steps:</b>

<ul>
  <li>Data validation (e.g., mandatory fields like claim_id, policy_id).</li>
  <li>Deduplication (e.g., removing duplicate claims).</li>
  <li>Type casting (e.g., claim amount as decimal).</li>
  <li>Enriching claims with customer data (joining with policyholder database).</li>
  <li>Flagging missing information for review.</li>
</ul>
<b>Example Table/View:</b>
<ul><li>/silver/claims/processed_claims.parquet</li>
<li>/silver/adjuster_reports/cleaned_reports.parquet</li>
<li>/silver/iot/processed_telematics.parquet</li></ul>

<b>Characteristics:</b>
<ul>
  <li>Clean, validated, enriched data.</li>
  <li>Used for generating reports and feeding downstream applications like fraud detection models.</li>
</ul>
  </td></tr>

  <tr>
    <th>Gold Layer (Business-Ready Data for Analytics)</th>
    <td> <b>Aggregations and Modeling:</b>

<ul>
High-risk claims flagged for audit.
  <li>Total claims by region, type, and adjuster</li>
  <li>Average claim settlement time.</li>
  <li>Fraud risk scoring (using Silver + ML models)</li>
  <li>High-risk claims flagged for audit.</li>
</ul>
<b>Example Table/View:</b>
<ul><li>/gold/claims/claims_summary.parquet</li>
<li>/gold/claims/claims_fraud_risk.parquet</li>
<li>/gold/analytics/claim_adjuster_performance.parquet</ul>

<b>Consumers</b>
<ul><li>Claims adjusters to prioritize high-value or high-risk claims.</li>
<li>Fraud detection teams to investigate suspicious claims.</li>
<li>Executives for KPIs and trend analysis via Power BI/Tableau dashboards.</li>
</ul>
</td></tr>
</table>


## Benefits Achieved:
<table>
  <tr>
    <th>Traceability</th>
    <td>Ability to trace every Gold-level insight back to raw data.</td>
  </tr>
  <tr>
    <th>Data Quality</th>
    <td>Improved as data flows from Bronze → Silver → Gold.</td>
  </tr>
  <tr>
    <th>Performance</th>
    <td>Aggregated and optimized datasets in Gold for fast querying.</td>
  </tr>
  <tr>
    <th>Flexibility</th>
    <td>Ability to handle new claim types or adjuster formats by updating Bronze/Silver layers without disrupting Gold consumers</td>
  </tr>
  <tr>
    <th>Audit and Compliance</th>
    <td>Retain raw data for regulatory audits</td>
  </tr>
</table>

## Summary Flow:

* Raw Claims Data (APIs, files, IoT) --> [Bronze] --> 
* Clean, Enriched Claims Data (validated, joined) --> [Silver] --> 
* Business Insights (KPIs, risk scores, dashboards) --> [Gold]

