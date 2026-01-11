This is a **classic multi-tenant document-generation architecture problem**, and you are right to pause here.
If you do not draw the boundary correctly, the service will either become **over-coupled to client logic** or **impossible to extend safely**.

Below is a **proven, enterprise-grade approach** that preserves:

* A single document-generation service
* Client-specific rendering behavior
* Strong isolation between tenants
* Clean ViewModel usage

---

## 1. First Principles (Non-Negotiable)

1. **The document service must NOT depend on client code**
2. **Clients must NOT ship Java classes into the service**
3. **Rendering variability must be declarative, not imperative**
4. **The service owns the rendering engine and ViewModel contract**
5. **Clients own configuration, not execution**

This leads directly to a **Plugin-by-Configuration architecture**.

---

## 2. Correct Responsibility Split

### Document Generation Service (Core Platform)

Owns:

* Rendering engine (FreeMarker / PDFBox / AcroForm)
* ViewModel contracts (stable, versioned)
* ViewModel builders (generic, config-driven)
* Security, validation, execution sandbox

### Client Applications (Tenants)

Own:

* Mapping specifications (YAML / JSON)
* Template selection
* Conditional rendering rules
* Column definitions
* Layout variants

Clients do **not** own Java code inside the service.

---

## 3. High-Level Architecture

```
Client App
   └── Mapping Spec (JSON/YAML)
        └── POST /generate

Document Service
   ├── Spec Parser
   ├── ViewModel Factory
   ├── Data Extractors
   ├── Renderers
   └── PDF Engine
```

---

## 4. Key Abstraction: ViewModel = Stable + Generic

The service exposes **one canonical ViewModel schema**.

Example (simplified):

```json
{
  "document": {
    "sections": [
      {
        "type": "table",
        "id": "coverageTable",
        "columns": [...],
        "rows": [...]
      }
    ]
  }
}
```

Clients never provide Java classes.
They provide **instructions** to build this structure.

---

## 5. Mapping Specification (Client-Owned)

### Example YAML

```yaml
document:
  sections:
    - type: table
      id: coverageTable
      visibleWhen: "$.hasCoverage == true"

      rowSource:
        type: path
        path: "coverages"

      columns:
        - name: coverageType
          label: Coverage
          field: "type"

        - name: benefits
          label: Benefits
          nestedTable:
            rowSource:
              type: path
              path: "benefits"
            columns:
              - name: code
                label: Code
                field: "code"
              - name: desc
                label: Description
                field: "description"
```

This is **client-specific** and fully declarative.

---

## 6. ViewModel Builder Registry (Inside the Service)

```java
public interface ViewModelBuilder {
    RenderNode build(BuildContext context, SectionSpec spec);
}
```

Registry:

```java
Map<String, ViewModelBuilder> builders = Map.of(
    "table", new TableViewModelBuilder(),
    "text", new TextViewModelBuilder()
);
```

The client selects **types**, not implementations.

---

## 7. Build Context (Execution Boundary)

```java
public class BuildContext {
    private Object requestData;
    private ExpressionEvaluator evaluator;
    private FieldExtractor extractor;
}
```

Clients never access this directly.

---

## 8. Conditional Rendering Without Client Code

```yaml
visibleWhen: "$.applicant.age > 18"
```

Internally:

```java
if (!context.evaluator.evaluate(spec.getVisibleWhen(), data)) {
    return null;
}
```

This supports:

* Per-client conditions
* Per-request variation
* No Java branching

---

## 9. AcroForm vs FreeMarker Unified Model

### AcroForm

* ViewModel → field-value map
* Table rows → page duplication
* Repeated sections → addendum pages

### FreeMarker

* ViewModel → render tree
* Tables → HTML
* Nested tables → recursive rendering

**Same ViewModel. Different renderer.**

---

## 10. How Client-Specific Logic Is Expressed

| Requirement          | Where               |
| -------------------- | ------------------- |
| Conditional sections | Mapping spec        |
| Different layouts    | Different templates |
| Different columns    | Mapping spec        |
| Different grouping   | Mapping spec        |
| Field formatting     | Mapping spec        |
| Page breaks          | Mapping spec        |

No Java branching per client.

---

## 11. Versioning Strategy (Critical)

```yaml
specVersion: 1.2
templateVersion: 3
viewModelVersion: 1
```

The service:

* Validates compatibility
* Rejects invalid specs early
* Allows safe evolution

---

## 12. Deployment Model Options

### Option A — Central Service (Most Common)

* One service
* Multi-tenant specs
* Strong validation

### Option B — Client-Specific Config Bundles

* Clients publish spec bundles
* Service loads per client
* No code deployment

### Option C — Sidecar Renderer (Advanced)

* Same engine
* Different specs per deployment
* Rarely needed

---

## 13. Why NOT Client-Specific Java Builders

| Problem                | Impact                   |
| ---------------------- | ------------------------ |
| Classpath pollution    | High risk                |
| Security               | Arbitrary code execution |
| Version conflicts      | Frequent outages         |
| Operational complexity | Unmanageable             |

Declarative specs avoid all of this.

---

## 14. Mental Model

```
Client = "What"
Service = "How"
```

Clients say:

> “Render this table when X is true, using these columns.”

Service decides:

> “How to evaluate, extract, build, and render.”

---

## 15. Answering Your Core Question Directly

> **How can different view model objects and builders from client apps be used by the service endpoint?**

**They should not be.**

Instead:

* The service exposes a **fixed, extensible ViewModel schema**
* Clients supply **mapping specifications**
* The service interprets specs using **internal builders**
* Rendering differences are entirely data-driven

This is the only approach that scales beyond 2–3 clients.

---

## 16. When Would Client Code Be Acceptable?

Only if:

* You run **one service per client**
* You fully trust the client
* You accept operational coupling

Even then, configuration-first is usually better.

---

## 17. Recommended Next Steps

If you want, I can:

1. Define a **formal JSON Schema** for the mapping spec
2. Show **AcroForm + FreeMarker rendering from the same ViewModel**
3. Provide a **plugin-safe extension model** (no arbitrary code)
4. Design **error reporting & validation feedback**
5. Walk through a **full request → PDF lifecycle**

Tell me which one you want next.
