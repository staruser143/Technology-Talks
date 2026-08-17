**Scenario**

A publishing company wants Claude to turn a single 90-minute podcast transcript into a finished blog post: a coherent narrative with quotes properly attributed, section headers, a summary at the top, and SEO metadata at the bottom. A junior engineer's first attempt was one giant prompt: "Here's the full transcript, write me a polished blog post with headers, quotes, a summary, and SEO metadata." The output was often mediocre — it would miss good quotes buried in the middle of the transcript, section headers didn't always match what was actually discussed, and the SEO metadata sometimes contradicted the summary.

**Question**: What's the most likely reason this single-prompt approach is underperforming, and what should be done instead?

A) The task should be made agentic — let Claude loop through the transcript autonomously deciding what to extract, since a single prompt can't handle open-ended tasks.

B) The prompt is being asked to hold too much at once — extracting quotes, structuring narrative, writing summary, and generating SEO metadata are separable subtasks that compete for attention in one pass. Decompose into separate steps (e.g., extract key quotes and themes first, then draft narrative with headers, then generate summary and metadata from the draft) and chain them.

C) The transcript is too long for Claude to handle at all; the fix is to use a bigger context window model and nothing else needs to change.

D) The problem is prompt phrasing, not structure — a more detailed single prompt with explicit formatting instructions would fix all three issues without decomposing anything.

Take your best guess and I'll walk through it.


Correct. This scenario is testing whether you can diagnose "task is doing too much in one pass" as a *structural* problem, not a phrasing problem — a distinction the exam draws on repeatedly.

**Why B is right**

Look at the specific failure modes described, because each one is a symptom of decomposition need, not prompt-wording weakness:

- **Missing quotes buried in the middle** — this is an attention/salience problem. When one pass has to simultaneously scan for quotes *and* build narrative structure *and* write a summary, the model's attention is split across competing objectives, and details in the middle of a long input are the first casualty. A dedicated "extract key quotes and themes" pass, with that as its *only* job, gives the model full attention on just that task.
- **Headers not matching what was discussed** — this is a sequencing problem. Structure (headers) should be derived *from* the extracted content, not invented in parallel with extraction. If quote/theme extraction happens first, header generation has something concrete to build from instead of guessing at structure while still processing raw transcript.
- **SEO metadata contradicting the summary** — this is a consistency problem, and it's the clearest tell. If summary and metadata are generated in the same pass without one depending on the other, there's no guarantee they agree, because the model isn't literally reading its own summary back before writing metadata. Chaining — generate summary, *then* generate metadata from that summary — removes the possibility of contradiction structurally, not through better wording.

The general principle: when a task has clearly separable phases, and errors in one phase would be easier to catch or fix in isolation, decompose it. That's stated directly in the domain's task list ("apply decomposition techniques for complex problem solving"), and this scenario is the textbook illustration of it.

**Why the others are wrong**

- **A) Make it agentic** solves the wrong problem. The task here isn't unpredictable in path — you always want quotes, then structure, then summary, then metadata, in that order, for every transcript. That's a workflow, not agentic; the earlier domain material applies here too, and adding autonomy doesn't fix an attention/sequencing problem.
- **C) Bigger context window fixes it** confuses "can the model technically fit the input" with "can the model produce quality output while doing four different jobs at once." A bigger context window doesn't fix competing objectives within a single pass — you'd still get header/summary mismatches even with unlimited context, because the problem isn't length, it's concurrent tasks diluting attention.
- **D) Better phrasing alone** is the most common wrong answer people pick because it feels like the "cheap fix." It can help marginally, but it doesn't address the structural cause: a summary written before or during metadata generation has no way to *guarantee* consistency with metadata written in that same pass. No amount of instruction wording turns one pass into a self-consistency check across two things the model hasn't finished writing yet.

**Pattern to remember**: 
- if a single-prompt task shows these three specific failure signatures — dropped details in long inputs, structure that doesn't match content, or internal contradictions between outputs — that's diagnostic of a decomposition problem, not a prompting problem.
- Fix the architecture (split + chain), not just the words.

