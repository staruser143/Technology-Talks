✅ **Correct configuration: Use AWS Outposts with Amazon EC2 on Outposts and Amazon S3 on Outposts**

***

### ✅ Why this is the right answer

This scenario has **four hard constraints**, and only **AWS Outposts** satisfies all of them simultaneously:

| Requirement                                                           | Why AWS Outposts fits                                                                                                  |
| --------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------- |
| **Sub‑second latency between EC2 and S3**                             | EC2 and S3 run **on the same on‑premises Outposts hardware**, enabling **local, LAN-level communication**              |
| **Data must reside on hardware under the company’s physical control** | Outposts hardware is **installed in the company’s data center**                                                        |
| **Must use EC2 and S3**                                               | AWS provides **EC2 on Outposts** and **Amazon S3 on Outposts**                                                         |
| **Managed through AWS Console and CLI**                               | Outposts resources are managed **exactly like AWS Regional resources** using the **same APIs, Console, SDKs, and CLI** |

***

### ✅ The correct configuration

> **Deploy an AWS Outposts rack in the company’s data center, run Amazon EC2 instances on Outposts, and create an Amazon S3 bucket on Outposts. Manage all resources using the AWS Management Console and AWS CLI.**

***

### ✅ Architecture overview

    On‑premises data center (company-controlled)
    ┌───────────────────────────────────────────┐
    │ AWS Outposts Rack                          │
    │                                           │
    │  ┌───────────────┐   ┌─────────────────┐ │
    │  │ EC2 Instances │◄──►│ S3 on Outposts  │ │
    │  └───────────────┘   └─────────────────┘ │
    │        (local, sub‑ms latency)             │
    └───────────────────────────────────────────┘
                ▲
                │  Managed via AWS Console / CLI
                ▼
            AWS Region (control plane)

***

### ✅ Why the other common alternatives are wrong (exam elimination)

| Option                   | Why it fails                                                          |
| ------------------------ | --------------------------------------------------------------------- |
| **AWS Local Zones**      | Hardware is **owned and operated by AWS**, not the customer           |
| **Snowball / Snowcone**  | Not designed for persistent EC2 workloads + S3 integration            |
| **Dedicated Hosts**      | Still run in AWS Regions, **not on customer-controlled hardware**     |
| **Direct Connect to S3** | Low latency to AWS, but **data does not reside on customer hardware** |
| **EBS Local + EC2**      | Does not provide **S3-compatible object storage**                     |

***

### ✅ Key AWS exam phrases that point *directly* to Outposts

*   “**Hardware under the company’s physical control**”
*   “**Sub‑second latency**”
*   “**Use EC2 and S3**”
*   “**Managed using AWS Console and CLI**”

👉 When you see all four together, the answer is **AWS Outposts** — no ambiguity.

***

### ✅ Final answer (exam-ready wording)

> **Deploy AWS Outposts in the company’s data center. Launch Amazon EC2 instances on Outposts and create an Amazon S3 bucket on Outposts to achieve sub-second local latency. Manage the resources using the AWS Management Console and AWS CLI.**


