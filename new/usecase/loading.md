Great question! You should **always load your YAML into `MappingConfig`**, not `TemplateConfig`. Let me explain why and show you the correct approach.

---

## 🎯 The Correct Loading Strategy

### ✅ **ALWAYS load into `MappingConfig`**

Your YAML structure has **two possible formats**:

### Format 1: Single Template (Legacy/Backward Compatible)
```yaml
contexts:
  primary:
    from: applicants
    filter: { type: PRIMARY }
    first: true

mappings:
  - source: primary.demographics.firstName
    target: "firstName"
```

### Format 2: Multiple Templates (New)
```yaml
contexts:
  primary:
    from: applicants
    filter: { type: PRIMARY }
    first: true

templates:
  enrollmentForm:
    pdfTemplatePath: "enrollment.pdf"
    outputPath: "output.pdf"
    mappings:
      - source: primary.demographics.firstName
        target: "firstName"
```

**Both formats should be loaded into `MappingConfig`** because:
- `MappingConfig` contains **all top-level properties** (`contexts`, `mappings`, `templates`)
- `TemplateConfig` is just a **nested object** within `MappingConfig`
- This maintains **backward compatibility**

---

## 🔧 Correct Loading Code

### Step 1: Your YAML Loading Method
```java
// ALWAYS load into MappingConfig
public MappingConfig loadConfig(String yamlPath) throws Exception {
    Yaml yaml = new Yaml(new Constructor(MappingConfig.class));
    try (InputStream in = new FileInputStream(yamlPath)) {
        return yaml.load(in);
    }
}
```

### Step 2: Process Based on What's Present
```java
public void processConfig(String yamlPath, String jsonInput) throws Exception {
    MappingConfig config = loadConfig(yamlPath); // ← ALWAYS MappingConfig
    
    if (config.hasTemplates()) {
        // Handle multiple templates
        processMultipleTemplates(config, jsonInput);
    } else {
        // Handle single template (legacy mode)
        processSingleTemplate(config, jsonInput);
    }
}

private void processMultipleTemplates(MappingConfig config, String jsonInput) {
    // config.getTemplates() contains TemplateConfig objects
    for (Map.Entry<String, TemplateConfig> entry : config.getTemplates().entrySet()) {
        TemplateConfig templateConfig = entry.getValue();
        List<FieldMapping> mappings = templateConfig.getMappings();
        // Process mappings for this template
    }
}

private void processSingleTemplate(MappingConfig config, String jsonInput) {
    // config.getMappings() contains FieldMapping objects
    List<FieldMapping> mappings = config.getMappings();
    // Process mappings for single template
}
```

---

## 📁 Model Structure Recap

### `MappingConfig.java` (Root Object)
```java
public class MappingConfig {
    private Map<String, ContextDef> contexts;      // ✅ Present in both formats
    private List<FieldMapping> mappings;          // ✅ Only in single template format
    private Map<String, TemplateConfig> templates; // ✅ Only in multiple template format
    
    public boolean hasTemplates() {
        return templates != null && !templates.isEmpty();
    }
}
```

### `TemplateConfig.java` (Nested Object)
```java
public class TemplateConfig {
    private String pdfTemplatePath;
    private String outputPath;
    private List<FieldMapping> mappings; // ← This is the same FieldMapping list as single template
}
```

---

## 🧪 Loading Examples

### Example 1: Single Template YAML
```yaml
contexts:
  primary:
    from: applicants
    filter: { type: PRIMARY }
    first: true
mappings:
  - source: primary.demographics.firstName
    target: "firstName"
```

**Loading Result:**
```java
MappingConfig config = loadConfig("single.yaml");
// config.getContexts() → has data
// config.getMappings() → has 1 FieldMapping  
// config.getTemplates() → null
// config.hasTemplates() → false
```

### Example 2: Multiple Templates YAML
```yaml
contexts:
  primary:
    from: applicants
    filter: { type: PRIMARY }
    first: true
templates:
  enrollment:
    pdfTemplatePath: "enrollment.pdf"
    mappings:
      - source: primary.demographics.firstName
        target: "firstName"
```

