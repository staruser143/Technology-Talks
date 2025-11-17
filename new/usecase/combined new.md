Excellent question! Combining data from multiple source fields into a single target field is a **very common requirement** in PDF generation (e.g., full name = first name + last name, address = street + city + state).

Let me show you several powerful approaches to handle this efficiently.

---

## 🎯 Common Use Cases

| Scenario | Source Fields | Target Field |
|---------|---------------|--------------|
| **Full Name** | `firstName`, `lastName` | `fullName` |
| **Full Address** | `street`, `city`, `state`, `zip` | `fullAddress` |
| **Contact Info** | `phone`, `email` | `contactDetails` |
| **Product Description** | `productName`, `sku`, `price` | `itemDescription` |

---

## ✅ Solution 1: Concat Transform (Recommended)

### Step 1: Create a Concat Transform

```java
package engine.transform;

import java.util.List;
import java.util.Map;

public class ConcatTransform extends BaseTransformer {
    @Override
    public Object transform(Object value, Object config) {
        // For concat, 'value' will be a Map of multiple source values
        if (!(value instanceof Map)) {
            return toString(value);
        }
        
        @SuppressWarnings("unchecked")
        Map<String, Object> sourceValues = (Map<String, Object>) value;
        
        String separator = " ";
        String format = null;
        boolean skipEmpty = true;
        
        // Parse configuration
        if (config instanceof Map) {
            Map<String, Object> args = (Map<String, Object>) config;
            separator = (String) args.getOrDefault("separator", separator);
            format = (String) args.get("format");
            skipEmpty = (Boolean) args.getOrDefault("skipEmpty", skipEmpty);
        }
        
        if (format != null) {
            return applyFormat(format, sourceValues, skipEmpty);
        } else {
            return applySimpleConcat(sourceValues, separator, skipEmpty);
        }
    }
    
    private String applySimpleConcat(Map<String, Object> values, String separator, boolean skipEmpty) {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        
        for (Object val : values.values()) {
            String str = toString(val);
            if (skipEmpty && str.trim().isEmpty()) {
                continue;
            }
            
            if (!first) {
                result.append(separator);
            }
            result.append(str);
            first = false;
        }
        
        return result.toString();
    }
    
    private String applyFormat(String format, Map<String, Object> values, boolean skipEmpty) {
        String result = format;
        
        // Replace placeholders like {firstName}, {lastName}
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            String key = entry.getKey();
            String value = toString(entry.getValue());
            
            if (skipEmpty && value.trim().isEmpty()) {
                // Replace with empty string or remove the placeholder entirely?
                result = result.replace("{" + key + "}", "");
                // Also handle cases like ", {middleName}" → remove comma too
                result = result.replaceAll(",\\s*\\{[^}]*\\}", "");
            } else {
                result = result.replace("{" + key + "}", value);
            }
        }
        
        // Clean up extra spaces and commas
        result = result.replaceAll("\\s+,", ",").trim();
        result = result.replaceAll(",\\s*$", ""); // Remove trailing comma
        
        return result;
    }
}
```

### Step 2: Enhanced Field Mapping for Multi-Source

```java
// Add to FieldMapping.java
private List<String> sources; // For multi-source mappings

public boolean isMultiSource() {
    return sources != null && !sources.isEmpty();
}
```

### Step 3: Processing Logic for Multi-Source

```java
// In PdfFieldMapper.java
private void processMultiSourceMapping(FieldMapping mapping, DocumentContext rootCtx,
                                     Map<String, Object> contextCache, MappingResult result) {
    // Collect all source values
    Map<String, Object> sourceValues = new HashMap<>();
    
    for (String sourcePath : mapping.getSources()) {
        // Extract field name from path (e.g., "primary.firstName" → "firstName")
        String fieldName = extractFieldName(sourcePath);
        Object value = resolveValue(sourcePath, rootCtx, contextCache);
        sourceValues.put(fieldName, value);
    }
    
    boolean conditionPassed = ConditionEvaluator.evaluate(
        mapping.getCondition(), rootCtx, sourceValues
    );
    
    if (!conditionPassed) return;
    
    // Apply concat transform
    Object transformed = TransformRegistry.getInstance()
        .applyTransform(sourceValues, mapping.getTransform());
    
    String finalValue = (transformed != null) ? transformed.toString() : "";
    if (finalValue.trim().isEmpty() && mapping.getDefaultValue() != null) {
        finalValue = mapping.getDefaultValue();
    }
    
    result.setFieldValue(mapping.getTarget(), finalValue);
}

private String extractFieldName(String sourcePath) {
    // Simple extraction - you can make this more sophisticated
    if (sourcePath.contains(".")) {
        return sourcePath.substring(sourcePath.lastIndexOf(".") + 1);
    }
    return sourcePath;
}
```

