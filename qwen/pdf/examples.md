Absolutely! Below are **real-world YAML configuration examples** covering different data scenarios, filtering strategies, and template types (PDF + HTML). Each example includes:

- **Source JSON structure**
- **YAML mapping configuration**
- **Explanation of key concepts**

---

## 📁 Common Source Data Structure

All examples use this base JSON:
```json
{
  "metadata": {
    "submissionId": "APP-2025-001",
    "date": "2025-01-15",
    "formType": "family"
  },
  "applicants": [
    {
      "id": "A1",
      "relationship": "primary",
      "firstName": "John",
      "lastName": "Doe",
      "ssn": "123-45-6789",
      "dob": "1980-05-20",
      "income": 75000,
      "country": "US",
      "active": true
    },
    {
      "id": "A2",
      "relationship": "spouse",
      "firstName": "Jane",
      "lastName": "Doe",
      "ssn": "987-65-4321",
      "dob": "1982-08-12",
      "income": 65000,
      "country": "US",
      "active": true
    },
    {
      "id": "A3",
      "relationship": "dependent",
      "firstName": "Alice",
      "lastName": "Doe",
      "dob": "2015-04-10",
      "age": 9,
      "student": true,
      "active": true
    },
    {
      "id": "A4",
      "relationship": "dependent",
      "firstName": "Bob",
      "lastName": "Doe",
      "dob": "2018-11-22",
      "age": 6,
      "student": false,
      "active": false
    }
  ],
  "assets": [
    {
      "type": "real_estate",
      "value": 350000,
      "location": "CA"
    },
    {
      "type": "vehicle",
      "value": 25000,
      "location": "CA"
    }
  ]
}
```

---

## 📄 Example 1: Basic Single-Value Mapping (PDF)

**Scenario**: Map primary applicant's name to PDF fields

```yaml
templates:
  - name: "basic-pdf"
    type: PDF
    mappings:
      # Simple field mapping
      - sourceField: "metadata.submissionId"
        targetField: "submission_id.1"
      
      # Primary applicant (single object from array)
      - sourceObject: "applicants"
        itemFilters:
          - field: "relationship"
            operator: EQ
            value: "primary"
        fieldMappings:
          - sourceField: "firstName"
            targetField: "primary.fname.1"
          - sourceField: "lastName"
            targetField: "primary.lname.1"
          - sourceField: "ssn"
            targetField: "primary.ssn.1"
            transforms:
              - type: custom
                name: maskSsn
```

**Key Concepts**:
- `sourceObject` + `itemFilters` to extract single applicant
- Transform applied to SSN field
- Simple `sourceField` for metadata

---

## 🌐 Example 2: Filtered List for HTML Template

**Scenario**: Display only **active dependents** in HTML summary

```yaml
templates:
  - name: "summary-html"
    type: HTML
    mappings:
      # Entire applicants list (for reference)
      - sourceField: "applicants"
        targetField: "allApplicants"
      
      # ONLY ACTIVE DEPENDENTS
      - sourceField: "applicants[relationship=dependent][active=true]"
        targetField: "activeDependents"
      
      # Metadata
      - sourceField: "metadata"
        targetField: "metadata"
```

**FreeMarker Template**:
```html
<h2>Active Dependents</h2>
<#list activeDependents as dep>
  <p>${dep.firstName} (Age: ${dep.age})</p>
</#list>
```

**Key Concepts**:
- Simplified path with **multiple filters**: `[relationship=dependent][active=true]`
- Entire objects passed to HTML (rich data model)
- No flattening needed for HTML

---

## 📄 Example 3: Conditional PDF Mapping

**Scenario**: Only include spouse fields if applicant is from US and form type is "family"

```yaml
templates:
  - name: "conditional-pdf"
    type: PDF
    mappings:
      # Global filter: only process if conditions met
      - sourceObject: "applicants"
        filters:  # ← Applied to ROOT JSON
          - field: "metadata.formType"
            operator: EQ
            value: "family"
          - field: "applicants[relationship=primary].country"
            operator: EQ
            value: "US"
        itemFilters:
          - field: "relationship"
            operator: EQ
            value: "spouse"
        fieldMappings:
          - sourceField: "firstName"
            targetField: "spouse.fname.1"
          - sourceField: "lastName"
            targetField: "spouse.lname.1"
```

**Key Concepts**:
- `filters` on mapping → applied to **root JSON**
- `itemFilters` → applied to **array items**
- Spouse fields only appear if both conditions are true

---

## 📊 Example 4: Repeating Assets with Transformations (PDF)

**Scenario**: Map real estate assets to PDF with formatted values

```yaml
templates:
  - name: "assets-pdf"
    type: PDF
    mappings:
      - sourceArray: "assets"
        itemFilters:
          - field: "type"
            operator: EQ
            value: "real_estate"
        maxRepetitions: 3
        itemMappings:
          - sourceField: "value"
            targetFieldTemplate: "asset.value.{index}"
            transforms:
              - type: custom
                name: currencyFormat
                params:
                  currency: "USD"
          - sourceField: "location"
            targetFieldTemplate: "asset.location.{index}"
```

**Key Concepts**:
- `sourceArray` + `itemFilters` for specific asset types
- Custom transformation with parameters
- `maxRepetitions` prevents overflow

---

## 🌐 Example 5: Complex HTML with Multiple Filtered Lists

**Scenario**: Create HTML report with separate sections for different applicant types

