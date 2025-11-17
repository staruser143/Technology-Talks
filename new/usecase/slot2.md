Absolutely! Here's the **complete code implementation** for the `slotOffset` enhancement that will allow you to map dependents to PDF slots 3-5 while keeping primary and spouse in slots 1-2.

---

## 📁 Updated Model Classes

### `CollectionMapping.java` (Enhanced)
```java
package model;

import java.util.List;

public class CollectionMapping {
    private String source;
    private Integer maxItems;
    private String targetPrefix;
    private String targetSuffix;
    private List<ItemFieldMapping> itemMappings;
    private Condition condition;
    private Integer slotOffset; // ← NEW FIELD for slot mapping

    // Getters and setters
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public Integer getMaxItems() { return maxItems; }
    public void setMaxItems(Integer maxItems) { this.maxItems = maxItems; }

    public String getTargetPrefix() { return targetPrefix; }
    public void setTargetPrefix(String targetPrefix) { this.targetPrefix = targetPrefix; }

    public String getTargetSuffix() { return targetSuffix; }
    public void setTargetSuffix(String targetSuffix) { this.targetSuffix = targetSuffix; }

    public List<ItemFieldMapping> getItemMappings() { return itemMappings; }
    public void setItemMappings(List<ItemFieldMapping> itemMappings) { this.itemMappings = itemMappings; }

    public Condition getCondition() { return condition; }
    public void setCondition(Condition condition) { this.condition = condition; }

    public Integer getSlotOffset() { return slotOffset; }
    public void setSlotOffset(Integer slotOffset) { this.slotOffset = slotOffset; }
}
```

---

## ⚙️ Enhanced `PdfFieldMapper.java`

### Updated `processCollectionToResult` Method
```java
private void processCollectionToResult(
        CollectionMapping coll,
        DocumentContext rootJson,
        Map<String, Object> contextCache,
        MappingResult result,
        String currentPrefix,
        Object parentItem,
        int outerIndex
) throws Exception {
    String resolvedPrefix = coll.getTargetPrefix() != null ?
        coll.getTargetPrefix().replace("${index}", String.valueOf(outerIndex)) :
        currentPrefix;

    List<?> items;
    if (coll.getSource().contains(".")) {
        String[] parts = coll.getSource().split("\\.", 2);
        String contextName = parts[0];
        Object contextValue = contextCache.get(contextName);
        if (contextValue == null) {
            items = Collections.emptyList();
        } else {
            String subPath = parts[1];
            try {
                Object subValue = JsonPath.parse(contextValue).read("$." + subPath);
                items = (subValue instanceof List) ? (List<?>) subValue : Collections.emptyList();
            } catch (Exception e) {
                items = Collections.emptyList();
            }
        }
    } else {
        if (contextCache.containsKey(coll.getSource())) {
            Object cached = contextCache.get(coll.getSource());
            items = (cached instanceof List) ? (List<?>) cached : Collections.emptyList();
        } else {
            Object raw = resolveValue(coll.getSource(), rootJson, contextCache);
            items = (raw instanceof List) ? (List<?>) raw : Collections.emptyList();
        }
    }

    if (items == null) items = Collections.emptyList();

    // Apply collection-level filter
    List<Object> filteredItems = new ArrayList<>();
    for (Object item : items) {
        boolean passes = ConditionEvaluator.evaluate(
            coll.getCondition(),
            JsonPath.parse(item),
            item
        );
        if (passes) {
            filteredItems.add(item);
        } else if (dryRun) {
            System.out.println("⏭️  Skipped collection item (condition failed)");
        }
    }

    int limit = coll.getMaxItems() != null ?
        Math.min(filteredItems.size(), coll.getMaxItems()) :
        filteredItems.size();

    // Get slot offset (default to 0 if not specified)
    int slotOffset = coll.getSlotOffset() != null ? coll.getSlotOffset() : 0;

    for (int i = 0; i < limit; i++) {
        Object item = filteredItems.get(i);
        int collectionIndex = i + 1; // 1-based index for the current collection
        int slotIndex = slotOffset + collectionIndex; // Calculate actual PDF slot

        for (ItemFieldMapping itemMap : coll.getItemMappings()) {
            if (itemMap.isNestedCollection()) {
                // Handle nested collections (recursive)
                String innerPrefix = resolvedPrefix;
                if (itemMap.getCollection().getTargetPrefix() != null) {
                    innerPrefix = itemMap.getCollection().getTargetPrefix()
                        .replace("${index}", String.valueOf(collectionIndex));
                }
                processCollectionToResult(
                    itemMap.getCollection(),
                    rootJson,
                    contextCache,
                    result,
                    innerPrefix,
                    item,
                    collectionIndex
                );
            } else {
                // Process scalar field within collection item
                Object rawValue = null;
                try {
                    rawValue = JsonPath.parse(item).read(itemMap.getSource());
                } catch (Exception e) {
                    rawValue = null;
                }

                boolean fieldConditionPassed = ConditionEvaluator.evaluate(
                    itemMap.getCondition(),
                    JsonPath.parse(item),
                    rawValue
                );

                if (!fieldConditionPassed) {
                    if (dryRun) {
                        // Build target field name for logging
                        String targetField = buildTargetFieldName(itemMap, resolvedPrefix, slotIndex);
                        System.out.println("⏭️  Skipped field (condition): " + targetField);
                    }
                    continue;
                }

                Object transformed = DataTransformer.applyTransform(rawValue, itemMap.getTransform());
                String finalValue = (transformed != null) ? transformed.toString() : "";
                if (finalValue.trim().isEmpty() && itemMap.getDefaultValue() != null) {
                    finalValue = itemMap.getDefaultValue();
                }

                // Build target field name with slot replacement
                String targetField = buildTargetFieldName(itemMap, resolvedPrefix, slotIndex);
                result.setFieldValue(targetField, finalValue);

                if (dryRun) {
                    String safeVal = SensitiveFieldDetector.isSensitive(targetField) ?
                        SensitiveFieldDetector.maskValue(finalValue, targetField.contains("email")) :
                        finalValue;
                    System.out.println("✅ " + targetField + " = '" + safeVal + "'");
                }
            }
        }
    }
}

// Helper method to build target field name with slot replacement
private String buildTargetFieldName(ItemFieldMapping itemMap, String prefix, int slotIndex) {
    // Check if target field uses {slot} placeholder
    if (itemMap.getTarget() != null) {
        return itemMap.getTarget().replace("{slot}", String.valueOf(slotIndex));
    }
    
    // Build from prefix/suffix
    String suffix = itemMap.getTargetSuffix() != null ? itemMap.getTargetSuffix() : "";
    String targetField = prefix + "{slot}" + suffix;
    return targetField.replace("{slot}", String.valueOf(slotIndex));
}
```

