Excellent choice! Unit tests will give you confidence that your **filtering logic** (including nested collections and compound conditions) works correctly.

Below is a **complete, ready-to-run JUnit 5 test suite** for your `ConditionEvaluator` and **collection filtering logic**.

---

## 📁 Project Structure (Add to Existing)

```
src/test/java/
└── engine/
    ├── ConditionEvaluatorTest.java
    └── CollectionFilteringTest.java
```

> ✅ Uses **JUnit 5**, **AssertJ**, and **JsonPath**

---

## 📦 Dependencies (Maven)

Add to `pom.xml` if not already present:

```xml
<dependency>
  <groupId>org.junit.jupiter</groupId>
  <artifactId>junit-jupiter</artifactId>
  <version>5.10.0</version>
  <scope>test</scope>
</dependency>
<dependency>
  <groupId>org.assertj</groupId>
  <artifactId>assertj-core</artifactId>
  <version>3.24.2</version>
  <scope>test</scope>
</dependency>
```

---

## ✅ 1. Unit Tests for `ConditionEvaluator`

### `ConditionEvaluatorTest.java`
```java
package engine;

import model.Condition;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class ConditionEvaluatorTest {

    @Test
    void shouldPassNotNullCondition_WhenValueIsPresent() {
        Condition cond = new Condition();
        cond.setType("notNull");

        assertThat(ConditionEvaluator.evaluate(cond, null, "hello")).isTrue();
        assertThat(ConditionEvaluator.evaluate(cond, null, 42)).isTrue();
    }

    @Test
    void shouldFailNotNullCondition_WhenValueIsNull() {
        Condition cond = new Condition();
        cond.setType("notNull");

        assertThat(ConditionEvaluator.evaluate(cond, null, null)).isFalse();
        assertThat(ConditionEvaluator.evaluate(cond, null, "")).isFalse();
    }

    @Test
    void shouldPassEqualsCondition() {
        Condition cond = new Condition();
        cond.setType("equals");
        cond.setField("user.status");
        cond.setValue("ACTIVE");

        String json = "{ \"user\": { \"status\": \"ACTIVE\" } }";
        DocumentContext ctx = JsonPath.parse(json);

        assertThat(ConditionEvaluator.evaluate(cond, ctx, null)).isTrue();
    }

    @Test
    void shouldFailEqualsCondition() {
        Condition cond = new Condition();
        cond.setType("equals");
        cond.setField("user.status");
        cond.setValue("ACTIVE");

        String json = "{ \"user\": { \"status\": \"INACTIVE\" } }";
        DocumentContext ctx = JsonPath.parse(json);

        assertThat(ConditionEvaluator.evaluate(cond, ctx, null)).isFalse();
    }

    @Test
    void shouldPassGreaterThanCondition() {
        Condition cond = new Condition();
        cond.setType("greaterThan");
        cond.setValue(100);

        assertThat(ConditionEvaluator.evaluate(cond, null, 150)).isTrue();
        assertThat(ConditionEvaluator.evaluate(cond, null, "150")).isTrue(); // string number
    }

    @Test
    void shouldFailGreaterThanCondition() {
        Condition cond = new Condition();
        cond.setType("greaterThan");
        cond.setValue(100);

        assertThat(ConditionEvaluator.evaluate(cond, null, 50)).isFalse();
        assertThat(ConditionEvaluator.evaluate(cond, null, "50")).isFalse();
    }

    @Test
    void shouldPassAndCondition_WhenAllSubConditionsPass() {
        Condition cond1 = new Condition();
        cond1.setType("equals");
        cond1.setField("status");
        cond1.setValue("SHIPPED");

        Condition cond2 = new Condition();
        cond2.setType("greaterThan");
        cond2.setField("total");
        cond2.setValue(50);

        Condition andCond = new Condition();
        andCond.setAnd(java.util.Arrays.asList(cond1, cond2));

        String json = "{ \"status\": \"SHIPPED\", \"total\": 100 }";
        DocumentContext ctx = JsonPath.parse(json);

        assertThat(ConditionEvaluator.evaluate(andCond, ctx, null)).isTrue();
    }

    @Test
    void shouldFailAndCondition_WhenOneSubConditionFails() {
        Condition cond1 = new Condition();
        cond1.setType("equals");
        cond1.setField("status");
        cond1.setValue("SHIPPED");

        Condition cond2 = new Condition();
        cond2.setType("greaterThan");
        cond2.setField("total");
        cond2.setValue(50);

        Condition andCond = new Condition();
        andCond.setAnd(java.util.Arrays.asList(cond1, cond2));

        String json = "{ \"status\": \"PENDING\", \"total\": 100 }"; // status fails
        DocumentContext ctx = JsonPath.parse(json);

        assertThat(ConditionEvaluator.evaluate(andCond, ctx, null)).isFalse();
    }

    @Test
    void shouldPassOrCondition_WhenOneSubConditionPasses() {
        Condition cond1 = new Condition();
        cond1.setType("equals");
        cond1.setField("role");
        cond1.setValue("ADMIN");

        Condition cond2 = new Condition();
        cond2.setType("equals");
        cond2.setField("tier");
        cond2.setValue("VIP");

        Condition orCond = new Condition();
        orCond.setOr(java.util.Arrays.asList(cond1, cond2));

        String json = "{ \"role\": \"USER\", \"tier\": \"VIP\" }";
        DocumentContext ctx = JsonPath.parse(json);

        assertThat(ConditionEvaluator.evaluate(orCond, ctx, null)).isTrue();
    }

    @Test
    void shouldFailOrCondition_WhenAllSubConditionsFail() {
        Condition cond1 = new Condition();
        cond1.setType("equals");
        cond1.setField("role");
        cond1.setValue("ADMIN");

        Condition cond2 = new Condition();
        cond2.setType("equals");
        cond2.setField("tier");
        cond2.setValue("VIP");

        Condition orCond = new Condition();
        orCond.setOr(java.util.Arrays.asList(cond1, cond2));

        String json = "{ \"role\": \"USER\", \"tier\": \"REGULAR\" }";
        DocumentContext ctx = JsonPath.parse(json);

        assertThat(ConditionEvaluator.evaluate(orCond, ctx, null)).isFalse();
    }

    @Test
    void shouldHandleNestedAndOrCondition() {
        // (status == SHIPPED) AND (country == US OR country == CA)
        Condition countryUs = new Condition();
        countryUs.setType("equals");
        countryUs.setField("country");
        countryUs.setValue("US");

        Condition countryCa = new Condition();
        countryCa.setType("equals");
        countryCa.setField("country");
        countryCa.setValue("CA");

        Condition countryOr = new Condition();
        countryOr.setOr(java.util.Arrays.asList(countryUs, countryCa));

        Condition statusCond = new Condition();
        statusCond.setType("equals");
        statusCond.setField("status");
        statusCond.setValue("SHIPPED");

        Condition rootAnd = new Condition();
        rootAnd.setAnd(java.util.Arrays.asList(statusCond, countryOr));

        String json = "{ \"status\": \"SHIPPED\", \"country\": \"CA\" }";
        DocumentContext ctx = JsonPath.parse(json);

        assertThat(ConditionEvaluator.evaluate(rootAnd, ctx, null)).isTrue();
    }
}
```

