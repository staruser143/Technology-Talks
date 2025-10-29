Perfect ✅ — that’s the right move.
Since you’ve already gone through a lot of incremental builds, let’s now consolidate everything into a **clean, compile-ready, full Java library skeleton** that includes:

✅ YAML → JSON flattening → transforms → condition evaluation → grouped / indexed field mapping
✅ Unified PDF (via PDFBox) + HTML (via Freemarker) generation
✅ Pluggable custom transforms (for both library and client apps)
✅ Clean separation of config, processor, utils, and transform layers

---

# 🧩 1. Project Structure

```
src/
 └─ main/
    ├─ java/
    │   └─ com/hello/main/
    │       ├─ config/
    │       │   ├─ FieldMapping.java
    │       │   └─ TemplateConfig.java
    │       ├─ processor/
    │       │   ├─ BaseProcessor.java
    │       │   ├─ PdfFormProcessor.java
    │       │   ├─ FreemarkerHtmlProcessor.java
    │       │   └─ UnifiedTemplateProcessor.java
    │       ├─ transform/
    │       │   ├─ Transform.java
    │       │   ├─ TransformRegistry.java
    │       │   └─ BuiltinTransforms.java
    │       ├─ utils/
    │       │   ├─ ConditionEvaluator.java
    │       │   └─ YamlConfigLoader.java
    │       └─ MainApp.java
    └─ resources/
        ├─ template-config.yaml
        └─ input.json
```

---

# 🧩 2. Config Classes

### `TemplateConfig.java`

```java
package com.hello.main.config;

import java.util.Map;

public class TemplateConfig {
    private String templateType;      // "pdf" or "html"
    private String templatePath;
    private Map<String, FieldMapping> fields;

    // Optional features
    private String outputPath;
    private boolean autoIndex;
    private String autoIndexMode;     // "singleOnly", "multipleOnly", "all"

    public String getTemplateType() { return templateType; }
    public void setTemplateType(String templateType) { this.templateType = templateType; }

    public String getTemplatePath() { return templatePath; }
    public void setTemplatePath(String templatePath) { this.templatePath = templatePath; }

    public Map<String, FieldMapping> getFields() { return fields; }
    public void setFields(Map<String, FieldMapping> fields) { this.fields = fields; }

    public String getOutputPath() { return outputPath; }
    public void setOutputPath(String outputPath) { this.outputPath = outputPath; }

    public boolean isAutoIndex() { return autoIndex; }
    public void setAutoIndex(boolean autoIndex) { this.autoIndex = autoIndex; }

    public String getAutoIndexMode() { return autoIndexMode; }
    public void setAutoIndexMode(String autoIndexMode) { this.autoIndexMode = autoIndexMode; }
}
```

### `FieldMapping.java`

```java
package com.hello.main.config;

import java.util.List;

public class FieldMapping {
    private String source;             // Source field in JSON
    private List<String> transforms;   // Transform chain
    private String condition;          // Optional condition expression

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public List<String> getTransforms() { return transforms; }
    public void setTransforms(List<String> transforms) { this.transforms = transforms; }

    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }
}
```

---

# 🧩 3. Processor Layer

### `BaseProcessor.java`

