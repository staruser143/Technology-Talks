# Foundation model (FM) customization 
- Modifies model weights to adapt base behavior to specialized domain terminology, proprietary styles, or task-specific performance.

## Core Customization Techniques
| Customization Technique | Core Mechanism | Training Data Format | When to Use |
|---|---|---|---|
| **Supervised Fine-Tuning (SFT)** | Adjusts weights using next-token prediction loss on labeled demonstrations. | Supervised prompt-completion pairs in .jsonl format. | • Enforcing strict output formats, classification labels, or JSON schemas.
• Aligning responses to corporate tone, persona, or brand voice.
• Eliminating lengthy, repetitive few-shot prompt instructions. |
| **Continued Pre-Training (CPT)** | Further pre-trains base model weights on unstructured text via self-supervised learning. | Unlabeled raw text in .jsonl format ({"input": "..."}). | • Injecting deep domain-specific knowledge and specialized terminology.
• Highly regulated or niche fields (legal statutes, clinical papers, proprietary internal documentation). |
| **Reinforcement Fine-Tuning (RFT)** | Updates policy weights using reinforcement learning driven by reward signals and outcome evaluations. | Prompts paired with reward functions, rule checkers, or ground-truth verifiers. | • Complex multi-step reasoning, mathematical solutions, and code generation.
• Tasks with verifiable, deterministic outcomes (unit tests, SQL queries, API schemas).
• Overcoming the quality plateau of simple supervised imitation. |
| **Model Distillation** | Transfers knowledge and reasoning behaviors from a large "teacher" model into a compact "student" model. | Prompt logs or input prompts (teacher generates synthetic completions). | • Slashing inference latency and serving costs on high-volume production traffic.
• Retaining frontier-level accuracy in a smaller, edge-friendly, or cheaper model footprint. |
| **Parameter-Efficient Fine-Tuning (PEFT / LoRA)** | Freezes base weights and trains lightweight, low-rank decomposition matrices inside transformer layers. | Labeled prompt-completion pairs (often trained in Amazon SageMaker). | • Multi-tenant systems requiring hundreds of customized model variants.
• Cost-efficient hosting via multi-adapter dynamic loading and hot-swapping on shared GPU endpoints. |
| **Custom Model Import (CMI)** | Imports externally trained or fine-tuned weights into Amazon Bedrock. | Model checkpoints/weights (e.g., Safetensors, PyTorch) stored in Amazon S3. | • Running open-source models (fine-tuned Llama, Mistral, Qwen) via managed Bedrock APIs.
• Training with custom PyTorch loops or DeepSpeed before serving serverlessly. |

## Architectural Decision Framework: When to Customize
Before selecting a customization technique, evaluate whether model weight updates are genuinely required:
  ```
                                [Business Requirement]
                                          │
               Is access to frequently changing or dynamic data required?
                                   ┌──────┴──────┐
                                 Yes             No
                                  │              │
                       [RAG / Knowledge Bases]    Does prompt engineering (few-shot)
                                                  provide acceptable consistency?
                                                    ┌──────┴──────┐
                                                  Yes             No
                                                   │              │
                                          [Prompt Management]   [Weight Customization]
                                                                  │
                    ┌─────────────────────────────────────────────┼─────────────────────────────────────────────┐
                    ▼                                             ▼                                             ▼
          Need raw terminology                        Need formatting, style,                       Need lower cost/latency
           or domain jargon?                            or strict schema?                            at high query volume?
                    │                                             │                                             │
         [Continued Pre-Training]                             [SFT / LoRA]                             [Model Distillation]
                    │                                             │
                    └──────────────────────┬──────────────────────┘
                                           ▼
                            Requires verifiable reasoning
                                or multi-step logic?
                                           │
                                  [Reinforcement FT]
```

 * **Use Prompt Engineering**: Prototyping, low-volume tasks, or whenever context easily fits within standard model token limits.
 * **Use RAG (Bedrock Knowledge Bases)**: Up-to-date factual data, dynamic corpora, audit trails, and strict citation requirements without modifying weights.
 * **Use Customization (CPT, SFT, RFT, Distillation)**: When tasks demand consistent domain terminology, deterministic formatting across thousands of calls, low latency at scale, or reasoning capabilities that in-context prompting cannot satisfy.