---

## ✅ 2. Integration Tests for Collection Filtering

### `CollectionFilteringTest.java`
```java
package engine;

import model.*;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import java.util.*;

import static org.assertj.core.api.Assertions.*;

class CollectionFilteringTest {

    @Test
    void shouldFilterOuterCollection_WhenConditionIsApplied() {
        // Given: orders with mixed status
        String json = """
        {
          "orders": [
            { "id": "O1", "status": "PENDING" },
            { "id": "O2", "status": "SHIPPED" },
            { "id": "O3", "status": "SHIPPED" }
          ]
        }
        """;

        // Create collection mapping with condition
        CollectionMapping coll = new CollectionMapping();
        coll.setSource("orders");
        coll.setMaxItems(10);

        Condition cond = new Condition();
        cond.setType("equals");
        cond.setField("status");
        cond.setValue("SHIPPED");
        coll.setCondition(cond);

        ItemFieldMapping idField = new ItemFieldMapping();
        idField.setSource("id");
        idField.setTargetSuffix("_id");
        coll.setItemMappings(Arrays.asList(idField));

        // When: evaluate which items pass
        DocumentContext ctx = JsonPath.parse(json);
        List<?> rawItems = ctx.read("$.orders");
        List<Object> filtered = new ArrayList<>();

        for (Object item : rawItems) {
            boolean passes = ConditionEvaluator.evaluate(
                coll.getCondition(),
                JsonPath.parse(item),
                item
            );
            if (passes) filtered.add(item);
        }

        // Then: only SHIPPED orders remain
        assertThat(filtered).hasSize(2);
        assertThat(filtered).extracting(item -> JsonPath.parse(item).read("$.id"))
                .containsExactly("O2", "O3");
    }

    @Test
    void shouldFilterInnerCollection_WhenConditionIsApplied() {
        String json = """
        {
          "orders": [
            {
              "id": "O1",
              "items": [
                { "sku": "A", "price": 0 },
                { "sku": "B", "price": 100 },
                { "sku": "C", "price": 0 }
              ]
            }
          ]
        }
        """;

        // Inner collection condition: price > 0
        CollectionMapping innerColl = new CollectionMapping();
        innerColl.setSource("items");
        innerColl.setMaxItems(10);

        Condition priceCond = new Condition();
        priceCond.setType("greaterThan");
        priceCond.setField("price");
        priceCond.setValue(0);
        innerColl.setCondition(priceCond);

        ItemFieldMapping skuField = new ItemFieldMapping();
        skuField.setSource("sku");
        skuField.setTargetSuffix("_sku");
        innerColl.setItemMappings(Arrays.asList(skuField));

        // Simulate outer item
        DocumentContext ctx = JsonPath.parse(json);
        Object orderItem = ctx.read("$.orders[0]");

        List<?> rawItems = JsonPath.parse(orderItem).read("$.items");
        List<Object> filtered = new ArrayList<>();

        for (Object item : rawItems) {
            boolean passes = ConditionEvaluator.evaluate(
                innerColl.getCondition(),
                JsonPath.parse(item),
                item
            );
            if (passes) filtered.add(item);
        }

        assertThat(filtered).hasSize(1);
        assertThat(JsonPath.parse(filtered.get(0)).read("$.sku")).isEqualTo("B");
    }

    @Test
    void shouldHandleNestedCollectionsWithFiltering() {
        String json = """
        {
          "orders": [
            {
              "id": "O1",
              "status": "DRAFT",
              "items": [ { "sku": "X", "price": 50 } ]
            },
            {
              "id": "O2",
              "status": "SHIPPED",
              "items": [
                { "sku": "A", "price": 0 },
                { "sku": "B", "price": 200 }
              ]
            }
          ]
        }
        """;

        // Outer: only SHIPPED
        CollectionMapping outer = new CollectionMapping();
        outer.setSource("orders");
        outer.setMaxItems(10);
        Condition outerCond = new Condition();
        outerCond.setType("equals");
        outerCond.setField("status");
        outerCond.setValue("SHIPPED");
        outer.setCondition(outerCond);

        // Inner: only price > 0
        CollectionMapping inner = new CollectionMapping();
        inner.setSource("items");
        inner.setMaxItems(10);
        Condition innerCond = new Condition();
        innerCond.setType("greaterThan");
        innerCond.setField("price");
        innerCond.setValue(0);
        inner.setCondition(innerCond);

        ItemFieldMapping skuField = new ItemFieldMapping();
        skuField.setSource("sku");
        skuField.setTargetSuffix("_sku");
        inner.setItemMappings(Arrays.asList(skuField));

        ItemFieldMapping orderItem = new ItemFieldMapping();
        orderItem.setCollection(inner);
        outer.setItemMappings(Arrays.asList(orderItem));

        // Simulate full filtering
        DocumentContext ctx = JsonPath.parse(json);
        List<?> rawOrders = ctx.read("$.orders");
        List<Object> shippedOrders = new ArrayList<>();

        for (Object order : rawOrders) {
            if (ConditionEvaluator.evaluate(outer.getCondition(), JsonPath.parse(order), order)) {
                shippedOrders.add(order);
            }
        }

        assertThat(shippedOrders).hasSize(1);

        // Now filter items in the shipped order
        Object shippedOrder = shippedOrders.get(0);
        List<?> rawItems = JsonPath.parse(shippedOrder).read("$.items");
        List<Object> paidItems = new ArrayList<>();

        for (Object item : rawItems) {
            if (ConditionEvaluator.evaluate(inner.getCondition(), JsonPath.parse(item), item)) {
                paidItems.add(item);
            }
        }

        assertThat(paidItems).hasSize(1);
        assertThat(JsonPath.parse(paidItems.get(0)).read("$.sku")).isEqualTo("B");
    }
}
```

