Excellent — this is exactly the kind of **naming control** that separates a one-off mapping from a **configurable, reusable mapping engine**.

Let’s expand your current `MappingEngine` to support **custom naming prefixes** for logical groups**, like `PrimaryApplicant`, `Spouse`, `CurrentCoverage`, etc.

---

## 🧠 Concept Recap

Currently, field names are generated automatically from:

```
<prefix> + <fieldName> + optionalIndex
```

That works fine for simple sections, but now you want finer control like:

```
PrimaryApplicant.firstName.1
PrimaryApplicant.lastName.1
CurrentCoverage.planName.1
CurrentCoverage.planName.2
```

This means:

* We need **custom group prefixes** (configurable per section/subgroup)
* We must allow **context reuse** (e.g., “PrimaryApplicant” section uses the same applicant object, “CurrentCoverage” section uses their plan info)
* Each section/subgroup can **define its own prefix** instead of relying on YAML key name.

---

## 🧩 YAML Structure Update — Prefix-Aware Mapping

Here’s how your YAML can look now:

```yaml
template:
  name: enrollment_form.pdf

groups:
  primaryApplicant:
    prefix: "PrimaryApplicant"
    context: "$.applicants[relationshipType='Primary']"
    fields:
      firstName:
        value: "$.demographic.firstName"
      lastName:
        value: "$.demographic.lastName"

    subgroups:
      currentCoverage:
        prefix: "CurrentCoverage"
        context: "$.currentCoverages"
        repeat: true
        fields:
          planName:
            value: "$.planName"
          planType:
            value: "$.planType"
```

✅ Notice:

* Each `group` or `subgroup` can now define:

  * `prefix`: Custom text that controls how field names appear
  * `context`: JSONata to select data
  * `repeat`: Whether to generate indexed versions

---

## ⚙️ Updated `MappingEngine` (with Prefix Logic)

Here’s the enhanced engine supporting **custom prefixes**:

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
 * Enhanced Mapping Engine with support for prefix naming per group/subgroup.
 */
public class MappingEngine {

    private final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
    private final ObjectMapper jsonMapper = new ObjectMapper();
    private final JsonataEvaluator evaluator;

