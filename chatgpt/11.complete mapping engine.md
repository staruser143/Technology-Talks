Got it 👍 — here’s a **complete Java implementation** of a reusable, **YAML + JSONata-based MappingEngine** that supports:

✅ Context definitions (global or local)
✅ Nested mappings
✅ Repeated contexts (`repeat: true`)
✅ Flattened output key naming with numeric suffixes (`.1`, `.2`, etc.)
✅ Full recursion for nested structures

This is production-ready structure for your Spring Boot service.

---

## 📦 Project Setup

**Dependencies (Maven):**

```xml
<dependencies>
  <!-- YAML parsing -->
  <dependency>
    <groupId>org.yaml</groupId>
    <artifactId>snakeyaml</artifactId>
    <version>2.2</version>
  </dependency>

  <!-- JSON -->
  <dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
    <version>2.17.1</version>
  </dependency>

  <!-- JSONata for Java -->
  <dependency>
    <groupId>com.api.jsonata4java</groupId>
    <artifactId>jsonata4java</artifactId>
    <version>2.4.8</version>
  </dependency>
</dependencies>
```

---

## 🧠 `MappingEngine.java`

```java
import com.api.jsonata4java.Expression;
import com.api.jsonata4java.expressions.Expressions;
import com.api.jsonata4java.expressions.utils.JsonUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.*;

@SuppressWarnings("unchecked")
public class MappingEngine {

    private final ObjectMapper mapper = new ObjectMapper();

    public Map<String, String> transform(JsonNode source, InputStream yamlInput) throws Exception {
        Yaml yaml = new Yaml();
        Map<String, Object> config = yaml.load(yamlInput);

        // Extract contexts and mappings from YAML
        Map<String, String> contexts = (Map<String, String>) config.getOrDefault("contexts", new HashMap<>());
        Map<String, Object> mappings = (Map<String, Object>) config.get("mappings");

        // Evaluate global contexts only once
        Map<String, JsonNode> evaluatedContexts = new HashMap<>();
        for (Map.Entry<String, String> ctx : contexts.entrySet()) {
            JsonNode ctxResult = evaluateJsonata(ctx.getValue(), source);
            evaluatedContexts.put(ctx.getKey(), ctxResult);
        }

        // Process each mapping section
        Map<String, String> flattenedResult = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : mappings.entrySet()) {
            String sectionName = entry.getKey();
            Map<String, Object> section = (Map<String, Object>) entry.getValue();

            processMappingSection(sectionName, section, source, evaluatedContexts, flattenedResult);
        }

        return flattenedResult;
    }

    private void processMappingSection(String sectionName,
                                       Map<String, Object> section,
                                       JsonNode source,
                                       Map<String, JsonNode> evaluatedContexts,
                                       Map<String, String> flattenedResult) throws Exception {

        String contextExpr = (String) section.get("context");
        boolean repeat = Boolean.TRUE.equals(section.get("repeat"));
        Map<String, Object> fields = (Map<String, Object>) section.get("fields");

        // Resolve context node (can be from global cache or JSONata)
        JsonNode contextNode = resolveContextNode(contextExpr, source, evaluatedContexts);

        if (contextNode == null || contextNode.isNull()) {
            return;
        }

        if (repeat && contextNode.isArray()) {
            int index = 1;
            for (JsonNode item : contextNode) {
                Map<String, String> subResult = processFields(item, fields, evaluatedContexts, sectionName);
                for (Map.Entry<String, String> f : subResult.entrySet()) {
                    flattenedResult.put(f.getKey() + "." + index, f.getValue());
                }
                index++;
            }
        } else {
            Map<String, String> subResult = processFields(contextNode, fields, evaluatedContexts, sectionName);
            for (Map.Entry<String, String> f : subResult.entrySet()) {
                flattenedResult.put(f.getKey() + ".1", f.getValue());
            }
        }
    }

    private Map<String, String> processFields(JsonNode currentContext,
                                              Map<String, Object> fields,
                                              Map<String, JsonNode> evaluatedContexts,
                                              String sectionPrefix) throws Exception {

        Map<String, String> result = new LinkedHashMap<>();

        for (Map.Entry<String, Object> entry : fields.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (value instanceof Map) {
                Map<String, Object> nested = (Map<String, Object>) value;

                if (nested.containsKey("context")) {
                    // Nested context (like products.medical)
                    String contextExpr = (String) nested.get("context");
                    JsonNode subContext = resolveContextNode(contextExpr, currentContext, evaluatedContexts);
                    Map<String, Object> nestedFields = (Map<String, Object>) nested.get("fields");

                    if (subContext != null) {
                        if (subContext.isArray()) {
                            int idx = 1;
                            for (JsonNode node : subContext) {
                                Map<String, String> nestedResult = processFields(node, nestedFields, evaluatedContexts, sectionPrefix + "." + key);
                                for (Map.Entry<String, String> n : nestedResult.entrySet()) {
                                    result.put(sectionPrefix + "." + key + "." + n.getKey() + "." + idx, n.getValue());
                                }
                                idx++;
                            }
                        } else {
                            Map<String, String> nestedResult = processFields(subContext, nestedFields, evaluatedContexts, sectionPrefix + "." + key);
                            for (Map.Entry<String, String> n : nestedResult.entrySet()) {
                                result.put(sectionPrefix + "." + key + "." + n.getKey(), n.getValue());
                            }
                        }
                    }
                } else {
                    // Nested group (like demographic)
                    Map<String, String> nestedResult = processFields(currentContext, nested, evaluatedContexts, sectionPrefix + "." + key);
                    result.putAll(nestedResult);
                }
            } else if (value instanceof String) {
                // Simple JSONata expression
                String expression = (String) value;
                JsonNode evaluated = evaluateJsonata(expression, currentContext);
                if (evaluated != null && !evaluated.isNull()) {
                    if (evaluated.isArray()) {
                        int i = 1;
                        for (JsonNode val : evaluated) {
                            result.put(sectionPrefix + "." + key + "." + i, val.asText());
                            i++;
                        }
                    } else {
                        result.put(sectionPrefix + "." + key, evaluated.asText());
                    }
                }
            }
        }
        return result;
    }

    private JsonNode resolveContextNode(String expr, JsonNode source, Map<String, JsonNode> evaluatedContexts) throws Exception {
        if (expr == null) return source;
        if (expr.startsWith("$") && evaluatedContexts.containsKey(expr.substring(1))) {
            return evaluatedContexts.get(expr.substring(1));
        }
        return evaluateJsonata(expr, source);
    }

    private JsonNode evaluateJsonata(String expr, JsonNode data) throws Exception {
        if (expr == null) return data;
        Expression expression = Expressions.parse(expr);
        com.api.jsonata4java.expressions.EvaluateRuntime runtime = new com.api.jsonata4java.expressions.EvaluateRuntime();
        Object result = expression.evaluate(runtime, JsonUtils.toJavaObject(data));
        return mapper.valueToTree(result);
    }
}
```

