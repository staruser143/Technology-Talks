Yes, it uses an LLM for that. Because you are providing a single, open-ended chat bar, human language is too unpredictable for traditional code (like RegEx or keyword matching) to categorize reliably.
A small, fast, and cost-effective model—such as Anthropic Claude 3.5 Haiku or Amazon Nova Micro via standard Amazon Bedrock—is perfect for this. It acts as an intelligent traffic controller.
------------------------------
## How the Classifier Works (The Mechanics)
When a user types a message, your application backend does not send it to your main workflow yet. Instead, it makes a rapid, single-turn API call (InvokeModel) to the classifier LLM.
You provide a strict System Prompt that trains the model to act as a pure classification engine. It analyzes the linguistic intent, extracts any necessary variables, and returns a structured JSON object instead of conversational text.
## The System Prompt Template

You are an advanced API router and intent classifier for a corporate assistant. 
Analyze the user's input and classify it into exactly one of these categories:

1. STATIC_KNOWLEDGE: The user is asking an informational question about company rules, guidelines, policies, or documentation. (e.g., "What is the holiday policy?")
2. LIVE_SYSTEM_API: The user is asking for specific, dynamic, or analytical data that resides in live systems, or wants to check statuses. (e.g., "What is the status of invoice #502?")

Do not include any conversational text, greetings, or explanations. 
Your response must be a single, valid JSON object following this schema:
{
  "category": "STATIC_KNOWLEDGE" | "LIVE_SYSTEM_API",
  "extracted_parameters": {
    "key": "value"
  }
}

------------------------------
## Real-World Processing Examples
Here is exactly how the LLM evaluates different inputs and how your backend code reacts to the JSON output.
## Example 1: The user types "How do I submit an expense report?"

* The LLM Classifier Outputs:

{
  "category": "STATIC_KNOWLEDGE",
  "extracted_parameters": {}
}

* Your Backend Routing Logic: Seeing STATIC_KNOWLEDGE, your application code triggers a standard Bedrock Knowledge Base query (RetrieveAndGenerate) against your static policy documents stored in Amazon S3.

## Example 2: The user types "Check if invoice INV-9902 has been paid yet."

* The LLM Classifier Outputs:

{
  "category": "LIVE_SYSTEM_API",
  "extracted_parameters": {
    "invoice_id": "INV-9902"
  }
}

* Your Backend Routing Logic: Seeing LIVE_SYSTEM_API, your application code bypasses the document storage entirely. It securely invokes your internal API endpoint (GET /api/v1/invoices/INV-9902). Once the API returns the database row as clean JSON, your backend passes that JSON back to Bedrock just to format it into a friendly sentence for the user ("Yes, invoice INV-9902 was paid on Friday.").

------------------------------
## Why Using an LLM Here is Highly Effective

   1. It Handles Synonyms Flawlessly: A user might say "Look up invoice 502", "Is 502 paid?", or "Track my bill #502". The LLM recognizes that all of these mean LIVE_SYSTEM_API and extracts 502 as the variable, whereas traditional code would require hundreds of messy rules to catch every variation.
   2. It Prevents System Overload: By intercepting the request upfront, you ensure your database-fronting APIs are only called when the user actually wants real-time data, protecting your production infrastructure.
   3. Speed and Cost: Because you are using a lightweight model (like Haiku or Nova Micro) and asking it to output only a few lines of JSON, the execution takes less than a second and costs fractions of a cent per request.

Would you like to see a sample Node.js or Python AWS Lambda function showing how to invoke Bedrock to get this JSON classification, or should we look at how to handle a user request that accidentally mixes both categories?

