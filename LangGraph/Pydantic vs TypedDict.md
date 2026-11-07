Pydantic is a data validation and settings management library for Python that uses standard type annotations to enforce runtime validation, data coercion, and serialization.
While TypedDict is purely a static type-hinting construct (used by linters and IDEs to suggest keys), Pydantic operates at runtime: it parses incoming inputs, converts data types when possible, rejects invalid data, and provides methods like .model_dump() to export dictionaries.
In LangGraph, both can serve as the schema for your graph state.
Key Differences: TypedDict vs. Pydantic
| Feature | TypedDict | Pydantic (BaseModel) |
|---|---|---|
| Validation Timing | Static analysis only (IDE / mypy). No enforcement at runtime. | Runtime validation every time an instance or update is created. |
| Type Coercion | None. Passing a string "42" to an int field remains a string. | Automatic coercion. Converts "42" to 42 if valid. |
| Custom Validators | None. | Rich validation using @field_validator and @model_validator. |
| Performance Overhead | Zero runtime overhead (it is just a standard Python dict). | Minimal parsing and validation overhead. |
| Serialization | Standard dictionary operations. | Built-in .model_dump(), .model_dump_json(). |
| LangGraph Usage | Access values via dict syntax: state["key"]. | Access values via attribute syntax: state.key. |
When to Use TypedDict
Use TypedDict when you want a simple, high-performance graph with zero extra overhead:
 * Standard LLM & Agent Workflows: For typical chat loops, conversational agents, and tool nodes where nodes return standard dictionaries, TypedDict is the community default and cleanest option.
 * Maximum Performance / Low Overhead: Since TypedDict instances are plain Python dictionaries at runtime, there is no validation cost on every single node execution.
 * Simplicity: If you already validate data before it enters the workflow and only need type hints for IDE autocompletion.
from typing import TypedDict

class SimpleState(TypedDict):
    query: str
    retry_count: int

When to Use Pydantic
Use Pydantic when your graph handles unstructured, external, or untrusted data that must satisfy strict constraints before downstream nodes execute:
 * Validating External or User Input: If data comes directly from an external API, webhook, or form submit and needs schema enforcement (e.g., regex checks, required string lengths, numeric ranges).
 * Guarding LLM Outputs (Structured Extraction): When an LLM generates structured output or JSON, a Pydantic state model guarantees that fields meet validation rules before passing to downstream nodes.
 * Custom Field & Cross-Field Validation: When specific business logic must hold across state variables (e.g., ensuring end_date > start_date or formatting URLs).
 * Default Values & Value Coercion: If you want fields to automatically initialize default values or coerce compatible types (e.g., parsing a ISO datetime string into a Python datetime object).
from pydantic import BaseModel, Field, field_validator

class ValidatedState(BaseModel):
    user_id: str
    score: float = Field(ge=0.0, le=100.0)  # Must be between 0 and 100
    email: str

    @field_validator("email")
    def validate_email(cls, v):
        if "@" not in v:
            raise ValueError("Invalid email format")
        return v.lower()

Practical Rule of Thumb
 * Default to TypedDict for internal graph state orchestration, standard ReAct agents, and chat history.
 * Switch to Pydantic when you need runtime boundaries, such as validating raw user inputs at START, validating LLM tool arguments, or enforcing structured outputs.
