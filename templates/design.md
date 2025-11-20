 **template variability** and **field reuse** across multiple PDF AcroForm templates in a healthcare enrollment context. The key goals you mentioned—avoiding duplication, reducing maintenance, and making the mapping specification simple—point toward a **modular, hierarchical design**.

Here’s a structured approach you can follow:

***

### ✅ **1. Define a Layered Mapping Specification**

Break the mapping into **three layers**:

*   **Common Layer**:  
    Contains mappings for fields that are shared across most templates (e.g., `MemberName`, `DOB`, `Address`).  
    This acts as the **base specification**.

*   **Market/State Layer**:  
    Adds or overrides mappings for fields specific to a market category or state (e.g., `MedicareID` for Medicare, `StateSpecificConsent` for certain states).

*   **Template-Specific Layer**:  
    Handles unique fields for a particular template (e.g., `EmployerGroupNumber` for a group enrollment form).

This hierarchy allows **inheritance and overrides**, similar to configuration layering.

***

### ✅ **2. Use a Declarative, JSON/YAML-Based Structure**

Represent mappings in a **declarative format** that supports:

*   **Inheritance**: A template can reference a base mapping and override specific fields.
*   **Overrides**: Explicitly mark fields that replace common ones.
*   **Optional Fields**: Indicate conditional presence.

Example in **YAML**:

```yaml
common:
  MemberName: source.member.name
  DOB: source.member.dob
  Address: source.member.address

market_specific:
  Medicare:
    MedicareID: source.member.medicare_id
  Medicaid:
    MedicaidID: source.member.medicaid_id

state_specific:
  CA:
    StateConsent: source.member.consent_ca

templates:
  EnrollmentForm_A:
    inherits: [common, Medicare, CA]
    overrides:
      Address: source.member.address_line1
    additional:
      EmployerGroupNumber: source.employer.group_number
```

***

### ✅ **3. Introduce a Mapping Engine**

Instead of hardcoding logic, create a **mapping engine** that:

*   Reads the layered specification.
*   Resolves inheritance and overrides.
*   Applies conditional logic (e.g., market = Medicare, state = CA).

This makes the system **data-driven**, reducing code changes when templates evolve.

***

### ✅ **4. Handle Field Variability with Metadata**

Add metadata for:

*   **Field type** (text, checkbox, date).
*   **Validation rules** (e.g., SSN format).
*   **Conditional visibility** (e.g., show only if `market = Medicare`).

This helps keep the mapping **self-descriptive** and easier to maintain.

***

### ✅ **5. Versioning & Governance**

Since healthcare enrollment forms change frequently:

*   Maintain **versioned mapping specs**.
*   Use **Git or a CMS** for governance.
*   Document **change history** for compliance.

***

### ✅ **6. Optional: Use a DSL or Schema**

If complexity grows, consider:

*   A **Domain-Specific Language (DSL)** for mappings.
*   Or a **schema-driven approach** (e.g., JSON Schema) to validate mappings.

***

#### **Benefits of This Approach**

*   **No duplication**: Common fields defined once.
*   **Easy overrides**: State/market-specific changes isolated.
*   **Scalable**: Adding a new template = referencing existing layers + minimal overrides.
*   **Readable**: YAML/JSON is human-friendly for business analysts.

***