---

## 📄 Complete YAML Configuration Example

```yaml
contexts:
  primaryApplicant:
    from: applicants
    filter: { type: PRIMARY }
    first: true

  spouseApplicant:
    from: applicants
    filter: { type: SPOUSE }
    first: true

  dependents:
    from: applicants
    filter: { type: DEPENDENT }

mappings:
  # === SLOT 1: Primary Applicant ===
  - source: primaryApplicant.currentCoverages[?(@.isActive == true)][0].insuranceCovType
    target: "HaveCurrentCovApplication.InsuranceCovType.1"
  - source: primaryApplicant.currentCoverages[?(@.isActive == true)][0].insuranceName
    target: "HaveCurrentCovApplication.InsuranceName.1"
  - source: primaryApplicant.currentCoverages[?(@.isActive == true)][0].insuranceId
    target: "HaveCurrentCovApplication.InsuranceId.1"
  - source: primaryApplicant.currentCoverages[?(@.isActive == true)][0].coverageDateStart
    target: "HaveCurrentCovApplication.coverageDateStart.1"
  - source: primaryApplicant.currentCoverages[?(@.isActive == true)][0].coverageDateEnd
    target: "HaveCurrentCovApplication.coverageDateEnd.1"
  - source: primaryApplicant.currentCoverages[?(@.isActive == true)][0].coveragePaidThruDate
    target: "HaveCurrentCovApplication.coveragePaidThruDate.1"

  # === SLOT 2: Spouse ===
  - source: spouseApplicant.currentCoverages[?(@.isActive == true)][0].insuranceCovType
    target: "HaveCurrentCovApplication.InsuranceCovType.2"
  - source: spouseApplicant.currentCoverages[?(@.isActive == true)][0].insuranceName
    target: "HaveCurrentCovApplication.InsuranceName.2"
  - source: spouseApplicant.currentCoverages[?(@.isActive == true)][0].insuranceId
    target: "HaveCurrentCovApplication.InsuranceId.2"
  - source: spouseApplicant.currentCoverages[?(@.isActive == true)][0].coverageDateStart
    target: "HaveCurrentCovApplication.coverageDateStart.2"
  - source: spouseApplicant.currentCoverages[?(@.isActive == true)][0].coverageDateEnd
    target: "HaveCurrentCovApplication.coverageDateEnd.2"
  - source: spouseApplicant.currentCoverages[?(@.isActive == true)][0].coveragePaidThruDate
    target: "HaveCurrentCovApplication.coveragePaidThruDate.2"

  # === SLOTS 3-5: Dependents ===
  - collection:
      source: dependents
      maxItems: 3
      slotOffset: 2  # dependents start at slot 3 (2 + 1 = 3, 2 + 2 = 4, 2 + 3 = 5)
      itemMappings:
        - source: currentCoverages[?(@.isActive == true)][0].insuranceCovType
          target: "HaveCurrentCovApplication.InsuranceCovType.{slot}"
        - source: currentCoverages[?(@.isActive == true)][0].insuranceName
          target: "HaveCurrentCovApplication.InsuranceName.{slot}"
        - source: currentCoverages[?(@.isActive == true)][0].insuranceId
          target: "HaveCurrentCovApplication.InsuranceId.{slot}"
        - source: currentCoverages[?(@.isActive == true)][0].coverageDateStart
          target: "HaveCurrentCovApplication.coverageDateStart.{slot}"
        - source: currentCoverages[?(@.isActive == true)][0].coverageDateEnd
          target: "HaveCurrentCovApplication.coverageDateEnd.{slot}"
        - source: currentCoverages[?(@.isActive == true)][0].coveragePaidThruDate
          target: "HaveCurrentCovApplication.coveragePaidThruDate.{slot}"
```

