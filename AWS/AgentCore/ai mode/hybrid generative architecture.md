If your application handles both simple single-turn tasks and complex multi-step workflows, you have a classic hybrid generative AI architecture.
You should not try to force your single-turn logic into AgentCore, nor should you build complex multi-agent logic purely inside standard Bedrock API loops. Instead, you design a centralized application backend that serves as an AI Router. [1, 2] 
------------------------------
## The Hybrid Reference Architecture
A standard implementation utilizes a unified client interface (like a React or mobile app) pointing to an orchestrator backend (such as an API Gateway backed by AWS Lambda or an ECS container). This backend acts as the traffic controller: [2, 3] 

                            ┌───────────────────┐
                            │    Frontend UI    │
                            └─────────┬─────────┘
                                      │
                         ┌────────────▼────────────┐
                         │   Application Router    │
                         │ (API Gateway / Lambda)  │
                         └──────┬────────────┬─────┘
                                │            │
         ┌──────────────────────┘            └──────────────────────┐
         ▼                                                          ▼
  [Scenario A: Single-Turn]                                  [Scenario B: Multi-Step Agentic]
  • Simple Chat                                              • Multi-system data retrieval
  • Summarisation / Q&A                                      • Autonomous calculations & tasks
  • Guardrails checking                                      • Long-running workflows
         │                                                          │
         ▼                                                          ▼
┌────────────────────────────────┐                        ┌────────────────────────────────┐
│      Standard Bedrock APIs     │                        │    Amazon Bedrock AgentCore    │
│  (Direct LLM / Knowledge Base) │                        │ (LangGraph/CrewAI in MicroVMs) │
└────────────────────────────────┘                        └────────────────────────────────┘

------------------------------
## Step-by-Step Execution Pattern## 1. Scenario A: Fast-Path Routing (Standard Bedrock)
When the user submits an intent that is purely informational (e.g., "Summarize my last chat session" or "What is our company's refund policy?"):

* 
* The Action: Your Application Router bypasses the agent platform entirely.
* The Execution: It calls standard Amazon Bedrock endpoints directly—either using InvokeModel on an LLM or querying a [Bedrock Knowledge Base](https://aws.amazon.com/blogs/aws/introducing-amazon-bedrock-managed-knowledge-base-for-faster-more-accurate-enterprise-ai-applications/) for retrieval.
* The Benefit: Minimum latency, minimal cost, and simple request/response structures. [4] 
* 

## 2. Scenario B: Agentic-Path Routing (Bedrock AgentCore)
When the user gives a command that requires multi-system actions (e.g., "Process an exception for Invoice #5022 and update the status in Salesforce"):

* 
* The Action: Your Application Router identifies the intent as complex and packages the session payload. [4] 
* The Execution: It sends the payload to the Amazon Bedrock AgentCore Runtime API. AgentCore boots a secure, serverless microVM, provisions your stateful agent framework (like [LangGraph](https://www.langchain.com/langgraph) or [CrewAI](https://www.crewai.com/)), connects to third-party APIs via the AgentCore Gateway, and executes the multi-turn loop. [4, 5, 6, 7] 
* The Benefit: Complete execution safety, sandbox code isolation, unified trace observability in CloudWatch, and managed long-term memory across sessions. [1, 8] 
* 

------------------------------
## Implementation Options for the Router
To determine how your app splits the traffic, consider two implementation paths:

| Router Strategy | How it Works | Best For |
|---|---|---|
| Deterministic Routing | Hardcoded logic based on frontend UI buttons or specific API endpoints (e.g., a "Chat with Docs" tab vs. an "Automate Task" button). | Maximizing control, reducing LLM costs, and avoiding intent confusion. |
| LLM-Based Intent Routing | The application backend passes the user's initial prompt to a small, fast model (like Claude 3.5 Haiku) to classify the intent into SIMPLE_CHAT or COMPLEX_AGENT. | Flexible, conversational text inputs where the user can ask for anything in a single bar. |

Are your users interacting with this system through distinct UI features (like clicking specific buttons for automated tasks), or will it be a single open-ended chat bar where an LLM needs to figure out the intent?

[1] [https://docs.aws.amazon.com](https://docs.aws.amazon.com/prescriptive-guidance/latest/agentic-ai-frameworks/amazon-bedrock-agent-core.html)
[2] [https://www.youtube.com](https://www.youtube.com/watch?v=k_62duKHk6M)
[3] [https://aws.amazon.com](https://aws.amazon.com/blogs/machine-learning/accelerate-agentic-application-development-with-a-full-stack-starter-template-for-amazon-bedrock-agentcore/)
[4] [https://www.youtube.com](https://www.youtube.com/watch?v=wzIQDPFQx30)
[5] [https://docs.aws.amazon.com](https://docs.aws.amazon.com/bedrock-agentcore/latest/devguide/what-is-bedrock-agentcore.html)
[6] [https://www.youtube.com](https://www.youtube.com/watch?v=YmXszEVI-x8&t=117)
[7] [https://www.youtube.com](https://www.youtube.com/watch?v=ZGElhJmN_8o&t=10)
[8] [https://aws.amazon.com](https://aws.amazon.com/about-aws/whats-new/2026/07/amazon-bedrock-agentcore-unified-observability-single-log-group/)
