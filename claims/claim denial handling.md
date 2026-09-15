# Reference Architecture for implementing the denial-escalation process as a controlled, auditable agentic workflow. 
I am assuming these are healthcare claims and that ServiceNow is the ticketing and workflow system.
The same pattern can be adapted for insurance, banking, warranty, or other claim types.

## 1. Recommended design principle

Do not give a single large-language-model agent unrestricted authority to decide whether a denial is correct.

Use a hybrid approach:

### 1. Deterministic validation
- Eligibility
- Coverage dates
- Authorization requirements
- Timely filing
- Duplicate claim checks
- Coding and contract rules
### 2. AI-assisted evidence analysis
- Interpret denial explanations
- Compare claim evidence with policy documents
- Identify contradictions and missing documents
- Produce an evidence-backed recommendation
### 3. Controlled decisioning
- Auto-close only low-risk, high-confidence cases
- Route ambiguous, clinical, high-value, or regulated cases to a human
### 4. Human-in-the-loop approval
- Required for clinical judgment, policy ambiguity, financial thresholds, or conflicting evidence

For healthcare interoperability, the claim and adjudication information can be represented using the FHIR Claim and FHIR ClaimResponse structures. Health Level Seven International</Health Level Seven International> defines Claim as the request containing financial and supporting clinical information, while ClaimResponse represents the adjudication result or processing error.

## 2. Target architecture

```
Claim Adjudication System
        |
        | Claim ID / Denial Event
        v
ServiceNow Denial Ticket
        |
        v
Agentic Workflow Orchestrator
        |
        +--> Ticket Intake Agent
        |
        +--> Claim Data Retrieval Agent
        |
        +--> Denial Normalization Agent
        |
        +--> Policy and Contract Retrieval Agent
        |
        +--> Deterministic Rules Engine
        |
        +--> Evidence Evaluation Agent
        |
        +--> Risk and Confidence Agent
        |
        +--> Decision Agent
        |
        +--> Ticket Resolution Agent
        |
        +--> Human Reviewer, when required

```
ServiceNow AI Agent Studio supports creating, managing, testing, and monitoring agents and agentic workflows, including activity logs, automated evaluations, and security controls. This makes it suitable as the workflow orchestration layer when the organization already manages the tickets in ServiceNow.

Alternatively, you can keep the AI and rules services outside ServiceNow and use Workflow Studio or IntegrationHub to call them through REST APIs. ServiceNow supports REST-triggered flows and outbound REST actions for external-system integration.

## 3. Agent responsibilities
### Agent 1: Ticket Intake Agent
**Responsibilities**
- Trigger when a denial ticket is created
- Validate mandatory ticket fields
- Extract:
  - Claim ID
  - Member or patient reference
  -  Provider reference
  -  Payer
-  Denial code
-  Denial description
-  Service date
-  Claimed amount
-  Appeal deadline
- Detect duplicate escalation tickets
- Classify priority based on value, urgency, and appeal deadline

**Output**
```json
{
  "ticket_id": "DEN0012345",
  "claim_id": "CLM987654",
  "denial_code": "AUTH_REQUIRED",
  "priority": "HIGH",
  "appeal_deadline": "2026-09-30",
  "intake_status": "COMPLETE"
}
```


If the claim ID or critical identifiers are missing, the agent should not attempt a decision. It should assign the ticket to a data-completion queue.

## Agent 2: Claim Data Retrieval Agent

This agent obtains the complete claim context from the claims platform, document repository, policy administration system, and prior-authorization system.

### Evidence to retrieve
- Original claim header and line items
- Claim version and adjustment history
- Original denial response
- Explanation of Benefits or remittance details
- Eligibility and coverage snapshot as of the service date
- Benefit configuration
- Provider network status
- Prior authorization
- Referral information
- Medical records or attachments
- Previous related claims
- Contract and fee schedule version
- Adjudication rule version
- Audit trail from the original adjudication