### Step 4: YAML Configuration

```yaml
contexts:
  primary:
    from: applicants
    filter: { type: PRIMARY }
    first: true

mappings:
  # Simple concatenation with space separator
  - sources: 
      - primary.demographics.firstName
      - primary.demographics.lastName
    target: primary_fullName
    transform: "concat"

  # Custom separator
  - sources:
      - primary.addresses[0].city
      - primary.addresses[0].state
      - primary.addresses[0].zipCode
    target: primary_cityStateZip
    transform:
      name: "concat"
      args:
        separator: ", "

  # Formatted concatenation
  - sources:
      - primary.demographics.firstName
      - primary.demographics.middleName
      - primary.demographics.lastName
    target: primary_formalName
    transform:
      name: "concat"
      args:
        format: "{firstName} {middleName} {lastName}"
        skipEmpty: true
```

---

## ✅ Solution 2: Template Transform (More Flexible)

### Step 1: Create Template Transform

```java
package engine.transform;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TemplateTransform extends BaseTransformer {
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{([^}]+)\\}");
    
    @Override
    public Object transform(Object value, Object config) {
        if (!(value instanceof Map)) {
            return toString(value);
        }
        
        @SuppressWarnings("unchecked")
        Map<String, Object> context = (Map<String, Object>) value;
        String template = toString(config);
        
        if (template.isEmpty()) {
            return "";
        }
        
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(template);
        StringBuffer result = new StringBuffer();
        
        while (matcher.find()) {
            String fieldName = matcher.group(1);
            String fieldValue = toString(context.get(fieldName));
            matcher.appendReplacement(result, Matcher.quoteReplacement(fieldValue));
        }
        matcher.appendTail(result);
        
        return result.toString();
    }
}
```

### Step 2: YAML Configuration

```yaml
mappings:
  - sources:
      - primary.demographics.firstName
      - primary.demographics.lastName
      - primary.demographics.title
    target: primary_displayName
    transform:
      name: "template"
      args: "{title} {firstName} {lastName}"

  - sources:
      - primary.addresses[0].line1
      - primary.addresses[0].line2
      - primary.addresses[0].city
      - primary.addresses[0].state
      - primary.addresses[0].zipCode
    target: primary_fullAddress
    transform:
      name: "template"
      args: "{line1}\n{line2}\n{city}, {state} {zipCode}"
```

---

## ✅ Solution 3: Custom Transform for Complex Logic

### Custom Transform Example
```java
package engine.transform.custom;

import engine.transform.CustomTransformer;
import java.util.Map;

public class FullNameTransform implements CustomTransformer {
    @Override
    public Object transform(Object value, Object config) {
        if (!(value instanceof Map)) {
            return "";
        }
        
        @SuppressWarnings("unchecked")
        Map<String, Object> fields = (Map<String, Object>) value;
        
        String title = toString(fields.get("title"));
        String firstName = toString(fields.get("firstName"));
        String middleName = toString(fields.get("middleName"));
        String lastName = toString(fields.get("lastName"));
        String suffix = toString(fields.get("suffix"));
        
        StringBuilder fullName = new StringBuilder();
        
        if (!title.isEmpty()) fullName.append(title).append(" ");
        fullName.append(firstName);
        if (!middleName.isEmpty()) fullName.append(" ").append(middleName);
        if (!lastName.isEmpty()) fullName.append(" ").append(lastName);
        if (!suffix.isEmpty()) fullName.append(", ").append(suffix);
        
        return fullName.toString().trim();
    }
    
    private String toString(Object obj) {
        return obj != null ? obj.toString() : "";
    }
}
```

### YAML Configuration
```yaml
mappings:
  - sources:
      - primary.demographics.title
      - primary.demographics.firstName  
      - primary.demographics.middleName
      - primary.demographics.lastName
      - primary.demographics.suffix
    target: primary_legalName
    transform: "fullName"
```

