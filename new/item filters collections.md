**Absolutely!** You can apply **filters (conditions)** at **both outer and inner collection levels** — and even **per-item** within those collections.

This lets you:
- Skip entire outer records (e.g., only process `"status": "SHIPPED"` orders)
- Skip inner items (e.g., hide `"price": 0` line items)
- Combine conditions (`status == "ACTIVE"` AND `price > 0`)

Let’s extend your recursive mapper to support this.

---

## ✅ Step 1: Update YAML Syntax to Support Collection-Level Filters

### New capability: `collection.condition`

```yaml
mappings:
  - collection:
      source: "orders"
      maxItems: 5
      # 👇 Filter: only process orders with status = "SHIPPED"
      condition:
        type: "equals"
        field: "status"
        value: "SHIPPED"
      targetPrefix: "order_"
      itemMappings:
        - source: "orderId"
          targetSuffix: "_id"

        - collection:
            source: "items"
            maxItems: 10
            # 👇 Filter: only include items with price > 0
            condition:
              type: "greaterThan"
              field: "price"
              value: 0
            targetPrefix: "order_${index}_item_"
            itemMappings:
              - source: "sku"
                targetSuffix: "_sku"
              - source: "price"
                targetSuffix: "_price"
```

> ✨ Now **each `collection` block** can have its own `condition` — applied to **every item** in that array.

---

## 🧱 Step 2: Update Model Classes

### `CollectionMapping.java` → add `condition`

```java
public class CollectionMapping {
    private String source;
    private Integer maxItems;
    private String targetPrefix;
    private String targetSuffix;
    private Condition condition; // ← NEW
    private List<ItemFieldMapping> itemMappings;

    public Condition getCondition() { return condition; }
}
```

> Note: `ItemFieldMapping` **already supports** `condition` (for scalar fields).  
> Now `CollectionMapping` supports it too — for **filtering entire items**.

---

## ⚙️ Step 3: Update Recursive Processor to Evaluate Collection Conditions

### In `PdfFieldMapper.java`, modify `processCollection`:

```java
private void processCollection(
        CollectionMapping coll,
        DocumentContext rootJson,
        PDAcroForm form,
        String currentPrefix,
        Object parentItem,
        int outerIndex
) throws Exception {
    String resolvedPrefix = coll.getTargetPrefix() != null ?
        coll.getTargetPrefix().replace("${index}", String.valueOf(outerIndex)) :
        currentPrefix;

    // Read array
    List<?> rawItems;
    if (parentItem != null) {
        try {
            rawItems = JsonPath.parse(parentItem).read(coll.getSource());
        } catch (Exception e) {
            rawItems = Collections.emptyList();
        }
    } else {
        rawItems = rootJson.read("$." + coll.getSource());
    }
    if (rawItems == null) rawItems = Collections.emptyList();

    // 🔍 FILTER: Apply collection-level condition to each item
    List<Object> filteredItems = new ArrayList<>();
    for (Object item : rawItems) {
        boolean passes = ConditionEvaluator.evaluate(
            coll.getCondition(),          // ← collection-level condition
            JsonPath.parse(item),        // context = current item
            item                         // source = whole item (for field ref)
        );
        if (passes) {
            filteredItems.add(item);
        } else {
            if (dryRun) {
                System.out.println("⏭️  Skipped collection item (condition failed)");
            }
        }
    }

    int limit = coll.getMaxItems() != null ?
        Math.min(filteredItems.size(), coll.getMaxItems()) :
        filteredItems.size();

    for (int i = 0; i < limit; i++) {
        Object item = filteredItems.get(i);
        int innerIndex = i + 1;

        for (ItemFieldMapping itemMap : coll.getItemMappings()) {
            if (itemMap.isNestedCollection()) {
                // Recurse into nested collection
                String innerPrefix = resolvedPrefix;
                if (itemMap.getCollection().getTargetPrefix() != null) {
                    innerPrefix = itemMap.getCollection().getTargetPrefix()
                        .replace("${index}", String.valueOf(innerIndex));
                }
                processCollection(
                    itemMap.getCollection(),
                    rootJson,
                    form,
                    innerPrefix,
                    item,
                    innerIndex
                );
            } else {
                // Scalar field: evaluate its own condition
                Object rawValue = null;
                try {
                    rawValue = JsonPath.parse(item).read(itemMap.getSource());
                } catch (Exception e) {
                    rawValue = null;
                }

                // 🔍 Evaluate field-level condition
                boolean fieldConditionPassed = ConditionEvaluator.evaluate(
                    itemMap.getCondition(),
                    JsonPath.parse(item),
                    rawValue
                );

                if (!fieldConditionPassed) {
                    if (dryRun) {
                        System.out.println("⏭️  Skipped field (condition failed): " + 
                            resolvedPrefix + innerIndex + 
                            (itemMap.getTargetSuffix() != null ? itemMap.getTargetSuffix() : ""));
                    }
                    continue;
                }

                // ... [rest of transform + set logic] ...
            }
        }
    }
}
```

