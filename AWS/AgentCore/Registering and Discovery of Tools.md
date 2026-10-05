Yes, **you must explicitly register tools in the Amazon Bedrock AgentCore Gateway before agents can discover and use them.** 

AgentCore Gateway does not automatically scan your VPC or network to find and expose internal APIs. Instead, it operates on a strict **"allow-list" model** using a concept called **Gateway Targets**. 

This design is intentional and highly beneficial for regulated enterprises like healthcare and insurance, where preventing unauthorized data access (Shadow IT) is just as important as enabling AI capabilities.

Here is a detailed breakdown of how tool registration and discovery work in AgentCore Gateway:

### 1. How Tool Registration Works (Gateway Targets)
Before an agent can interact with an internal system, an administrator or developer must define a **Gateway Target**. A Gateway Target is the bridge between the agent and your backend system. You can register three main types of targets:
* **OpenAPI / Swagger Specifications:** You upload the JSON/YAML schema of your internal REST API (e.g., your FHIR-based EHR endpoint or claims database API). The Gateway parses this and automatically generates the corresponding MCP tools.
* **AWS Lambda Functions:** You provide the ARN of an existing Lambda function. The Gateway wraps it as a tool, handling the invocation and payload translation.
* **External MCP Servers:** If you already have internal services exposing the Model Context Protocol, you simply register their endpoint URL as a target.

*Example using the AgentCore CLI:*
```bash
agentcore add gateway-target \
  --name ClaimsDatabaseAPI \
  --type openapi \
  --schema-file ./claims-api-schema.json \
  --gateway EnterpriseClaimsGateway
```

### 2. How Agent Discovery Works at Runtime
Once the tools are registered, the discovery process happens dynamically at runtime via the **Model Context Protocol (MCP)**:
1. **Agent Initialization:** When the AgentCore Runtime spins up the agent, the agent is configured with the endpoint URL of the specific Gateway it is allowed to use.
2. **Tool Catalog Query:** The agent automatically queries the Gateway (e.g., via an MCP `tools/list` request) to ask, *"What tools are available to me?"*
3. **Schema Injection:** The Gateway responds with a clean, standardized list of the registered tools, including their names, descriptions, and required input parameters (JSON schemas). 
4. **LLM Reasoning:** The agent's underlying Foundation Model ingests this catalog and uses it to decide which tool to call based on the user's prompt.

### 3. Why Explicit Registration is Critical for Healthcare & Insurance
While automatic discovery might sound convenient, explicit registration provides vital enterprise guardrails:

* **Enforcing Least Privilege:** You can create multiple Gateways (or restrict access to specific targets within a Gateway). A "Member FAQ Agent" might only be registered with the `GetMemberName` target, while a "Prior Authorization Agent" is registered with the `GetClinicalHistory` target. The agents physically cannot discover or call tools they haven't been explicitly granted access to.
* **Schema Validation & Security:** By registering an OpenAPI spec, the Gateway knows exactly what parameters are valid. If a hallucinating LLM tries to call the `CheckClaimStatus` tool but forgets to include the mandatory `MemberID` parameter, the Gateway intercepts and blocks the call *before* it ever hits your internal database, preventing errors or accidental full-table scans.
* **Decoupling Infrastructure from AI:** If your internal claims database changes its API endpoint or authentication method, you only update the registration in the Gateway. The agent's code and prompts remain completely unchanged because it only knows the Gateway's standardized MCP interface.

### 4. What About Discovering *Other Agents*? (Agent-to-Agent)
It is important to distinguish between discovering **Tools** (APIs/Databases) and discovering **Other Agents** in a multi-agent system.
* **Tool Discovery:** Handled via the **Gateway** (as described above, requiring explicit registration of Gateway Targets).
* **Agent Discovery:** Handled via the **Agent-to-Agent (A2A) Protocol**. If your "Triage Agent" needs to discover a "Clinical Coding Agent", it does not use the Gateway. Instead, it looks for an **Agent Card** (a standardized JSON metadata file hosted at `/.well-known/agent-card.json` on the target agent's runtime endpoint). This card advertises the agent's capabilities and authentication requirements, allowing the host agent to securely initiate a collaboration.

### Summary
To integrate internal systems, you must act as the "gatekeeper." You explicitly register your internal APIs and Lambdas as **Gateway Targets** within the AgentCore Gateway. At runtime, the agent dynamically queries the Gateway to discover these pre-approved, secure, and standardized tools, ensuring your AI agents operate strictly within the boundaries of your enterprise security and compliance policies.