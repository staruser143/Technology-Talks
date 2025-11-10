**Yes, the current design fully supports this complex nested filtering scenario**, but the implementation depends on how you structure your YAML mappings. Let me show you the optimal approach.

---

## 📊 Your Data Structure

```json
{
  "applicants": [
    {
      "id": "A1",
      "relationship": "primary",
      "firstName": "John",
      "addresses": [
        { "type": "home", "street": "123 Main St", "city": "NYC" },
        { "type": "billing", "street": "456 Business Ave", "city": "NYC" }
      ]
    },
    {
      "id": "A2", 
      "relationship": "spouse",
      "firstName": "Jane",
      "addresses": [
        { "type": "home", "street": "123 Main St", "city": "NYC" }
      ]
    }
  ]
}
```

---

## 🔍 Challenge Breakdown

You need to:
1. **Filter applicants** by `relationship` (primary, spouse, dependent)
2. **Filter addresses** by `type` (home, billing, etc.)
3. **Map specific address fields** to PDF fields

---

## ✅ Solution 1: **JSONata Approach (Recommended)**

JSONata handles nested filtering elegantly:

```yaml
mappings:
  - sourceField: "$"
    targetField: "__all_fields"
    transforms:
      - type: jsonata
        expression: |
          (
            $primary := applicants[relationship = "primary"][0];
            $spouse := applicants[relationship = "spouse"][0];
            $dependents := applicants[relationship = "dependent"];
            
            $primaryHome := $primary.addresses[type = "home"][0];
            $primaryBilling := $primary.addresses[type = "billing"][0];
            $spouseHome := $spouse.addresses[type = "home"][0];
            
            {
              "primary.fname.1": $primary.firstName,
              "primary.home.street.1": $primaryHome.street,
              "primary.home.city.1": $primaryHome.city,
              "primary.billing.street.1": $primaryBilling.street,
              "primary.billing.city.1": $primaryBilling.city,
              "spouse.fname.1": $spouse.firstName,
              "spouse.home.street.1": $spouseHome.street
            }
          )
```

**Benefits**:
- ✅ Single pass through data
- ✅ Clean, readable expressions
- ✅ Handles missing addresses gracefully (returns `null`)
- ✅ Cross-language compatible

---

## ✅ Solution 2: **Enhanced Object Mapping (Current Design)**

If you prefer to extend your current mapping model:

### Step 1: Enhance `ObjectFieldMapping` with Nested Filters

```java
public class ObjectFieldMapping {
    private String sourceField;
    private String targetField;
    private List<FilterCondition> filters = new ArrayList<>();
    
    // NEW: Nested field mappings for complex objects
    private List<NestedFieldMapping> nestedMappings = new ArrayList<>();
}

public class NestedFieldMapping {
    private String sourceField;      // e.g., "addresses"
    private List<FilterCondition> itemFilters; // e.g., type = "home"
    private String targetFieldTemplate; // e.g., "primary.home.{field}"
    private List<FieldMapping> fieldMappings; // street, city, etc.
}
```

### Step 2: YAML Configuration

```yaml
mappings:
  - sourceObject: "applicants"
    itemFilters:
      - field: "relationship"
        operator: EQ
        value: "primary"
    fieldMappings:
      - sourceField: "firstName"
        targetField: "primary.fname.1"
      
      # Nested address mapping
      - sourceField: "addresses"
        targetField: "__primary_addresses"
        nestedMappings:
          - itemFilters:
              - field: "type"
                operator: EQ
                value: "home"
            targetFieldTemplate: "primary.home.{field}"
            fieldMappings:
              - sourceField: "street"
                targetField: "street"
              - sourceField: "city" 
                targetField: "city"
          
          - itemFilters:
              - field: "type"
                operator: EQ
                value: "billing"
            targetFieldTemplate: "primary.billing.{field}"
            fieldMappings:
              - sourceField: "street"
                targetField: "street"
```

### Step 3: Enhanced DataMapper Logic

```java
private void handleObjectMapping(Object jsonData, FieldMapping mapping, Map<String, Object> result) {
    Object extractedObject = readSimplePath(jsonData, mapping.getSourceObject());
    if (extractedObject == null) return;

    // Process regular field mappings
    for (ObjectFieldMapping fieldMap : mapping.getFieldMappings()) {
        // ... existing logic ...
        
        // Handle nested mappings
        if (!fieldMap.getNestedMappings().isEmpty()) {
            handleNestedMappings(extractedObject, fieldMap, result);
        }
    }
}

private void handleNestedMappings(Object parentObject, ObjectFieldMapping parentFieldMap, Map<String, Object> result) {
    Object nestedArray = readSimplePath(parentObject, parentFieldMap.getSourceField());
    if (!(nestedArray instanceof List)) return;

    for (NestedFieldMapping nestedMap : parentFieldMap.getNestedMappings()) {
        // Find first item matching nested filters
        Object selectedItem = ((List<?>) nestedArray).stream()
            .filter(item -> passesFilters(item, nestedMap.getItemFilters()))
            .findFirst()
            .orElse(null);

        if (selectedItem != null) {
            // Process nested field mappings
            for (FieldMapping nestedFieldMap : nestedMap.getFieldMappings()) {
                Object value = readSimplePath(selectedItem, nestedFieldMap.getSourceField());
                if (value != null) {
                    String targetField = nestedMap.getTargetFieldTemplate()
                        .replace("{field}", nestedFieldMap.getTargetField());
                    result.put(targetField, safeToString(value));
                }
            }
        }
    }
}
```