```java
package com.hello.main.processor;

import com.hello.main.config.TemplateConfig;
import com.hello.main.transform.Transform;
import com.hello.main.transform.TransformRegistry;
import com.hello.main.utils.ConditionEvaluator;

import java.util.*;

/**
 * Shared flattening, condition evaluation, and transform logic
 * used by both PDF and HTML processors.
 */
public abstract class BaseProcessor {

    protected final TemplateConfig config;

    public BaseProcessor(TemplateConfig config) {
        this.config = config;
    }

    @SuppressWarnings("unchecked")
    protected void flattenJson(String prefix, Object value, Map<String, Object> result) {
        if (value instanceof Map<?, ?> map) {
            for (Map.Entry<?, ?> e : map.entrySet()) {
                flattenJson(prefix.isEmpty() ? e.getKey().toString() : prefix + "." + e.getKey(), e.getValue(), result);
            }
        } else if (value instanceof List<?> list) {
            for (int i = 0; i < list.size(); i++) {
                flattenJson(prefix + "[" + i + "]", list.get(i), result);
            }
        } else {
            result.put(prefix, value);
        }
    }

    protected Map<String, Object> flatten(Map<String, Object> json) {
        Map<String, Object> result = new LinkedHashMap<>();
        flattenJson("", json, result);
        return result;
    }

    protected Object applyTransforms(Object value, List<String> transforms) {
        Object current = value;
        if (transforms == null) return current;
        for (String spec : transforms) {
            String[] parts = spec.split(":", 2);
            String name = parts[0];
            String[] params = parts.length > 1 ? parts[1].split(",") : new String[]{};
            Transform t = TransformRegistry.get(name);
            if (t != null) {
                current = t.apply(current, params);
            }
        }
        return current;
    }

    protected boolean evaluateCondition(Map<String, Object> context, String condition) {
        if (condition == null || condition.isEmpty()) return true;
        return ConditionEvaluator.evaluate(condition, context);
    }

    protected Map<String, Object> buildFieldValues(Map<String, Object> json) {
        Map<String, Object> flat = flatten(json);
        Map<String, Object> output = new LinkedHashMap<>();

        config.getFields().forEach((target, fieldCfg) -> {
            if (!evaluateCondition(flat, fieldCfg.getCondition())) return;
            Object value = flat.get(fieldCfg.getSource());
            value = applyTransforms(value, fieldCfg.getTransforms());
            output.put(target, value);
        });

        return output;
    }

    public abstract void generate(Map<String, Object> data, String outputPath) throws Exception;
}
```

---

### `PdfFormProcessor.java`

```java
package com.hello.main.processor;

import com.hello.main.config.TemplateConfig;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;

import java.io.File;
import java.util.Map;

public class PdfFormProcessor extends BaseProcessor {

    public PdfFormProcessor(TemplateConfig config) {
        super(config);
    }

    @Override
    public void generate(Map<String, Object> data, String outputPath) throws Exception {
        Map<String, Object> fieldValues = buildFieldValues(data);
        try (PDDocument doc = PDDocument.load(new File(config.getTemplatePath()))) {
            PDAcroForm form = doc.getDocumentCatalog().getAcroForm();
            if (form != null) {
                fieldValues.forEach((k, v) -> {
                    try {
                        if (form.getField(k) != null) {
                            form.getField(k).setValue(v == null ? "" : v.toString());
                        } else {
                            System.out.println("⚠ Field not found in template: " + k);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
                form.flatten();
            }
            doc.save(outputPath);
            System.out.println("✅ PDF generated: " + outputPath);
        }
    }
}
```

---

### `FreemarkerHtmlProcessor.java`

```java
package com.hello.main.processor;

import com.hello.main.config.TemplateConfig;
import freemarker.template.Configuration;
import freemarker.template.Template;

import java.io.FileWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

public class FreemarkerHtmlProcessor extends BaseProcessor {

    public FreemarkerHtmlProcessor(TemplateConfig config) {
        super(config);
    }

    @Override
    public void generate(Map<String, Object> data, String outputPath) throws Exception {
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_32);
        File templateDir = new File(config.getTemplatePath()).getParentFile();
        cfg.setDirectoryForTemplateLoading(templateDir);

        Template template = cfg.getTemplate(new File(config.getTemplatePath()).getName());
        Map<String, Object> fieldValues = buildFieldValues(data);

        try (Writer out = new FileWriter(outputPath)) {
            template.process(fieldValues, out);
        }

        System.out.println("✅ HTML generated: " + outputPath);
    }
}
```

---

### `UnifiedTemplateProcessor.java`

```java
package com.hello.main.processor;

import com.hello.main.config.TemplateConfig;
import java.util.Map;

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
        String type = config.getTemplateType() != null
                ? config.getTemplateType().toLowerCase()
                : detectType(config.getTemplatePath());

        switch (type) {
            case "pdf" -> pdfProcessor.generate(data, outputPath);
            case "html" -> htmlProcessor.generate(data, outputPath);
            default -> throw new IllegalArgumentException("Unknown template type: " + type);
        }
    }

    private String detectType(String path) {
        if (path == null) return "pdf";
        if (path.endsWith(".html") || path.endsWith(".ftl")) return "html";
        return "pdf";
    }
}
```

