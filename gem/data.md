That's a very insightful point. You are correct that relying purely on flattening forces your configuration to be verbose, as you must explicitly list every indexed field (items_0_product, items_1_product, etc.). This means maintaining a large configuration file even when the array structure is simple.
The solution is to combine a targeted configuration with dynamic data traversal and PDF field discovery. This approach eliminates the need to list every indexed field.
Here is the approach to dynamically determine and map nested fields without full, up-front flattening:
1. ⚙️ Simplified Configuration (The Key)
Instead of listing every potential indexed field, your configuration should only define the root path of the array/object and the field suffixes within it.
Configuration Example
| Source Path (Root) | Target PDF Field Prefix | Field Suffixes (Within Array/Object) |
|---|---|---|
| orders (an array) | order_item_ | productName, quantity, price |
| customer.address (an object) | cust_addr_ | street, city, zip |
2. 🚶 Dynamic Traversal and Field Discovery
Your generation logic will iterate through the source data based on the configuration and, for each array element, dynamically construct the target PDF field name based on an index.
A. Identify and Iterate Arrays
For an array defined in the config (e.g., orders):
 * Access the array data from the source (e.g., sourceData.get("orders")).
 * Loop through the array elements using an index (i = 0, 1, 2, \dots).
 * For each element, loop through the defined Field Suffixes (productName, quantity, etc.).
 * Dynamically Construct PDF Field Name:
| Iteration (i) | Suffix | Constructed Target Field | Source Path (Data Retrieval) |
|---|---|---|---|
| 0 | productName | order_item_0_productName | orders[0].productName |
| 1 | quantity | order_item_1_quantity | orders[1].quantity |
B. Handle Objects
For an object defined in the config (e.g., customer.address):
 * Access the object data (e.g., sourceData.get("customer").get("address")).
 * Loop through the defined Field Suffixes (street, city, etc.).
 * Dynamically Construct PDF Field Name:
| Suffix | Constructed Target Field | Source Path (Data Retrieval) |
|---|---|---|
| street | cust_addr_street | customer.address.street |
3. 💾 Set the PDF Field Value (PDFBox)
Inside your dynamic loops, you perform the PDFBox action:
 * Retrieve Value: Use the fully constructed source path (e.g., orders[0].productName) to pull the data from the source.
 * Find Field: Use the fully constructed target field name (e.g., order_item_0_productName) to find the PDF field: acroForm.getField(TargetField).
 * Set Value: field.setValue(dataValue).
This approach keeps your configuration lightweight and manageable because you only configure the structure, not every individual instance.
Caveat: Maximum Array Size
You still need to know the maximum number of fields your PDF template supports (e.g., if the template has 5 rows for items, you stop the array loop after i=4). This limit must be respected by your logic to prevent searching for non-existent PDF fields.
This dynamic mapping process provides a clean solution: it uses a small config file, dynamically generates field names, and avoids the complexity of full data flattening, achieving what you are looking for.
Would you like me to focus on the Java implementation of the dynamic array iteration and field name construction?
