When handling payments in an event-sourced system with Event Store, an **asynchronous, event-driven approach** is typically preferred for scalability, resilience, and user experience. Here's a structured solution:

---

### **Recommended Approach**
1. **Immediate Acknowledgment**  
   - Append a `PaymentInitiated` event to the Event Store upon receiving the payment request.  
   - Return a **202 Accepted** response with a status endpoint or correlation ID, indicating the payment is "in progress."

2. **Asynchronous Payment Processing**  
   - Trigger a background process (e.g., via a Saga or message handler) to communicate with the external payment gateway.  
   - Ensure **idempotency** by including a unique payment ID to handle retries safely.

3. **Event-Driven Status Updates**  
   - On success/failure, append `PaymentSucceeded` or `PaymentFailed` to the Event Store.  
   - Update the read model to reflect the latest status (e.g., `OrderPaid` or `PaymentDeclined`).

4. **Real-Time Client Notification**  
   - Use real-time mechanisms (WebSocket, Server-Sent Events, or push notifications) to notify the client of status changes.  
   - Allow clients to poll a status endpoint using the correlation ID if real-time push isn’t feasible.

5. **Compensation for Failures**  
   - If payment fails, trigger compensating actions (e.g., `OrderCancelled` event) via a Saga.  
   - Provide retry options to the client if applicable.

---

### **Why Asynchronous?**
- **Scalability**: Avoid blocking resources while waiting for external payment systems (which may have variable latency).  
- **Resilience**: Decouple payment processing from the client interaction, allowing retries without user involvement.  
- **Auditability**: All state changes are captured as events, ensuring a reliable audit trail.  
- **User Experience**: Clients receive immediate feedback ("request received") instead of waiting for uncertain processing times.

---

### **Example Workflow**
1. **Client Request**:  
   `POST /payments` → Appends `PaymentInitiated` event.  
   → Response: `202 Accepted` with `{ statusUrl: "/payments/123/status" }`.

2. **Async Processing**:  
   - A `ProcessPayment` command is sent to a background service.  
   - The service interacts with the payment gateway (e.g., Stripe, PayPal).

3. **Outcome Handling**:  
   - **Success**: Append `PaymentSucceeded` → Update read model → Notify client.  
   - **Failure**: Append `PaymentFailed` → Update read model → Notify client and suggest retry.

4. **Client Status Check**:  
   - Poll `GET /payments/123/status` or receive real-time updates via WebSocket.  
   - Final status: `completed`, `failed`, or `retrying`.

---

### **Key Considerations**
- **Idempotency Keys**: Ensure payment gateways process duplicate requests safely.  
- **Saga Pattern**: Manage long-running transactions and handle failures with compensating events.  
- **Read Model Consistency**: Use projections to derive current payment status for queries.  
- **Monitoring**: Track pending payments and alert on timeouts/errors.

---

This approach balances responsiveness with reliability, leveraging event sourcing’s strengths while integrating with external systems pragmatically.