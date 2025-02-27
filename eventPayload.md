## Payload field in Event Specification
* The **`payload`** field in an event specification should **only include the fields relevant to the event** being emitted, rather than the entire domain entity object. 
* This approach ensures that the event is concise, focused, and only carries the necessary information for consumers to process it.
* Including the entire domain entity object can lead to unnecessary data duplication, increased payload size, and potential privacy or security concerns.

Let’s break this down with an example in the context of the **insurance domain** (e.g., Quotes and Enrollments).

---

### **Key Principles for Designing the `payload` Field**

1. **Relevance**:
   - The payload should only include data that is directly related to the event.
   - Avoid including fields that are not relevant to the event's purpose.

2. **Minimalism**:
   - Include only the data that consumers need to process the event.
   - Avoid bloating the payload with unnecessary fields.

3. **Contextual Changes**:
   - If the event represents a change to a domain entity, include only the fields that were modified or are relevant to the change.
   - Do not include the entire domain entity unless absolutely necessary.

4. **Lookup for Additional Data**:
   - If consumers need additional data, they can perform a lookup in the domain data store using a unique identifier (e.g., `quoteId`, `enrollmentId`) provided in the payload.

---

### **Scenario: Insurance Quotes and Enrollments**

#### **Event: QuoteRequested**
- **Purpose**: A customer requests a quote for an insurance policy.
- **Payload**:
  - Only include the fields necessary to process the quote request (e.g., `customerId`, `policyType`, `coverageAmount`).
  - Do not include the entire customer or policy entity.

```json
{
  "eventId": "event-001",
  "eventType": "QuoteRequested",
  "payload": {
    "quoteId": "quote-123",
    "customerId": "cust-456",
    "policyType": "Health",
    "coverageAmount": 500000
  }
}
```

#### **Event: QuoteGenerated**
- **Purpose**: A quote is generated based on the customer's request.
- **Payload**:
  - Include only the fields relevant to the generated quote (e.g., `quoteId`, `premiumAmount`).
  - Do not include the entire customer or policy entity.

```json
{
  "eventId": "event-002",
  "eventType": "QuoteGenerated",
  "payload": {
    "quoteId": "quote-123",
    "premiumAmount": 1000
  }
}
```

#### **Event: EnrollmentRequested**
- **Purpose**: A customer requests enrollment based on a quote.
- **Payload**:
  - Include only the fields necessary to process the enrollment (e.g., `enrollmentId`, `quoteId`, `customerId`).
  - Do not include the entire quote or customer entity.

```json
{
  "eventId": "event-003",
  "eventType": "EnrollmentRequested",
  "payload": {
    "enrollmentId": "enroll-789",
    "quoteId": "quote-123",
    "customerId": "cust-456"
  }
}
```

#### **Event: PaymentProcessed**
- **Purpose**: Payment for the enrollment is processed.
- **Payload**:
  - Include only the fields relevant to the payment (e.g., `enrollmentId`, `paymentId`, `status`).
  - Do not include the entire enrollment or customer entity.

```json
{
  "eventId": "event-004",
  "eventType": "PaymentProcessed",
  "payload": {
    "enrollmentId": "enroll-789",
    "paymentId": "payment-123",
    "status": "Success"
  }
}
```

#### **Event: PolicyGenerated**
- **Purpose**: A policy is generated after successful enrollment and payment.
- **Payload**:
  - Include only the fields relevant to the policy (e.g., `policyId`, `enrollmentId`, `coverageAmount`).
  - Do not include the entire enrollment or customer entity.

```json
{
  "eventId": "event-005",
  "eventType": "PolicyGenerated",
  "payload": {
    "policyId": "policy-456",
    "enrollmentId": "enroll-789",
    "coverageAmount": 500000
  }
}
```

---

### **Why Not Include the Entire Domain Entity?**

1. **Data Duplication**:
   - Including the entire domain entity in every event leads to redundant data being transmitted, which increases network overhead and storage requirements.

2. **Privacy and Security**:
   - Domain entities often contain sensitive information (e.g., customer details). Transmitting this data in every event can expose it to unnecessary risks.

3. **Event Size**:
   - Large payloads can slow down event processing and increase costs, especially in high-throughput systems.

4. **Decoupling**:
   - Events should be self-contained and decoupled from the internal state of the domain entity. Consumers should only receive the data they need to process the event.

---

### **When to Include Additional Data**

In some cases, we may need to include additional data in the payload to avoid forcing consumers to perform lookups. This is typically done when:
- The additional data is small and directly relevant to the event.
- The lookup would introduce significant latency or complexity.
- The data is immutable and unlikely to change.

For example, in the `QuoteGenerated` event, you might include the `policyType` and `coverageAmount` to provide context without requiring a lookup:

```json
{
  "eventId": "event-002",
  "eventType": "QuoteGenerated",
  "payload": {
    "quoteId": "quote-123",
    "policyType": "Health",
    "coverageAmount": 500000,
    "premiumAmount": 1000
  }
}
```

---

### **Key Takeaways**

1. **Keep Payloads Focused**:
   - Include only the fields relevant to the event.
   - Avoid including the entire domain entity.

2. **Use Lookups for Additional Data**:
   - If consumers need more data, they can perform a lookup using a unique identifier (e.g., `quoteId`, `enrollmentId`).

3. **Balance Between Data and Lookups**:
   - Include additional data only when it significantly improves efficiency or reduces complexity.

By following these principles, you can design event payloads that are efficient, secure, and easy to process, while avoiding unnecessary data duplication and complexity.
