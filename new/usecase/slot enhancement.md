Perfect! You have a **structured PDF template** with 5 coverage slots (1-5) that correspond to your applicant types:
- **Slot 1**: Primary applicant
- **Slot 2**: Spouse  
- **Slots 3-5**: Dependents (up to 3)

Let me show you the **cleanest and most maintainable approach** to map your coverage data to these PDF fields.

---

## 🎯 Your Data Structure & PDF Mapping

### PDF Field Pattern:
```
HaveCurrentCovApplication.InsuranceCovType.1  → Primary
HaveCurrentCovApplication.InsuranceCovType.2  → Spouse  
HaveCurrentCovApplication.InsuranceCovType.3  → Dependent 1
HaveCurrentCovApplication.InsuranceCovType.4  → Dependent 2
HaveCurrentCovApplication.InsuranceCovType.5  → Dependent 3
```

### Your JSON Structure:
```json
{
  "applicants": [
    { "type": "PRIMARY", "currentCoverages": [...] },
    { "type": "SPOUSE", "currentCoverages": [...] },  
    { "type": "DEPENDENT", "currentCoverages": [...] },
    { "type": "DEPENDENT", "currentCoverages": [...] },
    { "type": "DEPENDENT", "currentCoverages": [...] }
  ]
}
```

---

## ✅ Recommended Approach: Ordered Applicant Context + Collection Mapping

### Step 1: Create an Ordered Applicants Context

Instead of separate contexts for primary, spouse, and dependents, create **one context that returns applicants in the correct PDF order**:

```yaml
contexts:
  # Returns applicants in PDF slot order: [PRIMARY, SPOUSE, DEPENDENT, DEPENDENT, DEPENDENT]
  coverageApplicants:
    from: applicants
    filterExpr: |
      (
        @.type == 'PRIMARY' ||
        @.type == 'SPOUSE' ||
        @.type == 'DEPENDENT'
      )
    # We'll sort them in the correct order programmatically
```

But sorting in JsonPath is limited, so let's use a **better approach**.

---

## ✅ Better Approach: Explicit Slot Mapping

Create **separate contexts for each slot** and use **scalar mappings**:

```yaml
contexts:
  # Slot 1: Primary
  primaryApplicant:
    from: applicants
    filter: { type: PRIMARY }
    first: true

  # Slot 2: Spouse  
  spouseApplicant:
    from: applicants
    filter: { type: SPOUSE }
    first: true

  # Slots 3-5: Dependents (up to 3)
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
  # ... (repeat for all spouse fields)

  # === SLOTS 3-5: Dependents ===
  - collection:
      source: dependents
      maxItems: 3
      # Map to slots 3, 4, 5 (dependent index 1,2,3 → slot 3,4,5)
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

> ⚠️ **Problem**: The `{slot}` placeholder needs to map `dependent_1 → slot 3`, `dependent_2 → slot 4`, etc.

---

## ✅ Optimal Solution: Custom Slot Index Handling

### Step 1: Enhanced Collection Mapping with Slot Offset

Modify your `processCollectionToResult` method to support **slot offsets**:

```java
// In your collection mapping YAML, add a slotOffset
- collection:
    source: dependents
    maxItems: 3
    slotOffset: 2  # dependents start at slot 3 (1 + 2 = 3)
    itemMappings:
      - source: currentCoverages[?(@.isActive == true)][0].insuranceCovType
        target: "HaveCurrentCovApplication.InsuranceCovType.{slot}"
```

### Step 2: Enhanced Processing Logic

```java
// In processCollectionToResult method
private void processCollectionToResult(
        CollectionMapping coll,
        DocumentContext rootJson,
        Map<String, Object> contextCache,
        MappingResult result,
        String currentPrefix,
        Object parentItem,
        int outerIndex
) throws Exception {
    
    // Calculate slot index for dependents
    int slotOffset = coll.getSlotOffset() != null ? coll.getSlotOffset() : 0;
    
    // ... existing logic to get filteredItems ...
    
    for (int i = 0; i < limit; i++) {
        Object item = filteredItems.get(i);
        int collectionIndex = i + 1; // 1-based index for the collection
        int slotIndex = slotOffset + collectionIndex; // For dependents: 2 + 1 = 3, 2 + 2 = 4, etc.
        
        for (ItemFieldMapping itemMap : coll.getItemMappings()) {
            // ... existing field processing ...
            
            // Build target field name with slot replacement
            String targetTemplate = itemMap.getTarget() != null ? itemMap.getTarget() : 
                (itemMap.getTargetPrefix() + "{slot}" + itemMap.getTargetSuffix());
            
            String targetField = targetTemplate.replace("{slot}", String.valueOf(slotIndex));
            
            result.setFieldValue(targetField, finalValue);
        }
    }
}
```

### Step 3: Update Your Model

```java
// In CollectionMapping.java
private Integer slotOffset;

public Integer getSlotOffset() { return slotOffset; }
public void setSlotOffset(Integer slotOffset) { this.slotOffset = slotOffset; }
```

---

## ✅ Complete Working YAML Configuration

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
      slotOffset: 2  # dependents map to slots 3, 4, 5
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

## 🧪 Expected Output

For an application with PRIMARY, SPOUSE, and 2 DEPENDENTS:

```java
{
  "HaveCurrentCovApplication.InsuranceCovType.1": "MEDICAL",
  "HaveCurrentCovApplication.InsuranceName.1": "Blue Cross",
  "HaveCurrentCovApplication.InsuranceCovType.2": "DENTAL",  
  "HaveCurrentCovApplication.InsuranceName.2": "Delta Dental",
  "HaveCurrentCovApplication.InsuranceCovType.3": "MEDICAL",
  "HaveCurrentCovApplication.InsuranceName.3": "Child Health Plan",
  "HaveCurrentCovApplication.InsuranceCovType.4": "MEDICAL",
  "HaveCurrentCovApplication.InsuranceName.4": "Child Health Plan",
  // Slot 5 remains empty (no 3rd dependent)
}
```

---

## ✅ Alternative: All-in-One Collection Approach

If you prefer **maximum consistency**, you can put **all applicants** in one collection:

```yaml
contexts:
  allCoverageApplicants:
    from: applicants
    filterExpr: "@.type == 'PRIMARY' || @.type == 'SPOUSE' || @.type == 'DEPENDENT'"
    # But you need to ensure order: PRIMARY, SPOUSE, then DEPENDENTS

mappings:
  - collection:
      source: allCoverageApplicants
      maxItems: 5
      itemMappings:
        - source: currentCoverages[?(@.isActive == true)][0].insuranceCovType
          target: "HaveCurrentCovApplication.InsuranceCovType.{index}"
        # ... other fields
```

> ⚠️ **Challenge**: Ensuring the correct order (PRIMARY first, then SPOUSE, then DEPENDENTS) requires sorting logic that's hard to do in JsonPath.

---

## ✅ Recommendation

**Use the explicit slot mapping approach** (first solution) because it:

- ✅ **Guarantees correct slot assignment** (PRIMARY=1, SPOUSE=2, DEPENDENTS=3-5)
- ✅ **Handles missing applicants gracefully** (no spouse = slot 2 empty)
- ✅ **Is explicit and readable** (no magic ordering logic)
- ✅ **Allows different logic per applicant type** if needed
- ✅ **Works with your existing architecture** with minimal changes

The only enhancement needed is adding the `slotOffset` support to your collection mapping, which gives you the flexibility for the dependent slots.

Would you like me to provide the **complete code for the slotOffset enhancement** or show how to handle **missing coverage data** (empty slots when no active coverage exists)?