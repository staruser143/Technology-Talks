Documents arrive continuously throughout business hours and need structured data extracted. To reduce costs, you want to use the `Message Batches API` (50% discount, up-to-24-hour processing window). Your SLA specifies that extraction results must be available within 30 hours of document arrival with 99.9% reliability. Which batching strategy is most appropriate?

A) Submit batches every 6 hours containing documents from that window

B) Submit a single batch at end of day containing all documents from that day

C) Submit batches every 4 hours containing documents from that window

D) Use the real-time API for all documents instead of batch processing



In production, you observe that simple fact-checking queries (e.g., "What year was the Paris Climate Agreement signed?") traverse all four subagents sequentially, consuming 40+ seconds and significant tokens per query. Complex comparative research benefits from the full pipeline. Your query distribution is diverse and evolving as users discover new applications. What's the most effective approach to optimize for varying query complexity

a) Implement pattern-based routing that categorizes queries by structure (single-fact vs. comparative vs. analytical) and maps each category to a predefined subagent combination.

b) Create a fast-path for factual questions that bypasses subagents entirely, routing all other queries through the complete pipeline to ensure research thoroughness

c) Have the coordinator analyze each query and dynamically decide which subagents to invoke based on its assessment of query requirements.

d) Train a query complexity classifier on labeled historical data to predict optimal subagent combinations, retraining periodically as query patterns evolve

