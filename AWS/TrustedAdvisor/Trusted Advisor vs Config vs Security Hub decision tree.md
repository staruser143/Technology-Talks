Below is a **clear, exam‑ready decision tree** to choose between **AWS Trusted Advisor**, **AWS Config**, and **AWS Security Hub** — optimized for **SAP‑C02 elimination logic**.

***

# Trusted Advisor vs Config vs Security Hub

## One‑Glance Decision Tree

    START
     │
     │─► Do you want AWS best‑practice recommendations 
     │    (cost, performance, fault tolerance, service limits)?
     │
     │      YES ─► AWS Trusted Advisor ✅
     │      NO
     │
     │─► Do you need to track, evaluate, or enforce
     │    resource configuration changes over time?
     │
     │      YES ─► AWS Config ✅
     │      NO
     │
     │─► Do you want centralized security findings
     │    across AWS services and partner tools?
     │
     │      YES ─► AWS Security Hub ✅
     │
     END

***

## Layered “Why” Decision Tree (Deeper)

    START
     │
     │─► Is the question asking:
     │     - "Are we following best practices?"
     │     - "How do we reduce cost?"
     │     - "What AWS recommends?"
     │
     │      ─► YES → Trusted Advisor
     │
     │─► Is the question about:
     │     - Drift detection
     │     - Compliance rules
     │     - Who changed what & when
     │     - Enforcing configuration standards
     │
     │      ─► YES → AWS Config
     │
     │─► Is the question about:
     │     - Security posture management
     │     - Central security dashboard
     │     - Combining GuardDuty, Inspector, Macie findings
     │
     │      ─► YES → AWS Security Hub
     │
     END

***

## Quick Mental Trigger (Exam Reflex)

| Keyword Seen in Question         | Pick This       |
| -------------------------------- | --------------- |
| **Best practices**, optimize     | Trusted Advisor |
| **Configuration history**, drift | AWS Config      |
| **Security findings**, posture   | Security Hub    |
| **Cost reduction**               | Trusted Advisor |
| **Compliance evidence**          | AWS Config      |
| **CIS / Security Standards**     | Security Hub    |

***

## How They Work Together (Important SAP‑C02 Insight)

    AWS Config
       └─ Evaluates compliance
           └─ Sends findings
               └─ AWS Security Hub
                    └─ Central security visibility

    Trusted Advisor
       └─ Independent
           └─ Best‑practice recommendations

✅ **Trusted Advisor does NOT enforce**  
✅ **Config can trigger auto‑remediation**  
✅ **Security Hub aggregates, not monitors directly**

***

## Do NOT Confuse These (Classic Traps)

❌ *“I need to know who modified an S3 public policy”*  
→ **AWS Config**, not Trusted Advisor

❌ *“I need cost optimization alerts”*  
→ **Trusted Advisor**, not Security Hub

❌ *“Single pane of glass for security tools”*  
→ **Security Hub**, not Config

***

## One‑Line Summaries (Exam Memory)

*   **Trusted Advisor** → *AWS best‑practice advisor*
*   **AWS Config** → *Configuration compliance & drift*
*   **Security Hub** → *Security findings aggregation*

***

## Ultra‑Fast Elimination Speed Card

    BEST PRACTICE? → Trusted Advisor
    DRIFT / COMPLIANCE? → Config
    SECURITY DASHBOARD? → Security Hub

***

