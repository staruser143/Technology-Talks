## Apply Prompt Engineering Techniques (zero-shot, few-shot, chain-of-thought)

### 1. Zero-shot prompting

Just the instruction, no examples. Works well when the task is simple, the desired output format is obvious or easily described in words, or the model's general training already covers the pattern well (e.g., "summarize this," "translate this," "is this email spam or not"). The exam angle: zero-shot is the default starting point — you only add few-shot examples or chain-of-thought scaffolding when zero-shot demonstrably falls short, not preemptively. Reaching for more technique than a task needs adds tokens, cost, and prompt complexity without a corresponding quality benefit — the same "don't decompose a task that's already working well" discipline from Domain 1, applied to prompting technique instead of pipeline architecture.

### 2. Few-shot prompting

Providing example input/output pairs before the actual request, so the model can pattern-match the desired format, style, or edge-case handling from the examples rather than from instructions alone. Few-shot earns its cost specifically when:
- **Format precision matters and is hard to fully specify in words** — e.g., a very particular output structure, tone, or level of detail that's easier to show than describe.
- **The task has known edge cases** that benefit from being demonstrated (e.g., "here's how to handle a borderline case" shown via example, rather than trying to write an exhaustive rule).
- **Zero-shot output drifts** from what's wanted in a way that examples can correct more reliably than more instructions can.

## Watch for the trade-off the exam likes to test:
- Few-shot examples consume real tokens on every single request (cost, and — if placed poorly relative to caching — repeated cost on every call), so the exam expects you to weigh "does this task actually need demonstrated examples" against "am I just adding tokens because more examples feels safer."
-  Also worth knowing: few-shot examples are exactly the kind of content that should live in the *stable, cacheable* part of a prompt if they don't change per request — tying directly back to your caching material.

### 3. Chain-of-thought (CoT) prompting

Explicitly instructing the model to reason step-by-step before producing a final answer — "think through this before answering," or providing a structure like "first do X, then Y, then conclude." This is exactly the technique from your loan-approval arithmetic scenario: multi-step reasoning tasks (calculations, multi-criteria decisions, anything requiring tracking intermediate state) benefit substantially from being asked to show that work explicitly, rather than being asked to jump straight to a final answer.

**Exam-relevant trade-off**:
- CoT reliably improves accuracy on genuinely multi-step reasoning tasks, but it costs real output tokens (and therefore latency and money) for the reasoning trace itself — directly invoking the accuracy-latency-cost trade-off discipline from Domain 3.
- Applying CoT to a task that doesn't actually need multi-step reasoning (a simple lookup, a straightforward classification) adds cost and latency for no accuracy benefit — the same "don't apply a technique the task doesn't need" principle as few-shot's misuse case, just for a different technique.

### 4. Matching technique to task — the core exam judgment

None of these three are inherently "better" — each is the right tool for a specific symptom:
- Output is fine, task is simple → **zero-shot** is sufficient; adding more doesn't help.
- Output format/style is inconsistent or doesn't match a specific desired pattern → **few-shot**, showing the pattern via examples.
- Output is wrong specifically on multi-step logic, calculations, or tasks requiring tracking several pieces of intermediate information → **chain-of-thought**, giving the model room to reason explicitly.
- These aren't mutually exclusive — a prompt can combine few-shot examples *and* chain-of-thought instruction when a task needs both consistent formatting *and* careful multi-step reasoning.

## A common exam trap: 
- Reaching for chain-of-thought on a task that's actually failing because of a *formatting/consistency* problem (which few-shot would fix more directly), or reaching for few-shot on a task that's actually failing because of a *reasoning* problem (which no number of static examples fully solves, because the failure is in the reasoning process itself, not the output shape).

---