---

## ✅ Solution 3: **Repeating + Filtering Approach**

For cases where you need multiple addresses per applicant:

```yaml
mappings:
  # Primary applicant addresses
  - sourceObject: "applicants"
    itemFilters:
      - field: "relationship"
        operator: EQ
        value: "primary"
    fieldMappings:
      - sourceField: "firstName"
        targetField: "primary.fname.1"

  # Primary home address
  - sourceArray: "applicants[relationship='primary'].addresses"
    itemFilters:
      - field: "type"
        operator: EQ
        value: "home"
    maxRepetitions: 1
    itemMappings:
      - sourceField: "street"
        targetFieldTemplate: "primary.home.street.1"
      - sourceField: "city"
        targetFieldTemplate: "primary.home.city.1"

  # Primary billing address  
  - sourceArray: "applicants[relationship='primary'].addresses"
    itemFilters:
      - field: "type"
        operator: EQ
        value: "billing"
    maxRepetitions: 1
    itemMappings:
      - sourceField: "street"
        targetFieldTemplate: "primary.billing.street.1"
```

---

## 🔧 Enhanced SimplePathResolver for Nested Paths

Update your path resolver to handle nested array access:

```java
public static Object read(Object jsonContext, String simplePath) {
    if (simplePath == null || simplePath.trim().isEmpty()) {
        return null;
    }
    
    // Handle nested paths like "applicants[relationship='primary'].addresses[type='home']"
    String processedPath = simplePath;
    
    // Convert applicants[relationship='primary'] to JsonPath: $.applicants[?(@.relationship == 'primary')]
    processedPath = processedPath
        .replaceAll("\\[(.*?)='(.*?)'\\]", "[?(@.$1 == '$2')]")
        .replaceAll("\\.", ".");
    
    // Prepend root selector
    if (!processedPath.startsWith("$")) {
        processedPath = "$." + processedPath;
    }
    
    try {
        return JsonPath.read(jsonContext, processedPath);
    } catch (Exception e) {
        return null;
    }
}
```

Now you can use paths like:
- `"applicants[relationship='primary'].addresses[type='home'].street"` 
- Returns the street of primary applicant's home address

---

## 📄 Real-World YAML Example

```yaml
templates:
  - name: "detailed-application-pdf"
    type: PDF
    mappings:
      # Primary applicant
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

      # Primary home address
      - sourceArray: "applicants"
        itemFilters:
          - field: "relationship"
            operator: EQ
            value: "primary"
        itemMappings:
          - sourceField: "addresses[type='home'].street"
            targetFieldTemplate: "primary.home.street.1"
          - sourceField: "addresses[type='home'].city"
            targetFieldTemplate: "primary.home.city.1"

      # Primary billing address
      - sourceArray: "applicants"
        itemFilters:
          - field: "relationship"
            operator: EQ
            value: "primary"
        itemMappings:
          - sourceField: "addresses[type='billing'].street"
            targetFieldTemplate: "primary.billing.street.1"
          - sourceField: "addresses[type='billing'].zip"
            targetFieldTemplate: "primary.billing.zip.1"

      # Spouse addresses
      - sourceArray: "applicants"
        itemFilters:
          - field: "relationship"
            operator: EQ
            value: "spouse"
        itemMappings:
          - sourceField: "addresses[type='home'].street"
            targetFieldTemplate: "spouse.home.street.1"
```

---

## ⚡ Performance Considerations

### For Large Datasets:
1. **Use JSONata variables** to avoid repeated filtering
2. **Cache path resolution** results
3. **Pre-filter source data** at the API layer

### JSONata Optimized Version:
```jsonata
(
  $primary := applicants[relationship = "primary"][0];
  $primaryHome := $primary.addresses[type = "home"][0];
  $primaryBilling := $primary.addresses[type = "billing"][0];
  
  {
    "primary.home.street.1": $primaryHome.street,
    "primary.home.city.1": $primaryHome.city,
    "primary.billing.street.1": $primaryBilling.street,
    // ... other fields
  }
)
```

This scans the applicants array **once** and addresses arrays **once per type**.

---

## ✅ Current Design Support Summary

| Feature | Supported? | How |
|---------|------------|-----|
| **Nested object filtering** | ✅ | JSONata or enhanced ObjectFieldMapping |
| **Multi-level filtering** | ✅ | `itemFilters` at multiple levels |
| **Dynamic field naming** | ✅ | `targetFieldTemplate` with placeholders |
| **Missing data handling** | ✅ | Null-safe path resolution |
| **Performance optimization** | ✅ | Variables in JSONata, caching |

---

## 🔚 Recommendation

**Use JSONata for complex nested scenarios** like yours because:
- ✅ **Most readable and maintainable**
- ✅ **Best performance** (single pass through data)
- ✅ **Cross-language compatible**
- ✅ **Handles missing data gracefully**
- ✅ **Already supported in your current design**

Your current architecture is **perfectly capable** of handling this requirement — you just need to leverage JSONata's full power for the complex cases and keep your existing mapping model for simpler scenarios.

The nested filtering scenario you described is a **perfect use case for JSONata**, and your design already supports it! 🚀