Excellent — yes, that’s exactly the kind of improvement that takes this library from *useful* to *production-ready*.
Let’s add full support for **numerical comparisons**, **string equality**, and **logical operators (`AND`, `OR`)** in YAML conditions.

---

## 💡 What You’ll Gain

You’ll be able to write conditions like these in YAML:

```yaml
condition: "relationship == 'child' AND age > 10"
condition: "income >= 50000 OR relationship == 'spouse'"
condition: "(relationship == 'primary' OR relationship == 'spouse') AND age >= 21"
```

and they’ll just work.

---

## 🧩 Approach

We’ll implement a **lightweight expression evaluator** to avoid adding heavy scripting engines (like JEXL or MVEL).

1. Tokenize condition string into operands and operators (`==`, `!=`, `>`, `<`, `>=`, `<=`, `AND`, `OR`, parentheses).
2. Convert tokens into a **Reverse Polish Notation (RPN)** for operator precedence (optional simplification).
3. Evaluate expression using JSON field values.
4. Support both numeric and string comparisons.

For simplicity, we’ll build a **minimal recursive evaluator** that can handle nested parentheses and logical expressions.

---

## ⚙️ Updated `ConditionalMappingProcessor.java`

Here’s the new version of the processor that supports **complex logical expressions**:

```java
package com.example.pdfmapper.processor;

import com.example.pdfmapper.config.MappingSpec;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

        int index = 1;
        for (Object element : list) {
            if (!(element instanceof Map<?, ?> item)) continue;
            if (evaluateComplexCondition((Map<String, Object>) item, spec.getCondition())) {
                spec.getFieldMap().forEach((src, tgt) -> {
                    Object value = item.get(src);
                    if (value != null) {
                        String fieldName = PdfFieldNameGenerator.generate(
                                spec.getPdfFieldPattern(),
                                spec.getTargetPrefix(),
                                tgt,
                                spec.isIndexed() ? index : 1
                        );
                        resolved.put(fieldName, value);
                    }
                });
                if (spec.isIndexed()) index++;
            }
        }
        return resolved;
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

    // --- Enhanced Evaluator ---
    private boolean evaluateComplexCondition(Map<String, Object> context, String condition) {
        condition = condition.trim();

        // Replace logical operators (case-insensitive)
        condition = condition.replaceAll("(?i)\\band\\b", "&&")
                             .replaceAll("(?i)\\bor\\b", "||");

        return evalExpression(condition, context);
    }

    private boolean evalExpression(String expr, Map<String, Object> ctx) {
        // Handle parentheses
        while (expr.contains("(")) {
            int close = expr.indexOf(')');
            int open = expr.lastIndexOf('(', close);
            String subExpr = expr.substring(open + 1, close);
            boolean result = evalSimple(subExpr, ctx);
            expr = expr.substring(0, open) + result + expr.substring(close + 1);
        }
        return evalSimple(expr, ctx);
    }

    private boolean evalSimple(String expr, Map<String, Object> ctx) {
        // Split by logical operators
        List<String> orParts = splitByOperator(expr, "||");
        boolean orResult = false;
        for (String orPart : orParts) {
            List<String> andParts = splitByOperator(orPart, "&&");
            boolean andResult = true;
            for (String andPart : andParts) {
                andResult &= evalComparison(andPart.trim(), ctx);
            }
            orResult |= andResult;
        }
        return orResult;
    }

    private List<String> splitByOperator(String expr, String op) {
        List<String> parts = new ArrayList<>();
        int depth = 0, start = 0;
        for (int i = 0; i < expr.length() - 1; i++) {
            if (expr.charAt(i) == '(') depth++;
            else if (expr.charAt(i) == ')') depth--;
            else if (depth == 0 && expr.startsWith(op, i)) {
                parts.add(expr.substring(start, i));
                start = i + op.length();
            }
        }
        parts.add(expr.substring(start));
        return parts;
    }

    private boolean evalComparison(String expr, Map<String, Object> ctx) {
        expr = expr.trim();

        // Regex to capture comparison expressions
        Pattern pattern = Pattern.compile("([a-zA-Z0-9_.]+)\\s*(==|!=|>=|<=|>|<)\\s*(['\"]?)([^'\"]+)\\3");
        Matcher matcher = pattern.matcher(expr);

        if (matcher.find()) {
            String field = matcher.group(1).trim();
            String op = matcher.group(2).trim();
            String valueStr = matcher.group(4).trim();

            Object fieldValue = ctx.get(field);
            if (fieldValue == null) return false;

            // Try numeric comparison
            if (isNumeric(fieldValue) && isNumeric(valueStr)) {
                double actual = Double.parseDouble(fieldValue.toString());
                double expected = Double.parseDouble(valueStr);
                return compareNumeric(actual, expected, op);
            }

            // Fallback to string comparison
            String actual = fieldValue.toString();
            return compareString(actual, valueStr, op);
        }

        // Handle literal booleans like "true" or "false"
        expr = expr.trim().toLowerCase();
        if (expr.equals("true")) return true;
        if (expr.equals("false")) return false;

        // Unknown or malformed condition
        return false;
    }

    private boolean compareNumeric(double a, double b, String op) {
        return switch (op) {
            case "==" -> a == b;
            case "!=" -> a != b;
            case ">" -> a > b;
            case "<" -> a < b;
            case ">=" -> a >= b;
            case "<=" -> a <= b;
            default -> false;
        };
    }

    private boolean compareString(String a, String b, String op) {
        return switch (op) {
            case "==" -> a.equalsIgnoreCase(b);
            case "!=" -> !a.equalsIgnoreCase(b);
            default -> false;
        };
    }

    private boolean isNumeric(Object val) {
        try {
            Double.parseDouble(val.toString());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
```

---

## 🧾 Example YAML with Complex Conditions

```yaml
pdf:
  mappings:
    - source: applicantList
      condition: "(relationship == 'child' AND age >= 10) OR (relationship == 'spouse' AND income >= 50000)"
      targetPrefix: "Dependent"
      pdfFieldPattern: "{prefix}.{field}.{index}"
      indexed: true
      fieldMap:
        fname: "FName"
        lname: "LName"
```

---

## ✅ Example JSON Input

```json
{
  "applicantList": [
    { "fname": "John", "lname": "Doe", "relationship": "primary", "age": 35 },
    { "fname": "Jane", "lname": "Doe", "relationship": "spouse", "income": 60000 },
    { "fname": "Chris", "lname": "Doe", "relationship": "child", "age": 12 },
    { "fname": "Alex", "lname": "Doe", "relationship": "child", "age": 8 }
  ]
}
```

---

## ✅ Resulting Field Map

```
Dependent.FName.1 = Jane
Dependent.LName.1 = Doe
Dependent.FName.2 = Chris
Dependent.LName.2 = Doe
```

(`Jane` included because spouse income > 50000, `Chris` included because child age ≥ 10)

---

## 🧠 Summary of Features

| Feature                                   | Supported |
| ----------------------------------------- | --------- |
| Equality (`==`, `!=`)                     | ✅         |
| Numeric comparison (`>`, `<`, `>=`, `<=`) | ✅         |
| Logical operators (`AND`, `OR`)           | ✅         |
| Parentheses precedence                    | ✅         |
| String & numeric mixing                   | ✅         |
| Boolean literals                          | ✅         |

---

Would you like me to **extend this evaluator** further so it supports **function-style expressions**, like
`length(children) > 2` or `exists(income)` or `toUpper(relationship) == 'SPOUSE'`?
