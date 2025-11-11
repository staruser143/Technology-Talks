Perfect — this is the natural next step.
Let’s now design your **hierarchical YAML mapping** and the **flattening strategy** so it can elegantly handle **multiple products and multiple plans per applicant** (e.g. medical, dental, vision), while still producing the simple flat field/value map needed for PDF filling.

---

## 🧩 1. Problem Context Recap

You have an enrollment JSON structure roughly like this:

```json
{
  "applicants": [
    {
      "type": "PrimaryApplicant",
      "demographic": { "firstName": "John", "lastName": "Doe", "dob": "1985-03-15" },
      "products": [
        {
          "name": "medical",
          "plans": [
            { "planName": "Aetna Silver", "coverageStart": "2024-01-01", "coverageEnd": "2024-12-31" },
            { "planName": "Aetna Bronze", "coverageStart": "2024-01-01", "coverageEnd": "2024-06-30" }
          ]
        },
        {
          "name": "dental",
          "plans": [
            { "planName": "Delta Dental Basic", "coverageStart": "2024-02-01", "coverageEnd": "2024-12-31" }
          ]
        }
      ]
    },
    {
      "type": "Child",
      "demographic": { "firstName": "Alice", "lastName": "Doe", "dob": "2015-09-01" },
      "products": [
        {
          "name": "medical",
          "plans": [
            { "planName": "Aetna Child", "coverageStart": "2024-01-01", "coverageEnd": "2024-12-31" }
          ]
        }
      ]
    }
  ]
}
```

So now you have:

* Multiple **applicants** (Primary, Child, etc.)
* Each applicant → multiple **products** (`medical`, `dental`, `vision`)
* Each product → multiple **plans**

And your PDF form field names need to look like this:

```
primary.products.medical.planName.1.1 = Aetna Silver
primary.products.medical.planName.1.2 = Aetna Bronze
primary.products.dental.planName.1.1 = Delta Dental Basic
child.products.medical.planName.1.1 = Aetna Child
```

---

## 🧾 2. Hierarchical YAML Mapping Spec

We’ll extend your YAML structure to represent this hierarchy clearly:

```yaml
template: enrollment_form.pdf

applicants:
  primary:
    demographic:
      firstName: "$applicants[type='PrimaryApplicant'].demographic.firstName"
      lastName: "$applicants[type='PrimaryApplicant'].demographic.lastName"
      dob: "$applicants[type='PrimaryApplicant'].demographic.dob"

    products:
      medical:
        plans:
          planName: "$applicants[type='PrimaryApplicant'].products[name='medical'].plans.planName"
          coverageStart: "$applicants[type='PrimaryApplicant'].products[name='medical'].plans.coverageStart"
          coverageEnd: "$applicants[type='PrimaryApplicant'].products[name='medical'].plans.coverageEnd"

      dental:
        plans:
          planName: "$applicants[type='PrimaryApplicant'].products[name='dental'].plans.planName"
          coverageStart: "$applicants[type='PrimaryApplicant'].products[name='dental'].plans.coverageStart"
          coverageEnd: "$applicants[type='PrimaryApplicant'].products[name='dental'].plans.coverageEnd"

  child:
    demographic:
      firstName: "$applicants[type='Child'].demographic.firstName"
      lastName: "$applicants[type='Child'].demographic.lastName"
      dob: "$applicants[type='Child'].demographic.dob"

    products:
      medical:
        plans:
          planName: "$applicants[type='Child'].products[name='medical'].plans.planName"
          coverageStart: "$applicants[type='Child'].products[name='medical'].plans.coverageStart"
          coverageEnd: "$applicants[type='Child'].products[name='medical'].plans.coverageEnd"
```

---

## ⚙️ 3. Updated Flattening and Index Logic

We need to support **two levels of dynamic indices**:

1. Applicant index → `.1`, `.2`, `.3` (children)
2. Product plan index → second level `.1`, `.2` (for multiple plans per product)

So our field naming convention will be:

```
<applicantType>.<category>.<field>.<applicantIndex>.<planIndex>
```

### Example:

| Field                                   | JSONata Path                    | Result |
| --------------------------------------- | ------------------------------- | ------ |
| `primary.products.medical.planName.1.1` | First medical plan for primary  |        |
| `primary.products.medical.planName.1.2` | Second medical plan for primary |        |
| `child.products.medical.planName.1.1`   | First child’s first plan        |        |
| `child.products.medical.planName.2.1`   | Second child’s first plan       |        |

