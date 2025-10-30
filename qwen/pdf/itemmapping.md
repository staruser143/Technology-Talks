Certainly! Below is the **updated `FieldMapping` class** that fully supports:

- **Single-value mappings** (for non-repeating fields like `applicant.fname.1`)
- **Repeating mappings** (for arrays like dependents, beneficiaries, etc.)
- **Global filters** (applied to the entire mapping)
- **Item-level filters** (applied per array item in repeating sections)
- **Transformations at both levels**

This design keeps the configuration **clean, explicit, and backward-compatible**.

---

### ✅ `FieldMapping.java`

```java
package com.example.templatemerge.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a field mapping configuration from source JSON to target template field(s).
 * Supports both single-value and repeating (array-based) mappings.
 */
public class FieldMapping {

    // ===== Single-value mapping (non-repeating) =====
    /**
     * JsonPath expression to extract a single value from the root JSON.
     * Example: "$.applicant.firstName"
     */
    private String sourceField;

    /**
     * Exact target field name in the template (e.g., PDF AcroForm field).
     * Example: "applicant.fname.1"
     */
    private String targetField;

    // ===== Repeating mapping (array-based) =====
    /**
     * JsonPath to an array in the source JSON.
     * Example: "$.dependents"
     */
    private String sourceArray;

    /**
     * Filters applied to each item in the array before mapping.
     * Only items that pass ALL conditions will be mapped.
     * Example: include only active dependents under age 26.
     */
    private List<FilterCondition> itemFilters = new ArrayList<>();

    /**
     * List of per-item field mappings for repeating sections.
     * Each defines how to map a property of an array item to an indexed target field.
     */
    private List<ItemMapping> itemMappings = new ArrayList<>();

    /**
     * Optional: maximum number of repetitions to map (for safety).
     * Prevents overflow if PDF has limited slots (e.g., max 5 dependents).
     */
    private Integer maxRepetitions;

    // ===== Shared properties =====
    /**
     * Filters applied at the mapping level (before any extraction).
     * If this mapping is for a repeating section, these filters apply to the ROOT JSON.
     * Example: only map dependents if applicant.country == "US"
     */
    private List<FilterCondition> filters = new ArrayList<>();

    /**
     * Transformations applied to the final value (for single-value mappings).
     * For repeating mappings, transformations are defined per {@link ItemMapping}.
     */
    private List<TransformSpec> transforms = new ArrayList<>();

    // ===== Utility methods =====

    /**
     * @return true if this mapping represents a repeating section (array-based)
     */
    public boolean isRepeating() {
        return sourceArray != null && !sourceArray.trim().isEmpty();
    }

    /**
     * Validates that the mapping is correctly configured.
     * Throws {@link IllegalStateException} if invalid.
     */
    public void validate() {
        if (isRepeating()) {
            if (sourceField != null || targetField != null) {
                throw new IllegalStateException(
                    "For repeating mappings, use 'sourceArray' and 'itemMappings' — " +
                    "'sourceField' and 'targetField' must be null."
                );
            }
            if (itemMappings == null || itemMappings.isEmpty()) {
                throw new IllegalStateException(
                    "Repeating mapping requires non-empty 'itemMappings'."
                );
            }
        } else {
            if (sourceField == null || sourceField.trim().isEmpty()) {
                throw new IllegalStateException("'sourceField' is required for single-value mappings.");
            }
            if (targetField == null || targetField.trim().isEmpty()) {
                throw new IllegalStateException("'targetField' is required for single-value mappings.");
            }
            if (sourceArray != null) {
                throw new IllegalStateException(
                    "Single-value mapping must not define 'sourceArray'."
                );
            }
        }
    }

    // ===== Getters and Setters =====

    public String getSourceField() {
        return sourceField;
    }

    public void setSourceField(String sourceField) {
        this.sourceField = sourceField;
    }

    public String getTargetField() {
        return targetField;
    }

    public void setTargetField(String targetField) {
        this.targetField = targetField;
    }

    public String getSourceArray() {
        return sourceArray;
    }

    public void setSourceArray(String sourceArray) {
        this.sourceArray = sourceArray;
    }

    public List<FilterCondition> getItemFilters() {
        return itemFilters;
    }

    public void setItemFilters(List<FilterCondition> itemFilters) {
        this.itemFilters = itemFilters != null ? itemFilters : new ArrayList<>();
    }

    public List<ItemMapping> getItemMappings() {
        return itemMappings;
    }

    public void setItemMappings(List<ItemMapping> itemMappings) {
        this.itemMappings = itemMappings != null ? itemMappings : new ArrayList<>();
    }

    public Integer getMaxRepetitions() {
        return maxRepetitions;
    }

    public void setMaxRepetitions(Integer maxRepetitions) {
        this.maxRepetitions = maxRepetitions;
    }

    public List<FilterCondition> getFilters() {
        return filters;
    }

    public void setFilters(List<FilterCondition> filters) {
        this.filters = filters != null ? filters : new ArrayList<>();
    }

    public List<TransformSpec> getTransforms() {
        return transforms;
    }

    public void setTransforms(List<TransformSpec> transforms) {
        this.transforms = transforms != null ? transforms : new ArrayList<>();
    }
}
```

