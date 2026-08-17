When we get a max_tokens in stop_reason, how should we handle that


Good — confirmed, and worth being precise about since this connects to real design decisions, not just error-handling boilerplate.

**What `max_tokens` as `stop_reason` actually means**: it's not an error — the request succeeded, but Claude was cut off mid-generation specifically because it hit the token ceiling *you* set in the request, not because it naturally finished. `stop_reason` is part of every successful response and tells you *why* generation ended; `max_tokens` specifically means the response is truncated and incomplete, distinct from `end_turn` (finished naturally) or `model_context_window_exceeded` (a different, input-side limit).

**Two legitimate ways to handle it, both directly documented:**

**Option 1 — Continue generation**: append the truncated assistant response back into the `messages` array, then make a new request so Claude picks up where it left off:
```python
messages = [
  {"role": "user", "content": original_prompt},
  {"role": "assistant", "content": response.content[0].text}  # the truncated output
]
continuation = client.messages.create(
    model="claude-sonnet-4-5",
    max_tokens=1024,
    messages=messages + [{"role": "user", "content": "Please continue"}]
)
final_text = response.content[0].text + continuation.content[0].text
```

**Option 2 — Surface the truncation honestly rather than continuing**: if a partial answer is acceptable for your use case, append an explicit notice so the reader knows the output is incomplete, rather than silently presenting a cut-off response as if it were complete.

**The actual judgment call, and where this connects to everything else in this thread**: which of the two you pick isn't arbitrary — it should be diagnosed the same way you've diagnosed nearly every other symptom in this whole session. `max_tokens` firing repeatedly is itself a *symptom*, and the right fix depends on the cause:

- **If it's a one-off, legitimately long response** (a genuinely long document, a complex multi-step answer) — continuing is reasonable, or simply raising your `max_tokens` limit for that request type is often the more direct fix, the same "measure and set the actual budget the task needs" discipline from your token-counting material, rather than reflexively chaining continuation requests every time.
- **If it's happening systematically across many requests for a given feature** — that's a signal your `max_tokens` setting doesn't match the task's actual output needs, worth diagnosing and fixing at the design level (raise the limit appropriately, or — if outputs are unexpectedly long — check whether something upstream, like an overly verbose chain-of-thought instruction or an unconstrained free-form return from a subagent, the exact same problem from your legal-contract-review scenario, is inflating output length beyond what's actually needed).
- **Don't blindly retry an empty or already-truncated response without modification** — the documentation is explicit that resending the same truncated content unchanged won't help; continuation has to actually append and extend, not just repeat the request.

**One important distinction worth not conflating**: `
- max_tokens` is a different failure mode from `pause_turn` (which happens specifically with server tools awaiting a result and gets resumed by sending the response back with tool results attached) and from `model_context_window_exceeded` (an input-side problem, meaning your prompt itself was too large — fixed by reducing input, not by continuing generation).
- Treating all three as "just retry" is a real bug risk the documentation explicitly warns about — each needs its own handling branch, not a single generic retry-on-any-non-`end_turn` catch-all.

Want a scenario that tests choosing the right response to a `max_tokens` truncation versus reflexively continuing every time?
