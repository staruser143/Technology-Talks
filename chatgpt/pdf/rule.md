Excellent catch 👍 — what you’re seeing is a **common subtle bug** in expression evaluation logic, especially when the condition parsing or variable scoping isn’t handled correctly during iteration over grouped or array elements.

Let’s diagnose what’s likely going wrong, and then I’ll show you the **fixed and enhanced version** of the conditional evaluator.

---

### 🧩 **The Root Cause**

From your description:

> “I used one dependent with `age < 5`, but all dependents are being matched.”

That means:

* The condition evaluator is **not actually evaluating per dependent instance**.
* Instead, it’s either:

  1. Evaluating the expression **against the full JSON list**, or
  2. The variable used for evaluation (like `age`) isn’t being resolved to the **current element’s field**.

So if your YAML says something like:

```yaml
groups:
  - name: dependents
    condition: "relationship == 'Dependent' and age < 5"
```

The processor might be using a global binding of `age`, not the element’s `age`.

---

### ✅ **Correct Behavior**

Each dependent object should be evaluated **in its own context**, so:

```json
{
  "fname": "Alice",
  "age": 3,
  "relationship": "Dependent"
}
```

matches the condition,
but

```json
{
  "fname": "Bob",
  "age": 12,
  "relationship": "Dependent"
}
```

does **not**.

---

### 🛠️ **Fix: Scoped Expression Evaluation**

Here’s the corrected `ConditionEvaluator` class.

```java
package com.example.pdfgen.util;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.SimpleBindings;
import java.util.Map;

public class ConditionEvaluator {

    private static final ScriptEngine engine = new ScriptEngineManager().getEngineByName("JavaScript");

    public static boolean evaluate(String condition, Map<String, Object> context) {
        if (condition == null || condition.isEmpty()) return true;

        try {
            // Convert logical operators for JavaScript
            String jsCondition = condition
                    .replaceAll("(?i)\\band\\b", "&&")
                    .replaceAll("(?i)\\bor\\b", "||");

            // Ensure numeric comparisons are valid (no quotes around numbers)
            // Example: "age < 5" works, "age"=="5" still works
            Object result = engine.eval(jsCondition, new SimpleBindings(context));

            return result instanceof Boolean && (Boolean) result;
        } catch (Exception e) {
            System.err.println("Condition evaluation failed for: " + condition + " -> " + e.getMessage());
            return false;
        }
    }
}
```

---

### 🔁 **Usage in GroupProcessor**

Make sure you’re calling this **for each element individually**:

```java
for (Map<String, Object> element : applicantList) {
    if (ConditionEvaluator.evaluate(groupConfig.getCondition(), element)) {
        groupedData.computeIfAbsent(groupConfig.getName(), k -> new ArrayList<>()).add(element);
    }
}
```

Each element’s map is passed directly to the evaluator, so `age`, `relationship`, etc. are available as local variables.

---

### 💡 **Tips for Robust Conditions**

* Use **parentheses** to make AND/OR logic explicit:

  ```yaml
  condition: "(relationship == 'Dependent' and age < 5) or (relationship == 'Primary')"
  ```
* Strings need quotes, numbers don’t:

  ```yaml
  condition: "relationship == 'Dependent' and age < 5"
  ```
* You can also safely compare fields:

  ```yaml
  condition: "income > spouseIncome * 0.5"
  ```

---

### ✅ **Example Test Case**

```json
{
  "applicantList": [
    {"fname": "John", "relationship": "Primary", "age": 35},
    {"fname": "Jane", "relationship": "Spouse", "age": 33},
    {"fname": "Tom", "relationship": "Dependent", "age": 3},
    {"fname": "Lily", "relationship": "Dependent", "age": 12}
  ]
}
```

YAML:

```yaml
groups:
  - name: youngDependents
    condition: "relationship == 'Dependent' and age < 5"
```

✅ Only **Tom** gets selected into `youngDependents`.

---

Would you like me to show the **enhanced full integrated version** (with YAML, condition evaluator, and grouped processing code updated together)?
That version will support nested fields, arrays, and both string/numeric logical conditions end-to-end.