---

## 🧪 How It Works - Step by Step

### For Dependents Collection:
1. **`slotOffset: 2`** is specified in YAML
2. **First dependent** (collection index = 1): `slotIndex = 2 + 1 = 3`
   - `HaveCurrentCovApplication.InsuranceCovType.{slot}` → `HaveCurrentCovApplication.InsuranceCovType.3`
3. **Second dependent** (collection index = 2): `slotIndex = 2 + 2 = 4`
   - `HaveCurrentCovApplication.InsuranceCovType.{slot}` → `HaveCurrentCovApplication.InsuranceCovType.4`
4. **Third dependent** (collection index = 3): `slotIndex = 2 + 3 = 5`
   - `HaveCurrentCovApplication.InsuranceCovType.{slot}` → `HaveCurrentCovApplication.InsuranceCovType.5`

### For Primary and Spouse:
- **No collection needed** - direct scalar mappings to slots 1 and 2
- **Explicit and clear** - no magic numbering

---

## 🧪 Expected Output Example

For an application with PRIMARY, SPOUSE, and 2 DEPENDENTS:

```java
MappingResult result = // ... processing ...

Map<String, String> fieldValues = result.getFieldValues();
System.out.println("Mapped fields:");
fieldValues.forEach((field, value) -> 
    System.out.println(field + " = '" + value + "'")
);
```

**Output:**
```
Mapped fields:
HaveCurrentCovApplication.InsuranceCovType.1 = 'MEDICAL'
HaveCurrentCovApplication.InsuranceName.1 = 'Blue Cross Primary'
HaveCurrentCovApplication.InsuranceCovType.2 = 'DENTAL'
HaveCurrentCovApplication.InsuranceName.2 = 'Delta Dental Spouse'
HaveCurrentCovApplication.InsuranceCovType.3 = 'MEDICAL'
HaveCurrentCovApplication.InsuranceName.3 = 'Child Health Plan Dep1'
HaveCurrentCovApplication.InsuranceCovType.4 = 'MEDICAL'
HaveCurrentCovApplication.InsuranceName.4 = 'Child Health Plan Dep2'
```

---

## ✅ Benefits of This Implementation

| Feature | Benefit |
|--------|---------|
| **Slot Offset Support** | Map collections to any PDF slot range |
| **Backward Compatible** | Existing mappings without `slotOffset` work unchanged |
| **Flexible Placeholders** | Use `{slot}` in target fields |
| **Graceful Handling** | Missing applicants leave slots empty |
| **Maintainable** | Clear, explicit configuration |
| **Extensible** | Easy to add other placeholder types |

---

## 🔧 Usage Notes

1. **`slotOffset` is optional** - if not specified, defaults to 0 (behaves like original)
2. **`{slot}` placeholder** only works in `target` field of `ItemFieldMapping`
3. **Collection index is always 1-based** (first item = 1, second = 2, etc.)
4. **Works with all transform types** - transforms are applied before slot mapping

This implementation gives you **complete control** over mapping your applicant data to the exact PDF slots you need! 🎯

Would you like me to provide **unit tests** for the slot offset functionality or show how to handle **missing coverage data** (empty slots when no active coverage exists)?