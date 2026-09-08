Yes, exactly. You have hit on one of the most unique and fundamental design choices of the Strands framework. 

In Strands, **sub-agents and multi-agent orchestration patterns are modeled as tools** that the primary (orchestrating) agent can call. 

Instead of agents talking directly to each other via message-passing (like in AutoGen or CrewAI), the primary agent interacts with other agents by **invoking them as tools**.

Here is a breakdown of how and why Strands handles multi-agent systems this way:

### 1. How "Agents as Tools" Works in Practice
If you want to build a multi-agent system in Strands, the architecture generally looks like this:
* **The Sub-Agents:** You create specialized agents (e.g., a `ResearchAgent`, a `CodingAgent`, a `DataAgent`). 
* **The Tool Wrapper:** You wrap these agents (or groups of them) inside tool definitions. Strands actually provides built-in multi-agent tools like the **`workflow`**, **`graph`**, and **`swarm`** tools to make this easy.
* **The Orchestrator:** You create a primary "Manager" agent. You give this Manager agent a prompt and a list of tools—which includes your standard tools (like a web search or database query) **plus your agent-tools**.

When the Manager agent receives a complex user request, its reasoning engine evaluates the task and decides: *"I need to use the `research_agent_tool` first, and then pass that output to the `coding_agent_tool`."* It executes them sequentially or dynamically, just like it would execute a standard Python function.

### 2. Why Does Strands Do This?
This design is a direct result of Strands' **model-driven philosophy**. 

Large Language Models (LLMs) are incredibly good at understanding and using **tools** (functions). They are not natively wired to understand complex, peer-to-peer agent messaging protocols. By turning an agent into a tool:
* **Unified Interface:** The primary agent doesn't need to learn a new way to communicate with a sub-agent. It just looks at the tool's schema (inputs and outputs) and decides when to use it.
* **Dynamic Delegation:** The primary agent can reason about *whether* it needs to call a sub-agent at all. If a task is simple, it just answers directly. If it's complex, it delegates to the "agent-tool."
* **Massive Scalability:** As mentioned in the AWS documentation, an agent can have access to thousands of tools. If you have 100 specialized sub-agents, you don't want the primary agent trying to manage 100 separate chat threads. Instead, you can use a **Retrieve Tool** to semantically search for the exact "agent-tool" needed for the current task, present only that tool to the LLM, and execute it.

### 3. How This Compares to Other Frameworks
To visualize the difference, compare the communication flow:

* **CrewAI / AutoGen (Message-Passing):** 
  * *Manager Agent:* "Hey Researcher Agent, find me data on X." 
  * *Researcher Agent:* "Here is the data. Hey Coder Agent, write a script for it."
  * *Flow:* Agent-to-Agent direct conversation.

* **Strands Agents (Tool-Mediated):**
  * *Manager Agent:* "I will use the `research_tool`." *(Internally, the research_tool spins up the Research Agent, gets the data, and returns it to the Manager).*
  * *Manager Agent:* "Now I will use the `coding_tool` with this data." *(Internally, the coding_tool spins up the Coder Agent, writes the script, and returns it).*
  * *Flow:* Agent -> Tool (which happens to be an Agent) -> Agent.

### Summary
By treating agents as tools, Strands keeps the core framework incredibly simple. You don't need to learn a complex multi-agent messaging syntax. You just define your sub-agents, wrap them in tools, and let the primary agent's natural reasoning capabilities figure out how to orchestrate them to get the job done.