```yaml
templates:
  - name: "report-html"
    type: HTML
    mappings:
      # Primary applicant (single object)
      - sourceField: "applicants[relationship=primary]"
        targetField: "primaryApplicant"
      
      # Spouse (single object)
      - sourceField: "applicants[relationship=spouse]"
        targetField: "spouseApplicant"
      
      # Active dependents (list)
      - sourceField: "applicants[relationship=dependent][active=true]"
        targetField: "activeDependents"
      
      # All dependents (list)
      - sourceField: "applicants[relationship=dependent]"
        targetField: "allDependents"
      
      # High-value assets
      - sourceField: "assets[value>100000]"
        targetField: "highValueAssets"
      
      # Metadata
      - sourceField: "metadata.submissionId"
        targetField: "submissionId"
```

**FreeMarker Template**:
```html
<h1>Application Report</h1>

<h2>Primary: ${primaryApplicant.firstName!""}</h2>

<#if spouseApplicant??>
  <h2>Spouse: ${spouseApplicant.firstName!""}</h2>
</#if>

<h2>Active Dependents (${activeDependents?size})</h2>
<#list activeDependents as dep>
  <p>${dep.firstName} - ${dep.age} years old</p>
</#list>

<h2>High Value Assets</h2>
<#list highValueAssets as asset>
  <p>${asset.type}: ${asset.value}</p>
</#list>
```

**Key Concepts**:
- Multiple filtered lists for different sections
- Numeric filtering: `[value>100000]`
- Null-safe template rendering

---

## 📄 Example 6: Boolean and Numeric Filtering (PDF)

**Scenario**: Map high-income primary applicant only if income > 70000 and active

```yaml
templates:
  - name: "high-income-pdf"
    type: PDF
    mappings:
      - sourceObject: "applicants"
        itemFilters:
          - field: "relationship"
            operator: EQ
            value: "primary"
          - field: "income"
            operator: GT
            value: 70000
          - field: "active"
            operator: EQ
            value: true
        fieldMappings:
          - sourceField: "firstName"
            targetField: "high_income_applicant.1"
          - sourceField: "income"
            targetField: "income_amount.1"
            transforms:
              - type: custom
                name: currencyFormat
```

**Key Concepts**:
- Multiple `itemFilters` with **different operators** (EQ, GT)
- Boolean filtering works with `true`/`false`
- Transform applied to numeric field

---

## 🌐 Example 7: Default Values and Null Handling (HTML)

**Scenario**: Handle missing spouse gracefully in HTML

```yaml
templates:
  - name: "robust-html"
    type: HTML
    mappings:
      - sourceField: "applicants[relationship=primary]"
        targetField: "primary"
      
      # Spouse might not exist - that's OK
      - sourceField: "applicants[relationship=spouse]"
        targetField: "spouse"
        transforms:
          - type: default
            params:
              defaultValue: {}
      
      # Always provide empty list for dependents
      - sourceField: "applicants[relationship=dependent]"
        targetField: "dependents"
        transforms:
          - type: default
            params:
              defaultValue: []
```

**Custom Default Transformer**:
```java
@Component("default")
public class DefaultTransformer implements CustomTransformer {
    @Override
    public Object transform(Object input, Map<String, Object> params) {
        if (input == null || (input instanceof List && ((List<?>) input).isEmpty())) {
            return params.get("defaultValue");
        }
        return input;
    }
}
```

**Key Concepts**:
- Default values prevent null pointer errors in templates
- Empty lists ensure `<#list>` works without null checks
- Graceful handling of optional data

---

## 📊 Example 8: IN Operator for Multiple Values

**Scenario**: Map applicants from specific countries

```yaml
templates:
  - name: "international-pdf"
    type: PDF
    mappings:
      - sourceArray: "applicants"
        itemFilters:
          - field: "country"
            operator: IN
            value: "US,CA,MX"  # comma-separated list
        itemMappings:
          - sourceField: "firstName"
            targetFieldTemplate: "intl_applicant.{index}"
```

**Alternative with YAML list**:
```yaml
          - field: "country"
            operator: IN
            value: 
              - "US"
              - "CA"
              - "MX"
```

**Key Concepts**:
- `IN` operator supports both comma-separated strings and YAML lists
- Useful for multi-value filtering

---

## 🔍 Summary of Key Patterns

| Pattern | YAML Property | Use Case |
|--------|---------------|----------|
| **Single field** | `sourceField` + `targetField` | Simple values, filtered lists |
| **Single object** | `sourceObject` + `fieldMappings` | Extract one item from array |
| **Repeating fields** | `sourceArray` + `itemMappings` | PDF indexed fields |
| **Global filter** | `filters` (on mapping) | Skip entire mapping based on root data |
| **Item filter** | `itemFilters` | Filter array items |
| **Multiple conditions** | Multiple filter entries | AND logic across fields |
| **Numeric comparison** | `GT`, `LT`, `GTE`, `LTE` | Income, age, value comparisons |
| **Boolean filtering** | `EQ` with `true`/`false` | Active/inactive flags |
| **List filtering** | `IN` operator | Country codes, categories |

---

## 💡 Best Practices

1. **Use `sourceField` for HTML** (rich objects/lists)
2. **Use `sourceArray` for PDF** (flattened indexed fields)
3. **Keep filters simple** — complex logic belongs in data layer
4. **Always handle nulls** in HTML templates or via transforms
5. **Validate mappings** at startup to catch configuration errors
6. **Use descriptive template names** for easy selection

These examples cover 95% of real-world scenarios you'll encounter. Start with these patterns and adapt as needed! 🚀