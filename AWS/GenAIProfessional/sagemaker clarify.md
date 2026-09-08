Amazon SageMaker Clarify is a specialized tool within the AWS machine learning ecosystem designed to help data scientists and developers detect bias, explain model predictions, and evaluate foundation models for responsible AI development. [1, 2] 
It acts as a transparency layer across the entire machine learning (ML) lifecycle—from initial data preparation all the way to production monitoring. [3, 4] 
------------------------------
## Key Capabilities

* 
* Bias Detection: Clarify checks for imbalances or statistical disparities in your training data (pre-training) and model predictions (post-training). You can specify sensitive attributes—like age, gender, or income—to calculate fairness metrics and identify where the data or model might be underrepresented or unfair. [1, 5, 6] 
* Model Explainability: It calculates feature attributions using algorithms like SHAP (SHapley Additive exPlanations) and Partial Dependence Plots (PDP). This helps you understand exactly how much weight each input variable (e.g., credit history vs. income level) carries in a specific model prediction. [1, 6, 7, 8] 
* Production Monitoring: Once a model is deployed, Clarify tracks it in real-time to watch for bias drift and feature attribution drift, alerting you if the model's behavior shifts unexpectedly as real-world data changes. [1] 
* Foundation Model (FM) Evaluation: It supports evaluating Large Language Models (LLMs). It scores both internal and external foundation models on metrics such as toxicity, factual knowledge, hallucinations, creativity, and robustness, utilizing both algorithmic checks and human-in-the-loop validation. [9, 10] 
* 

## Why Businesses Use It
SageMaker Clarify is primarily used to build trust, satisfy compliance and regulatory requirements (like ISO 42001), and debug complex "black box" models like neural networks and ensemble trees before they negatively impact customers. [8, 11] 
Are you looking to implement SageMaker Clarify for traditional machine learning models (like tabular classification) or are you looking to evaluate Large Language Models (LLMs)? Let me know your specific use case so I can provide the right architectural steps or sample configurations.