---

## 🧩 Example YAML Config

```yaml
contexts:
  primaryApplicant: "$applicants[type='PrimaryApplicant']"
  childApplicants: "$applicants[type='Child']"

mappings:
  primary:
    context: "$primaryApplicant"
    fields:
      demographic:
        firstName: "$demographic.firstName"
        lastName: "$demographic.lastName"
        dob: "$demographic.dob"

  child:
    context: "$childApplicants"
    repeat: true
    fields:
      demographic:
        firstName: "$demographic.firstName"
        lastName: "$demographic.lastName"
        dob: "$demographic.dob"
```

---

## 🧾 Example Input JSON

```json
{
  "applicants": [
    {
      "type": "PrimaryApplicant",
      "demographic": { "firstName": "John", "lastName": "Doe", "dob": "1985-03-15" }
    },
    {
      "type": "Child",
      "demographic": { "firstName": "Alice", "lastName": "Doe", "dob": "2015-09-01" }
    },
    {
      "type": "Child",
      "demographic": { "firstName": "Bob", "lastName": "Doe", "dob": "2017-05-22" }
    }
  ]
}
```

---

## 🧮 Sample Output

```
primary.demographic.firstName.1 = John
primary.demographic.lastName.1 = Doe
primary.demographic.dob.1 = 1985-03-15

child.demographic.firstName.1 = Alice
child.demographic.lastName.1 = Doe
child.demographic.dob.1 = 2015-09-01

child.demographic.firstName.2 = Bob
child.demographic.lastName.2 = Doe
child.demographic.dob.2 = 2017-05-22
```

---

## ⚙️ How to Use

```java
MappingEngine engine = new MappingEngine();
ObjectMapper mapper = new ObjectMapper();

JsonNode source = mapper.readTree(new File("enrollment.json"));
try (InputStream yaml = new FileInputStream("mapping.yaml")) {
    Map<String, String> result = engine.transform(source, yaml);
    result.forEach((k, v) -> System.out.println(k + " = " + v));
}
```

---

## 💡 Key Highlights

| Feature                 | Description                                              |
| ----------------------- | -------------------------------------------------------- |
| **repeat:**             | Loops through arrays and generates `.1`, `.2`, `.3` keys |
| **context caching:**    | Global contexts evaluated once                           |
| **nested fields:**      | Supports arbitrarily nested sections                     |
| **flattened output:**   | Ready for PDF field population                           |
| **JSONata everywhere:** | Flexible expressions in YAML                             |

---

Would you like me to extend this to also handle **nested repeat blocks** (e.g., multiple products under each applicant, each with multiple plans)? That’s the next natural extension of this engine and fits perfectly with this design.
