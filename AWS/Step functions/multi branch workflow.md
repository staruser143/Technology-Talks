Here’s a multi-branch healthcare claim workflow using AWS Step Functions, showing how ResultPath and OutputPath combine with Choice states to handle Approved, Denied, and Needs Review outcomes.  

---

🏥 JSON Workflow Snippet
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
          "Next": "AdjudicateClaim"
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
    "AdjudicateClaim": {
      "Type": "Task",
      "Resource": "arn:aws:lambda:region:account:function:AdjudicateClaim",
      "OutputPath": "$.validationResult.decision",
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

🔎 Step-by-Step Flow
1. ValidateClaim  
   - Runs a Lambda to check claim rules.  
   - Result stored in $.validationResult.  

2. RouteDecision (Choice State)  
   - Branches based on decision:  
     - "Approved" → AdjudicateClaim  
     - "Denied" → NotifyDenial  
     - "NeedsReview" → ManualReview  
     - Anything else → Fail  

3. AdjudicateClaim  
   - Uses OutputPath to pass only the "Approved" decision downstream.  

4. NotifyDenial  
   - Sends denial notification.  

5. ManualReview  
   - Places claim in human review queue.  

---

📐 Visual Workflow
`mermaid
stateDiagram-v2
    [*] --> ValidateClaim
    ValidateClaim --> RouteDecision
    RouteDecision --> AdjudicateClaim: Approved
    RouteDecision --> NotifyDenial: Denied
    RouteDecision --> ManualReview: NeedsReview
    RouteDecision --> ErrorHandler: Default
    AdjudicateClaim --> [*]
    NotifyDenial --> [*]
    ManualReview --> [*]
    ErrorHandler --> [*]
`

---

✅ Why This Matters
- ResultPath ensures validation details are preserved for audit.  
- Choice State enforces deterministic branching.  
- OutputPath trims payloads so downstream states only see what they need.  
- Fail State guarantees compliance by catching unexpected outcomes.  

---

Would you like me to extend this into a nested workflow—for example, Approved claims go into a Parallel state that simultaneously triggers payment processing and audit logging? That would show how Step Functions handles concurrent regulated tasks.