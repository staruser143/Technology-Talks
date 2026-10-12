To incorporate actual commission splits or override percentages, you treat the relationship (edge) as a financial contract between the two nodes. You store the mathematical rules—like percentages, flat fees, or tier calculations—as properties directly on that ROLLS_UP_TO edge alongside your product and date filters.
When a premium payment event occurs, Neptune traverses the path, extracts the percentage values from each edge, and hands those numbers over to your billing engine to compute the exact dollar amounts.
------------------------------
## 1. Enhanced Data Model with Financial Properties
Let’s expand the properties stored on the ROLLS_UP_TO edge to include:

* payout_type: Dictates if it's a percentage of premium ("PERCENT") or a flat dollar amount ("FLAT").
* rate: The actual numeric value (e.g., 0.10 for a 10% override).

                        (ROLLS_UP_TO {product_line: "Medicare", rate: 0.05, payout_type: "PERCENT"})
               ┌─────────────────────────────────────────────────────────────────────────────────────► [Regional FMO]
               │
        [Writing Agent]
               │
               └─────────────────────────────────────────────────────────────────────────────────────► [Commercial Agency]
                        (ROLLS_UP_TO {product_line: "Commercial", rate: 15.00, payout_type: "FLAT"})

------------------------------
## 2. Traversal & Extraction Query
When your application processes a premium payment, it needs to know who to pay and exactly what their split rule is. The Gremlin query walks the hierarchy and extracts the entity's name along with its financial parameters in a single payload.

String targetProduct = "Medicare"
long txDate = 20261010  // YYYYMMDD

g.V('agent_123')
  .repeat(
    // Move up the edge and capture its financial properties
    outE('ROLLS_UP_TO')
      .has('product_line', targetProduct)
      .has('start_date', lte(txDate))
      .has('end_date', gte(txDate))
      .as('contract') // Tag the edge as 'contract'
    .inV().as('agency') // Tag the parent node as 'agency'
  )
  .until(__.not(
    outE('ROLLS_UP_TO')
      .has('product_line', targetProduct)
      .has('start_date', lte(txDate))
      .has('end_date', gte(txDate))
  ))
  // Format the output into a scannable payment map
  .select('agency', 'contract')
    .by('name') // Extract name from the agency node
    .by(valueMap('rate', 'payout_type')) // Extract rules from the contract edge

## What the Result Payload Looks Like:
Your application backend receives a highly structured, sequential array mapping the entire money path:

[
  { "agency": "Local Brokerage Corp", "contract": { "rate": [0.12], "payout_type": ["PERCENT"] } },
  { "agency": "Southeast Regional FMO", "contract": { "rate": [0.05], "payout_type": ["PERCENT"] } },
  { "agency": "National Insurance Masters", "contract": { "rate": [0.02], "payout_type": ["PERCENT"] } }
]

------------------------------
## 3. How the Backend Processes the Splits (Net vs. Gross Overrides)
Once Neptune delivers the hierarchy chain and the rates, your transactional billing system handles the math. Graph databases give you the flexibility to calculate overrides in two different ways depending on your business rules:
## Option A: Cumulative Overrides (Gross)
Each tier's percentage is calculated against the total collected gross premium.

* Example: For a $1,000 premium payment, Local gets 12% ($120), Regional gets 5% ($50), and National gets 2% ($20).

## Option B: Differential Overrides (The "Schedule" Method)
In some insurance structures, an FMO is assigned a total payout rate (e.g., 20%), and they pass down a portion to sub-agencies. If the National FMO is at 20%, the Regional is at 18%, and the Agent is at 12%, the graph calculation engine simply subtracts down the path:

* Agent gets their base 12%.
* Regional gets (18% - 12%) = 6% override.
* National gets (20% - 18%) = 2% override.

Because Neptune returns the path as a structured, ordered array, your backend code can loop through the list sequentially and subtract the previous tier's rate from the current tier's rate effortlessly.
Would you like to explore how to handle chargebacks or commission clawbacks through this model (e.g., if a policy cancels early), or should we look at how to bulk-load your existing broker data into Amazon Neptune to test this out?