> ✅ **Two levels of filtering**:
> 1. **Collection-level**: skip entire item (e.g., skip order if not shipped)
> 2. **Field-level**: skip individual field (e.g., skip price if $0)

---

## 🧪 Example: Full Filtering in Action

### Input JSON:
```json
{
  "orders": [
    {
      "orderId": "O1",
      "status": "PENDING",
      "items": [
        { "sku": "A", "price": 100 }
      ]
    },
    {
      "orderId": "O2",
      "status": "SHIPPED",
      "items": [
        { "sku": "B", "price": 0 },      // ← free item
        { "sku": "C", "price": 200 }
      ]
    }
  ]
}
```

### YAML Config:
```yaml
- collection:
    source: "orders"
    condition:
      type: "equals"
      field: "status"
      value: "SHIPPED"
    targetPrefix: "order_"
    itemMappings:
      - source: "orderId"
        targetSuffix: "_id"

      - collection:
          source: "items"
          condition:
            type: "greaterThan"
            field: "price"
            value: 0
          targetPrefix: "order_${index}_item_"
          itemMappings:
            - source: "sku"
              targetSuffix: "_sku"
            - source: "price"
              targetSuffix: "_price"
```

### Dry-Run Output:
```text
✅ order_1_id = 'O2'
✅ order_1_item_1_sku = 'C'
✅ order_1_item_1_price = '200'
⏭️  Skipped collection item (condition failed)   ← free item (price=0)
⏭️  Skipped collection item (condition failed)   ← PENDING order
```

> ✅ Only **shipped orders** with **paid items** appear!

---

## 🔁 How Conditions Work at Each Level

| Level | Condition Applied To | Example Use Case |
|------|----------------------|------------------|
| **Outer Collection** | Each `order` object | Skip draft orders |
| **Inner Collection** | Each `item` object | Skip free/discounted items |
| **Scalar Field** | Individual field value | Hide internal notes unless debug mode |

---

## 💡 Advanced: Combine Conditions with `and`/`or`

Your `ConditionEvaluator` already supports compound conditions:

```yaml
condition:
  and:
    - type: "equals"
      field: "status"
      value: "SHIPPED"
    - type: "greaterThan"
      field: "total"
      value: 50
```

Use this in **any** `condition` block — collection or field.

---

## ✅ Summary

You now support **multi-level filtering**:

- ✅ **Collection-level conditions** → skip entire array items
- ✅ **Field-level conditions** → skip individual mapped fields
- ✅ **Recursive** → works at any nesting depth
- ✅ **Dry-run logs** show exactly what was skipped and why
- ✅ **Reuses** your existing `ConditionEvaluator`

This gives you **enterprise-grade control** over what data appears in your PDF.

---

Would you like:
- A **real-world example** with `and`/`or` conditions?
- **Performance tips** for large arrays?
- **Unit tests** for the filtering logic?

Just say the word — I’ll help you lock it down! 🛡️📄