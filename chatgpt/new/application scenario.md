Perfect — let’s evolve your current flat YAML into a **hierarchical, structured mapping specification**, which is **much easier to maintain**, while still producing the same flattened `{ fieldName → value }` output for your PDF form.

We’ll design this so your `MappingEngine` can easily process it recursively, flatten it into the field map, and apply your same JSONata logic and `.*` suffix expansion.

---

## 🧩 1. Motivation

Instead of this (flat, harder to read):

```yaml
fields:
  primary.firstName.1: "$applicants[type='PrimaryApplicant'].demographic.firstName"
  child.firstName.*: "$applicants[type='Child'].demographic.firstName"
```

We’ll use this (structured, readable, scalable):

```yaml
applicants:
  primary:
    demographic:
      firstName: "$applicants[type='PrimaryApplicant'].demographic.firstName"
      lastName: "$applicants[type='PrimaryApplicant'].demographic.lastName"
      dob: "$applicants[type='PrimaryApplicant'].demographic.dob"
    addresses:
      home: "$applicants[type='PrimaryApplicant'].addresses[type='home'].line1"
      billing: "$applicants[type='PrimaryApplicant'].addresses[type='billing'].line1"
    products:
      medical:
        planName: "$applicants[type='PrimaryApplicant'].products[name='medical'].plans[0].planName"
        coverageStart: "$applicants[type='PrimaryApplicant'].products[name='medical'].plans[0].coverageStart"

  spouse:
    demographic:
      firstName: "$applicants[type='Spouse'].demographic.firstName"
      lastName: "$applicants[type='Spouse'].demographic.lastName"
      dob: "$applicants[type='Spouse'].demographic.dob"

  child:
    demographic:
      firstName: "$applicants[type='Child'].demographic.firstName"
      lastName: "$applicants[type='Child'].demographic.lastName"
      dob: "$applicants[type='Child'].demographic.dob"
    addresses:
      home: "$applicants[type='Child'].addresses[type='home'].line1"
```

---

## 🧱 2. Template Header

Add the top-level keys like before:

```yaml
template: enrollment_form.pdf
applicants:
  primary:
    ...
```

---

## 🧠 3. Flattening Strategy

You’ll write a small recursive flattener that walks through nested maps and constructs field names using dot-notation.
Then, when it hits a **leaf expression string**, it treats it as a mapping expression.

### Flattening rules

| Level                                    | Key                               | Output Key Prefix Example |
| ---------------------------------------- | --------------------------------- | ------------------------- |
| applicants.primary.demographic.firstName | `primary.demographic.firstName.1` |                           |
| applicants.child.demographic.firstName   | `child.demographic.firstName.*`   |                           |

We’ll append:

* `".1"` for single-value applicant types (`PrimaryApplicant`, `Spouse`)
* `".*"` for repeating applicant type (`Child`)

---

## ⚙️ 4. Extended Model Classes

```java
public class HierarchicalMappingConfig {
    private String template;
    private Map<String, Object> applicants;

    public String getTemplate() { return template; }
    public void setTemplate(String template) { this.template = template; }

    public Map<String, Object> getApplicants() { return applicants; }
    public void setApplicants(Map<String, Object> applicants) { this.applicants = applicants; }
}
```

We’ll load this YAML into a nested `Map<String, Object>` structure.

---

## 🔧 5. Recursive Flattening Utility

Add this helper to your `MappingEngine`:

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
            // Dive deeper
            flattenMappings((Map<String, Object>) value, newPrefix, flat, applicantType);
        } else if (value instanceof String) {
            // It's a JSONata expression
            String fieldKey = newPrefix;

            // Add suffix based on applicant type
            if ("child".equalsIgnoreCase(applicantType)) {
                fieldKey += ".*";
            } else {
                fieldKey += ".1";
            }
            flat.put(fieldKey, (String) value);
        }
    }
}
```

---

## 🧩 6. Transform Method Integration

Your transform method now becomes:

```java
public Map<String, String> transform(String sourceJson, HierarchicalMappingConfig config) {
    Map<String, String> result = new HashMap<>();

    try {
        // 1. Flatten YAML hierarchy into field→expression map
        Map<String, String> flatMappings = new HashMap<>();
        config.getApplicants().forEach((applicantType, section) -> {
            flattenMappings((Map<String, Object>) section, applicantType, flatMappings, applicantType);
        });

        // 2. Evaluate JSONata expressions
        JsonNode jsonNode = JsonUtils.jsonToNode(sourceJson);
        for (Map.Entry<String, String> entry : flatMappings.entrySet()) {
            String fieldKey = entry.getKey();
            String expression = entry.getValue();

            var evaluated = Expressions.parse(expression).evaluate(jsonNode);
            if (evaluated == null) continue;

            if (fieldKey.endsWith(".*")) {
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
```

---

## 🧾 7. Output Example

For the same Enrollment JSON input, this hierarchical YAML produces:

```
primary.demographic.firstName.1 = John
primary.demographic.lastName.1 = Doe
primary.demographic.dob.1 = 1985-03-15
primary.addresses.home.1 = 123 Main St
primary.addresses.billing.1 = PO Box 567
primary.products.medical.planName.1 = Aetna Silver
primary.products.medical.coverageStart.1 = 2024-01-01
child.demographic.firstName.1 = Alice
child.demographic.firstName.2 = Bob
child.demographic.lastName.1 = Doe
child.demographic.lastName.2 = Doe
child.demographic.dob.1 = 2015-09-01
child.demographic.dob.2 = 2018-11-10
child.addresses.home.1 = 123 Main St
child.addresses.home.2 = 123 Main St
```

Which is **exactly the flat field map** your PDF filler expects.

---

## 🧱 8. Advantages of Hierarchical YAML

| Feature          | Benefit                                                                   |
| ---------------- | ------------------------------------------------------------------------- |
| **Readable**     | Clearly separates applicant types and domains                             |
| **Scalable**     | Easily extend to new applicant roles or new field categories              |
| **Declarative**  | Still pure YAML + JSONata; no code changes for mapping updates            |
| **Reuses logic** | Your flattening logic automatically converts it into a flat PDF field map |

---

## 🧮 9. Next Step (Optional)

We can further enhance this by:

* Allowing **field groups** (like multiple products or plans) to be treated as repeating sections (`.*` or indexed suffixes).
* Adding YAML shortcuts for common JSONata filters (like `applicantType: Child` to auto-generate JSONata filter expressions).
* Supporting per-template **conditional mappings** (e.g., include a field only if `$exists(...)` is true).

---

Would you like me to extend this design to show **how to represent multiple products and their plans per applicant hierarchically**, and how the flattening logic can dynamically name them (`medical.planName.1.1`, `dental.planName.1.1`, etc.)?
