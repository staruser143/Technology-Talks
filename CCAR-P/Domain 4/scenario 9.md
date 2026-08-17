**Scenario**

An e-commerce company wants to test whether a new product-recommendation prompt (variant) outperforms their current one (control) at driving purchases. They run a live A/B test, splitting traffic 50/50. After **6 hours**, the results show: control converted 4.1% of 180 visitors (roughly 7 purchases), variant converted 5.6% of 175 visitors (roughly 10 purchases). The variant looks meaningfully better — a relative improvement of over 35% — and the team, excited by the early result, ships the variant to 100% of traffic immediately, declaring the test a success.

**Question**: Is it appropriate to conclude the variant is genuinely better and ship it based on this result? What's the core concern, and what should the team have done?

A) Yes — a 35% relative improvement is a large, meaningful difference, and shipping immediately based on this result is the correct, fast-moving approach.

B) The core concern is sample size and statistical significance: the difference between roughly 7 and 10 purchases out of ~180 visitors each is a very small absolute number of outcomes, well within the range that could easily be random noise rather than a genuine effect — a few visitors behaving differently by chance could produce a "35% relative improvement" that isn't real. The team should have predetermined an adequate sample size/duration before drawing any conclusion, let the test run to that point (likely needing thousands of visitors per arm, depending on baseline conversion rate and desired sensitivity, not hundreds), and ideally checked statistical significance formally rather than shipping based on an early result that looked favorable after only 6 hours.

C) The core concern is that the traffic split should have been 90/10 instead of 50/50, since giving more traffic to the control group would have made the result more trustworthy.

D) There's no concern with the sample size, but the test should have run on desktop users only, since mobile and desktop traffic patterns are fundamentally incomparable and mixing them invalidates any A/B test regardless of duration.

Take your best guess and I'll walk through it.



Correct — and this scenario is close to the textbook illustration of the exact trap named in the concepts overview: "peeking" and stopping the moment a result *looks* favorable, without the volume to actually distinguish signal from noise.

**Why B is right**

Do the arithmetic the way you'd want the team to have done before celebrating: 4.1% of ~180 is roughly 7 purchases; 5.6% of ~175 is roughly 10 purchases. The entire "35% relative improvement" the team is excited about rests on a difference of **about 3 purchases** between the two arms. With numbers that small, a handful of visitors behaving slightly differently — one extra impulse buyer landing in the variant arm, one hesitant browser landing in control, pure chance in who happened to visit during those 6 hours — can easily produce a swing of exactly this size without there being any real underlying difference in prompt quality at all. This is precisely the "small number of observations that could easily be noise" trap named directly in the material, and it's compounded by the second problem: 6 hours is a very short window, meaning the team is also "peeking" and stopping right when the numbers happened to look good, rather than letting the test run to a predetermined, adequately-powered duration.

The fix is the discipline named in the overview: **decide the required sample size or duration in advance** (based on the baseline conversion rate and how small a difference you actually care about detecting — smaller true effects require larger samples to reliably distinguish from noise), let the test run to that threshold, and ideally check statistical significance formally before concluding anything — rather than shipping the moment an early result looks exciting. Given a baseline conversion rate around 4-5%, reliably detecting a real effect of the size claimed here would likely require thousands of visitors per arm, not hundreds — the current sample is nowhere close to that.

**Why the others are wrong**

- **A)** treats the *size* of the relative improvement (35%) as if it were evidence of its *reliability*, but those are different properties — a large-looking relative difference computed from a tiny number of raw events (3 purchases' worth of difference) is exactly the situation where the percentage can look dramatic while being statistically meaningless. This is the same "trust the number without checking what it's built on" mistake running through this entire domain, just now applied to test conclusions instead of eval metrics.
- **C)** proposes an unrelated fix that doesn't address the actual problem — traffic split ratio (50/50 vs. 90/10) affects how much data accumulates in each arm per unit time, but it doesn't fix the core issue of the test being stopped after too little total volume and too short a duration; shifting the ratio without addressing sample size/duration wouldn't have prevented this specific premature conclusion.
- **D)** invents a device-segmentation concern with no basis in the scenario — nothing in the setup suggests desktop/mobile traffic was mixed inappropriately or that this is the actual issue; this misdiagnoses the problem as being about traffic *composition* when the real, evidenced issue is traffic *volume and test duration*, and "invalidates any A/B test regardless of duration" is also an overstated, unsupported claim — mixed device traffic isn't inherently invalidating if it's randomly and comparably split across both arms, which is a separate question from whether enough total observations have accumulated.

**Pattern to remember, opening the A/B testing scenario arc**: a result that looks exciting after a short window is exactly when the discipline to *not* act yet matters most — the temptation to ship a "35% improvement" immediately is strongest exactly when the underlying sample is too thin to trust it. Predetermining sample size/duration before starting the test — and sticking to that threshold even when early numbers look tempting one way or the other — is what separates a genuine finding from a lucky (or unlucky) early read.

Want another A/B testing scenario, or move to a different aspect of A/B testing methodology?