**Scenario**

A financial advisory company wants to improve their Claude-powered portfolio-question chatbot. An engineer proposes a variant that bundles three changes at once: (1) switching from the current model to a newer model tier, (2) rewriting the system prompt with more detailed formatting instructions, and (3) adding two few-shot examples for complex tax-related questions. They run a properly powered, statistically sound A/B test comparing this bundled variant against the current control, tracking a full, appropriate metric set (accuracy, user satisfaction, latency, cost). The variant wins clearly and significantly on accuracy and satisfaction, with an acceptable latency and cost increase. The team ships the variant to 100% of traffic.

Two months later, the team wants to further optimize costs and proposes reverting just the model upgrade (component 1) to save money, reasoning "the model upgrade was probably only a small part of the accuracy gain, and the prompt and few-shot changes were likely doing most of the work — reverting just the model should preserve most of the benefit at lower cost." They have no way to actually verify this, since the original test only ever compared "all three changes together" against "none of the changes."

**Question**: What's the core methodological gap that's now limiting the team's ability to make this cost-optimization decision confidently, and what should they have done differently in the original test?

A) There's no gap — since the bundled variant won clearly on the metrics that matter, the original test was methodologically sound, and the team's current uncertainty about reverting just the model is an unrelated, separate question that doesn't reflect on the original test's design.

B) The core gap is that bundling three distinct changes into a single variant made it impossible to attribute the observed improvement to any one specific change — the team now has no data on how much each individual change (model upgrade, prompt rewrite, few-shot examples) contributed on its own, only on their combined effect. This is the same "can't isolate which change caused the result" problem as testing two simultaneous changes together without individual attribution. The team should have tested the changes individually (or in a structured multi-variant/factorial test isolating each component's contribution) from the start, which would let them now make an evidence-based decision about reverting just the model upgrade instead of guessing.

C) The issue is that the test should have used a smaller sample size, since large sample sizes make it harder to distinguish which specific change caused an effect.

D) The issue is irrelevant in retrospect; the team should simply run a brand new A/B test now, testing "current bundled variant" against "bundled variant minus the model upgrade," which fully resolves the problem with no loss of information compared to having tested components individually from the start.

Take your best guess and I'll walk through it.