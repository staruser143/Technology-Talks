To handle chargebacks or commission clawbacks (e.g., when a policy cancels early, a member lapses, or a broker misrepresents a sale), you must map the reverse flow of money.
Because a chargeback is a transaction that happens at a specific point in time, you handle it by combining your historical hierarchy edges with a transactional ledger event.
Here are the two industry-standard patterns for handling chargebacks using Amazon Neptune.
------------------------------
## Pattern 1: Point-in-Time Traversal (Recommended)
This approach dictates that money must be clawed back from the exact same entities who received it, based on the original sale date of the policy, even if the broker hierarchy has changed since then.

   1. Find the Original Event: Look up the original transaction record to find the Policy Effective Date (e.g., January 1, 2026).
   2. Traverse the Past: Query Neptune to climb the hierarchy using that past date.
   3. Generate Negative Ledger Entries: Extract the rates and entities active at that time, reverse the math, and post negative balances to your RDBMS financial ledger.

// Use the original policy sale date, NOT today's date, to find who was paid
long originalSaleDate = 20260101 
String targetProduct = "Medicare"

g.V('agent_123')
  .repeat(
    outE('ROLLS_UP_TO')
      .has('product_line', targetProduct)
      .has('start_date', lte(originalSaleDate))
      .has('end_date', gte(originalSaleDate)).as('contract')
    .inV().as('agency')
  )
  .until(__.not(outE('ROLLS_UP_TO').has('product_line', targetProduct).has('start_date', lte(originalSaleDate)).has('end_date', gte(originalSaleDate))))
  .select('agency', 'contract')
    .by('name')
    .by(valueMap('rate', 'payout_type'))


* Why it works: If an agent left Regional Agency A and moved to Regional Agency B in June, a chargeback processed in October for a January sale will accurately claw back the override from Regional Agency A.

------------------------------
## Pattern 2: Explicit Chargeback Rule Edges (For Exceptions)
Sometimes, insurance payer contracts state that if an agent leaves, any subsequent chargebacks roll up to their current upline, or a completely different guarantor agency takes the financial hit.
To handle this complexity, you can add explicit CHARGEBACK_ROLLS_TO edges to your graph to override the standard payment route.

                             (ROLLS_UP_TO {rate: 0.10})
                     ┌───────────────────────────────────────► [Agency A (Past Master)]
                     │
    [Writing Agent] ─┤
                     │       (CHARGEBACK_ROLLS_TO)
                     └───────────────────────────────────────► [Agency B (Current Guarantor)]

## Traversal with a Fallback Chargeback Edge:
You can write a query that checks if a special chargeback relationship exists. If it does, follow it; if it doesn't, default to the standard historical hierarchy.

long chargebackEventDate = 20261010 // Today's date

g.V('agent_123')
  .choose(
    // Check if a specific chargeback override route exists for today
    __.outE('CHARGEBACK_ROLLS_TO').has('start_date', lte(chargebackEventDate)).has('end_date', gte(chargebackEventDate)),
    // If YES: Follow the chargeback edge
    __.outE('CHARGEBACK_ROLLS_TO').inV(),
    // If NO: Fallback to the standard product-specific payment hierarchy
    __.outE('ROLLS_UP_TO').has('product_line', 'Medicare').has('start_date', lte(chargebackEventDate)).has('end_date', gte(chargebackEventDate)).inV()
  )
  .valueMap('name')

------------------------------
## Architecture Best Practice: The Financial Safeguard
Never perform the math calculation or hold the "current balance" inside Amazon Neptune.

* Neptune's Job: Map the structural path of who is responsible for the chargeback and what their contractual clawback rate is.
* RDBMS Ledger's Job: Receive the array of entities from Neptune, multiply the rates by the chargeback amount (e.g., -$500), check if the broker has an active balance to offset it, or flag their account for a "negative recovery" collection cycle.

Now that we've covered hierarchies, product splits, and chargebacks, would you like to see how to bulk-load your existing SQL data tables (Brokers, Relationships, Products) into Amazon Neptune using the AWS bulk loader?