---

## ✅ Solution 4: Context-Based Combination (Most Efficient)

### Step 1: Create Combined Context
```yaml
contexts:
  primary:
    from: applicants
    filter: { type: PRIMARY }
    first: true

  primaryNameParts:
    from: primary.demographics
    # This gives us access to all demographics fields

mappings:
  - source: primaryNameParts
    target: primary_fullName
    transform:
      name: "template"
      args: "{firstName} {lastName}"
```

### Step 2: Enhanced Template Transform to Handle Object Sources
```java
@Override
public Object transform(Object value, Object config) {
    Map<String, Object> context;
    
    if (value instanceof Map) {
        context = (Map<String, Object>) value;
    } else {
        // Convert object to map using reflection or JsonPath
        context = objectToMap(value);
    }
    
    // ... rest of template logic
}
```

---

## 🧪 Handling Edge Cases

### 1. **Missing Fields**
```yaml
transform:
  name: "concat"
  args:
    skipEmpty: true  # Skip null/empty fields
    separator: " "
```

### 2. **Conditional Combination**
```yaml
- sources:
    - primary.demographics.firstName
    - primary.demographics.lastName
  target: primary_fullName
  condition:
    type: "notNull"
    field: "primary.demographics.firstName"
  transform: "concat"
```

### 3. **Default Values for Missing Parts**
```yaml
- sources:
    - primary.demographics.firstName
    - primary.demographics.lastName
  target: primary_fullName
  transform:
    name: "template"
    args: "{firstName:-[No First Name]} {lastName:-[No Last Name]}"
```

---

## 📄 Complete Working Example

### JSON Input
```json
{
  "applicants": [
    {
      "type": "PRIMARY",
      "demographics": {
        "title": "Mr.",
        "firstName": "John",
        "middleName": "Robert",
        "lastName": "Doe",
        "suffix": "Jr."
      },
      "addresses": [
        {
          "line1": "123 Main St",
          "line2": "Apt 4B",
          "city": "Springfield",
          "state": "IL",
          "zipCode": "62701"
        }
      ]
    }
  ]
}
```

### YAML Configuration
```yaml
contexts:
  primary:
    from: applicants
    filter: { type: PRIMARY }
    first: true

mappings:
  # Full name with title and suffix
  - sources:
      - primary.demographics.title
      - primary.demographics.firstName
      - primary.demographics.middleName  
      - primary.demographics.lastName
      - primary.demographics.suffix
    target: primary_fullName
    transform:
      name: "template"
      args: "{title} {firstName} {middleName} {lastName}, {suffix}"
      skipEmpty: true

  # Full address
  - sources:
      - primary.addresses[0].line1
      - primary.addresses[0].line2
      - primary.addresses[0].city
      - primary.addresses[0].state
      - primary.addresses[0].zipCode
    target: primary_fullAddress
    transform:
      name: "template"
      args: "{line1}\n{line2}\n{city}, {state} {zipCode}"
      skipEmpty: true

  # Simple city, state, zip
  - sources:
      - primary.addresses[0].city
      - primary.addresses[0].state  
      - primary.addresses[0].zipCode
    target: primary_cityStateZip
    transform:
      name: "concat"
      args:
        separator: ", "
        skipEmpty: true
```

### Expected Output
```java
{
  "primary_fullName": "Mr. John Robert Doe, Jr.",
  "primary_fullAddress": "123 Main St\nApt 4B\nSpringfield, IL 62701",
  "primary_cityStateZip": "Springfield, IL, 62701"
}
```

---

## ✅ Recommendation

**Use Solution 1 (Concat Transform) for simple cases** and **Solution 2 (Template Transform) for complex formatting** because they:

- ✅ **Require minimal changes** to your existing architecture
- ✅ **Are highly reusable** across different fields
- ✅ **Support configuration** (separators, formats, skipEmpty)
- ✅ **Handle edge cases** gracefully
- ✅ **Are easy to understand and maintain**

The key is extending your `FieldMapping` model to support `sources` (plural) and updating your processing logic to collect multiple values before applying the transform.

Would you like:
- **Support for mathematical operations** (e.g., sum multiple numeric fields)?
- **Conditional templates** (different formats based on conditions)?
- **HTML-to-text conversion** for rich text fields?

I'm happy to extend this solution further! 🧩✨