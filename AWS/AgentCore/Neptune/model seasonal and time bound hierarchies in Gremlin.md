To model seasonal, time-bound, or shifting broker hierarchies in Amazon Neptune, you leverage Edge Properties.
In a graph database, relationships (edges) are first-class citizens. You can attach start_date and end_date attributes directly to the ROLLS_UP_TO edge. This allows you to preserve historical data, manage future contract changes, and query the hierarchy exactly as it looked on the day a specific policy was sold.
------------------------------
## 1. The Data Model Design
Instead of deleting an old relationship when an agent switches agencies, you simply "close" it by setting an end_date and create a new edge for the new agency.

                     (ROLLS_UP_TO {start: 2024-01-01, end: 2026-05-01})
              ┌────────────────────────────────────────────────────────► [Legacy Agency A]
              │
       [Writing Agent]
              │
              └────────────────────────────────────────────────────────► [New Agency B]
                     (ROLLS_UP_TO {start: 2026-05-02, end: 2099-12-31})

------------------------------
## 2. Writing the Gremlin Traversal
When a premium payment comes in, you need to traverse the hierarchy based on the Policy Effective Date (or the date the premium event occurred).
Here is how you write a Gremlin query that filters out irrelevant historical or future relationships and only climbs the active hierarchy for a specific date (e.g., June 1, 2026).

// Define the transaction date you want to audit/pay out for
long txDate = 20260601 // Format: YYYYMMDD (or use epoch timestamps)

g.V('agent_123')
  .repeat(
    // 1. Move to the outgoing "ROLLS_UP_TO" edge
    outE('ROLLS_UP_TO')
    // 2. Filter the edge: The transaction date must fall between start and end dates
    .has('start_date', lte(txDate))
    .has('end_date', gte(txDate))
    // 3. Move to the parent agency node
    .inV()
  )
  // Continue climbing until there are no active parent edges left
  .until(__.not(
    outE('ROLLS_UP_TO')
    .has('start_date', lte(txDate))
    .has('end_date', gte(txDate))
  ))
  // Return the verified historical path of agencies
  .path()
    .by(valueMap('name'))

## Why this approach is powerful:

* No Code Changes for Seasonal Structuring: If an agency structure is seasonal (e.g., a special hierarchy specifically for the Medicare Annual Enrollment Period from Oct 15 – Dec 7), you just add those specific dates to the edge properties.
* Point-in-Time Auditing: If a compliance auditor asks why a commission split was paid to Agency A instead of Agency B for a two-year-old policy, you can run this exact query using the 2024 policy date to reconstruct the past perfectly.

------------------------------
## 3. Handling Mid-Month Hierarchy Switches (Pro-Tip)
If an agent switches agencies on June 15th, and a monthly policy premium is processed on June 20th, who gets the override? You have two architectural choices in your graph logic:

   1. Policy-Date Driven (Most Common): The hierarchy is evaluated using the Policy Issue Date. Even if the agent moves tomorrow, past policies stay tied to the historical hierarchy path via the edge dates.
   2. Event-Date Driven: The hierarchy is evaluated using the Payment Processing Date. The query will automatically route the override to the new agency because the edge constraints evaluate to true for the new timeline.

Would you like to look at how to handle Product-Specific Hierarchies (e.g., an agent rolls up to Agency A for Life Insurance, but rolls up to Agency B for Health Insurance), or should we look at how to bulk-load this data into Neptune?