---

### 📌 Companion: `ItemMapping.java` (for completeness)

```java
package com.example.templatemerge.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Defines how to map a single property of an array item to a target field
 * in a repeating section.
 */
public class ItemMapping {

    /**
     * JsonPath relative to the array item.
     * Example: "$.firstName" (evaluated against each dependent object)
     */
    private String sourceField;

    /**
     * Template for the target field name, with "{index}" placeholder.
     * Example: "dependent.fname.{index}" → becomes "dependent.fname.1"
     */
    private String targetFieldTemplate;

    /**
     * Optional: transformations to apply to this field's value.
     */
    private List<TransformSpec> transforms = new ArrayList<>();

    /**
     * Optional: field-level filters (rare; prefer itemFilters in FieldMapping)
     */
    private List<FilterCondition> filters = new ArrayList<>();

    // Getters and Setters

    public String getSourceField() {
        return sourceField;
    }

    public void setSourceField(String sourceField) {
        this.sourceField = sourceField;
    }

    public String getTargetFieldTemplate() {
        return targetFieldTemplate;
    }

    public void setTargetFieldTemplate(String targetFieldTemplate) {
        this.targetFieldTemplate = targetFieldTemplate;
    }

    public List<TransformSpec> getTransforms() {
        return transforms;
    }

    public void setTransforms(List<TransformSpec> transforms) {
        this.transforms = transforms != null ? transforms : new ArrayList<>();
    }

    public List<FilterCondition> getFilters() {
        return filters;
    }

    public void setFilters(List<FilterCondition> filters) {
        this.filters = filters != null ? filters : new ArrayList<>();
    }
}
```

---

### 📄 Example YAML Usage

```yaml
mappings:
  # Single-value field
  - sourceField: "$.applicant.ssn"
    targetField: "ssn.1"
    filters:
      - field: "$.applicant.country"
        operator: EQ
        value: "US"

  # Repeating section
  - sourceArray: "$.dependents"
    filters:                     # ← applied to ROOT JSON (e.g., only if form type = 'family')
      - field: "$.formType"
        operator: EQ
        value: "family"
    itemFilters:                 # ← applied to EACH dependent
      - field: "$.active"
        operator: EQ
        value: true
      - field: "$.age"
        operator: LT
        value: 26
    maxRepetitions: 4
    itemMappings:
      - sourceField: "$.firstName"
        targetFieldTemplate: "dependent.fname.{index}"
        transforms:
          - type: uppercase
      - sourceField: "$.dob"
        targetFieldTemplate: "dependent.dob.{index}"
        transforms:
          - type: custom
            name: formatDate
            params:
              input: "yyyy-MM-dd"
              output: "MM/dd/yyyy"
```

---

### ✅ Key Design Notes

| Feature | Location | Purpose |
|-------|--------|--------|
| **Root-level filters** | `FieldMapping.filters` | Skip entire mapping based on root data |
| **Per-item filters** | `FieldMapping.itemFilters` | Skip individual array items |
| **Per-field transforms** | `ItemMapping.transforms` | Format each field independently |
| **Index-based naming** | `targetFieldTemplate` | Supports `xxx.{index}` → `xxx.1`, `xxx.2`, etc. |
| **Validation** | `validate()` method | Fail fast on misconfiguration |

---

This `FieldMapping` class gives you a **flexible, safe, and expressive** way to describe complex PDF/HTML merging logic — all driven by YAML.

Let me know if you'd like the corresponding `MergeConfig` class or Spring Boot `@ConfigurationProperties` binding!