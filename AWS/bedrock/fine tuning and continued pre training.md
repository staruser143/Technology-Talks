# Fine Tuning vs Continued Pre-Training Data
- We cannot mix both Fine-Tuning and Continued Pre-Training data together in the same .jsonl file or use them interchangeably within a single Amazon Bedrock customization job.
- While both customization techniques consume newline-delimited JSON (.jsonl) files stored in Amazon S3, they have strictly incompatible data schemas and operate as separate job types.


## Key Differences & Incompatibilities
| Dimension | Fine-Tuning (FINE_TUNING) | Continued Pre-Training (CONTINUED_PRE_TRAINING) |
|---|---|---|
| JSON Line Schema | {"prompt": "...", "completion": "..."} | {"input": "..."} |
| Data Nature | Supervised: Explicit prompt-completion pairs demonstrating target behavior. | Unsupervised: Raw, continuous domain text without target answers. |
| API Parameter | customizationType="FINE_TUNING" | customizationType="CONTINUED_PRE_TRAINING" |
| Objective | Teaches formatting, style, tone, and deterministic task behavior. | Injects deep domain vocabulary and technical terminology. |


## What Happens If You Combine Them?
 * **Schema Validation Failure**:
   When you submit a customization job via create_model_customization_job, you must set customizationType to either FINE_TUNING or **CONTINUED_PRE_TRAINING**.
   * If customizationType="**FINE_TUNING**", the validation parser expects prompt and completion keys on every line. Any row formatted as {"input": "..."} will trigger a data validation failure.
   * If customizationType="**CONTINUED_PRE_TRAINING**", the parser expects {"input": "..."}. Any row containing prompt and completion keys will likewise fail parsing.
 * **Single Entry Point (s3Uri)**:
   The trainingDataConfig object accepts an S3 URI pointing to a single file or a prefix containing training files. Every file under that prefix must strictly conform to the schema required by that specific customization type.

## Recommended Pattern: Two-Stage Customization
If the use case requires both raw domain knowledge and structured task execution, execute them sequentially as a two-phase pipeline:
```
[Raw Domain Corpora (.jsonl)] 
           │
           ▼
Stage 1: Continued Pre-Training (CPT)  ──► Produces: Custom Domain Model
                                                           │
[Supervised Labeled Pairs (.jsonl)]                        │
           │                                               │
           ▼                                               ▼
Stage 2: Fine-Tuning (SFT)             ◄───────────────────┘
           │
           ▼
Final Specialized Custom Model (Ready for Provisioned Throughput)
```

 * **Phase 1 (Continued Pre-Training)**:
   * Use an unlabeled dataset (pretrain.jsonl using {"input": "..."}) to adapt the base model to your industry or proprietary terminology.
   * Set customizationType="CONTINUED_PRE_TRAINING".
 * **Phase 2 (Supervised Fine-Tuning)**:
   * Take the custom model generated from Phase 1 as the new baseModelIdentifier (or base checkpoint).
   * Run a second customization job with customizationType="FINE_TUNING" using a supervised dataset (train.jsonl using {"prompt": "...", "completion": "..."}) to align its formatting and output behavior.
