# Amazon Bedrock Model Evaluation

Amazon Bedrock Model Evaluation is a fully managed capability that allows you to assess, compare, and validate foundation model (FM) outputs against specific performance, quality, and safety criteria.

## Evaluation approaches

Bedrock provides two primary evaluation pathways within its orchestration layer:

### Automated evaluations

- Algorithmic / programmatic metrics: Uses deterministic natural language algorithms such as ROUGE, BLEU, BERTScore, and exact match to score model outputs against reference ground-truth data.
- LLM-as-a-judge: Uses a capable frontier model (for example, Anthropic Claude) to evaluate target model responses across nuanced dimensions such as factual accuracy (groundedness), context relevance, and instruction-following quality.

### Human evaluations

- Routes model outputs to custom UI rating interfaces for qualitative assessment.
- Supports either a private workforce (internal subject-matter experts) or an AWS-managed public workforce through Amazon SageMaker Ground Truth / Mechanical Turk.

## When to use Amazon Bedrock Model Evaluation

| Use case / scenario | When to apply | Evaluation type |
|---|---|---|
| Model selection & benchmarking | Comparing multiple foundation models (for example, Anthropic Claude vs. Amazon Nova/Titan) on tasks such as Q&A, summarization, or classification to find the best balance of quality, cost, and latency. | Automated |
| Custom model validation | Assessing models after supervised fine-tuning (SFT), continued pre-training (CPT), or model distillation to verify that fine-tuning improved task performance without catastrophic regressions. | Automated |
| Deployment gates & CI/CD pipelines | Acting as an automated blocking check in AWS Lambda or Step Functions pipelines before promoting a new prompt or model version to production (for example, failing releases if evaluation thresholds are not met). | Automated |
| Model lifecycle deprecation | Validating drop-in replacement models when an existing production model enters the legacy lifecycle stage and approaches end-of-life (EOL). | Automated |
| Subjective & brand tone review | Evaluating nuanced qualities that automated heuristics cannot reliably score, such as brand voice alignment, humor, creativity, empathy, or cultural appropriateness. | Human or LLM-as-a-judge |
| Hallucination & groundedness audits | Verifying whether model responses remain strictly factual and grounded in source contexts (such as RAG pipelines) before production cutover. | Automated (LLM-as-a-judge) |

## Related tutorial

For a hands-on walkthrough of setting up these workflows in the AWS console, refer to the Amazon Bedrock Model Evaluation: Automatic & Human tutorial (#13).

This video demonstrates how to configure both automatic and human-based evaluation jobs directly within the Amazon Bedrock console.