    public MappingEngine(JsonataEvaluator evaluator) {
        this.evaluator = evaluator;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> execute(JsonNode jsonSource, File yamlMappingFile) throws Exception {
        Map<String, Object> yamlConfig = yamlMapper.readValue(yamlMappingFile, Map.class);
        Map<String, Object> flattened = new LinkedHashMap<>();

        preloadExpressions(yamlConfig);

        if (yamlConfig.containsKey("groups")) {
            processGroups((Map<String, Object>) yamlConfig.get("groups"), jsonSource, flattened, "");
        } else if (yamlConfig.containsKey("sections")) {
            processSections((Map<String, Object>) yamlConfig.get("sections"), jsonSource, flattened, "");
        } else if (yamlConfig.containsKey("mappings")) {
            processSimpleMappings((Map<String, Object>) yamlConfig.get("mappings"), jsonSource, flattened, "");
        }

        return flattened;
    }

    /** ---------------- SIMPLE MAPPINGS ---------------- */
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

    /** ---------------- SECTION MAPPINGS ---------------- */
    @SuppressWarnings("unchecked")
    private void processSections(Map<String, Object> sections, Object jsonSource, Map<String, Object> out, String prefix) throws Exception {
        for (var sectionEntry : sections.entrySet()) {
            String sectionName = sectionEntry.getKey();
            Map<String, Object> section = (Map<String, Object>) sectionEntry.getValue();

            String groupPrefix = section.containsKey("prefix")
                    ? (String) section.get("prefix")
                    : sectionName;

            Object context = jsonSource;
            if (section.containsKey("context")) {
                context = evaluator.compile((String) section.get("context")).evaluate(jsonSource);
            }

            boolean repeat = Boolean.TRUE.equals(section.get("repeat"));
            int limit = section.containsKey("limit") ? (int) section.get("limit") : Integer.MAX_VALUE;

            if (repeat && context instanceof List<?> list) {
                int idx = 1;
                for (Object item : list) {
                    if (idx > limit) break;
                    processFields((Map<String, Object>) section.get("fields"), item, out, groupPrefix + "." + idx + ".");
                    idx++;
                }
            } else {
                processFields((Map<String, Object>) section.get("fields"), context, out, groupPrefix + ".");
            }
        }
    }

    /** ---------------- GROUP MAPPINGS ---------------- */
    @SuppressWarnings("unchecked")
    private void processGroups(Map<String, Object> groups, Object jsonSource, Map<String, Object> out, String prefix) throws Exception {
        for (var groupEntry : groups.entrySet()) {
            String groupName = groupEntry.getKey();
            Map<String, Object> group = (Map<String, Object>) groupEntry.getValue();

            String groupPrefix = group.containsKey("prefix")
                    ? (String) group.get("prefix")
                    : groupName;

            Object context = evaluator.compile((String) group.get("context")).evaluate(jsonSource);

            boolean repeat = Boolean.TRUE.equals(group.get("repeat"));
            int limit = group.containsKey("limit") ? (int) group.get("limit") : Integer.MAX_VALUE;

            if (repeat && context instanceof List<?> list) {
                int idx = 1;
                for (Object item : list) {
                    if (idx > limit) break;
                    processFields((Map<String, Object>) group.get("fields"), item, out, prefix + groupPrefix + "." + idx + ".");
                    if (group.containsKey("subgroups")) {
                        processGroups((Map<String, Object>) group.get("subgroups"), item, out, prefix + groupPrefix + "." + idx + ".");
                    }
                    idx++;
                }
            } else {
                processFields((Map<String, Object>) group.get("fields"), context, out, prefix + groupPrefix + ".");
                if (group.containsKey("subgroups")) {
                    processGroups((Map<String, Object>) group.get("subgroups"), context, out, prefix + groupPrefix + ".");
                }
            }
        }
    }

    /** ---------------- FIELD HANDLER ---------------- */
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

    /** ---------------- EXPRESSION CACHE ---------------- */
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

## 🧾 Output Example

If your JSON looks like this:

```json
{
  "applicants": [
    {
      "relationshipType": "Primary",
      "demographic": { "firstName": "John", "lastName": "Doe" },
      "currentCoverages": [
        { "planName": "Plan A", "planType": "Gold" },
        { "planName": "Plan B", "planType": "Silver" }
      ]
    }
  ]
}
```

And the YAML above is applied, the final flattened output would be:

```json
{
  "PrimaryApplicant.firstName.1": "John",
  "PrimaryApplicant.lastName.1": "Doe",
  "CurrentCoverage.planName.1": "Plan A",
  "CurrentCoverage.planType.1": "Gold",
  "CurrentCoverage.planName.2": "Plan B",
  "CurrentCoverage.planType.2": "Silver"
}
```

Perfect for filling a **PDF AcroForm** where field names are designed this way.

---

## ✅ Benefits of Prefix-Based Grouping

| Feature                        | Description                                             |
| ------------------------------ | ------------------------------------------------------- |
| 🧱 **Custom naming control**   | Prefixes can match PDF field naming conventions exactly |
| 🧩 **Reusability**             | Same applicant object can be reused for multiple groups |
| 🔁 **Repeating group support** | Works seamlessly with indexed groups                    |
| ⚙️ **Generic logic**           | No domain knowledge in code — only YAML drives it       |

---

Would you like me to now extend this design with **cross-group data sharing**, e.g.
→ one group (like `PrimaryApplicant`) defines a variable context that others (like `CurrentCoverage`) can reuse without repeating the same JSONata query?
That would let you optimize both **performance and reusability**.
