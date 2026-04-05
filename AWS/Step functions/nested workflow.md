Here’s how a nested workflow looks when Approved claims trigger parallel tasks—for example, simultaneously processing payment and writing an audit log.  

---

🏥 JSON Workflow Snippet with Nested Parallel State
`json
{
  "StartAt": "ValidateClaim",
  "States": {
    "ValidateClaim": {
      "Type": "Task",
      "Resource": "arn:aws:lambda:region:account:function:ValidateClaim",
      "ResultPath": "$.validationResult",
      "Next": "RouteDecision"
    },
    "RouteDecision": {
      "Type": "Choice",
      "Choices": [
        {
          "Variable": "$.validationResult.decision",
          "StringEquals": "Approved",
          "Next": "ApprovedBranch"
        },
        {
          "Variable": "$.validationResult.decision",
          "StringEquals": "Denied",
          "Next": "NotifyDenial"
        },
        {
          "Variable": "$.validationResult.decision",
          "StringEquals": "NeedsReview",
          "Next": "ManualReview"
        }
      ],
      "Default": "ErrorHandler"
    },
    "ApprovedBranch": {
      "Type": "Parallel",
      "Branches": [
        {
          "StartAt": "ProcessPayment",
          "States": {
            "ProcessPayment": {
              "Type": "Task",
              "Resource": "arn:aws:lambda:region:account:function:ProcessPayment",
              "End": true
            }
          }
        },
        {
          "StartAt": "AuditLog",
          "States": {
            "AuditLog": {
              "Type": "Task",
              "Resource": "arn:aws:lambda:region:account:function:WriteAuditLog",
              "End": true
            }
          }
        }
      ],
      "Next": "NotifyApproval"
    },
    "NotifyApproval": {
      "Type": "Task",
      "Resource": "arn:aws:lambda:region:account:function:SendApprovalNotification",
      "End": true
    },
    "NotifyDenial": {
      "Type": "Task",
      "Resource": "arn:aws:lambda:region:account:function:SendDenialNotification",
      "End": true
    },
    "ManualReview": {
      "Type": "Task",
      "Resource": "arn:aws:lambda:region:account:function:ManualReviewQueue",
      "End": true
    },
    "ErrorHandler": {
      "Type": "Fail",
      "Error": "InvalidDecision",
      "Cause": "Unexpected validation result"
    }
  }
}
`

---

🔎 Step-by-Step Behavior
1. ValidateClaim → Enriches payload with validation results (ResultPath).  
2. RouteDecision → Branches based on decision outcome.  
3. ApprovedBranch (Parallel State) → Runs two branches at the same time:  
   - ProcessPayment → Executes financial transaction.  
   - AuditLog → Writes compliance record.  
4. NotifyApproval → Sends confirmation to claimant.  
5. Other Branches → Denied → NotifyDenial; NeedsReview → ManualReview.  

---

📐 Visual Workflow
`mermaid
stateDiagram-v2
    [*] --> ValidateClaim
    ValidateClaim --> RouteDecision
    RouteDecision --> ApprovedBranch: Approved
    RouteDecision --> NotifyDenial: Denied
    RouteDecision --> ManualReview: NeedsReview
    RouteDecision --> ErrorHandler: Default

    state ApprovedBranch {
        [*] --> ProcessPayment
        [*] --> AuditLog
        ProcessPayment --> [*]
        AuditLog --> [*]
    }

    ApprovedBranch --> NotifyApproval
    NotifyApproval --> [*]
    NotifyDenial --> [*]
    ManualReview --> [*]
    ErrorHandler --> [*]
`

---

✅ Why This Matters
- Parallel State ensures compliance and business logic run together—no risk of skipping audit logging.  
- ResultPath preserves validation details for regulators.  
- OutputPath can trim payloads so payment and audit branches only see what they need.  
- This design supports governance, auditability, and resilience in regulated domains like healthcare and insurance.  

---

Would you like me to extend this further into a Map state example—where multiple claims are processed in batch, each going through validation and branching independently? That would show how Step Functions scales for high-volume workflows.