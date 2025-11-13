Excellent — this is *exactly* the kind of complex, real-world transformation that JSONata + YAML configuration is great for.
Let’s design this properly so you can declaratively map hierarchical JSON input like this **Enrollment Application** into flattened, form-ready key/value pairs.

---

## 🧩 1. Problem Context

You have something like this (simplified JSON structure):

```json
{
  "applicants": [
    {
      "type": "PrimaryApplicant",
      "demographic": {
        "firstName": "John",
        "lastName": "Doe",
        "dob": "1985-03-15"
      },
      "addresses": [
        { "type": "home", "line1": "123 Main St", "city": "Boston" },
        { "type": "billing", "line1": "PO Box 567", "city": "Cambridge" }
      ],
      "products": [
        {
          "name": "medical",
          "plans": [
            { "planName": "Aetna Silver", "coverageStart": "2024-01-01", "coverageEnd": "2024-12-31" }
          ]
        }
      ]
    },
    {
      "type": "Child",
      "demographic": { "firstName": "Alice", "lastName": "Doe", "dob": "2015-09-01" },
      "addresses": [{ "type": "home", "line1": "123 Main St", "city": "Boston" }],
      "products": []
    },
    {
      "type": "Child",
      "demographic": { "firstName": "Bob", "lastName": "Doe", "dob": "2018-11-10" },
      "addresses": [{ "type": "home", "line1": "123 Main St", "city": "Boston" }],
      "products": []
    }
  ]
}
```

---

## 🧠 2. Mapping Requirements

* Flatten all nested data for PDF fields
* Primary, Spouse, and Child are distinct sections in the PDF
* Each child’s fields repeat with index suffixes:

  * `child.firstName.1`, `child.firstName.2`, etc.
* Other single-value fields get suffix `"1"`

  * e.g. `primary.firstName.1`

---

## 🧾 3. YAML Mapping Spec Using JSONata

You can express all mappings **declaratively** like this:

```yaml
template: enrollment_form.pdf
fields:
  # Primary applicant
  primary.firstName.1: "$applicants[type='PrimaryApplicant'].demographic.firstName"
  primary.lastName.1: "$applicants[type='PrimaryApplicant'].demographic.lastName"
  primary.dob.1: "$applicants[type='PrimaryApplicant'].demographic.dob"

  # Address - pick specific type
  primary.homeAddress.1: "$applicants[type='PrimaryApplicant'].addresses[type='home'].line1"
  primary.billingAddress.1: "$applicants[type='PrimaryApplicant'].addresses[type='billing'].line1"

  # Example product details
  primary.medical.planName.1: "$applicants[type='PrimaryApplicant'].products[name='medical'].plans[0].planName"
  primary.medical.coverageStart.1: "$applicants[type='PrimaryApplicant'].products[name='medical'].plans[0].coverageStart"

  # Child dependents — array flattening with indexes
  # We'll dynamically produce .1, .2, .3 for each child in the list
  child.firstName.*: "$applicants[type='Child'].demographic.firstName"
  child.lastName.*: "$applicants[type='Child'].demographic.lastName"
  child.dob.*: "$applicants[type='Child'].demographic.dob"
  child.homeAddress.*: "$applicants[type='Child'].addresses[type='home'].line1"
```

> Note: The `.*` notation here is **your own convention**, not JSONata’s — your engine will handle expanding it dynamically (shown below).

---

## ⚙️ 4. How JSONata Handles Arrays

JSONata expressions like:

```jsonata
$applicants[type='Child'].demographic.firstName
```

will return an **array**, e.g.:

```json
["Alice", "Bob"]
```

You can then expand these into:

```
child.firstName.1 = Alice
child.firstName.2 = Bob
```

This is exactly how you handle **repeating form fields** in PDFs.

---

## 🧱 5. Extend the `MappingEngine` to Handle Indexed Expansion

Update your `MappingEngine`:

```java
public class MappingEngine {

    public Map<String, String> transform(String sourceJson, MappingConfig config) {
        Map<String, String> result = new HashMap<>();

        try {
            JsonNode jsonNode = JsonUtils.jsonToNode(sourceJson);

            for (Map.Entry<String, String> entry : config.getFields().entrySet()) {
                String fieldKey = entry.getKey();
                String expression = entry.getValue();

                var evaluated = Expressions.parse(expression).evaluate(jsonNode);
                if (evaluated == null) continue;

                if (fieldKey.endsWith(".*")) {
                    // Expand array results dynamically
                    List<String> values = jsonNodeToList(evaluated);
                    for (int i = 0; i < values.size(); i++) {
                        String indexedKey = fieldKey.replace(".*", "." + (i + 1));
                        result.put(indexedKey, values.get(i));
                    }
                } else {
                    result.put(fieldKey, evaluated.toString());
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Error during mapping transformation", e);
        }

        return result;
    }

    private List<String> jsonNodeToList(JsonNode node) {
        List<String> list = new ArrayList<>();
        if (node.isArray()) {
            for (JsonNode n : node) {
                list.add(n.asText(""));
            }
        } else {
            list.add(node.asText(""));
        }
        return list;
    }
}
```

✅ This supports both:

* Single scalar fields (like primary applicant)
* Array-valued fields (like multiple children)

---

## 🧩 6. Sample Output

For the example JSON above, you’d get:

```
primary.firstName.1 = John
primary.lastName.1 = Doe
primary.dob.1 = 1985-03-15
primary.homeAddress.1 = 123 Main St
primary.billingAddress.1 = PO Box 567
primary.medical.planName.1 = Aetna Silver
primary.medical.coverageStart.1 = 2024-01-01
child.firstName.1 = Alice
child.firstName.2 = Bob
child.lastName.1 = Doe
child.lastName.2 = Doe
child.dob.1 = 2015-09-01
child.dob.2 = 2018-11-10
child.homeAddress.1 = 123 Main St
child.homeAddress.2 = 123 Main St
```

These are **flat, form-ready** fields, each easily mapped to your PDF form field names.

---

## 🧰 7. Design Advantages

| Aspect                 | Approach                                                 | Benefit                                          |
| ---------------------- | -------------------------------------------------------- | ------------------------------------------------ |
| **Declarative config** | YAML with JSONata                                        | No Java code for new mappings                    |
| **Supports hierarchy** | JSONata filters, path navigation                         | Handles nested demographics, addresses, products |
| **Repeating fields**   | `.*` convention + auto-expansion                         | Flexible for variable child counts               |
| **Future-proof**       | Same structure works for spouse, multiple products, etc. | Extend without code changes                      |

---

## 💡 8. Optional Enhancements

* Allow nested YAML sections per applicant type for better readability.
* Add `$map()` functions if you want more control over how child arrays are flattened.
* Add custom JSONata functions like `$formatDate()` or `$upper()` for domain-specific transforms.

---

Would you like me to show you a **YAML structure that’s more hierarchical** (e.g. grouped by applicant type and product type), but still compiles down into the same flat field map for your PDF engine? That structure is often easier for teams to maintain.
