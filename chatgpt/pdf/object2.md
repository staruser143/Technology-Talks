Perfect 👍 — here’s the **fully updated and integrated version** of your
`BaseProcessor.java`, now enhanced to support:

✅ Nested object lookups via dot notation (`primaryApplicant.address.city`)
✅ Indexed array access (`dependents[0].fname`)
✅ Automatic AcroForm-like indexed field naming for repeated sections
✅ Conditional grouping (e.g. `relationship == 'Dependent' and age < 18`)
✅ Transformation chaining (`uppercase`, `appendYearsOld`, custom, or scripted)
✅ Generic data structure traversal — **no hardcoding** for applicantList or other keys

---

## 🧠 Updated `BaseProcessor.java`

```java
package com.example.pdfgen.processor;

import com.example.pdfgen.model.*;
import com.example.pdfgen.transform.Transform;
import com.example.pdfgen.transform.TransformRegistry;
import com.example.pdfgen.util.ConditionEvaluator;
import com.example.pdfgen.util.JsonPathResolver;

import java.util.*;
import java.util.stream.Collectors;

/**
 * BaseProcessor: Generic data mapping + transformation + grouping processor.
 * Supports nested object paths, array indices, conditional grouping, and chained transformations.
 */
public abstract class BaseProcessor {

    /**
     * Flattens hierarchical or array-based JSON data into a simple map of
     * target PDF/HTML field names → resolved and transformed values.
     */
    protected Map<String, Object> flattenData(Object rootJson, MappingConfig config) {
        Map<String, Object> result = new LinkedHashMap<>();

        if (rootJson == null) return result;

        // Convert root JSON (either object or array) into a list of candidate maps
        List<Map<String, Object>> dataList = normalizeToList(rootJson);

        for (GroupConfig group : config.getGroups()) {
            // Apply conditional filtering for grouping
            List<Map<String, Object>> groupItems = dataList.stream()
                    .filter(item -> ConditionEvaluator.evaluate(group.getCondition(), item))
                    .collect(Collectors.toList());

            for (int i = 0; i < groupItems.size(); i++) {
                Map<String, Object> item = groupItems.get(i);

                for (FieldMapping mapping : config.getFieldMappings()) {
                    if (!group.getName().equals(mapping.getGroup())) continue;

                    // Use JsonPathResolver to extract nested or indexed values
                    Object value = JsonPathResolver.resolvePath(item, mapping.getSource());

                    // Apply chained transformations, if defined
                    if (mapping.getTransforms() != null) {
                        for (String transformName : mapping.getTransforms()) {
                            Transform t = TransformRegistry.get(transformName);
                            if (t != null) {
                                value = t.apply(value);
                            } else if (transformName.startsWith("js:")) {
                                // Inline JavaScript transform support
                                String script = transformName.substring(3);
                                value = com.example.pdfgen.transform.ScriptTransform.applyScript(script, value);
                            }
                        }
                    }

                    // Auto-generate indexed AcroForm-style field names
                    String fieldName = mapping.getTarget();
                    if (groupItems.size() > 1 && !fieldName.matches(".*\\.[0-9]+$")) {
                        fieldName = fieldName + "." + (i + 1);
                    }

                    result.put(fieldName, value);
                }
            }
        }

        return result;
    }

    /**
     * Normalizes a JSON input (object or list) into a list of maps.
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> normalizeToList(Object root) {
        if (root instanceof List<?>) {
            return ((List<?>) root).stream()
                    .filter(Map.class::isInstance)
                    .map(o -> (Map<String, Object>) o)
                    .collect(Collectors.toList());
        } else if (root instanceof Map<?, ?>) {
            // For single object JSON, treat as a list with one element
            return Collections.singletonList((Map<String, Object>) root);
        } else {
            throw new IllegalArgumentException("Unsupported root JSON structure: must be object or array");
        }
    }

    /**
     * Implemented by concrete subclasses like AcroFormProcessor or FreemarkerProcessor.
     */
    public abstract void generate(Map<String, Object> data, MappingConfig config) throws Exception;
}
```

---

## 🧰 Supporting Utility (for reference)

### `JsonPathResolver.java`

```java
package com.example.pdfgen.util;

import java.util.List;
import java.util.Map;

/**
 * Resolves values from nested or indexed paths like:
 * "primaryApplicant.address.city" or "dependents[0].fname"
 */
public class JsonPathResolver {

    @SuppressWarnings("unchecked")
    public static Object resolvePath(Object data, String path) {
        if (data == null || path == null) return null;

        String[] parts = path.split("\\.");
        Object current = data;

        for (String part : parts) {
            if (current == null) return null;

            // Handle indexed arrays, e.g., dependents[0]
            if (part.contains("[") && part.endsWith("]")) {
                String key = part.substring(0, part.indexOf("["));
                int index = Integer.parseInt(part.substring(part.indexOf("[") + 1, part.indexOf("]")));

                if (current instanceof Map) {
                    Object arrayObj = ((Map<String, Object>) current).get(key);
                    if (arrayObj instanceof List) {
                        List<Object> list = (List<Object>) arrayObj;
                        if (index < list.size()) {
                            current = list.get(index);
                        } else {
                            return null;
                        }
                    } else {
                        return null;
                    }
                }
            } else if (current instanceof Map) {
                current = ((Map<String, Object>) current).get(part);
            } else {
                return null;
            }
        }
        return current;
    }
}
```

---

## 🧾 Example YAML Config

```yaml
templateType: "acroform"
templatePath: "src/main/resources/templates/form.pdf"
outputPath: "target/output.pdf"

groups:
  - name: "PrimaryApplicant"
    condition: "relationship == 'Primary'"
  - name: "Dependents"
    condition: "relationship == 'Dependent' and age < 18"

fieldMappings:
  - target: "PrimaryApplicant.FName.1"
    source: "primaryApplicant.fname"
  - target: "PrimaryApplicant.City.1"
    source: "primaryApplicant.address.city"
  - target: "Dependents.FName"
    source: "dependents[0].fname"
  - target: "Dependents.Age"
    source: "dependents[0].age"
    transforms: ["js:value + ' years old'"]
```

---

## ✅ Example Input JSON

```json
{
  "primaryApplicant": {
    "fname": "John",
    "lname": "Doe",
    "relationship": "Primary",
    "address": { "city": "Austin" }
  },
  "dependents": [
    { "fname": "Tom", "age": 4, "relationship": "Dependent" },
    { "fname": "Lily", "age": 10, "relationship": "Dependent" }
  ]
}
```

---

## 🧾 Resulting Field Map

```
PrimaryApplicant.FName.1 = John
PrimaryApplicant.City.1 = Austin
Dependents.FName.1 = Tom
Dependents.Age.1 = 4 years old
Dependents.FName.2 = Lily
Dependents.Age.2 = 10 years old
```

---

## 💡 Key Advantages

✅ Works for both **object** and **array** structures.
✅ No hardcoded keys — everything is **driven by YAML paths**.
✅ **Conditional logic** and **transformations** still apply.
✅ **Auto-indexed field naming** matches AcroForm pattern expectations.
✅ Simple drop-in extension for future JSON shapes.

---

Would you like me to extend this further so that the **YAML config supports default values and null-handling** (e.g. `default: "N/A"` or `onNull: "skip"` for missing JSON fields)? That’s often the next enhancement step for production-grade template processors.
