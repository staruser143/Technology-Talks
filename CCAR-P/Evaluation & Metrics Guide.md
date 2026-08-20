# CCAR-P Evaluation & Metrics Guide

## The Big Picture: Why Evaluations Exist

Every evaluation tries to answer one business question:

| Business Question | Evaluation Question |
|---|---|
| Is model B better? | Superiority |
| Is model B good enough? | Non-Inferiority |
| Are they roughly the same? | Equivalence |
| Which model should we deploy? | A/B Testing |
| Is performance improving? | Benchmark Tracking |
| Can we trust this metric? | Statistical Significance |
| Did we just get lucky? | Confidence Intervals |
| How much data do we need? | Power Analysis |

> **Key Insight:** Anthropic increasingly treats evals as experiments rather than leaderboards. The goal is making a deployment decision with an acceptable level of uncertainty.

---

## Level 1: Learn the Core Vocabulary

### Metric
A number that measures something.

**Examples:**
- Resolution Rate
- Accuracy
- Task Success Rate
- Hallucination Rate
- Latency
- Cost per Request
- Human Preference Score

**Example Application:**
```
Current Model = 83% Resolution Rate
New Model = 81% Resolution Rate
```
→ Resolution rate is the metric.

### Success Criteria
The business defines what "good enough" means.

**Example:**
- Must achieve ≥ 80% resolution rate
- Must stay below 2 seconds latency
- Must cost less than $0.01/request

> **Note:** Anthropic emphasizes defining success criteria before running experiments.

### Benchmark
A fixed collection of test cases.

**Example:**
```
1000 support tickets
500 coding problems
200 legal questions
```
→ You repeatedly evaluate models against the same benchmark.

### Eval
An experiment that measures model performance.

| Component | Details |
|---|---|
| **Input** | Prompt |
| **Output** | Model Response |
| **Grading** | Correct / Incorrect |

> **Note:** Anthropic describes evals as experiments with tasks, trials, and grading logic.

---

## Level 2: Understand Uncertainty

### Why Raw Scores Are Not Enough

**Scenario:**
```
Model A = 80%
Model B = 81%
```

**The Question:** Is Model B actually better?

- Maybe.
- Maybe not.
- What if the benchmark questions happened to favor Model B?

> **Why This Matters:** This is why Anthropic emphasizes statistical analysis and confidence intervals.

### Confidence Interval (CI)
**Definition:** "Plausible range of the true value."

**Example:**
```
Observed Accuracy = 81%
95% CI: 79% - 83%
```

**Meaning:**
- Best estimate: 81%
- Likely range: 79%-83%

**Mental Model:** Imagine shooting arrows.
- **One arrow:** 81% (single measurement)
- **Many arrows:** 79%, 80%, 81%, 82%, 83% (distribution of measurements)
- The confidence interval is the **cluster of arrows**.

---

## Level 3: Statistical Significance

Exam questions love this topic.

### Statistical Significance
**Question:** Is observed difference likely real?

**Example:**
```
A = 80%
B = 81%
Difference = 1%
```

**The Issue:** Maybe that's just random noise.

**What It Asks:** "Could this difference have happened by chance?"

> **Key Takeaway:** Anthropic's eval guidance encourages quantifying uncertainty instead of simply comparing scores.

---

## Level 4: The Three Comparison Tests

This is probably the **highest-value area for CCAR-P**.

### 1. Superiority
**Question:** Is new model better?

**Example:**
```
Current = 80%
New = 85%
```

**Goal:** Prove improvement

**Keyword Clues:**
- Better
- Increase
- Improve
- Outperform

---

### 2. Non-Inferiority
**Question:** Is new model not too much worse?

**Example:**
```
Current = 80%
New = 79%
Business says: "Anything above 79% is acceptable"
```

**Goal:** Acceptable degradation within tolerance

**Keyword Clues:**
- Good enough
- Acceptable degradation
- Within tolerance
- No more than X worse

---

### 3. Equivalence
**Question:** Are they effectively the same?

**Example:**
```
Acceptable Range: 79% - 81%
Current Model: 80%
```

**Goal:** Proof that performance remains within both upper and lower bounds

**Keyword Clues:**
- Equivalent
- Similar
- Same performance
- Interchangeable

---

### Easy Exam Trick

When reading a scenario, ask:

| Scenario | Test Type |
|---|---|
| Wants improvement? | **Superiority** |
| Accepts slight degradation? | **Non-Inferiority** |
| Wants same performance? | **Equivalence** |

---

## Level 5: A/B Testing

Most deployment decisions use this.

### Setup

| Element | Description |
|---|---|
| **Current Users** | Model A |
| **Half of Users** | Model B (experimental) |

### Measures
- Resolution rate
- Customer satisfaction
- Revenue

**Question:** Which performs better in production?

### Common Trap

| Focus | Result |
|---|---|
| People focus on | Accuracy ↑ |
| Business focuses on | Outcome |
| **Example** | Accuracy ↑ but Customer Satisfaction ↓ |
| **Consequence** | Deployment may still fail |

---

## Level 6: Power Analysis

One of the hardest topics.

### The Question
**How many examples do we need?**

| Scenario | Issue |
|---|---|
| Too few examples | Can't detect real differences |
| Too many | Expensive |

**Answer:** Power analysis determines the optimal experiment size.

> **Insight:** Anthropic's statistical evaluation work discusses planning evaluations and determining sufficient sample sizes.

### Mental Model

**To detect:**
```
80% vs 81% → requires far more data
80% vs 95% → requires less data
```

**Key Principle:** Small differences need bigger experiments.

---

## Level 7: Metrics You Should Know

These show up repeatedly.

### Accuracy
```
Correct Answers
──────────────── = Accuracy
Total Answers
```

### Precision
**Question:** When model says YES, how often is it right?

**Example:** Spam detection

### Recall
**Question:** How many real positives did we find?

**Example:** Fraud detection

### F1 Score
**Definition:** Balances Precision + Recall

### Latency
How quickly it responds.

**Examples:**
- TTFT (Time To First Token)
- End-to-End Response Time

### Cost
**Examples:**
- $/request
- $/1M tokens

### Success Rate
Most agent evaluations use this.

**Question:** Task completed? Yes/No

> **Note:** Anthropic's agent-evaluation guidance frequently focuses on task success and completion criteria.

---

## The Decision Framework

For every eval question, ask these **5 questions:**

### Q1. What metric are we measuring?
- Accuracy?
- Resolution rate?
- Latency?
- Cost?

### Q2. What business decision is being made?
- Deploy?
- Replace?
- Keep?
- Improve?

### Q3. What does success mean?
- Better?
- Same?
- Not much worse?

### Q4. Is uncertainty important?
- Confidence interval?
- Statistical significance?

### Q5. Which test matches the decision?
| Decision | Test |
|---|---|
| Better | → Superiority |
| Not much worse | → Non-Inferiority |
| Same | → Equivalence |

---

## What I'd Master for CCAR-P

### Priority Order

#### Tier 1 (Must Know)
- ✅ Confidence Intervals
- ✅ Statistical Significance
- ✅ Superiority
- ✅ Non-Inferiority
- ✅ Equivalence
- ✅ A/B Testing

#### Tier 2 (Frequently Appears)
- ✅ Precision
- ✅ Recall
- ✅ F1
- ✅ Success Rate
- ✅ Human Preference Evaluation
- ✅ Benchmark Design

#### Tier 3 (Advanced)
- ✅ Power Analysis
- ✅ Sample Size Planning
- ✅ Error Bars
- ✅ Variance Reduction
- ✅ Paired vs Unpaired Comparisons

---


