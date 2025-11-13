Excellent direction 👌 — you’re now thinking like a **platform designer**, not just implementing a one-off mapping.

Let’s step back and structure this in a truly **generic, schema-agnostic** way — so the `MappingEngine` can handle *any JSON input and YAML mapping config* without hardcoded fields like “primary”, “spouse”, etc.

We’ll do 3 things here:

---

## 🧭 1️⃣ Principles of the Generic Mapping Engine

Your `MappingEngine` should:

* Never assume domain-specific structures (like applicants, products, etc.)
* Only rely on:

  * **JSONata expressions** to extract or transform data
  * **YAML config structure** to organize mappings
* Be **hierarchical** — YAML defines groups, and groups can have:

  * context selectors (via JSONata)
  * repeatable sections
  * nested mappings

---

## 🧱 2️⃣ YAML Config — Flexible Variations

Here are 3 patterns you can use depending on your data and template structure.

---

### 🧩 **Variation 1: Simple direct mappings**

Best for small JSONs or flat documents.

```yaml
template:
  name: enrollment_form.pdf

mappings:
  applicantName:
    value: "$.applicant.demographic.name"
  applicantEmail:
    value: "$.applicant.demographic.email"
  policyStartDate:
    value: "$.policy.startDate"
  addressLine1:
    value: "$.applicant.addresses[type='home'].line1"
```

✅ Each entry defines a **target PDF field name** and a **JSONata expression**.

---

### 🧩 **Variation 2: Hierarchical / grouped mappings**

Useful when you want logical sections (like multiple form pages or groups).

```yaml
template:
  name: enrollment_form.pdf

sections:
  primaryApplicant:
    context: "$.applicants[relationshipType='Primary']"
    fields:
      name:
        value: "$.demographic.name"
      dob:
        value: "$.demographic.dob"
      homeAddress:
        value: "$.addresses[type='home'].line1"

  spouse:
    context: "$.applicants[relationshipType='Spouse']"
    fields:
      name:
        value: "$.demographic.name"

  dependents:
    context: "$.applicants[relationshipType='Dependent']"
    repeat: true
    limit: 3
    fields:
      name:
        value: "$.demographic.name"
      plan:
        value: "$.products[*].plans[*].planName"
```

✅ Each section can define:

* `context`: JSONata expression selecting its root
* `fields`: evaluated relative to that context
* `repeat`: if it’s an array
* `limit`: max rows per page (for pagination later)

---

### 🧩 **Variation 3: Fully nested contexts (deep hierarchies)**

Ideal for complex domain JSONs with repeating substructures (applicants → products → plans).

```yaml
template:
  name: complex_form.pdf

groups:
  applicants:
    context: "$.applicants"
    repeat: true
    limit: 5
    fields:
      applicantName:
        value: "$.demographic.name"
      relationship:
        value: "$.relationshipType"

    subgroups:
      products:
        context: "$.products"
        repeat: true
        fields:
          productType:
            value: "$.productType"
          planName:
            value: "$.plans[*].planName"
```

✅ Supports **multi-level repeat groups** and nested mappings.
Each group defines:

* `context` → JSONata selector
* `repeat` → whether it produces multiple PDF field sets
* `fields` → field definitions
* `subgroups` → optional nested context mappings

---

## ⚙️ 3️⃣ Generic MappingEngine Implementation (Context-Aware + Config-Driven)

Here’s a **truly generic** engine that works for all 3 YAML variants above.

