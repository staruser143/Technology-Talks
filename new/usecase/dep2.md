**Absolutely!** You can (and should) use your **clean filter syntax** instead of JsonPath's `?(@.)` syntax. This makes your YAML much more readable and maintainable.

Let me show you how to do this properly.

---

## ✅ Recommended Approach: Context-Based Filtering

Instead of embedding JsonPath filters in your field mappings, **create filtered contexts** that you can reuse across multiple fields.

### Step 1: Define Filtered Contexts

```yaml
contexts:
  primary:
    from: applicants
    filter: { type: PRIMARY }
    first: true

  # Get all dependents
  dependents:
    from: applicants
    filter: { type: DEPENDENT }

  # For each dependent, create filtered sub-contexts for their active coverages
  # We'll do this dynamically in the collection mapping
```

### Step 2: Enhanced Collection Mapping with Nested Contexts

Here's the **clean, maintainable approach**:

```yaml
mappings:
  - collection:
      source: dependents
      maxItems: 3
      targetPrefix: "dependent_"
      itemMappings:
        # Basic demographics
        - source: demographics.firstName
          targetSuffix: "_firstName"

        # Create a filtered context for active coverage within the collection
        # This uses the clean filter syntax instead of JsonPath ?(@.) 
        - collection:
            source: currentCoverages
            filter:
              isActive: true
            first: true
            targetPrefix: ""  # No additional prefix needed
            itemMappings:
              - source: medical.planName
                target: "dependent_{index}_medical_plan"
              - source: dental.planName  
                target: "dependent_{index}_dental_plan"
              - source: planDetails.products
                target: "dependent_{index}_products"
                # Handle products separately if needed
```

> ⚠️ **Wait!** There's a problem with this approach...

The nested collection mapping above won't work as expected because `target: "dependent_{index}_medical_plan"` would create field names like `dependent_1_medical_plan`, but the `{index}` refers to the **inner collection index** (which is always 1 since `first: true`), not the **outer dependent index**.

---

## ✅ Better Approach: Pre-filter at Context Level

The **cleanest solution** is to create **pre-filtered contexts** that handle the active coverage filtering:

### Step 1: Define Helper Contexts

```yaml
contexts:
  primary:
    from: applicants
    filter: { type: PRIMARY }
    first: true

  # All dependents
  dependents:
    from: applicants
    filter: { type: DEPENDENT }

  # Active coverages for PRIMARY (if needed)
  primaryActiveCoverage:
    from: primary.currentCoverages
    filter: { isActive: true }
    first: true

  # But we can't easily pre-define dependent-specific contexts...
```

### Step 2: Use Collection Mapping with Inline Filtering

Since you can't easily pre-define contexts for each dependent's active coverage, the **most practical approach** is to use your **clean filter syntax within the collection mapping** by enhancing your context resolution.

---

## ✅ Optimal Solution: Enhanced Context Resolution with Filters

Let's extend your context system to support **inline filtering** within collection mappings.

### Enhanced YAML Syntax

```yaml
mappings:
  - collection:
      source: dependents
      maxItems: 3
      targetPrefix: "dependent_"
      itemMappings:
        - source: demographics.firstName
          targetSuffix: "_firstName"

        # Use clean filter syntax instead of JsonPath
        - source: 
            from: currentCoverages
            filter: { isActive: true }
            first: true
          targetSuffix: "_medical_plan"
          # But we need to specify which field to extract...
```

This doesn't quite work with your current architecture.

---

## ✅ Recommended Practical Solution: Clean Filter Methods

The **best balance** of cleanliness and practicality is to create **custom transform methods** that handle the filtering:

### Step 1: Create Filtering Transforms

```java
package engine.transform;

import java.util.List;
import java.util.Map;

public class CoverageFilterTransform extends BaseTransformer {
    
    @Override
    public Object transform(Object value, Object config) {
        if (!(value instanceof List)) {
            return "";
        }
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> coverages = (List<Map<String, Object>>) value;
        
        // Get filter criteria from config
        Map<String, Object> filterCriteria = null;
        String fieldToExtract = "medical.planName"; // default
        
        if (config instanceof Map) {
            Map<String, Object> args = (Map<String, Object>) config;
            filterCriteria = (Map<String, Object>) args.get("filter");
            fieldToExtract = (String) args.getOrDefault("field", fieldToExtract);
        }
        
        // Find first matching coverage
        for (Map<String, Object> coverage : coverages) {
            if (matchesFilter(coverage, filterCriteria)) {
                // Extract the specified field
                return extractField(coverage, fieldToExtract);
            }
        }
        
        return ""; // No match found
    }
    
    private boolean matchesFilter(Map<String, Object> item, Map<String, Object> filter) {
        if (filter == null || filter.isEmpty()) {
            return true;
        }
        
        for (Map.Entry<String, Object> criterion : filter.entrySet()) {
            String key = criterion.getKey();
            Object expectedValue = criterion.getValue();
            Object actualValue = getItemValue(item, key);
            
            if (!valuesEqual(actualValue, expectedValue)) {
                return false;
            }
        }
        return true;
    }
    
    private Object getItemValue(Map<String, Object> item, String path) {
        // Handle nested paths like "isActive" or "product.type"
        if (path.contains(".")) {
            String[] parts = path.split("\\.");
            Object current = item;
            for (String part : parts) {
                if (current instanceof Map) {
                    current = ((Map) current).get(part);
                } else {
                    return null;
                }
            }
            return current;
        }
        return item.get(path);
    }
    
    private boolean valuesEqual(Object actual, Object expected) {
        if (actual == null && expected == null) return true;
        if (actual == null || expected == null) return false;
        return actual.equals(expected);
    }
    
    private Object extractField(Map<String, Object> coverage, String fieldPath) {
        return getItemValue(coverage, fieldPath);
    }
}
```

