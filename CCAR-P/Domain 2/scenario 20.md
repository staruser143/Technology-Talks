**Scenario**

A retail company runs two different multi-turn Claude assistants:

**Assistant A**: A live "browse and chat" shopping assistant. Customers ask about products, get recommendations, ask follow-ups, sometimes go on tangents ("what's the weather like for a beach trip" while shopping for swimwear), and conversations typically run 10-15 turns. Once a customer moves on to a completely different product category, anything discussed about the previous category is essentially irrelevant going forward — there's no standing fact from early in the conversation that stays relevant no matter how far the conversation drifts.

**Assistant B**: A multi-turn "trip planning" assistant that helps a customer plan a custom vacation package (flights, hotel, activities) across a long conversation, often 25+ turns. Early in the conversation, the customer states hard constraints that remain relevant for the entire rest of the planning process no matter how many turns pass — "I have a severe shellfish allergy," "my budget cap is $4,000 total," "I need to be back home by the 15th for a work event." Meanwhile, many individual turns in the middle are just back-and-forth refinement of specific sub-decisions (e.g., comparing three hotel options, most of which get discarded once a choice is made) that become irrelevant once resolved.

**Question**: Which conversation-history management technique fits each assistant better, and why?

A) Both assistants should use sliding window — it's the simplest technique to implement, and simplicity should be the deciding factor regardless of what each conversation actually needs to retain.

B) Assistant A is a good fit for sliding window: relevance correlates with recency (once the customer moves to a new product category, older discussion genuinely stops mattering), so keeping only the most recent N turns is simple and sufficient. Assistant B is a poor fit for sliding window and calls for selective retention instead: relevance does NOT correlate with recency (the allergy, budget cap, and return-date constraint stated early must remain available no matter how many turns pass, while many individual mid-conversation refinement turns become irrelevant once resolved) — a sliding window risks silently dropping a critical constraint like the shellfish allergy once enough turns pass, which sliding window's blind recency-based cutoff cannot prevent.

C) Both assistants should use selective retention, since it's the most precise technique and precision should always be maximized regardless of implementation cost or actual need.

D) Assistant A should use selective retention and Assistant B should use sliding window, since Assistant B's conversations are longer and longer conversations always benefit from simpler, cheaper techniques regardless of what needs to be retained.

Take your best guess and I'll walk through it.



Correct — and this scenario is the direct application of the exam-relevant judgment from the last message, tested with two assistants deliberately built to sit on opposite sides of the "does relevance correlate with recency" question.

**Why B is right**

Run the actual test for each assistant:

- **Assistant A**: does relevance correlate with recency? Yes, explicitly — the scenario states that once a customer moves to a new product category, "anything discussed about the previous category is essentially irrelevant going forward," and there's "no standing fact from early in the conversation that stays relevant no matter how far the conversation drifts." That's precisely the condition under which sliding window is the right, simplest fit: you don't need to do the extra design work of judging what's relevant versus what isn't, because in this conversation shape, *recency itself is already a reliable proxy for relevance*. Keeping the last N turns and dropping the rest loses nothing that still mattered.

- **Assistant B**: does relevance correlate with recency? Explicitly no — the scenario is engineered to make this unambiguous. The shellfish allergy, the $4,000 budget cap, and the return-by-the-15th constraint are all stated early (turn 2-3, presumably) and remain critical at turn 25+, regardless of how much unrelated back-and-forth has happened since. A sliding window keeping, say, the last 10 turns would silently drop all three of these constraints once the conversation passes turn 13 or so — and unlike Assistant A's discarded old product-category chat, losing the allergy constraint isn't a minor UX inconvenience, it's a genuine safety-relevant failure (recommending a seafood-inclusive restaurant or excursion to someone with a severe allergy). Meanwhile, the mid-conversation hotel-comparison turns *do* correlate with recency in the normal way — once a hotel is chosen, comparing the two rejected options stops mattering and can be safely dropped. Selective retention is what lets you keep the standing constraints indefinitely while still trimming the resolved, no-longer-relevant refinement turns — exactly the asymmetry sliding window can't express, since it only has one lever (how many turns back) and can't distinguish "old but still critical" from "old and now irrelevant."

**Why the others are wrong**

- **A)** treats implementation simplicity as the deciding factor regardless of fit — but you already established in the deep-dive that sliding window's simplicity comes at the cost of unconditionally dropping anything outside the window, which is only an acceptable trade when nothing that old still matters. Applying it to Assistant B specifically would risk exactly the kind of dropped-safety-constraint failure the scenario is built to expose — simplicity isn't free if it silently discards something genuinely critical.
- **C)** makes the mirror-image mistake — treating precision as something to maximize unconditionally, ignoring that selective retention requires real design effort (a mechanism for judging relevance, not just counting turns back). For Assistant A, where recency already reliably tracks relevance, that extra design cost buys nothing — sliding window achieves the same practical outcome more simply. This is the same "more sophisticated technique isn't automatically better" lesson as reaching for decomposition or agentic architecture when a simpler pattern already suffices.
- **D)** gets the mapping backwards and for the wrong reason entirely — it assumes conversation *length* alone determines which technique fits ("longer conversations always benefit from simpler techniques"), which isn't the actual deciding variable at all. Length is incidental here; what matters is whether relevance and recency move together or diverge. A short conversation with early standing constraints would need selective retention just as much as Assistant B does, and a very long conversation where relevance does track recency (like an extended version of Assistant A) would still be fine with sliding window regardless of how many turns it ran.

**Pattern to remember, closing out conversation-history management**: the deciding question is never "which technique is simpler" or "which conversation is longer" — it's specifically whether **relevance correlates with recency** in this task. When it does, sliding window is the right, cheap default. When early information can remain critical arbitrarily far into a long conversation while other content becomes irrelevant quickly once resolved, only selective retention (or targeted summarization that explicitly protects standing facts) avoids the failure mode of silently losing something that still matters — a failure mode with stakes ranging from mild inconvenience (Assistant A, hypothetically) to genuine safety risk (Assistant B, concretely, given the allergy).

That's a thorough, complete pass through context/token optimization and Domain 2 as a whole. Want a mixed review across all five Domain 2 sub-topics now, or move to Domain 4 (Evaluation, Testing & Optimization, 16%)?