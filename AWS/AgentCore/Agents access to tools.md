**No, agents do not have access to all registered targets by default.** 

Amazon Bedrock AgentCore Gateway is designed with a "zero-trust" and "least privilege" mindset. Simply registering a tool (Gateway Target) does not automatically grant every agent permission to discover or invoke it. 

To restrict tool access per agent, AgentCore Gateway provides two primary, complementary enforcement mechanisms, plus an optimization feature. For regulated domains like healthcare and insurance, using these in a layered "defense-in-depth" approach is the recommended best practice.

---

### 1. AgentCore Policy (Declarative, Security-Owned)
This is the primary and most robust way to restrict tool access. It uses **Cedar**, an open-source, deterministic authorization language developed by AWS.
* **How it works:** You write declarative policies that explicitly define which agents (or users) can call which tools. These policies are evaluated by a managed policy engine at the Gateway level *before* any tool code is executed.
* **Example:** You can write a policy stating: *"Deny the `fetch_clinical_history` tool unless the requesting `agent_id` is 'prior-auth-agent' AND the requested `patient_id` matches the authenticated user's JWT claim."*
* **Why it’s powerful:** It is completely independent of the LLM’s reasoning. Even if a prompt injection attack tricks the agent into trying to call an unauthorized tool, the Gateway’s infrastructure layer will deterministically block the request [[41]]. Security and compliance teams can manage these policies without requiring code deployments.

### 2. Gateway Interceptors (Programmatic, Developer-Owned)
For scenarios requiring complex, dynamic, or proprietary business logic that Cedar cannot easily express, you can attach a **Lambda Interceptor** to the Gateway.
* **How it works:** The Gateway invokes your custom Lambda function *before* routing any tool call. Your agent can pass a custom header (e.g., `x-agent-id: claims-triage-agent`). The Lambda function reads this header, checks a centralized allowlist (e.g., stored in AWS Systems Manager Parameter Store or DynamoDB), and explicitly denies the request if the tool is not on that specific agent's list [[30]].
* **Why it’s powerful:** It gives developers full programmatic control. You can implement time-of-day restrictions, complex multi-tenant routing, or external API calls to validate the request before it ever reaches the target system [[41]].

### 3. Semantic Tool Search (Context Optimization)
While Policy and Interceptors provide *hard security boundaries*, Semantic Tool Search provides *contextual optimization*.
* **How it works:** Instead of dumping the JSON schema of every single registered tool into the agent’s prompt (which wastes tokens and increases the chance of the LLM hallucinating or choosing the wrong tool), the Gateway uses semantic search to dynamically return only the tools that are highly relevant to the agent's current task [[34]].
* **Why it’s powerful:** It makes agents more efficient and accurate. However, **this should never be relied upon as a security control**, as it is probabilistic. It must always be backed by the deterministic enforcement of AgentCore Policy or Interceptors.

---

### Recommended Architecture for Healthcare & Insurance

To ensure strict compliance (e.g., HIPAA) and prevent data leakage, implement a **Three-Layer Security Model** at the Gateway:

1. **Layer 1: Identity Injection (Interceptor):** The agent passes its `x-agent-id` and the end-user's JWT. The Gateway Interceptor validates the agent's identity and enriches the request context.
2. **Layer 2: Deterministic Authorization (AgentCore Policy):** The Gateway evaluates the enriched request against Cedar policies. If a "Member FAQ Agent" attempts to call the `Update_Claim_Status` tool, the policy instantly returns a `Deny`, and the request is dropped.
3. **Layer 3: Target Execution:** Only if both layers pass does the Gateway forward the request to the internal system (e.g., the claims mainframe or EHR), often injecting vaulted credentials via AgentCore Identity.

### Summary
You have granular control. You can register hundreds of tools in a single Gateway, but by leveraging **AgentCore Policy** and **Gateway Interceptors**, you can ensure that Agent A only sees and can only execute the specific, pre-approved subset of tools required for its designated business function.