```java
package com.example.mapping.engine;

import com.example.mapping.jsonata.JsonataEvaluator;
import com.example.mapping.jsonata.impl.CachedJsonataEvaluator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.File;
import java.util.*;

/**
 * Generic Mapping Engine - no hardcoded fields or domain logic.
 * YAML defines structure and JSONata defines data extraction.
 */
public class MappingEngine {

    private final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
    private final JsonataEvaluator evaluator;
    private final ObjectMapper jsonMapper = new ObjectMapper();

    public MappingEngine(JsonataEvaluator evaluator) {
        this.evaluator = evaluator;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> execute(JsonNode jsonSource, File yamlMappingFile) throws Exception {
        Map<String, Object> yamlConfig = yamlMapper.readValue(yamlMappingFile, Map.class);
        Map<String, Object> flattened = new LinkedHashMap<>();

        preloadExpressions(yamlConfig);

        if (yamlConfig.containsKey("mappings")) {
            processSimpleMappings((Map<String, Object>) yamlConfig.get("mappings"), jsonSource, flattened, "");
        } else if (yamlConfig.containsKey("sections")) {
            processSections((Map<String, Object>) yamlConfig.get("sections"), jsonSource, flattened, "");
        } else if (yamlConfig.containsKey("groups")) {
            processGroups((Map<String, Object>) yamlConfig.get("groups"), jsonSource, flattened, "");
        }

        return flattened;
    }

    /**
     * Simple direct mappings (Variation 1)
     */
    @SuppressWarnings("unchecked")
    private void processSimpleMappings(Map<String, Object> mappings, Object context, Map<String, Object> out, String prefix) throws Exception {
        for (var entry : mappings.entrySet()) {
            String field = entry.getKey();
            Object value = entry.getValue();
            if (value instanceof Map<?, ?> m && m.containsKey("value")) {
                Object val = evaluator.compile((String) m.get("value")).evaluate(context);
                out.put(prefix + field, val);
            }
        }
    }

    /**
     * Section-based mappings (Variation 2)
     */
    @SuppressWarnings("unchecked")
    private void processSections(Map<String, Object> sections, Object jsonSource, Map<String, Object> out, String prefix) throws Exception {
        for (var sectionEntry : sections.entrySet()) {
            String sectionName = sectionEntry.getKey();
            Map<String, Object> section = (Map<String, Object>) sectionEntry.getValue();

            Object context = jsonSource;
            if (section.containsKey("context")) {
                context = evaluator.compile((String) section.get("context")).evaluate(jsonSource);
            }

            boolean repeat = Boolean.TRUE.equals(section.get("repeat"));
            int limit = section.containsKey("limit") ? (int) section.get("limit") : Integer.MAX_VALUE;

            if (repeat && context instanceof List<?> list) {
                int index = 1;
                for (Object item : list) {
                    if (index > limit) break;
                    processFields((Map<String, Object>) section.get("fields"), item, out, prefix + sectionName + "." + index + ".");
                    index++;
                }
            } else {
                processFields((Map<String, Object>) section.get("fields"), context, out, prefix + sectionName + ".");
            }
        }
    }

    /**
     * Deep group mappings (Variation 3)
     */
    @SuppressWarnings("unchecked")
    private void processGroups(Map<String, Object> groups, Object jsonSource, Map<String, Object> out, String prefix) throws Exception {
        for (var groupEntry : groups.entrySet()) {
            String groupName = groupEntry.getKey();
            Map<String, Object> group = (Map<String, Object>) groupEntry.getValue();

            Object context = evaluator.compile((String) group.get("context")).evaluate(jsonSource);

            boolean repeat = Boolean.TRUE.equals(group.get("repeat"));
            int limit = group.containsKey("limit") ? (int) group.get("limit") : Integer.MAX_VALUE;

            if (repeat && context instanceof List<?> list) {
                int idx = 1;
                for (Object item : list) {
                    if (idx > limit) break;
                    processFields((Map<String, Object>) group.get("fields"), item, out, prefix + groupName + "." + idx + ".");
                    if (group.containsKey("subgroups")) {
                        processGroups((Map<String, Object>) group.get("subgroups"), item, out, prefix + groupName + "." + idx + ".");
                    }
                    idx++;
                }
            } else {
                processFields((Map<String, Object>) group.get("fields"), context, out, prefix + groupName + ".");
            }
        }
    }

    /**
     * Field processing
     */
    @SuppressWarnings("unchecked")
    private void processFields(Map<String, Object> fields, Object context, Map<String, Object> out, String prefix) throws Exception {
        for (var entry : fields.entrySet()) {
            String fieldName = entry.getKey();
            Object value = entry.getValue();
            if (value instanceof Map<?, ?> fieldMap && fieldMap.containsKey("value")) {
                Object result = evaluator.compile((String) fieldMap.get("value")).evaluate(context);
                if (result instanceof List<?> list) {
                    int i = 1;
                    for (Object v : list) out.put(prefix + fieldName + "." + i++, v);
                } else {
                    out.put(prefix + fieldName, result);
                }
            }
        }
    }

    /**
     * Cache JSONata expressions ahead of time.
     */
    @SuppressWarnings("unchecked")
    private void preloadExpressions(Map<String, Object> yaml) {
        List<String> expressions = new ArrayList<>();
        collectExpressions(yaml, expressions);
        if (evaluator instanceof CachedJsonataEvaluator cached) {
            cached.preload(expressions);
        }
    }

    private void collectExpressions(Object node, List<String> expressions) {
        if (node instanceof Map<?, ?> map) {
            map.forEach((k, v) -> {
                if ("value".equals(k) && v instanceof String s && s.contains("$")) expressions.add(s);
                else collectExpressions(v, expressions);
            });
        } else if (node instanceof Collection<?> list) {
            list.forEach(item -> collectExpressions(item, expressions));
        }
    }
}
```

---

## ✅ 4️⃣ Advantages of This Generic Design

| Feature                       | Description                                           |
| ----------------------------- | ----------------------------------------------------- |
| 🔧 **Schema-agnostic**        | No domain assumptions; works for *any JSON and YAML*. |
| ⚡ **Cached JSONata**          | All expressions are precompiled and reused.           |
| 🧩 **Hierarchical mapping**   | Supports simple → nested → multi-level mappings.      |
| 🔁 **Repeating arrays**       | Handles collections and suffixes automatically.       |
| 📄 **Ready for PDF filling**  | Output is a flat map of field names → values.         |
| 🔒 **Separation of concerns** | YAML defines structure; engine just interprets.       |

---

Would you like me to follow this with a **generic YAML schema guide** (like a mini DSL reference) that defines all possible keys (`context`, `fields`, `repeat`, `subgroups`, `limit`, etc.) — so you can design mapping specs systematically for multiple use cases (e.g., claims, enrollment, benefits, etc.)?
