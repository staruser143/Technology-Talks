To handle product-specific hierarchies (e.g., an agent rolls up to Agency A for Medicare products, but rolls up to Agency B for Commercial Group Health), you expand your edge properties to include a product_line or line_of_business attribute.
This ensures you don’t need to build completely separate database structures for different lines of business. Instead, the same agent node can have multiple concurrent ROLLS_UP_TO paths that are filtered dynamically during the query.
------------------------------
## 1. The Data Model Design
In this model, the edges contain both the time-bound attributes (start_date, end_date) and the product scope attributes (product_line).

                     (ROLLS_UP_TO {product_line: "Medicare", start: 20260101})
              ┌────────────────────────────────────────────────────────────────► [Medicare FMO]
              │
       [Writing Agent]
              │
              └────────────────────────────────────────────────────────────────► [Commercial Agency]
                     (ROLLS_UP_TO {product_line: "Commercial", start: 20260101})

------------------------------
## 2. Writing the Product-Filtered Gremlin Traversal
When a commission needs to be calculated, your system will provide the Product Line and the Transaction Date. The Gremlin query will then traverse only the edges that match both criteria simultaneously.
Here is the query to calculate the hierarchy path for a Medicare policy processed on October 10, 2026:

// Input parameters from your application pipeline
String targetProduct = "Medicare"
long txDate = 20261010  // YYYYMMDD

g.V('agent_123')
  .repeat(
    // 1. Traverse the edge only if it matches the specific product line AND is currently active
    outE('ROLLS_UP_TO')
      .has('product_line', targetProduct)
      .has('start_date', lte(txDate))
      .has('end_date', gte(txDate))
    // 2. Move to the parent agency node
    .inV()
  )
  // Continue climbing until there are no parent agencies matching this product and date criteria
  .until(__.not(
    outE('ROLLS_UP_TO')
      .has('product_line', targetProduct)
      .has('start_date', lte(txDate))
      .has('end_date', gte(txDate))
  ))
  .path()
    .by(valueMap('name'))

------------------------------
## 3. Handling "Catch-All" Fallback Hierarchies
In real-world insurance scenarios, an agency might have a specific hierarchy for complex lines of business (like Individual ACA), but use a default global hierarchy for everything else.
You can model this by setting product_line: "All" as a fallback property on generic edges. Your Gremlin query can handle this gracefully using a fallback check:

// Filter for the specific product OR a fallback labeled "All"
outE('ROLLS_UP_TO')
  .has('start_date', lte(txDate))
  .has('end_date', gte(txDate))
  .has('product_line', within(targetProduct, 'All')) 
.inV()

## Why this approach keeps your codebase clean:

* Zero Schema Alterations: If your company launches a new product line tomorrow (e.g., Dental/Vision), you don’t change a single database table or column. You simply write new edges into Neptune with product_line: "Dental".
* Consolidated Profile Management: The broker’s demographic information, tax IDs, and basic licensing data stay stored once on the central Agent node, avoiding duplication across disparate product databases.

Would you like to see how to incorporate the actual commission split or override percentages directly onto these product-specific edges, or are you ready to look at how to bulk-load your existing relational tables into Amazon Neptune?