[1] [https://tutorialsdojo.com](https://tutorialsdojo.com/amazon-sagemaker-clarify/)
[2] [https://medium.com](https://medium.com/@salmananwaar1127/aws-sagemaker-clarify-an-overview-513f0de21d03)
[3] [https://www.amazonaws.cn](https://www.amazonaws.cn/en/sagemaker/clarify/)
[4] [https://www.linkedin.com](https://www.linkedin.com/learning/responsible-ai-with-amazon-sagemaker-ai/explore-sagemaker-clarify-for-bias-detection)
[5] [https://aws.amazon.com](https://aws.amazon.com/sagemaker/ai/clarify/)
[6] [https://www.cloudthat.com](https://www.cloudthat.com/resources/blog/amazon-sagemaker-clarify-for-bias-detection-in-machine-learning-models)
[7] [https://www.youtube.com](https://www.youtube.com/watch?v=jvcPZmnXaxo)
[8] [https://www.youtube.com](https://www.youtube.com/watch?v=cTa5HYCxTVg&t=360)
[9] [https://aws.amazon.com](https://aws.amazon.com/blogs/aws/amazon-sagemaker-clarify-makes-it-easier-to-evaluate-and-select-foundation-models-preview/)
[10] [https://www.youtube.com](https://www.youtube.com/watch?v=9X2oDkOBYyA&t=3173)
[11] [https://aws.amazon.com](https://aws.amazon.com/id/video/watch/31248d9d747/)


Yes, Amazon SageMaker Clarify can be used with Amazon Bedrock foundation models (FMs). [1, 2] 
AWS provides two ways to run these evaluations depending on your workflow:
## 1. Programmatic Evaluation via the fmeval Library
The most flexible way to evaluate Bedrock models using SageMaker Clarify is through FMEval, the open-source Python library that powers SageMaker Clarify's foundation model evaluations. [3] 
By setting up a custom ModelRunner in your code, you can point Clarify directly to a serverless Bedrock endpoint (such as Anthropic Claude or Amazon Titan). You supply a dataset, and Clarify will invoke the Bedrock API to evaluate the model on metrics like: [4] 

* 
* Factual Knowledge & Accuracy
* Toxicity
* Semantic Robustness
* Prompt Stereotyping [5, 6, 7] 
* 

## 2. The Unified Studio UI Experience
If you are using the updated SageMaker Unified Studio environment, Bedrock models are natively integrated. This allows you to launch automatic model evaluation jobs from a guided wizard UI to quickly benchmark Bedrock models on common tasks like text generation, question answering, and summarization. [7, 8, 9] 
------------------------------
## Alternative: Amazon Bedrock Native Evaluations
It is worth noting that if your architecture is fully built inside Amazon Bedrock, Amazon Bedrock also features its own native model evaluation tool. [10] 

* 
* Use SageMaker Clarify / FMEval if: You are managing an end-to-end MLOps pipeline inside SageMaker, want to compare Bedrock models directly against SageMaker JumpStart models, or require heavily customized evaluation code.
* Use Bedrock Evaluations if: You want a purely serverless, no-code setup focused exclusively on Bedrock models. [1, 3, 8] 
* 

Would you like a sample Python code snippet demonstrating how to configure the fmeval library with a Bedrock model runner, or are you looking to set this up using the SageMaker UI console? [2] 

[1] [https://repost.aws](https://repost.aws/articles/ARXsxgLFa7R-uw8TgUo1fDJg/accelerate-foundation-model-evaluation-with-amazon-sagemaker-clarify-and-fmeval)
[2] [https://www.youtube.com](https://www.youtube.com/watch?v=U3hgTepTBPc)
[3] [https://aws.plainenglish.io](https://aws.plainenglish.io/bring-your-own-llm-evaluation-algorithms-to-sagemaker-clarify-foundation-model-evaluations-714ce6f02fbb)
[4] [https://aws.plainenglish.io](https://aws.plainenglish.io/bring-your-own-llm-evaluation-algorithms-to-sagemaker-clarify-foundation-model-evaluations-714ce6f02fbb)
[5] [https://docs.aws.amazon.com](https://docs.aws.amazon.com/sagemaker/latest/dg/clarify-foundation-model-evaluate.html)
[6] [https://aws.amazon.com](https://aws.amazon.com/blogs/aws/amazon-sagemaker-clarify-makes-it-easier-to-evaluate-and-select-foundation-models-preview/)
[7] [https://docs.aws.amazon.com](https://docs.aws.amazon.com/sagemaker-unified-studio/latest/userguide/evaluation.html)
[8] [https://www.cloudthat.com](https://www.cloudthat.com/resources/blog/whats-new-in-amazon-sagemaker-ai-in-2025-a-practical-guide)
[9] [https://medium.com](https://medium.com/@shaik.mujtaba.hussain08/navigating-the-ai-frontier-understanding-aws-bedrock-foundation-models-and-the-role-of-sagemaker-ee05c394d563)
[10] [https://repost.aws](https://repost.aws/articles/ARXsxgLFa7R-uw8TgUo1fDJg/accelerate-foundation-model-evaluation-with-amazon-sagemaker-clarify-and-fmeval)


Amazon Bedrock Evaluations is a fully managed, serverless capability within Amazon Bedrock that allows you to test, benchmark, and compare the performance of foundation models (FMs) and Retrieval-Augmented Generation (RAG) applications. [1] 
Instead of writing complex code to manage data pipelines, model endpoints, and mathematical grading, you provide a dataset, pick your metrics, and Bedrock handles the rest. [2, 3] 
The system functions through three primary evaluation pathways:
------------------------------
## 1. Automatic Programmatic Evaluations
This approach is ideal for objective, quantitative metrics. It compares a model’s output directly against a ground-truth answer in your test dataset using deterministic algorithms. [1, 2, 4] 

* 
* How it works: You select a task type (e.g., text summarization, question answering) and a dataset. The service routes prompts to the model, collects responses, and computes mathematical scores. [4, 5, 6] 
* Key Metrics: Includes traditional NLP metrics like ROUGE (for summarization), BLEU or BERTScore (for text similarity), accuracy, and semantic robustness. [1, 4, 7] 
* Datasets: You can use Amazon's built-in, pre-curated public datasets or upload your own JSON Lines file to an Amazon S3 bucket. [8, 9] 
* 

## 2. LLM-as-a-Judge (Automated Model Evaluation)
For metrics that require cognitive reasoning but still need to scale automatically, Bedrock lets you use a powerful LLM to act as the evaluator. [1, 2] 

* 
* How it works: You choose the target model you want to test and select a secondary, highly capable model (such as Amazon Nova Pro or Anthropic Claude) to act as the "Judge". [8, 10] 
* Evaluation Loop: The target model answers the prompt, and the Judge model automatically reviews the answer against specified metrics, applying a defined scoring rubric to issue a grading report. [2, 10] 
* Key Metrics: Correctness, completeness, helpfulness, and harmfulness. You can also build entirely custom prompts for your own proprietary metrics. [1, 10, 11] 
* 

## 3. Human Evaluations
For highly subjective attributes—like brand voice, nuanced style, or creative alignment—automated metrics often fall short. Bedrock automates the workflow needed to bring real people into the loop. [12, 13] 

* 
* Workforce Options: You can choose between a private work team (your own employees or internal domain experts) or an AWS-managed workforce (where AWS sources and manages skilled external reviewers).
* How it works: Bedrock generates a secure web interface for your reviewers. It feeds prompts to one or two models, presents the responses side-by-side to the human workers, and asks them to rate or rank the outputs based on your custom criteria (e.g., "Which summary sounds more friendly?"). [3, 5, 12, 13] 
* 

------------------------------
## Expanding Capabilities: RAG and Agents
Bedrock has expanded its evaluation suite beyond standalone models:

* 
* Knowledge Bases (RAG Evaluation): You can isolate and test your search pipelines. It offers Retriever-only metrics (evaluating if the system fetched the right text blocks) and Retriever & Response metrics (evaluating if the LLM hallucinated based on that text).
* AgentCore Evaluations: Provides built-in evaluators to trace multi-turn AI agents, tracking specialized metrics like tool usage and session-level goal success dynamically. [1, 8, 14, 15] 
* 

At the end of any job, Bedrock outputs clear visual charts directly in the AWS Console, alongside raw JSON files in S3, making it easy to identify exactly where a model failed or succeeded. [3, 13] 
Are you planning to run automated benchmarks to choose between different models, or are you trying to build a human feedback loop for a specific application? Tell me about your goals and I can outline the prompt dataset structure you will need.

[1] [https://aws.amazon.com](https://aws.amazon.com/bedrock/evaluations/)
[2] [https://www.youtube.com](https://www.youtube.com/watch?v=NSpH6i7j2KI)
[3] [https://aws.amazon.com](https://aws.amazon.com/video/watch/bb1243512fe/)
[4] [https://www.cloudthat.com](https://www.cloudthat.com/resources/blog/evaluating-generative-ai-with-amazon-bedrock-model-evaluation)
[5] [https://www.youtube.com](https://www.youtube.com/watch?v=KvWkajjS4pU)
[6] [https://aws.amazon.com](https://aws.amazon.com/video/watch/bb1243512fe/)
[7] [https://technologuy.medium.com](https://technologuy.medium.com/mastering-ai-agent-quality-a-deep-dive-into-aws-bedrock-agentcore-evaluations-3692484849db)
[8] [https://www.youtube.com](https://www.youtube.com/watch?v=Qbgl9Ttugug)
[9] [https://repost.aws](https://repost.aws/articles/ARiAmoyLTVQvicq-gaLmTC4w/comprehensive-and-accessible-model-evaluation-for-foundation-models-on-amazon-bedrock)
[10] [https://aws.amazon.com](https://aws.amazon.com/blogs/machine-learning/llm-as-a-judge-on-amazon-bedrock-model-evaluation/)
[11] [https://docs.aws.amazon.com](https://docs.aws.amazon.com/bedrock/latest/userguide/model-evaluation-metrics.html)
[12] [https://aws.amazon.com](https://aws.amazon.com/bedrock/evaluations/)
[13] [https://www.youtube.com](https://www.youtube.com/watch?v=Sz2cw52CMEY)
[14] [https://www.youtube.com](https://www.youtube.com/watch?v=i0h7xA8cqYs&t=115)
[15] [https://aws.amazon.com](https://aws.amazon.com/blogs/machine-learning/evaluate-any-agent-framework-with-amazon-bedrock-agentcore-evaluations/)