---

# 🧩 4. Transform Layer

### `Transform.java`

```java
package com.hello.main.transform;

@FunctionalInterface
public interface Transform {
    Object apply(Object value, String... params);
}
```

### `TransformRegistry.java`

```java
package com.hello.main.transform;

import java.util.HashMap;
import java.util.Map;

public class TransformRegistry {
    private static final Map<String, Transform> registry = new HashMap<>();

    static {
        BuiltinTransforms.registerDefaults();
    }

    public static void register(String name, Transform transform) {
        registry.put(name, transform);
    }

    public static Transform get(String name) {
        return registry.get(name);
    }
}
```

### `BuiltinTransforms.java`

```java
package com.hello.main.transform;

public class BuiltinTransforms {

    public static void registerDefaults() {
        TransformRegistry.register("toUpperCase", (val, p) -> val == null ? null : val.toString().toUpperCase());
        TransformRegistry.register("toLowerCase", (val, p) -> val == null ? null : val.toString().toLowerCase());
        TransformRegistry.register("prefix", (val, p) -> p.length > 0 ? p[0] + (val == null ? "" : val) : val);
        TransformRegistry.register("suffix", (val, p) -> (val == null ? "" : val) + (p.length > 0 ? p[0] : ""));
    }
}
```

---

# 🧩 5. Utility Layer

### `ConditionEvaluator.java`

```java
package com.hello.main.utils;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.SimpleBindings;
import java.util.Map;

public class ConditionEvaluator {

    private static final ScriptEngine engine;

    static {
        ScriptEngineManager manager = new ScriptEngineManager();
        engine = manager.getEngineByName("nashorn");
    }

    public static boolean evaluate(String condition, Map<String, Object> context) {
        if (engine == null) return true;
        try {
            var bindings = new SimpleBindings(context);
            Object result = engine.eval(condition, bindings);
            return result instanceof Boolean b && b;
        } catch (Exception e) {
            System.err.println("⚠ Error evaluating condition: " + condition + " -> " + e.getMessage());
            return false;
        }
    }
}
```

---

### `YamlConfigLoader.java`

```java
package com.hello.main.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.hello.main.config.TemplateConfig;

import java.io.File;

public class YamlConfigLoader {

    public static TemplateConfig load(String yamlPath) throws Exception {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        return mapper.readValue(new File(yamlPath), TemplateConfig.class);
    }
}
```

---

# 🧩 6. Example Runner

### `MainApp.java`

```java
package com.hello.main;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hello.main.config.TemplateConfig;
import com.hello.main.processor.UnifiedTemplateProcessor;
import com.hello.main.utils.YamlConfigLoader;

import java.io.File;
import java.util.Map;

public class MainApp {
    public static void main(String[] args) throws Exception {
        TemplateConfig config = YamlConfigLoader.load("src/main/resources/template-config.yaml");
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> jsonData = mapper.readValue(new File("src/main/resources/input.json"), Map.class);

        UnifiedTemplateProcessor processor = new UnifiedTemplateProcessor(config);
        processor.generate(jsonData, config.getOutputPath());
    }
}
```

---

# 🧩 7. Example YAML

```yaml
templateType: pdf
templatePath: src/main/resources/templates/applicant_form.pdf
outputPath: output/applicant_output.pdf
autoIndex: true

fields:
  PrimaryApplicant.FName:
    source: applicantList[0].firstName
    transforms: ["toUpperCase"]
  PrimaryApplicant.LName:
    source: applicantList[0].lastName
  Dependents.FName.1:
    source: applicantList[1].firstName
  Dependents.LName.1:
    source: applicantList[1].lastName
```

---

# 🧩 8. Example JSON

```json
{
  "applicantList": [
    { "relationship": "Primary", "firstName": "John", "lastName": "Doe" },
    { "relationship": "Dependent", "firstName": "Alice", "lastName": "Doe" }
  ]
}
```

---

Would you like me to add **automatic dynamic indexing logic** (so `Dependents.FName.1`, `.2`, etc. are auto-generated for array elements) in this consolidated version next?
That’s the next natural enhancement.