A FHIR-based healthcare implementation can use Claim for the submitted request and ClaimResponse for adjudication results. ClaimResponse can contain claim-level and item-level adjudication information, identifiers, status, payment information, and processing errors.

### Important design rule

Always retrieve policy and eligibility information **effective on the date of service**, not merely the current version.

## Agent 3: Denial Normalization Agent

Denial messages from different systems are often inconsistent. This agent maps them to an enterprise denial taxonomy.

Example taxonomy:
```
Administrative
  - Missing information
  - Duplicate claim
  - Timely filing
  - Invalid member identifier

Eligibility
  - Member not eligible
  - Coverage terminated
  - Benefit excluded

Authorization
  - Authorization missing
  - Authorization expired
  - Procedure does not match authorization

Coding
  - Invalid procedure code
  - Diagnosis mismatch
  - Modifier issue
  - Bundling conflict

Contractual
  - Out-of-network
  - Non-covered service
  - Contract exclusion

Clinical
  - Medical necessity
  - Experimental or investigational
  - Level-of-care mismatch

Technical
  - Interface error
  - Incorrect rule configuration
  - Data synchronization problem

```
The normalized denial category determines which rule pack, policy corpus, and reviewer group should be used.

## Agent 4: Policy and Contract Retrieval Agent

This agent performs retrieval from an approved, version-controlled knowledge source.

**Knowledge sources**
- Benefit plan documents
- Payer reimbursement policies
- Provider contracts
- Clinical policies
- Authorization rules
- Coding guidelines
- Standard operating procedures
- Regulatory and appeal requirements
- Previously approved precedent decisions

**Retrieval metadata**

Every retrieved passage should include:

```json
{
  "document_id": "POL-784",
  "document_version": "6.2",
  "effective_from": "2026-01-01",
  "effective_to": "2026-12-31",
  "section": "4.3.2",
  "source_system": "PolicyRepository",
  "retrieved_text": "Prior authorization is required...",
  "relevance_score": 0.94
}
```


Do not allow the model to rely on general knowledge whenever an authoritative policy or contract should control the outcome.

## Agent 5: Deterministic Rules Engine

This component should not be an LLM. It should run explicit, testable rules.

Example:
```
IF service_date outside coverage_period
THEN denial_supported = true

IF authorization_required = true
AND valid_authorization_found = false
THEN denial_supported = true

IF authorization_found = true
AND authorization_service_code matches claim_service_code
AND authorization_date covers service_date
THEN authorization_denial_supported = false

IF claim_submission_date > filing_deadline
AND no approved exception exists
THEN denial_supported = true
```

The rules engine should return both the result and the trace.
```json
{
  "rule_id": "AUTH-102",
  "rule_version": "4.1",
  "result": "FAIL",
  "facts_used": {
    "authorization_required": true,
    "authorization_found": true,
    "authorization_valid_on_service_date": true
  },
  "explanation": "A valid authorization existed on the service date."
}
```

## Agent 6: Evidence Evaluation Agent

The evidence agent compares:

- Facts from the claim
- Original denial reason
- Rules-engine results
- Policy passages
- Contract provisions
- Supporting documents

It should not simply answer “correct” or “wrong.” It should create a structured evidence assessment.

```json
{
  "original_denial": "Authorization missing",
  "supporting_evidence": [
    {
      "fact": "Authorization PA-7821 exists",
      "source": "PriorAuthorizationSystem",
      "strength": "HIGH"
    },
    {
      "fact": "Authorization covers CPT group used by the claim",
      "source": "AuthorizationRuleEngine",
      "strength": "HIGH"
    }
  ],
  "contradicting_evidence": [],
  "missing_evidence": [],
  "recommended_outcome": "DENIAL_INCORRECT",
  "confidence": 0.97
}
```


The explanation should cite internal sources using **document IDs, effective dates, claim fields, and rule IDs**. An unsupported narrative generated by the model should never be treated as evidence.

## 4. Decision outcomes

Use three primary outcomes, but distinguish the meaning clearly.

### Outcome A: Denial Correct

Conditions:

- Required evidence is available
- Deterministic rules support the original denial
- No conflicting evidence exists
- Policy is applicable to the service date
- Confidence exceeds the auto-resolution threshold
The case is not in a mandatory-human-review category


Ticket action:

- Set outcome to Denial Upheld
- Attach decision summary and evidence
- Add applicable rule and policy references
- Close or resolve the ticket
- Notify the requester of appeal options, if applicable

### Outcome B: Denial Incorrect

Conditions:

- Evidence clearly contradicts the original denial
- A processing or configuration error is identified
- Valid authorization, eligibility, coverage, or supporting documentation exists
- A deterministic retest produces an approval or different adjudication result

Ticket action:

- Set outcome to Denial Overturned
- Trigger claim reprocessing or adjustment
- Keep the ticket in Pending Reprocessing
- Close only after the claim system returns a successful re-adjudication result
- Record recovered amount and root cause

**“Denial incorrect”** should therefore not directly mean **“ticket closed.”** The financial correction should be verified first.

### Outcome C: Human Handling Required

Conditions include:

- Missing information
- Conflicting policies
- Clinical or medical-necessity judgment
- Low confidence
- High financial value
- Appeal deadline risk
- Suspected fraud or abuse
- Policy version unavailable
- Contract ambiguity
- Model and rules engine disagreement
- External system failure
- New or previously unseen denial reason

Ticket action:

- Assign to the appropriate reviewer group
- Provide a concise case summary
- Show evidence for and against the denial
- Identify exactly what the reviewer must decide
- Preserve the complete agent execution trace

Healthcare claim denials may be subject to formal internal and external appeal rights, so automated closure must preserve applicable appeal information and deadlines. CMS explains that denied claims may be reconsidered through internal appeal and, in qualifying cases, external review.

## 5. Confidence and risk gating

Do not use a single model confidence value by itself. Calculate the disposition using multiple signals.

```
Decision score =
    rules completeness
  + evidence completeness
  + policy relevance
  + source reliability
  + historical accuracy for denial category
  - conflicting evidence
  - missing documents
  - operational risk
```

An example policy:

```
Auto-uphold:
  Confidence >= 0.97
  Evidence completeness = 100%
  No policy conflict
  Low or medium financial impact
  Non-clinical denial
  Deterministic rule confirms outcome

Auto-overturn and initiate reprocessing:
  Confidence >= 0.98
  Deterministic rule contradicts denial
  Re-adjudication simulation succeeds
  No clinical judgment involved

Human review:
  All remaining circumstances

```
Start with very conservative thresholds. Expand automation only after measuring performance on real adjudicated cases.

## 6. ServiceNow workflow

A suggested ticket state model is:

```
New
  -> Data Collection
  -> Automated Analysis
  -> Decision Proposed
       -> Human Review
       -> Pending Reprocessing
       -> Resolution Validation
  -> Resolved
  -> Closed
```

**Suggested custom fields**
```
u_claim_id
u_denial_code
u_denial_category
u_claim_amount
u_decision_outcome
u_decision_confidence
u_decision_reason
u_policy_references
u_rule_trace_id
u_agent_execution_id
u_human_review_reason
u_reprocessing_status
u_original_claim_status
u_final_claim_status
u_appeal_deadline
u_model_version
u_prompt_version
```

**Trigger approach**

Use a record-created or record-updated trigger when:
```

Ticket type = Claim Denial Escalation
AND State = New
AND Claim ID is not empty
```

Use IntegrationHub actions to retrieve claim data or call external analysis services. REST API triggers are also available when an external claims system needs to start the flow.

