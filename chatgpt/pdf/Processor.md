Absolutely 👍 — let’s go over **clean, compile-ready** versions of both
`BaseProcessor` and `UnifiedTemplateProcessor` as they should look **in your latest integrated setup** (which supports YAML, JSON flattening, PDFBox, Freemarker, grouping, and transforms).

---

## 🧩 `BaseProcessor.java`

This is the **common abstract class** used by both PDF and HTML template processors.

```java
package com.hello.main.processor;

import com.hello.main.config.TemplateConfig;
import com.hello.main.transform.TransformRegistry;
import com.hello.main.transform.Transform;
import com.hello.main.utils.ConditionEvaluator;

import java.util.*;
import java.util.stream.Collectors;

/**
 * BaseProcessor provides reusable logic for field extraction,
 * applying conditions, transforms, and preparing field-value maps.
 */
public abstract class BaseProcessor {

    protected TemplateConfig config;

    public BaseProcessor(TemplateConfig config) {
        this.config = config;
    }

    /**
     * Flatten nested JSON structures (Map/List) into dot.notation keys.
     */
    @SuppressWarnings("unchecked")
    protected void flattenJson(String prefix, Object value, Map<String, Object> result) {
        if (value instanceof Map<?, ?> map) {
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                String key = prefix.isEmpty() ? entry.getKey().toString() : prefix + "." + entry.getKey();
                flattenJson(key, entry.getValue(), result);
            }
        } else if (value instanceof List<?> list) {
            for (int i = 0; i < list.size(); i++) {
                flattenJson(prefix + "[" + i + "]", list.get(i), result);
            }
        } else {
            result.put(prefix, value);
        }
    }

    /**
     * Prepare flat JSON map for easier path resolution.
     */
    protected Map<String, Object> flatten(Map<String, Object> json) {
        Map<String, Object> flat = new LinkedHashMap<>();
        flattenJson("", json, flat);
        return flat;
    }

    /**
     * Apply transform chain defined in YAML config.
     */
    protected Object applyTransforms(Object value, List<String> transforms) {
        if (transforms == null || transforms.isEmpty()) return value;
        Object current = value;
        for (String spec : transforms) {
            String[] parts = spec.split(":", 2);
            String name = parts[0];
            String[] params = parts.length > 1 ? parts[1].split(",") : new String[]{};
            Transform transform = TransformRegistry.get(name);
            if (transform != null) {
                current = transform.apply(current, params);
            } else {
                System.err.println("⚠ Unknown transform: " + name);
            }
        }
        return current;
    }

    /**
     * Extract value from flattened JSON using a dotted key.
     */
    protected Object resolveValue(Map<String, Object> flatJson, String path) {
        return flatJson.get(path);
    }

    /**
     * Evaluate condition (if any) from YAML using ConditionEvaluator.
     */
    protected boolean evaluateCondition(Map<String, Object> context, String condition) {
        if (condition == null || condition.isEmpty()) return true;
        try {
            return ConditionEvaluator.evaluate(condition, context);
        } catch (Exception e) {
            System.err.println("⚠ Condition failed for '" + condition + "': " + e.getMessage());
            return false;
        }
    }

    /**
     * Build the final flattened map of target PDF/HTML field values.
     */
    protected Map<String, Object> buildFieldValues(Map<String, Object> json) {
        Map<String, Object> flatJson = flatten(json);
        Map<String, Object> output = new LinkedHashMap<>();

        for (var entry : config.getFields().entrySet()) {
            String targetField = entry.getKey();
            var fieldCfg = entry.getValue();

            // 1️⃣ Evaluate condition
            if (!evaluateCondition(flatJson, fieldCfg.getCondition())) continue;

            // 2️⃣ Extract value
            Object value = resolveValue(flatJson, fieldCfg.getSource());

            // 3️⃣ Apply transforms
            value = applyTransforms(value, fieldCfg.getTransforms());

            // 4️⃣ Assign
            output.put(targetField, value);
        }
        return output;
    }

    /**
     * Subclasses implement how to generate final output (PDF/HTML).
     */
    public abstract void generate(Map<String, Object> data, String outputPath) throws Exception;
}
```

---

## 🧩 `UnifiedTemplateProcessor.java`

This processor decides at runtime whether to handle a **PDF** or **HTML** template based on the YAML configuration’s `templateType` or file extension.

It delegates internally to either a `PdfFormProcessor` (PDFBox-based) or a `FreemarkerHtmlProcessor`.

```java
package com.hello.main.processor;

import com.hello.main.config.TemplateConfig;
import java.util.Map;

/**
 * UnifiedTemplateProcessor automatically routes processing
 * to either PDF (AcroForm/PDFBox) or HTML (Freemarker) renderer.
 */
public class UnifiedTemplateProcessor extends BaseProcessor {

    private final PdfFormProcessor pdfProcessor;
    private final FreemarkerHtmlProcessor htmlProcessor;

    public UnifiedTemplateProcessor(TemplateConfig config) {
        super(config);
        this.pdfProcessor = new PdfFormProcessor(config);
        this.htmlProcessor = new FreemarkerHtmlProcessor(config);
    }

    @Override
    public void generate(Map<String, Object> data, String outputPath) throws Exception {
        String templateType = config.getTemplateType() != null
                ? config.getTemplateType().toLowerCase()
                : detectTemplateType(config.getTemplatePath());

        switch (templateType) {
            case "pdf" -> {
                System.out.println("🧾 Generating PDF using PDFBox...");
                pdfProcessor.generate(data, outputPath);
            }
            case "html" -> {
                System.out.println("🌐 Generating HTML/PDF using Freemarker...");
                htmlProcessor.generate(data, outputPath);
            }
            default -> throw new IllegalArgumentException("Unknown template type: " + templateType);
        }
    }

    /**
     * Infer template type from file extension if not explicitly provided.
     */
    private String detectTemplateType(String path) {
        if (path == null) return "pdf";
        if (path.endsWith(".ftl") || path.endsWith(".html")) return "html";
        return "pdf";
    }
}
```

---

## ✅ Summary

| Class                        | Responsibility                                                                    | Key Features                                 |
| ---------------------------- | --------------------------------------------------------------------------------- | -------------------------------------------- |
| **BaseProcessor**            | Shared logic for JSON flattening, condition evaluation, and transform application | Works for both PDF and HTML                  |
| **UnifiedTemplateProcessor** | Routes generation between PDFBox and Freemarker renderers                         | Auto-detects based on YAML or file extension |

---

Would you like me to show the **TemplateConfig**, `FieldMapping`, and `PdfFormProcessor` classes as they should align with this version (so your build compiles cleanly end-to-end)?
That’ll ensure the pipeline from YAML → JSON flatten → transforms → PDF/HTML output works seamlessly.
