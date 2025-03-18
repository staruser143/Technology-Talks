Creating a materialized view in MongoDB using code involves using the aggregation framework with the `$merge` or `$out` stages. Here's an example of how to do this using Node.js and the MongoDB driver:

### Example Using Node.js

1. **Install MongoDB Driver**:
   If you haven't already, install the MongoDB driver for Node.js:
   ```bash
   npm install mongodb
   ```

2. **Connect to MongoDB**:
   Set up a connection to your MongoDB instance:
   ```javascript
   const { MongoClient } = require('mongodb');

   async function createMaterializedView() {
     const uri = 'your_mongodb_connection_string';
     const client = new MongoClient(uri, { useNewUrlParser: true, useUnifiedTopology: true });

     try {
       await client.connect();
       const database = client.db('your_database_name');
       const collection = database.collection('your_collection_name');

       // Define the aggregation pipeline
       const pipeline = [
         {
           $group: {
             _id: '$productId',
             totalSales: { $sum: '$amount' }
           }
         },
         {
           $merge: {
             into: 'materialized_view_collection',
             on: '_id',
             whenMatched: 'merge',
             whenNotMatched: 'insert'
           }
         }
       ];

       // Execute the aggregation pipeline
       await collection.aggregate(pipeline).toArray();
       console.log('Materialized view created successfully');
     } finally {
       await client.close();
     }
   }

   createMaterializedView().catch(console.error);
   ```

### Explanation:
1. **Install MongoDB Driver**: Ensure you have the MongoDB driver installed.
2. **Connect to MongoDB**: Establish a connection to your MongoDB instance using the connection string.
3. **Define the Aggregation Pipeline**: Create an aggregation pipeline that includes the stages you need. In this example, we use `$group` to aggregate sales data and `$merge` to write the results to a new collection.
4. **Execute the Aggregation Pipeline**: Run the aggregation pipeline using the `aggregate` method and handle the results.

### Example Using Python (Optional)

If you prefer using Python, here's a similar example using the `pymongo` library:

1. **Install PyMongo**:
   ```bash
   pip install pymongo
   ```

2. **Connect to MongoDB**:
   ```python
   from pymongo import MongoClient

   def create_materialized_view():
       uri = 'your_mongodb_connection_string'
       client = MongoClient(uri)
       database = client['your_database_name']
       collection = database['your_collection_name']

       # Define the aggregation pipeline
       pipeline = [
           {
               '$group': {
                   '_id': '$productId',
                   'totalSales': { '$sum': '$amount' }
               }
           },
           {
               '$merge': {
                   'into': 'materialized_view_collection',
                   'on': '_id',
                   'whenMatched': 'merge',
                   'whenNotMatched': 'insert'
               }
           }
       ]

       # Execute the aggregation pipeline
       collection.aggregate(pipeline)
       print('Materialized view created successfully')

   create_materialized_view()
   ```

This example follows the same logic as the Node.js example but uses Python and the `pymongo` library[1](https://www.mongodb.com/docs/manual/core/materialized-views/)[2](https://tapdata.io/data-engineering-resources/create-manage-materialized-views-mongodb/).
