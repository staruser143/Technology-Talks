The fundamental distinction is that TypedDict is only an editor hint (it does zero checks when code runs), whereas Pydantic actively intercepts, checks, and coerces data at runtime.
Three concrete scenarios show when Pydantic is recommended over TypedDict.
Scenario 1: Guarding Untrusted or User-Supplied Web Inputs
In an API (like FastAPI or Flask) receiving data directly from web forms, webhooks, or end users, TypedDict cannot prevent garbage or malformed values from breaking downstream nodes.
The Risk with TypedDict
from typing import TypedDict

class OrderState(TypedDict):
    customer_email: str
    quantity: int
    discount_code: str

# If a user submits a string for quantity or a broken email:
incoming_payload = {"customer_email": "not-an-email", "quantity": "three", "discount_code": ""}

# TypedDict runs without complaint:
state: OrderState = incoming_payload 

# Downstream node crashes later in the workflow:
total = state["quantity"] * 25  # TypeError: can't multiply sequence by non-int of type 'str'

Why Pydantic is Recommended
Pydantic validates fields at the graph's entry point, sanitizes inputs, and raises an immediate, readable validation error before broken state spreads through the workflow.
from pydantic import BaseModel, EmailStr, Field, field_validator

class OrderState(BaseModel):
    customer_email: EmailStr                    # Rejects 'not-an-email' immediately
    quantity: int = Field(gt=0, le=100)        # Ensures 1 <= quantity <= 100
    discount_code: str = ""

    @field_validator("discount_code")
    def clean_discount_code(cls, v: str) -> str:
        return v.strip().upper()                # Automatically normalizes ' promo20 ' -> 'PROMO20'

# Passing invalid data halts execution immediately at START with a clear ValidationError

Scenario 2: Structured LLM Output & Self-Correction Loops
When an LLM extracts structured data from unstructured text (such as invoices, medical records, or support tickets), it might produce missing keys, out-of-range scores, or hallucinated date formats.
Why Pydantic is Recommended
With Pydantic, if the LLM's structured output violates business constraints, the validation failure can trigger a self-correction loop in LangGraph.
from pydantic import BaseModel, Field
from langgraph.graph import StateGraph, START, END

class CustomerLeadState(BaseModel):
    lead_name: str
    lead_score: float = Field(ge=0.0, le=1.0)  # Must be a normalized confidence between 0 and 1
    phone_number: str
    is_valid: bool = False
    validation_error: str = ""

def validate_extraction(state: CustomerLeadState) -> dict:
    # If the LLM returned lead_score=85 instead of 0.85, 
    # Pydantic catches it immediately, letting you route back to an LLM retry node
    try:
        CustomerLeadState.model_validate(state)
        return {"is_valid": True}
    except Exception as e:
        return {"is_valid": False, "validation_error": str(e)}

# Dynamic router based on validation state
def route_lead(state: CustomerLeadState) -> str:
    return "save_to_crm" if state.is_valid else "retry_llm_extraction"

Scenario 3: Cross-Field Business Logic & Dynamic Defaults
TypedDict cannot compare two fields against each other or compute dynamic values. If your state depends on relationships between fields, Pydantic handles this using model validators.
Why Pydantic is Recommended
from datetime import date
from pydantic import BaseModel, model_validator

class TravelBookingState(BaseModel):
    start_date: date
    end_date: date
    travelers_count: int = Field(default=1, ge=1)
    
    @model_validator(mode="after")
    def check_dates(self):
        if self.end_date < self.start_date:
            raise ValueError("end_date cannot be earlier than start_date")
        return self

If an agent or user attempts to schedule a return date before the departure date, Pydantic prevents that state snapshot from saving to the checkpointer.
Summary Rule
 * Stick with TypedDict for internal agent orchestrations, multi-turn chat tracking (messages: Annotated[list, add_messages]), and internal intermediate variables where performance and simplicity come first.
 * Switch to Pydantic at system boundaries:
   * Entry point inputs received from web users, webhooks, or third-party APIs.
   * Structured extractions produced by LLMs that must satisfy strict numerical or formatting bounds.
   * Workflows where data validity directly determines conditional branching (e.g., valid \rightarrow execute; invalid \rightarrow retry).
