When using Azure Cosmos DB for NoSQL API as an event sourcing data store (for writes) and a read model domain store (by building projections from the change feed), the choice of consistency levels depends on the nature of event processing and read requirements. Below are my recommendations for both scenarios:


---

1. Event Sourcing Data Store (Writes)

Recommended Consistency Levels:

Strong Consistency (If Multi-Region Writes Disabled)

Guarantees strict ordering of events, ensuring that all writes are immediately visible and correctly sequenced.

Suitable for scenarios where event ordering is mission-critical, such as financial applications.


Session Consistency (Preferred for Performance)

Guarantees that within a session (or partition), all events are read in order.

Recommended for distributed event producers since it balances performance with per-session ordering.


Why Not Eventual or Consistent Prefix?

Eventual and Consistent Prefix may result in out-of-order writes, breaking the integrity of the event stream.




---

2. Read Model Domain Store (Projections from Change Feed)

Recommended Consistency Levels:

Session Consistency (Default for Most Use Cases)

Ensures that projections built within the same session read events in the correct order.

Ideal for systems where each consumer reads events independently and maintains read model consistency.


Consistent Prefix (If Ordering Is Important but Immediate Updates Aren’t)

Guarantees ordered events without enforcing strict session-level consistency.

Useful when eventual convergence of read models is acceptable.


Why Not Strong Consistency?

Strong consistency may introduce unnecessary latency and cost overhead when processing read models from projections.




---

Typical Configuration Recommendations


---

Additional Considerations

Partitioning: Proper partition key design is essential to ensure high performance for both writes and read models.

Change Feed Latency: Use session consistency when projecting read models from change feeds to ensure projection accuracy without increased latency.


Would you like help with architecture diagrams or implementation patterns for this setup?