### Step 2: Register the Transform

```java
// In TransformRegistry constructor
registerBuiltIn("filterCoverage", new CoverageFilterTransform());
```

### Step 3: Use in YAML

```yaml
mappings:
  - collection:
      source: dependents
      maxItems: 3
      targetPrefix: "dependent_"
      itemMappings:
        - source: demographics.firstName
          targetSuffix: "_firstName"

        # Get medical plan from active coverage
        - source: currentCoverages
          targetSuffix: "_medical_plan"
          transform:
            name: "filterCoverage"
            args:
              filter:
                isActive: true
              field: "medical.planName"

        # Get dental plan from active coverage  
        - source: currentCoverages
          targetSuffix: "_dental_plan"
          transform:
            name: "filterCoverage"
            args:
              filter:
                isActive: true
              field: "dental.planName"

        # Get medical product name
        - source: currentCoverages
          targetSuffix: "_medical_product"
          transform:
            name: "filterCoverage"
            args:
              filter:
                isActive: true
              field: "planDetails.products[?(@.productType == 'MEDICAL')].plans[0].name"
```

> ⚠️ The last example still uses JsonPath for the nested product structure, but the main filtering is clean.

---

## ✅ Best Practical Approach: Hybrid Solution

For **maximum readability and maintainability**, use this hybrid approach:

### 1. Keep Simple Filters in Transform
For basic filtering like `isActive: true`, use the custom transform above.

### 2. Use JsonPath for Complex Nested Structures
For deeply nested structures like products and plans, JsonPath is actually more concise:

```yaml
# This is actually cleaner than trying to represent it with nested filters
- source: currentCoverages[?(@.isActive == true)][0].planDetails.products[?(@.productType == 'MEDICAL')].plans[0].name
  targetSuffix: "_medical_product_name"
```

### 3. Hide JsonPath Complexity in Reusable Contexts
If you use the same complex path frequently, create a context:

```yaml
contexts:
  # Reusable context for medical product name extraction
  medicalProductNamePath: "planDetails.products[?(@.productType == 'MEDICAL')].plans[0].name"

mappings:
  - collection:
      source: dependents
      maxItems: 3
      targetPrefix: "dependent_"
      itemMappings:
        - source: currentCoverages[?(@.isActive == true)][0].{{medicalProductNamePath}}
          targetSuffix: "_medical_product"
```

But this requires template processing, which adds complexity.

---

## ✅ Final Recommendation

**Use this practical approach:**

```yaml
mappings:
  - collection:
      source: dependents
      maxItems: 3
      targetPrefix: "dependent_"
      itemMappings:
        # Basic fields - no filtering needed
        - source: demographics.firstName
          targetSuffix: "_firstName"
        - source: demographics.lastName
          targetSuffix: "_lastName"

        # Active coverage fields - use minimal JsonPath for filtering
        # This is actually more readable than complex nested filters
        - source: currentCoverages[?(@.isActive == true)][0].medical.planName
          targetSuffix: "_medical_plan"
        - source: currentCoverages[?(@.isActive == true)][0].dental.planName
          targetSuffix: "_dental_plan"
        - source: currentCoverages[?(@.isActive == true)][0].planDetails.products[?(@.productType == 'MEDICAL')].plans[0].name
          targetSuffix: "_medical_product_name"
```

### Why this works best:

1. **Readable**: The intent is clear (`isActive == true`)
2. **Concise**: No need for complex nested YAML structures
3. **Maintainable**: Easy to modify individual field mappings
4. **Performant**: JsonPath is optimized for this
5. **Consistent**: Uses the same pattern you already have working

The `?(@.isActive == true)` syntax, while technical, is **standard JsonPath** and is actually **more readable** than trying to represent the same logic with nested YAML filters in this context.

If you really want to avoid JsonPath entirely, go with the **custom transform approach** shown earlier, but be prepared for more complex YAML configuration.

Would you like me to implement the **full custom transform solution** with unit tests, or are you comfortable with the minimal JsonPath approach for the filtering?