## 7. Example orchestrator logic
```python
def process_denial_ticket(ticket):
    intake = validate_and_classify(ticket)

    if not intake.is_complete:
        return route_to_data_completion(intake)

    claim = retrieve_claim(intake.claim_id)
    denial = normalize_denial(claim.denial_response)

    evidence = collect_evidence(
        claim=claim,
        denial_category=denial.category,
        effective_date=claim.service_date
    )

    rules_result = execute_rules(
        claim=claim,
        denial=denial,
        evidence=evidence
    )

    ai_assessment = evaluate_evidence(
        claim=claim,
        denial=denial,
        evidence=evidence,
        rules_result=rules_result
    )

    decision = apply_decision_policy(
        rules_result=rules_result,
        ai_assessment=ai_assessment,
        claim_value=claim.amount,
        denial_category=denial.category
    )

    if decision.outcome == "DENIAL_CORRECT":
        resolve_ticket_with_evidence(ticket, decision)

    elif decision.outcome == "DENIAL_INCORRECT":
        reprocessing_result = trigger_reprocessing(claim, decision)

        if reprocessing_result.success:
            resolve_ticket_after_reprocessing(ticket, decision)
        else:
            route_to_claim_operations(ticket, reprocessing_result)

    else:
        route_to_human_reviewer(ticket, decision)

    write_complete_audit_record(
        ticket=ticket,
        claim=claim,
        evidence=evidence,
        rules_result=rules_result,
        decision=decision
    )
```

## 8. Guardrails and governance

Because claim decisions can have financial, clinical, contractual, and regulatory consequences, include the following controls:

- Minimum-necessary data access
- Role-based access in ServiceNow
- Encryption in transit and at rest
- Redaction of unnecessary patient information
- Prompt-injection detection on uploaded documents
- Approved-source retrieval only
- No unrestricted internet retrieval during case decisioning
- Immutable evidence and decision logs
- Model, rule, policy, and prompt version tracking
- Explainability for every outcome
- Manual override with reason capture
- Separation between “recommend decision” and “execute payment”
- Automatic escalation when an integration fails
- Periodic bias and error-rate analysis
- Retention and deletion controls
- Production kill switch

ServiceNow documents activity logging, testing, automated evaluations, analytics, and AI Guardian capabilities such as prompt-injection protection as part of its AI-agent environment.

## 9. Testing approach

Build a “golden dataset” of previously resolved denial cases, reviewed by claim-domain experts.

Include:

- Correct denials
- Incorrect denials
- Missing-document cases
- Conflicting-policy cases
- Clinical-review cases
- High-value cases
- Edge cases
- System-error cases
- Historical policy versions
- Adversarial document content

Measure:
```
Precision of auto-uphold decisions
Precision of auto-overturn decisions
False-uphold rate
False-overturn rate
Human-routing accuracy
Evidence citation accuracy
Policy-version accuracy
Average handling time
Straight-through processing rate
Reopened-ticket rate
Financial recovery
Appeal-deadline compliance
```

The most important measures are the false-uphold rate and false-overturn rate, not generic model accuracy.

## 10. Recommended implementation roadmap

### Phase 1: Discovery and shadow mode
- Select one non-clinical denial category
- Map the current process
- Identify authoritative data sources
- Build the denial taxonomy
- Load version-controlled policies
- Create deterministic rules
- Run the agent without changing tickets
- Compare recommendations with human decisions

### Phase 2: Assisted handling
- Agent gathers data
- Agent prepares evidence summary
- Human makes the final decision
- Capture human corrections as structured feedback

### Phase 3: Controlled automation
- Auto-close only high-confidence, low-risk correct denials
- Auto-initiate reprocessing for clearly incorrect administrative denials
- Require confirmation of successful re-adjudication before closure

### Phase 4: Scale
- Add more denial categories
- Introduce category-specific agents
- Add root-cause analytics
- Detect recurring claim-system configuration problems
- Use human overrides and production outcomes for continuous evaluation

**My suggested first pilot**

Start with an **administrative prior-authorization denial** or a **coverage-date denial**, rather than medical necessity.

A good pilot scope would be:
```
One payer
One claim type
One denial category
One policy family
Low-to-medium claim value
No clinical judgment
Human approval for every outcome during the first release
```


This gives you measurable results while keeping the operational and regulatory risk controlled. 
The key architectural choice is to make the agent an evidence-driven investigator and recommender, with deterministic controls around final ticket resolution, rather than treating the LLM itself as the adjudication engine.
