In this situation, you have two valid approaches, and which one is "correct" depends on factors like system design preferences, auditability, and event-sourcing practices. Let me break it down for you:

### Option 1: Fire `PersonalDetailsSaved` Event with Only the Changed Field (`emailAddress`)
- **How It Works**: The event payload would include only the `emailAddress` field, as it's the only field that changed.
- **Pros**:
  1. **Smaller Payloads**: Storing only the delta (i.e., what changed) reduces event size, optimizing storage and processing.
  2. **Easier Change Detection**: It clearly signals that only this specific field was updated, improving traceability.
  3. **Fine-Grained Replay**: Replaying events allows reconstructing changes in fine detail, including when specific fields were modified over time.
- **Cons**:
  1. **Complex Event Replay**: Rebuilding the full state for `PersonalDetails` during projection requires aggregating all historical events for the entity, which can be more computationally expensive.
  2. **Versioning Complexity**: If additional fields or transformations are introduced in future versions, tracking only deltas may complicate compatibility.

### Option 2: Fire `PersonalDetailsSaved` Event with the Complete Data
- **How It Works**: The event payload contains the entire `PersonalDetails` section, even though only `emailAddress` changed.
- **Pros**:
  1. **Simplified Replay**: The latest event represents the full state of `PersonalDetails`, reducing the need to aggregate earlier events for reconstruction.
  2. **Less Coupling to Previous Events**: If older events are missing or corrupted, the state can still be reconstructed from the most recent event.
  3. **Easier Debugging**: It's straightforward to identify the current state of the `PersonalDetails` section without piecing together deltas.
- **Cons**:
  1. **Larger Payloads**: Storing redundant, unchanged data increases storage and bandwidth usage.
  2. **Audit Traceability**: It may not be immediately evident what specifically changed between events.

---

### Deciding the Correct Approach:
1. **Delta (Option 1)** is better when:
   - Auditability is a priority, and you need a detailed change log (e.g., for compliance or debugging).
   - Your event store can efficiently handle aggregation for state reconstruction.
   - The domain logic emphasizes tracking "what changed" rather than the full current state.

2. **Complete Data (Option 2)** is better when:
   - Simplicity and performance of projections matter more (e.g., building queries, read models).
   - Reconstructing state efficiently is a priority, especially for large-scale systems with many events.
   - Your domain rules frequently deal with the complete state rather than changes.

---

### Suggested Approach for a Quote Application:
Given that you're working on a **Quote Application** and considering event sourcing principles:
- If **auditability and detailed tracking** (who changed what, when) are crucial, go for **Option 1** with deltas.
- If **simplicity and ease of state reconstruction** are more important (which is often true for quoting systems focused on scalability and reliability), choose **Option 2** with the full payload.

In practice, many systems combine these approaches. For instance, you could store deltas most of the time but occasionally emit a "snapshot event" containing the full state to simplify projections. Does this align with your understanding of CQRS and event sourcing, or would you like to dive into best practices for handling similar scenarios?