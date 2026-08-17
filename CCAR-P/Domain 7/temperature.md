Good topic to nail down precisely, and there's an important distinction buried in how the question is framed that's worth clarifying upfront: **temperature doesn't actually control reasoning** — it controls something different, and reasoning depth is governed by a separate, largely incompatible mechanism. Let's untangle both.

**What temperature actually does**

When Claude generates each token, it computes a probability distribution over what the next token could be. Temperature controls how that distribution gets sampled from:

- **Lower temperature** (closer to 0.0) → the model leans heavily toward the highest-probability tokens, producing more focused, consistent, predictable output.
- **Higher temperature** (closer to 1.0, the default) → the distribution gets flattened somewhat, giving lower-probability tokens more chance of being selected, producing more varied, less predictable output.

This is about **output variety/randomness in word choice and phrasing**, not about how deeply or carefully the model reasons through a problem. Two responses to the same prompt at high temperature might phrase things differently or take a different stylistic approach — but temperature isn't what determines whether the model does a good job breaking down a multi-step problem; that's a separate concern entirely (more below).

Range: **0.0 to 1.0**, default **1.0**. Worth knowing: even at temperature 0, output isn't fully deterministic — some minor variance remains.

**When to actually use it**

- **Lower temperature**: tasks needing consistency and precision — structured extraction, classification, code generation, factual Q&A, anything where you want the same input to reliably produce a similar, focused output.
- **Higher/default temperature**: creative or generative tasks — brainstorming, varied creative writing, tasks where variety across multiple generations is actually desirable.

One practical rule worth knowing: **adjust temperature or `top_p`, not both** — they both reshape the sampling distribution, and stacking changes to both makes behavior harder to predict and debug. Pick one lever.

**The important correction — this is where your question's framing needs adjusting**

What you're likely actually asking about — "how do I affect how carefully/deeply the model reasons" — is a **different, separate parameter**: **extended thinking / effort**, not temperature. These control whether and how much internal reasoning the model does before producing a final answer (directly your chain-of-thought material from Domain 2, now as an actual API mechanism rather than just a prompting technique). And critically: **temperature and extended thinking are largely incompatible** — when thinking is enabled, temperature must be left at its default (1) or unset; the API enforces this rather than treating it as a style suggestion. On the newest model generations (Claude 4.7 and later, per current docs), temperature is being deprecated entirely in favor of the `effort` parameter (low/medium/high/max) controlling reasoning depth directly.

**So, the corrected mental model for the exam**:
- **Temperature** → controls randomness/variety in the *final output's wording*, not reasoning quality.
- **Effort / extended thinking** → controls how much internal reasoning the model does, which is what actually affects performance on hard, multi-step tasks (your loan-arithmetic scenario's fix was conceptually this — get the model to reason more, not to sample its output tokens differently).

Given they're mutually exclusive on a request, the real design decision is: is this a reasoning-heavy task (use effort/thinking, leave temperature default) or a wording-variety task (adjust temperature, don't enable thinking)? Trying to use both to solve "make the model reason better *and* vary its phrasing" runs into the actual API constraint that these two levers don't stack.

Want a scenario testing this distinction — temperature vs. effort/thinking, matched to the right kind of task?