**Loading Result:**
```java
MappingConfig config = loadConfig("multi.yaml");
// config.getContexts() → has data  
// config.getMappings() → null
// config.getTemplates() → has 1 TemplateConfig
// config.hasTemplates() → true
```

---

## ⚠️ Common Mistakes to Avoid

### ❌ **Mistake 1: Loading into TemplateConfig**
```java
// WRONG - This will fail!
Yaml yaml = new Yaml(new Constructor(TemplateConfig.class));
TemplateConfig config = yaml.load(inputStream); // ❌ Fails because YAML has "contexts" and "templates" root keys
```

### ❌ **Mistake 2: Using Different Loading Logic**
```java
// WRONG - Don't try to detect format and load differently
if (yamlContainsTemplates(yamlContent)) {
    loadAsTemplateConfig(); // ❌ No!
} else {
    loadAsMappingConfig(); // ❌ Inconsistent!
}
```

### ✅ **Correct Approach: Always load as MappingConfig**
```java
// ALWAYS do this
MappingConfig config = loadConfig(yamlPath);
if (config.hasTemplates()) {
    // Handle multiple templates
} else {
    // Handle single template
}
```

---

## 🔧 Complete Working Example

### `PdfFieldMapper.java`
```java
public class PdfFieldMapper {
    
    public void processYamlConfig(String yamlPath, String jsonInput) throws Exception {
        // ✅ ALWAYS load into MappingConfig
        MappingConfig config = loadConfig(yamlPath);
        DocumentContext rootJson = JsonPath.parse(jsonInput);
        
        // Build shared contexts
        Map<String, Object> contextCache = buildContextCache(config, rootJson);
        
        if (config.hasTemplates()) {
            // Process multiple templates
            for (TemplateConfig templateConfig : config.getTemplates().values()) {
                MappingResult result = mapFields(templateConfig.getMappings(), rootJson, contextCache);
                if (!dryRun) {
                    mergeToPdf(result, templateConfig.getPdfTemplatePath(), templateConfig.getOutputPath());
                }
            }
        } else {
            // Process single template (you'll need PDF paths from somewhere else)
            MappingResult result = mapFields(config.getMappings(), rootJson, contextCache);
            if (!dryRun) {
                // For single template mode, you might pass PDF paths as method parameters
                // mergeToPdf(result, pdfTemplatePath, outputPath);
            }
        }
    }
    
    private MappingConfig loadConfig(String yamlPath) throws Exception {
        Yaml yaml = new Yaml(new Constructor(MappingConfig.class));
        try (InputStream in = new FileInputStream(yamlPath)) {
            return yaml.load(in);
        }
    }
    
    private Map<String, Object> buildContextCache(MappingConfig config, DocumentContext rootJson) {
        if (config.getContexts() == null) {
            return new HashMap<>();
        }
        Map<String, String> contextPaths = buildContextJsonPaths(config.getContexts());
        return evaluateContexts(contextPaths, rootJson);
    }
    
    private MappingResult mapFields(List<FieldMapping> mappings, DocumentContext rootJson, 
                                   Map<String, Object> contextCache) {
        MappingResult result = new MappingResult();
        for (FieldMapping mapping : mappings) {
            // Your existing field processing logic
            processFieldMapping(mapping, rootJson, contextCache, result);
        }
        return result;
    }
}
```

---

## ✅ Summary

| Question | Answer |
|---------|--------|
| **Which class to load YAML into?** | ✅ **Always `MappingConfig`** |
| **Why not `TemplateConfig`?** | ❌ `TemplateConfig` is nested, doesn't contain `contexts` |
| **How to handle both formats?** | ✅ Use `config.hasTemplates()` to branch logic |
| **Is this backward compatible?** | ✅ Yes! Single template format still works |

This approach gives you **maximum flexibility** while maintaining **clean, consistent code** that handles both single and multiple template scenarios correctly.

Would you like me to provide a **complete working example** with error handling or show how to handle **missing PDF template paths** in single template mode?