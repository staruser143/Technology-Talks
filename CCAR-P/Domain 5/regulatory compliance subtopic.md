## Sub-topic 4: Regulatory Compliance (GDPR, HIPAA, FedRAMP)

This is genuinely new ground. High-level, exam-relevant framing rather than legal expertise:

**GDPR** (EU data protection) — key concepts likely to show up in scenarios: **data minimization** (only collect/process what's genuinely needed), **right to erasure** (a person can request their data be deleted, which has real implications for systems that retain conversation history, fine-tuning data, or logs), **consent** for processing personal data, and — particularly relevant to AI specifically — restrictions on **fully automated decisions with significant effects** on individuals (e.g., automated loan denial) without a path to meaningful human review.

**HIPAA** (US healthcare) — protected health information (PHI): who can access it, minimum-necessary-access principles, audit logging requirements (directly your hospital attribution scenario from Domain 3), and secure transmission/storage requirements.

**FedRAMP** (US government cloud security) — a security authorization framework for cloud services handling government data; relevant when a system might serve government users/data, with strict infrastructure security requirements.

**The exam-relevant skill**: given a system's data types and users, recognize which regulatory considerations apply and what design implications follow — this connects directly to material you already have (data minimization ties to scope minimization from your auth/authz scenarios; audit logging ties to your hospital attribution scenario; automated-decision restrictions tie to your human-in-the-loop material).

Want to go into scenario practice for this now?