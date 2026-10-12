Not exactly. Amazon Bedrock AgentCore is a sub-service sitting natively inside the broader Amazon Bedrock platform. You do not choose between them as separate entities; rather, you use them together depending on the architectural pattern of your application. [1, 2] 
------------------------------
## The Real Architecture: Platform vs. Component

* 
* Amazon Bedrock is the parent ecosystem. It provides the foundational plumbing for all generative AI needs, including direct model inference (like calling Anthropic Claude or Llama 3), custom model fine-tuning, [Managed Knowledge Bases for RAG](https://aws.amazon.com/blogs/aws/introducing-amazon-bedrock-managed-knowledge-base-for-faster-more-accurate-enterprise-ai-applications/), and Bedrock Guardrails. [1, 2, 3] 
* [Amazon Bedrock AgentCore](https://aws.amazon.com/bedrock/agentcore/) is the specialized infrastructure module within Bedrock. It was built to solve a specific engineering headache: hosting, securing, and scaling stateful, multi-turn, and long-running AI workflows. [2, 4, 5, 6] 
* 

------------------------------
## How to Map Them to Your Application Style
Instead of viewing them as mutually exclusive, think of it in terms of single-turn apps versus stateful apps.

                           ┌─────────────────────────────────────────────────────────┐
                           │                     AMAZON BEDROCK                      │
                           └────────────────────────────┬────────────────────────────┘
                                                        │
                         ┌──────────────────────────────┴──────────────────────────────┐
                         ▼                                                             ▼
             [Standard Generative AI]                                        [Agentic Frameworks]
       (Text, Summaries, RAG, Chatbots)                              (Autonomous Workflows, Multi-Agents)
                         │                                                             │
                         ▼                                                             ▼
           Invokes Bedrock Model APIs                                      Runs on Bedrock AgentCore
         Directly (Lambda / ECS / etc.)                                   Serverless Runtime + Gateway

## 1. Standard Generative AI Apps (Direct Bedrock API)
If your app is built on a simple request-and-response pattern, you only interact with core Bedrock APIs:

* 
* Your Use Case: A user asks to summarize a PDF, write a piece of marketing copy, or ask questions against a corporate wiki via Bedrock Knowledge Bases. [1, 3] 
* The Architecture: Your backend application code (running on standard AWS services like AWS Lambda, Amazon ECS, or an EC2 instance) makes a direct, single-turn API call (InvokeModel) to Bedrock to fetch the answer. No agentic loop is required.
* 

## 2. Agentic AI Apps (Bedrock AgentCore)
If your app needs to autonomously figure out a complex plan over multiple steps, you deploy your code onto AgentCore. [5, 6] 

* 
* Your Use Case: A user requests: "Look up my pending travel invoice in Salesforce, cross-reference it against our corporate policy document, write an explanation if it violates policy, and draft an email response to the manager".
* The Architecture: You write an agent loop using an open-source framework like [LangGraph](https://www.langchain.com/langgraph) or [CrewAI](https://www.crewai.com/). You package that logic and hand it over to AgentCore Runtime. AgentCore provisions secure serverless microVMs to execute your multi-step loop, connects it to enterprise systems using [AgentCore Gateway and the Model Context Protocol (MCP)](https://aws.amazon.com/blogs/machine-learning/introducing-amazon-bedrock-agentcore-gateway-transforming-enterprise-ai-agent-tool-development/), manages short-term and long-term memory, and tracks step-by-step observability. [6, 7, 8, 9, 10, 11, 12] 
* 

## Summary Rule of Thumb
If your system operates on a single invocation (Prompt → LLM → Final Output), use standard Amazon Bedrock. If your system requires loops, memory retention across complex tool usage, or multi-agent orchestration, host your logic on Amazon Bedrock AgentCore. [5, 9] 
Is your team leaning toward a single-turn chat interface or building a backend worker that autonomously handles multi-step workflows?

[1] [https://aws.amazon.com](https://aws.amazon.com/bedrock/)
[2] [https://aws.amazon.com](https://aws.amazon.com/bedrock/faqs/)
[3] [https://aws.amazon.com](https://aws.amazon.com/blogs/aws/introducing-amazon-bedrock-managed-knowledge-base-for-faster-more-accurate-enterprise-ai-applications/)
[4] [https://aws.amazon.com](https://aws.amazon.com/bedrock/agentcore/)
[5] [https://www.cloudthat.com](https://www.cloudthat.com/resources/blog/agentic-ai-architecture-with-amazon-bedrock-agentcore-and-kubernetes-on-aws)
[6] [https://aws.amazon.com](https://aws.amazon.com/blogs/aws/introducing-amazon-bedrock-agentcore-securely-deploy-and-operate-ai-agents-at-any-scale/)
[7] [https://aws.amazon.com](https://aws.amazon.com/blogs/security/propagate-user-authorization-context-in-ai-agents-with-amazon-bedrock-agentcore/)
[8] [https://aws.amazon.com](https://aws.amazon.com/blogs/machine-learning/build-a-multi-account-ai-agent-with-agentcore-gateway-and-mcp/)
[9] [https://docs.aws.amazon.com](https://docs.aws.amazon.com/bedrock-agentcore/latest/devguide/what-is-bedrock-agentcore.html)
[10] [https://aws.amazon.com](https://aws.amazon.com/blogs/machine-learning/migrating-multi-model-ai-agents-to-amazon-bedrock-agentcore-runtime/)
[11] [https://aws.amazon.com](https://aws.amazon.com/blogs/physical-ai/simulating-expert-teams-with-agentic-ai-and-amazon-bedrock-agentcore/)
[12] [https://aws.amazon.com](https://aws.amazon.com/blogs/machine-learning/introducing-amazon-bedrock-agentcore-gateway-transforming-enterprise-ai-agent-tool-development/)
