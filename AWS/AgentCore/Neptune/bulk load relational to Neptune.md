To get your existing relational database (RDBMS) tables into Amazon Neptune, you cannot simply run a traditional SQL dump. Because graph databases use a fundamentally different format, you must convert your tabular relational data into highly specific Vertices (Nodes) and Edges (Relationships) CSV files. [1, 2] 
Once formatted, you can execute a low-overhead, fast native ingestion using the Amazon Neptune Bulk Loader API. [1, 3] 
------------------------------
## Step 1: Format Your Relational Tables into Graph CSVs
Neptune's bulk loader requires separate CSV files for Nodes and Edges. They must contain structural headers that tell Neptune how to construct the graph. [2, 4, 5] 
## A. The Nodes (Vertices) CSV File
Extract your core entities (Brokers, Agencies, Policies). Every node must have a unique ~id and a ~label. [2, 6] 
File name example: agencies_nodes.csv

~id,~label,name:String,type:String
agency_100,Agency,National Insurance Masters,FMO
agency_101,Agency,Southeast Regional FMO,Regional
agent_123,Agent,John Doe,WritingAgent

## B. The Edges (Relationships) CSV File
Extract your relational foreign keys (e.g., matching an agent to their parent agency). Edges require a unique ~id, a source node (~from), a destination node (~to), and a relationship type (~label). This is where you place your time-bound and product-specific properties. [2, 4] 
File name example: broker_hierarchy_edges.csv

~id,~from,~to,~label,product_line:String,rate:Double,payout_type:String,start_date:Long,end_date:Long
edge_001,agent_123,agency_101,ROLLS_UP_TO,Medicare,0.12,PERCENT,20260101,20991231
edge_002,agency_101,agency_100,ROLLS_UP_TO,Medicare,0.05,PERCENT,20260101,20991231
edge_003,agent_123,agency_100,CHARGEBACK_ROLLS_TO,Medicare,0.00,PERCENT,20260601,20991231

(Tip: You can use an ETL tool like AWS Glue or simple Python scripts to convert your SQL table exports into this layout). [5, 7] 
------------------------------
## Step 2: Upload Files to Amazon S3
Upload your converted CSV files to an Amazon S3 bucket. [3, 8] 

* 
* Ensure your S3 bucket resides in the same AWS Region as your Amazon Neptune cluster. [9, 10] 
* 

------------------------------
## Step 3: Configure AWS IAM Permissions
To allow Neptune to securely reach out and grab the files from S3, you must establish trust permissions: [3] 

   1. Create an IAM Role (e.g., NeptuneS3BulkLoadRole) with a policy allowing s3:GetBucketLocation, s3:ListBucket, and s3:GetObject on your bucket.
   2. Edit the role's Trust Relationship so that the Neptune service can assume it:
   
   {
     "Version": "2012-10-17",
     "Statement": [{
       "Effect": "Allow",
       "Principal": { "Service": "://amazonaws.com" },
       "Action": "sts:AssumeRole"
     }]
   }
   
   3. Attach this IAM Role to your Amazon Neptune Cluster using the AWS Neptune Console. [3, 8, 10] 

------------------------------
## Step 4: Trigger the Bulk Loader via API Call
The Neptune bulk loader doesn't have a UI button; you trigger it by making an HTTP POST request to your Neptune cluster's loader endpoint (usually via port 8182). [8, 11] 
You can run this curl command from an EC2 instance or a Cloud9 terminal inside the same VPC as your Neptune cluster: [8, 10] 

curl -X POST \
  -H 'Content-Type: application/json' \
  https://your-neptune-cluster-endpoint:8182/loader -d '{
    "source" : "s3://your-broker-bucket-name/folder-with-csvs/",
    "format" : "csv",
    "iamRoleArn" : "arn:aws:iam::123456789012:role/NeptuneS3BulkLoadRole",
    "region" : "us-east-1",
    "failOnError" : "FALSE",
    "parallelism" : "HIGH"
  }'


* 
* source: Point it directly to the S3 path containing your Node and Edge files.
* parallelism: Setting this to HIGH instructs Neptune to utilize all available cluster resources to split and load the data concurrently. [11] 
* 

------------------------------
## Step 5: Check Load Progress
The curl command will immediately return a JSON response containing a "loadId" UUID. You can monitor the progress, row count, and any syntax errors by appending that ID to a GET request: [4] 

curl -G 'https://your-neptune-cluster-endpoint:8182/loader/YOUR-LOAD-ID-UUID'

Once the status reads "LOAD_COMPLETED", your multi-tiered, product-specific broker hierarchy is fully active in memory and ready for Gremlin traversals. [7] 
Would you like assistance writing a script to automate the generation of these CSVs from your SQL data, or would you like to review how to handle incremental database updates (upserts) after the initial bulk load is complete? [5, 6] 

[1] [https://docs.aws.amazon.com](https://docs.aws.amazon.com/neptune/latest/userguide/load-data.html)
[2] [https://www.youtube.com](https://www.youtube.com/watch?v=kCmmgt2f278)
[3] [https://docs.aws.amazon.com](https://docs.aws.amazon.com/neptune/latest/userguide/bulk-load.html)
[4] [https://docs.aws.amazon.com](https://docs.aws.amazon.com/neptune-analytics/latest/userguide/bulk-import.html)
[5] [https://stackoverflow.com](https://stackoverflow.com/questions/74940659/csv-to-neptune-bulk-format)
[6] [https://repost.aws](https://repost.aws/questions/QUG_pdX_XhQiO40tR1uknV9w/how-can-insert-update-upsert-data-into-neptune-opencypher)
[7] [https://github.com](https://github.com/aws-samples/amazon-neptune-samples/blob/master/gremlin/etl-from-relational-model/README.md)
[8] [https://dev.to](https://dev.to/aws-builders/build-a-graph-database-with-amazon-neptune-part-2-load-data-32j1)
[9] [https://docs.aws.amazon.com](https://docs.aws.amazon.com/neptune-analytics/latest/userguide/loading-data-existing-graph.html)
[10] [https://docs.aws.amazon.com](https://docs.aws.amazon.com/neptune/latest/userguide/bulk-load-data.html)
[11] [https://medium.com](https://medium.com/@sanjeetsahay/3-steps-to-bulk-load-data-into-amazon-neptune-3888b667bb27)
