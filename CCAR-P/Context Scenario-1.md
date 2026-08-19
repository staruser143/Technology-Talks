Excellent question. This is exactly the kind of nuance CCAR-P likes to test.

The practice question intentionally says:

"fits comfortably within the context window"

That phrase is doing a lot of work.

If it truly fits comfortably

Then the total context budget includes:

System prompt
User message
Tool definitions
Tool results
Knowledge base
Model output tokens

All together must fit. Anthropic explicitly notes that everything counts toward the context window, including system prompts, messages, documents, tools, and generated output.

So if the question says it already fits comfortably, the exam expects you to assume all of those factors have been considered.

Therefore:

✅ Load the KB directly

✅ Use prompt caching

✅ Don't build RAG yet

What if we're getting close to the limit?

This is where architects need to think differently from exam candidates.

Suppose:

KB = 180K tokens
System prompt = 15K
Tool schemas = 30K
Conversation history = 20K
Desired output = 5K

Total = 250K+

Now you're no longer in the "comfortably fits" situation.

In that case, I would start questioning whether full-context loading remains the best design.

The architecture decision becomes:

Option 1: Reduce Context

Can we shrink:

Tool definitions
System prompt
Message history

using context engineering techniques? Anthropic emphasizes curating context, not merely maximizing it.

Option 2: Partial Retrieval

Instead of sending all 180K tokens each request:

Store KB externally
Retrieve only relevant sections
Add retrieved content to context

This is classic RAG. Anthropic recommends retrieval approaches when corpora become too large to fit conveniently in context.

Option 3: Hybrid Approach

A pattern I often recommend:

Cache stable material

System instructions
Policies
Frequently referenced docs

Retrieve dynamic material

Large KB sections
User-specific records
Frequently changing content

This gives a balance between simplicity and scalability. Prompt caching is specifically designed to make large reusable context economical.

How CCAR-P Usually Tests This

There are two distinct scenarios:

Scenario A

Knowledge base fits in context.

Correct thinking:

Context + Prompt Caching

No RAG.

Scenario B

Knowledge base is large, growing, or approaching context limits.

Correct thinking:

RAG / Contextual Retrieval

Possibly:

Contextual Embeddings
Contextual BM25
Reranking

depending on retrieval quality requirements.

Exam Mental Trigger

When reading a question, ask:

Question 1

"Does everything fit comfortably in context?"

If yes:

➡️ Prefer direct context + caching.

Question 2

"Am I close to the limit or likely to exceed it?"

If yes:

➡️ Start considering retrieval.

Question 3

"Is retrieval quality becoming problematic?"

If yes:

➡️ Consider Contextual Retrieval and reranking.

The Architecture Hierarchy

A useful CCAR-P decision ladder:

Can the whole corpus fit comfortably?
            |
           Yes
            |
    Context + Caching
            |
           No
            |
       Basic RAG
            |
 Retrieval quality poor?
            |
           Yes
            |
 Contextual Retrieval
 + Reranking


This decision framework aligns closely with Anthropic's guidance: start with the simplest approach that works, and only add retrieval complexity when context size or retrieval requirements justify it.

So for the original exam question, the phrase**"fits comfortably within the context window"** is actually the clue telling you not to worry about system prompts, tools, and conversation history. If the question instead said**"nearly fills the context window","approaching token limits", or"expected to grow significantly"**, then the answer would likely shift away from direct context loading and toward retrieval.