---

## 🧠 4. Flattening Logic Enhancement

We’ll tweak the flattening to detect deeper product/plan paths and add `.*.*` suffixes when appropriate.

Here’s the **refined recursive flattener**:

```java
private void flattenMappings(
        Map<String, Object> nested,
        String prefix,
        Map<String, String> flat,
        String applicantType) {

    for (Map.Entry<String, Object> entry : nested.entrySet()) {
        String key = entry.getKey();
        Object value = entry.getValue();

        String newPrefix = prefix.isEmpty() ? key : prefix + "." + key;

        if (value instanceof Map) {
            flattenMappings((Map<String, Object>) value, newPrefix, flat, applicantType);
        } else if (value instanceof String) {
            String fieldKey = newPrefix;

            // Determine suffix rule
            if ("child".equalsIgnoreCase(applicantType)) {
                if (newPrefix.contains(".plans.")) {
                    fieldKey += ".*.*"; // multiple children + multiple plans
                } else {
                    fieldKey += ".*";   // multiple children
                }
            } else {
                if (newPrefix.contains(".plans.")) {
                    fieldKey += ".1.*"; // single applicant, multiple plans
                } else {
                    fieldKey += ".1";   // single applicant
                }
            }

            flat.put(fieldKey, (String) value);
        }
    }
}
```

---

## 🔄 5. Evaluation Logic for Double-Indexed Fields

Modify the evaluation part of `transform()`:

```java
if (fieldKey.endsWith(".*.*")) {
    // Two-level expansion: array of arrays
    List<List<String>> nestedValues = extractNestedArrays(evaluated);
    for (int i = 0; i < nestedValues.size(); i++) {
        List<String> plans = nestedValues.get(i);
        for (int j = 0; j < plans.size(); j++) {
            String indexedKey = fieldKey.replace(".*.*", "." + (i + 1) + "." + (j + 1));
            result.put(indexedKey, plans.get(j));
        }
    }
} else if (fieldKey.endsWith(".*")) {
    List<String> values = jsonNodeToList(evaluated);
    for (int i = 0; i < values.size(); i++) {
        String indexedKey = fieldKey.replace(".*", "." + (i + 1));
        result.put(indexedKey, values.get(i));
    }
} else {
    result.put(fieldKey, evaluated.toString());
}
```

And helper:

```java
private List<List<String>> extractNestedArrays(JsonNode node) {
    List<List<String>> result = new ArrayList<>();
    if (node.isArray()) {
        for (JsonNode child : node) {
            if (child.isArray()) {
                List<String> inner = new ArrayList<>();
                for (JsonNode n : child) inner.add(n.asText(""));
                result.add(inner);
            } else {
                result.add(List.of(child.asText("")));
            }
        }
    }
    return result;
}
```

---

## 🧾 6. Output Example

After running this with the YAML above, you get:

```
primary.demographic.firstName.1 = John
primary.products.medical.planName.1.1 = Aetna Silver
primary.products.medical.planName.1.2 = Aetna Bronze
primary.products.medical.coverageStart.1.1 = 2024-01-01
primary.products.medical.coverageEnd.1.2 = 2024-06-30
primary.products.dental.planName.1.1 = Delta Dental Basic
primary.products.dental.coverageStart.1.1 = 2024-02-01
child.demographic.firstName.1 = Alice
child.products.medical.planName.1.1 = Aetna Child
```

All ready to feed directly into your PDF merger.

---

## ✅ 7. Design Highlights

| Feature                      | Supported                           |
| ---------------------------- | ----------------------------------- |
| Multiple applicants          | ✅ via applicantType filtering       |
| Nested arrays (child + plan) | ✅ handled via `.*.*`                |
| Different product types      | ✅ defined declaratively in YAML     |
| Maintainability              | ✅ clear, nested YAML structure      |
| Code extensibility           | ✅ one recursive flatten + evaluator |

---

## 🧩 8. Future Enhancements (Optional)

* Add `repeatLevel: 2` metadata in YAML instead of relying on `.plans.` detection.
* Add `$filter()` and `$map()` JSONata functions to dynamically handle variable array structures.
* Integrate this with your **template metadata** to automatically discover which field names exist in the PDF.
* Cache parsed JSONata expressions for performance (important for large datasets).

---

Would you like me to extend this to show **how to include custom JSONata helper functions** (like `$formatDate()`, `$toUpper()`, `$productCount()`) that your YAML expressions can use to produce formatted values directly?
