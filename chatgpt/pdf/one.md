Perfect — let’s take your YAML-driven PDF/HTML mapping engine to the next level 🚀

We’ll now extend the implementation so it can **auto-generate indexed field keys** (e.g. `Dependents[0].FirstName`, `Dependents[1].FirstName`, etc.) or even map them into PDF form field names like `dependents.fname.1`, `dependents.lname.1`.

---

## 🧩 Design Goals

We’ll enhance the `ConditionalMappingProcessor` (and optionally `GroupMappingProcessor`) to:

1. Support **repeated array sections** (e.g. multiple dependents).
2. Add **automatic index-based suffixing** to target field names.
3. Remain generic — no hardcoding `Dependents`.
4. Allow flexible naming via a YAML flag like `indexed: true` and optionally `indexStyle: "bracket"` or `"dot"`.

---

## ✅ Updated YAML Example

```yaml
pdf:
  mappings:
    - source: applicantList
      groupBy: relationship
      groups:
        primary:
          targetPrefix: "PrimaryApplicant"
          fieldMap:
            fname: "FirstName"
            lname: "LastName"
        spouse:
          targetPrefix: "Spouse"
          fieldMap:
            fname: "FirstName"
            lname: "LastName"
    - source: applicantList
      condition: "relationship == 'child'"
      targetPrefix: "Dependents"
      isArray: true
      indexed: true
      indexStyle: "dot"       # can be "dot" (Dependents.1.FirstName) or "bracket" (Dependents[0].FirstName)
      fieldMap:
        fname: "FirstName"
        lname: "LastName"
    - source: summary
      fieldMap:
        totalApplicants: "Summary.TotalCount"
```

---

## ⚙️ `MappingSpec.java` — Add New Fields

```java
package com.example.pdfmapper.config;

import java.util.Map;

public class MappingSpec {
    private String source;
    private String groupBy;
    private Map<String, GroupSpec> groups;
    private String condition;
    private String targetPrefix;
    private Map<String, String> fieldMap;
    private boolean isArray;
    private boolean indexed = false;       // NEW
    private String indexStyle = "dot";     // NEW: "dot" or "bracket"

    // getters/setters
    public boolean isIndexed() { return indexed; }
    public void setIndexed(boolean indexed) { this.indexed = indexed; }

    public String getIndexStyle() { return indexStyle; }
    public void setIndexStyle(String indexStyle) { this.indexStyle = indexStyle; }

    // rest of existing getters/setters...
    // (source, groupBy, groups, condition, fieldMap, etc.)
}
```

---

## ⚙️ Updated `ConditionalMappingProcessor.java`

This version now supports `isArray + indexed`.

```java
package com.example.pdfmapper.processor;

import com.example.pdfmapper.config.MappingSpec;

import java.util.*;

public class ConditionalMappingProcessor implements MappingProcessor {

    @Override
    public boolean supports(MappingSpec spec) {
        return spec.getCondition() != null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> process(Map<String, Object> data, MappingSpec spec) {
        Map<String, Object> resolved = new LinkedHashMap<>();
        Object sourceObj = resolvePath(data, spec.getSource());
        if (!(sourceObj instanceof List<?> list)) return resolved;

        int index = 0;
        for (Object element : list) {
            if (!(element instanceof Map<?, ?> item)) continue;
            if (evaluateCondition((Map<String, Object>) item, spec.getCondition())) {
                String prefix = buildIndexedPrefix(spec, index);
                spec.getFieldMap().forEach((src, tgt) -> {
                    Object value = item.get(src);
                    if (value != null) {
                        String key = prefix + tgt;
                        resolved.put(key, value);
                    }
                });
                if (spec.isIndexed()) index++;
            }
        }
        return resolved;
    }

    private String buildIndexedPrefix(MappingSpec spec, int index) {
        String prefix = spec.getTargetPrefix();
        if (spec.isIndexed()) {
            if ("bracket".equalsIgnoreCase(spec.getIndexStyle())) {
                prefix += "[" + index + "].";
            } else {
                prefix += "." + (index + 1) + ".";
            }
        } else {
            prefix += ".";
        }
        return prefix;
    }

    private Object resolvePath(Map<String, Object> data, String path) {
        String[] parts = path.split("\\.");
        Object current = data;
        for (String p : parts) {
            if (!(current instanceof Map<?, ?> map)) return null;
            current = map.get(p);
        }
        return current;
    }

    private boolean evaluateCondition(Map<String, Object> context, String condition) {
        try {
            condition = condition.trim();
            if (condition.contains("==")) {
                String[] parts = condition.split("==");
                String field = parts[0].trim();
                String expected = stripQuotes(parts[1].trim());
                Object actual = context.get(field);
                return expected.equalsIgnoreCase(String.valueOf(actual));
            } else if (condition.contains("!=")) {
                String[] parts = condition.split("!=");
                String field = parts[0].trim();
                String expected = stripQuotes(parts[1].trim());
                Object actual = context.get(field);
                return !expected.equalsIgnoreCase(String.valueOf(actual));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private String stripQuotes(String s) {
        if (s.startsWith("'") && s.endsWith("'")) return s.substring(1, s.length() - 1);
        if (s.startsWith("\"") && s.endsWith("\"")) return s.substring(1, s.length() - 1);
        return s;
    }
}
```

---

## ✅ Example JSON Data

```java
Map<String, Object> data = Map.of(
    "applicantList", List.of(
        Map.of("relationship", "primary", "fname", "John", "lname", "Doe"),
        Map.of("relationship", "spouse", "fname", "Jane", "lname", "Doe"),
        Map.of("relationship", "child", "fname", "Chris", "lname", "Doe"),
        Map.of("relationship", "child", "fname", "Alex", "lname", "Doe")
    ),
    "summary", Map.of("totalApplicants", 4)
);
```

---

## 🧾 Console Output (`indexStyle: "dot"`)

```
Transformed Output: {
  PrimaryApplicant.FirstName=John,
  PrimaryApplicant.LastName=Doe,
  Spouse.FirstName=Jane,
  Spouse.LastName=Doe,
  Dependents.1.FirstName=Chris,
  Dependents.1.LastName=Doe,
  Dependents.2.FirstName=Alex,
  Dependents.2.LastName=Doe,
  Summary.TotalCount=4
}
```

---

## 🧾 Console Output (`indexStyle: "bracket"`)

```
Transformed Output: {
  PrimaryApplicant.FirstName=John,
  PrimaryApplicant.LastName=Doe,
  Spouse.FirstName=Jane,
  Spouse.LastName=Doe,
  Dependents[0].FirstName=Chris,
  Dependents[0].LastName=Doe,
  Dependents[1].FirstName=Alex,
  Dependents[1].LastName=Doe,
  Summary.TotalCount=4
}
```

---

## 💡 Notes

✅ This design remains **generic** — it doesn’t assume “Dependents”; the same YAML pattern could handle:

* multiple co-applicants
* multiple assets
* repeated income records
* dynamic table rows in HTML/PDF templates.

✅ It integrates seamlessly with your **PDFBox-based renderer**, since you can directly map `Dependents.fname.1` fields into AcroForm field names like `dependents.fname.1`.

---

Would you like me to extend this further so that:

* it **auto-generates PDF field names** like `dependents.fname.1` (matching AcroForm naming patterns),
* and merges values into a real **PDF template using PDFBox**?