---

## ▶️ How to Run Tests

### With Maven:
```bash
mvn test
```

### With IntelliJ/Eclipse:
- Right-click test class → **Run**

---

## 🧪 Test Coverage

| Feature | Tested? |
|--------|--------|
| `notNull` condition | ✅ |
| `equals` / `greaterThan` | ✅ |
| `AND` compound | ✅ |
| `OR` compound | ✅ |
| Nested `AND`/`OR` | ✅ |
| Outer collection filtering | ✅ |
| Inner collection filtering | ✅ |
| Full nested filtering workflow | ✅ |

---

## 💡 Tips for Extending Tests

1. **Add edge cases**:
   - Empty arrays
   - Missing fields
   - Invalid numeric strings

2. **Test dry-run logging**:
   ```java
   @Test
   void shouldLogSkippedItemsInDryRun() {
       // Capture System.out and verify log messages
   }
   ```

3. **Parameterized tests** for multiple condition types:
   ```java
   @ParameterizedTest
   @ValueSource(strings = {"SHIPPED", "DELIVERED"})
   void shouldAcceptMultipleStatuses(String status) { ... }
   ```

---

You now have a **robust test suite** that validates your filtering logic at every level!

Would you like:
- **Parameterized tests** for transform logic?
- **PDF integration tests** (using a real fillable PDF)?
- **Performance benchmarks** for large arrays?

Just ask — happy to help you build confidence in your system! 🧪✅