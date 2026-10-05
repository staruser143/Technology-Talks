Preventing the unauthorized entry, processing, or leakage of PHI (Protected Health Information) and PII (Personally Identifiable Information) is the most critical security requirement for healthcare and insurance AI agents. Because LLMs are probabilistic, **you cannot rely on prompt engineering alone** to protect sensitive data. 

Amazon Bedrock AgentCore is designed for a **defense-in-depth, deterministic security model**. You must implement controls at three distinct layers: **Pre-Processing (Inbound)**, **In-Processing (Execution)**, and **Post-Processing (Outbound)**.

Here is the architectural blueprint for securing PHI/PII in AgentCore.

---

### Layer 1: Pre-Processing (Inbound Interception & Redaction)
*Goal: Stop sensitive data from ever reaching the LLM or internal databases.*

1. **AgentCore Gateway Interceptors (Request Phase)**
   Before the agent processes the user's input, the Gateway invokes a custom Lambda function (the Interceptor). You can use this to scan and sanitize the payload.
   * **Regex Pattern Matching**: Immediately block or redact obvious patterns (e.g., 9-digit SSNs, specific Member ID formats, credit card numbers) using regular expressions.
   * **Amazon Comprehend Integration**: The Interceptor Lambda can call the Amazon Comprehend `DetectPiiEntities` or `DetectProtectedHealthInformation` APIs. If PHI/PII is detected in a context where it shouldn't be (e.g., a general FAQ chat), the interceptor can either:
     * **Redact**: Replace the sensitive data with `[REDACTED]` before passing the prompt to the agent.
     * **Block**: Return a `400 Bad Request` with a message: *"For your security, please do not share full SSNs or medical record numbers in this chat."*

2. **Strict Input Schema Validation**
   Configure the AgentCore Gateway to strictly validate the JSON schema of incoming requests. If a user (or a malicious script) attempts to inject unexpected fields containing PII, the Gateway rejects the payload before it reaches the agent code.

---

### Layer 2: In-Processing (Deterministic Guardrails & Access Control)
*Goal: Ensure that even if data is processed, the agent cannot access or act on PHI/PII it is not explicitly authorized to see.*

1. **AgentCore Policy (Cedar)**
   This is your most powerful tool for preventing unauthorized data processing. AgentCore Policy evaluates rules at the Gateway level *before* any tool is executed.
   * **Example Policy**: *"Permit the `fetch_clinical_notes` tool ONLY IF the `patient_id` parameter in the tool request exactly matches the `sub` (user ID) claim in the authenticated JWT."*
   * **Impact**: Even if a prompt injection attack tricks the agent into trying to fetch another patient's records, the Cedar policy engine deterministically blocks the tool call. The LLM never sees the data, and the database is never queried.

2. **Amazon Bedrock Guardrails**
   Attach native Amazon Bedrock Guardrails to the Foundation Model invocation within AgentCore Runtime. You can configure these to:
   * **Deny specific topics**: Block the agent from discussing or generating content related to specific sensitive medical procedures or financial advice if outside its scope.
   * **Word/Phrase Filters**: Maintain a custom deny list of highly sensitive internal terms or codes that should never be generated or processed.

3. **Data Minimization in Tool Design**
   Design your Gateway Targets (tools) to return *only* the minimum necessary data. Instead of a tool that returns an entire 50-page medical record, create a tool that returns *only* the specific field requested (e.g., `get_last_mri_date`). 

---

### Layer 3: Post-Processing (Outbound Inspection)
*Goal: Prevent the agent from accidentally hallucinating, leaking, or echoing back sensitive data in its final response to the user.*

1. **AgentCore Gateway Interceptors (Response Phase)**
   Just as you can intercept requests, you can configure the Gateway Interceptor to run on the **RESPONSE** interception point. 
   * Before the agent's final answer is sent back to the user's browser, the Interceptor Lambda scans the output text using Amazon Comprehend or Regex.
   * If it detects an SSN, Member ID, or clinical diagnosis that violates the data-sharing policy for that specific user role, it dynamically redacts the text (e.g., replacing it with `***-**-****`) before it leaves the AWS environment.

2. **AgentCore Evaluations (Automated Auditing)**
   Enable the **Safety** and **Context Relevance** evaluators in AgentCore Evaluations. These run asynchronously in the background, scoring the agent's output. If an agent consistently scores low on "Safety" (indicating potential data leakage), it triggers an alert in your SIEM (e.g., Splunk/Datadog) for immediate human review.

---

### Layer 4: Infrastructure & Compliance Controls
*Goal: Ensure the environment itself meets HIPAA/SOC2 standards.*

* **Network Isolation**: Deploy AgentCore Runtime and Gateway within an **Amazon VPC** using **AWS PrivateLink**. This ensures that all traffic between the agent, the LLM (Amazon Bedrock), and your internal EHR/claims databases never traverses the public internet.
* **Encryption**: All data in transit is encrypted via TLS 1.3. All data at rest (including AgentCore Memory, session state, and logs) is encrypted using **AWS KMS** with customer-managed keys (CMKs).
* **Audit Logging**: Enable **AWS CloudTrail** data events and **AgentCore Observability**. Every tool invocation, policy evaluation (permit/deny), and interceptor execution is logged with the `userId`, `sessionId`, and timestamp, providing an immutable audit trail for HIPAA compliance officers.

---

### Practical Example: The "Member Claims" Flow

1. **User Input**: A member types, *"My SSN is 123-45-6789 and my claim #999 was denied, why?"*
2. **Pre-Processing (Interceptor)**: The Gateway Interceptor detects the SSN pattern. It redacts it to `[REDACTED-SSN]` and logs a security event. The sanitized prompt is passed to the agent.
3. **In-Processing (Policy)**: The agent decides to call the `get_claim_details` tool with `claim_id: 999`. AgentCore Policy checks the user's JWT. It confirms the user is the legitimate owner of claim #999 and returns `Permit`.
4. **In-Processing (Tool)**: The Gateway calls the internal claims API. The API is designed to return *only* the denial reason code, not the member's full address or DOB (Data Minimization).
5. **Post-Processing (Interceptor)**: The agent formulates a response. The Response Interceptor scans it to ensure no internal system codes or accidental PII were included in the natural language output.
6. **Delivery**: The clean, safe, and compliant response is streamed to the user.

### Summary Recommendation
To achieve enterprise-grade PHI/PII protection in AgentCore:
1. **Mandate Gateway Interceptors** for inbound regex/Comprehend redaction and outbound response scanning.
2. **Enforce AgentCore Policy (Cedar)** for deterministic, identity-based access control to all data-fetching tools.
3. **Design tools for data minimization**, ensuring APIs return only the exact fields needed for the specific task.
4. **Route all Observability and Audit logs** to your corporate SIEM to prove compliance to auditors without requiring AWS Console access.