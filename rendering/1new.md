To build a robust, declarative PDF engine, the goal is to decouple the **data orchestration** from the **visual layout**. Since you are using FreeMarker, your YAML specification should act as a "Controller" that tells the engine which data to fetch, which templates to load, and how to sequence them.

Here is a proposed structure for your declarative YAML specification.

---

## 1. The Core Architecture

The YAML should define a `DocumentManifest`. This manifest maps logical sections of your document to specific templates and data scopes.

### Proposed YAML Schema

```yaml
document_id: "invoice-service-v1"
output_format: "pdf"

# Global metadata shared across all pages
metadata:
  title: "Client Invoice"
  author: "GenService"

# Define reusable components/fragments
templates:
  header: "shared/header.ftl"
  footer: "shared/footer.ftl"

# The sequence of pages or sections
pages:
  - id: "cover_page"
    template: "covers/minimal_cover.ftl"
    condition: "request.includeCover == true" # Optional logic
    data_mapping:
      title: "$.order.title"
      date: "$.generatedAt"

  - id: "details_table"
    template: "tables/dynamic_grid.ftl"
    data_mapping:
      items: "$.order.lineItems"
      currency: "$.billing.currency"
    rendering_rules:
      break_on_overflow: true
      repeat_header: true

  - id: "terms_and_conditions"
    template: "legal/standard_tc.ftl"
    data_mapping:
      client_name: "$.customer.name"

```

---

## 2. Key Components of the Specification

### A. Data Mapping (JSONPath)

Instead of passing the entire request blob to every FreeMarker template, use the YAML to map specific parts of your request JSON to template variables.

* **Benefit:** Your FreeMarker templates stay "dumb" and generic. You can reuse a `table.ftl` for both "Invoice Items" and "Shipping Logs" just by changing the `data_mapping` in YAML.

### B. Conditional Rendering

Clients often need optional pages (e.g., a "Glossary" page only if certain terms are used).

* **Implementation:** Add a `condition` key that evaluates a boolean expression or checks for the existence of data before including that page in the PDF assembly.

### C. Layout Orchestration

Since PDF generation often involves merging pages or handling overflows:

* **Directives:** Include keys like `orientation` (portrait/landscape) or `margin_overrides` within the page definition to control the PDF library (like iText or OpenHTMLtoPDF) during the render phase.

---

## 3. How the Engine Processes the YAML

To make this work, your service should follow this execution flow:

1. **Parse Manifest:** Load the YAML and validate the client’s request data.
2. **Context Building:** For each `page` in the YAML, execute the `data_mapping` (using a tool like Jayway JsonPath) to create a specific data model for that template.
3. **Template Merging:** Pass the mapped data into the FreeMarker engine with the specified `.ftl` file.
4. **Aggregation:** If the document consists of multiple templates, render them to HTML/XSL-FO fragments and then use your PDF library to concatenate them into a single document.

---

## 4. Example: Generic Table Template

By using this approach, a single FreeMarker template can be extremely flexible:

**`dynamic_grid.ftl` (Generic):**

```html
<table>
  <thead>
    <tr><#list columns as col><th>${col}</th></#list></tr>
  </thead>
  <tbody>
    <#list rows as row>
      <tr><#list row as cell><td>${cell}</td></#list></tr>
    </#list>
  </tbody>
</table>

```

**YAML Mapping for Client A:**

```yaml
template: "dynamic_grid.ftl"
data_mapping:
  columns: ["Product", "Price"]
  rows: "$.items[*].['name', 'amount']"

```

---

### Suggested Next Step

Would you like me to create a **Java/Kotlin pseudocode example** showing how to parse this YAML and map the JSONPath data to a FreeMarker `TemplateModel`?