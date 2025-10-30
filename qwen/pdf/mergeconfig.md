Absolutely! Below are the **complete, production-ready `MergeConfig` and supporting model classes**, designed to work seamlessly with your YAML-driven, JsonPath-powered, index-aware template merging library.

These classes:
- Use **Lombok** for brevity (optional — plain Java version also provided)
- Are **Spring Boot `@ConfigurationProperties`-ready**
- Support **validation**
- Work with **Jackson + YAML**

---

## ✅ Option 1: With Lombok (Recommended)

### 📦 Dependencies (Maven)

```xml
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <optional>true</optional>
</dependency>
```

### 🧩 `MergeConfig.java`

```java
package com.example.templatemerge.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Top-level configuration for merging JSON data into a template (PDF/HTML).
 * Loaded from YAML.
 */
@Data
@NoArgsConstructor
@Component
@ConfigurationProperties(prefix = "template-merge") // Optional: if binding from application.yml
public class MergeConfig {

    /**
     * Type of template: PDF or HTML
     */
    private TemplateType templateType;

    /**
     * Path to source template file.
     * Supports: classpath:, file:, or relative path.
     * Example: "classpath:templates/applicant.pdf"
     */
    private String templatePath;

    /**
     * Output file path.
     * Example: "/tmp/output.pdf"
     */
    private String outputPath;

    /**
     * List of field mappings (single-value or repeating).
     */
    private List<FieldMapping> mappings = new ArrayList<>();

    /**
     * Validates the configuration.
     */
    public void validate() {
        if (templateType == null) {
            throw new IllegalStateException("'templateType' is required");
        }
        if (templatePath == null || templatePath.trim().isEmpty()) {
            throw new IllegalStateException("'templatePath' is required");
        }
        if (outputPath == null || outputPath.trim().isEmpty()) {
            throw new IllegalStateException("'outputPath' is required");
        }
        if (mappings == null || mappings.isEmpty()) {
            throw new IllegalStateException("At least one mapping is required");
        }
        for (int i = 0; i < mappings.size(); i++) {
            try {
                mappings.get(i).validate();
            } catch (IllegalStateException e) {
                throw new IllegalStateException("Invalid mapping at index " + i + ": " + e.getMessage(), e);
            }
        }
    }
}
```

---

### 🧩 Supporting Enums & Models

#### `TemplateType.java`

```java
package com.example.templatemerge.model;

public enum TemplateType {
    PDF,
    HTML
}
```

#### `FilterCondition.java`

```java
package com.example.templatemerge.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class FilterCondition {
    private String field;      // JsonPath: "$.age", "$.role"
    private String operator;   // EQ, NE, GT, IN, NOT_NULL, etc.
    private Object value;      // Can be String, Number, List, etc.

    public FilterCondition(String field, String operator, Object value) {
        this.field = field;
        this.operator = operator;
        this.value = value;
    }
}
```

#### `TransformSpec.java`

```java
package com.example.templatemerge.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
public class TransformSpec {
    private String type;       // "uppercase", "custom", etc.
    private String name;       // for custom: bean name (e.g., "formatDate")
    private Map<String, Object> params = new HashMap<>();
}
```

> ✅ **Note**: `ItemMapping` and `FieldMapping` were provided in the previous answer. Include them as-is.

---

## ✅ Option 2: Plain Java (No Lombok)

If you prefer **no Lombok**, here’s `MergeConfig` in plain Java:

```java
package com.example.templatemerge.model;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "template-merge")
public class MergeConfig {

    private TemplateType templateType;
    private String templatePath;
    private String outputPath;
    private List<FieldMapping> mappings = new ArrayList<>();

    // Getters
    public TemplateType getTemplateType() { return templateType; }
    public String getTemplatePath() { return templatePath; }
    public String getOutputPath() { return outputPath; }
    public List<FieldMapping> getMappings() { return mappings; }

    // Setters
    public void setTemplateType(TemplateType templateType) { this.templateType = templateType; }
    public void setTemplatePath(String templatePath) { this.templatePath = templatePath; }
    public void setOutputPath(String outputPath) { this.outputPath = outputPath; }
    public void setMappings(List<FieldMapping> mappings) { this.mappings = mappings != null ? mappings : new ArrayList<>(); }

    // Validation
    public void validate() {
        if (templateType == null) {
            throw new IllegalStateException("'templateType' is required");
        }
        if (templatePath == null || templatePath.trim().isEmpty()) {
            throw new IllegalStateException("'templatePath' is required");
        }
        if (outputPath == null || outputPath.trim().isEmpty()) {
            throw new IllegalStateException("'outputPath' is required");
        }
        if (mappings == null || mappings.isEmpty()) {
            throw new IllegalStateException("At least one mapping is required");
        }
        for (int i = 0; i < mappings.size(); i++) {
            try {
                mappings.get(i).validate();
            } catch (IllegalStateException e) {
                throw new IllegalStateException("Invalid mapping at index " + i + ": " + e.getMessage(), e);
            }
        }
    }
}
```

> Repeat similarly for `FilterCondition`, `TransformSpec`, etc., with full getter/setter boilerplate.

---

## 📄 Example `merge-config.yaml`

```yaml
templateType: PDF
templatePath: classpath:forms/applicant_form.pdf
outputPath: /tmp/filled_form.pdf
mappings:
  - sourceField: "$.applicant.firstName"
    targetField: "applicant.fname.1"
    filters:
      - field: "$.metadata.submissionType"
        operator: EQ
        value: "individual"

  - sourceArray: "$.dependents"
    filters:
      - field: "$.applicant.country"
        operator: EQ
        value: "US"
    itemFilters:
      - field: "$.age"
        operator: LT
        value: 26
      - field: "$.student"
        operator: EQ
        value: true
    maxRepetitions: 3
    itemMappings:
      - sourceField: "$.firstName"
        targetFieldTemplate: "dependent.fname.{index}"
        transforms:
          - type: uppercase
      - sourceField: "$.ssn"
        targetFieldTemplate: "dependent.ssn.{index}"
```

---

## 🧪 Loading `MergeConfig` from YAML File (Not Spring Boot Properties)

If you're **loading from an external YAML file** (not `application.yml`), use Jackson:

```java
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

public MergeConfig loadMergeConfig(Path yamlPath) throws IOException {
    ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
    MergeConfig config = yamlMapper.readValue(yamlPath.toFile(), MergeConfig.class);
    config.validate(); // fail fast
    return config;
}
```

> ✅ No need for `@ConfigurationProperties` in this case. Remove the annotation if using file-based loading.

---

## 🧩 Spring Boot Auto-Configuration Tip

If you want to **also support `application.yml` binding**, keep `@ConfigurationProperties`.  
If you only load from external files, **remove `@Component` and `@ConfigurationProperties`** to avoid Spring trying to bind it at startup.

---

## ✅ Summary of Full Model Package

```
com.example.templatemerge.model
├── MergeConfig.java
├── TemplateType.java
├── FieldMapping.java        ← (from previous answer)
├── ItemMapping.java         ← (from previous answer)
├── FilterCondition.java
└── TransformSpec.java
```

All classes are:
- **Serializable** (via Jackson)
- **Validatable**
- **YAML-friendly**
- **Extensible**

---

Let me know if you'd like:
- A `@ConfigurationProperties` validator with `javax.validation`
- A full Spring Boot starter example
- Unit tests for YAML deserialization

Happy coding! 🚀