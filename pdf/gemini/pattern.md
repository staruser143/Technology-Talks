You're absolutely right to be concerned about configuration bloat! Instead of flattening everything upfront, you can use a **dynamic field resolution approach** that traverses your data structure on-demand based on the actual PDF field names.

Here's a smarter, more generic solution:

## 1. **Extract PDF Field Patterns**

First, analyze your PDF template to understand the field naming conventions:

```java
public class PdfFieldAnalyzer {
    
    public FieldPattern analyzeFields(PDDocument document) {
        PDAcroForm acroForm = document.getDocumentCatalog().getAcroForm();
        if (acroForm == null) return new FieldPattern();
        
        FieldPattern pattern = new FieldPattern();
        
        for (PDField field : acroForm.getFields()) {
            String fieldName = field.getFullyQualifiedName();
            pattern.addField(fieldName);
        }
        
        return pattern;
    }
    
    public static class FieldPattern {
        private Set<String> simpleFields = new HashSet<>();
        private Map<String, ArrayPattern> arrayPatterns = new HashMap<>();
        private Map<String, ObjectPattern> objectPatterns = new HashMap<>();
        
        // Methods to add and analyze fields
        public void addField(String fieldName) {
            if (fieldName.contains("[")) {
                // Array field: orders[0].item
                parseArrayField(fieldName);
            } else if (fieldName.contains(".")) {
                // Object field: customer.address.street
                parseObjectField(fieldName);
            } else {
                simpleFields.add(fieldName);
            }
        }
        
        private void parseArrayField(String fieldName) {
            // Extract base path and index: "orders[0].item" -> base="orders", index=0, suffix="item"
            String[] parts = fieldName.split("\\[", 2);
            String base = parts[0];
            String remaining = parts[1];
            int closeBracket = remaining.indexOf(']');
            int index = Integer.parseInt(remaining.substring(0, closeBracket));
            String suffix = remaining.substring(closeBracket + 2); // +2 for "]."
            
            arrayPatterns.computeIfAbsent(base, k -> new ArrayPattern(base))
                        .addIndex(index, suffix);
        }
        
        private void parseObjectField(String fieldName) {
            // Extract object path: "customer.address.street" -> path=["customer", "address"], leaf="street"
            String[] parts = fieldName.split("\\.");
            String objectPath = String.join(".", Arrays.copyOf(parts, parts.length - 1));
            String leafField = parts[parts.length - 1];
            
            objectPatterns.computeIfAbsent(objectPath, k -> new ObjectPattern(objectPath))
                         .addLeafField(leafField);
        }
    }
    
    public static class ArrayPattern {
        private String baseName;
        private Set<Integer> indices = new HashSet<>();
        private Set<String> suffixes = new HashSet<>();
        
        public ArrayPattern(String baseName) {
            this.baseName = baseName;
        }
        
        public void addIndex(int index, String suffix) {
            indices.add(index);
            if (!suffix.isEmpty()) {
                suffixes.add(suffix);
            }
        }
        
        // Getters...
    }
    
    public static class ObjectPattern {
        private String objectPath;
        private Set<String> leafFields = new HashSet<>();
        
        public ObjectPattern(String objectPath) {
            this.objectPath = objectPath;
        }
        
        public void addLeafField(String leafField) {
            leafFields.add(leafField);
        }
        
        // Getters...
    }
}
```

## 2. **Dynamic Value Resolver**

Instead of flattening everything, resolve values on-demand based on field patterns:

```java
public class DynamicValueResolver {
    
    public String resolveValue(Object data, String fieldName, FieldPattern pattern) {
        try {
            if (fieldName.contains("[")) {
                return resolveArrayValue(data, fieldName, pattern);
            } else if (fieldName.contains(".")) {
                return resolveObjectValue(data, fieldName);
            } else {
                return resolveSimpleValue(data, fieldName);
            }
        } catch (Exception e) {
            // Log and return empty string for missing fields
            return "";
        }
    }
    
    private String resolveArrayValue(Object data, String fieldName, FieldPattern pattern) {
        // Parse: "orders[2].item.name"
        Pattern arrayPattern = Pattern.compile("([^\\[]+)\\[(\\d+)\\](.*)");
        Matcher matcher = arrayPattern.matcher(fieldName);
        
        if (matcher.matches()) {
            String baseName = matcher.group(1);      // "orders"
            int index = Integer.parseInt(matcher.group(2)); // 2
            String suffix = matcher.group(3);        // ".item.name" (may be empty)
            
            // Get the array/list from data
            Object arrayValue = getValueAtPath(data, baseName);
            if (arrayValue instanceof List) {
                List<?> list = (List<?>) arrayValue;
                if (index < list.size()) {
                    Object item = list.get(index);
                    if (suffix.isEmpty()) {
                        return item != null ? item.toString() : "";
                    } else {
                        // Resolve nested object within array item
                        return resolveObjectValue(item, suffix.substring(1)); // remove leading "."
                    }
                }
            }
        }
        return "";
    }
    
    private String resolveObjectValue(Object data, String fieldName) {
        // Navigate through object path: "customer.address.street"
        Object current = data;
        String[] parts = fieldName.split("\\.");
        
        for (String part : parts) {
            if (current == null) return "";
            current = getValueFromObject(current, part);
        }
        
        return current != null ? current.toString() : "";
    }
    
    private String resolveSimpleValue(Object data, String fieldName) {
        Object value = getValueFromObject(data, fieldName);
        return value != null ? value.toString() : "";
    }
    
    private Object getValueAtPath(Object data, String path) {
        if (path.isEmpty()) return data;
        Object current = data;
        for (String part : path.split("\\.")) {
            if (current == null) return null;
            current = getValueFromObject(current, part);
        }
        return current;
    }
    
    private Object getValueFromObject(Object obj, String fieldName) {
        if (obj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) obj;
            return map.get(fieldName);
        } else {
            // Use reflection for POJOs
            try {
                Class<?> clazz = obj.getClass();
                Method getter = findGetter(clazz, fieldName);
                return getter.invoke(obj);
            } catch (Exception e) {
                return null;
            }
        }
    }
    
    private Method findGetter(Class<?> clazz, String fieldName) throws NoSuchMethodException {
        String methodName = "get" + capitalize(fieldName);
        try {
            return clazz.getMethod(methodName);
        } catch (NoSuchMethodException e) {
            // Try boolean getter
            methodName = "is" + capitalize(fieldName);
            return clazz.getMethod(methodName);
        }
    }
    
    private String capitalize(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
```

## 3. **Generic PDF Filler with Dynamic Resolution**

```java
public class GenericPdfFormFiller {
    private final PdfFieldAnalyzer analyzer = new PdfFieldAnalyzer();
    private final DynamicValueResolver resolver = new DynamicValueResolver();
    
    public void fillForm(PDDocument document, Object data) throws IOException {
        // Analyze PDF field patterns once
        FieldPattern fieldPattern = analyzer.analyzeFields(document);
        PDAcroForm acroForm = document.getDocumentCatalog().getAcroForm();
        
        // Fill each field dynamically
        for (PDField field : acroForm.getFields()) {
            String fieldName = field.getFullyQualifiedName();
            String value = resolver.resolveValue(data, fieldName, fieldPattern);
            field.setValue(value);
        }
    }
}
```

## 4. **Minimal Configuration Approach**

With this approach, your configuration becomes much simpler:

```java
// Configuration only needed for special cases
public class TemplateConfig {
    // Only specify mappings for fields that don't follow standard naming
    private Map<String, String> customMappings = new HashMap<>();
    
    // Field name transformations (e.g., replace dots with underscores)
    private boolean useAlternativeNaming = false;
    
    // Maximum array size to prevent infinite loops
    private int maxArraySize = 50;
}
```

**Example usage with complex **

```java
// Your data structure
Map<String, Object> data = Map.of(
    "customer", Map.of(
        "name", "John Doe",
        "address", Map.of(
            "street", "123 Main St",
            "city", "Anytown"
        )
    ),
    "orders", List.of(
        Map.of("item", "Widget A", "quantity", 2, "price", 19.99),
        Map.of("item", "Widget B", "quantity", 5, "price", 29.99)
    )
);

// PDF fields automatically matched:
// customer.name -> "John Doe"
// customer.address.street -> "123 Main St"  
// orders[0].item -> "Widget A"
// orders[1].price -> "29.99"
```

## 5. **Benefits of This Approach**

✅ **No upfront flattening** - only resolves fields that actually exist in the PDF  
✅ **Minimal configuration** - follows natural field naming conventions  
✅ **Handles arbitrary nesting** - works with any depth of objects/arrays  
✅ **Memory efficient** - doesn't create huge flattened maps  
✅ **Flexible** - easy to add custom mappings only when needed  

## 6. **Enhanced with Caching**

For performance with repeated operations:

```java
public class CachedDynamicValueResolver {
    private final Map<String, FieldPath> fieldPathCache = new ConcurrentHashMap<>();
    
    private FieldPath parseFieldPath(String fieldName) {
        return fieldPathCache.computeIfAbsent(fieldName, this::doParseFieldPath);
    }
    
    private FieldPath doParseFieldPath(String fieldName) {
        // Parse once and cache the navigation path
        // Returns optimized path for value resolution
    }
}
```

This dynamic approach gives you the best of both worlds: the flexibility to handle complex nested data structures without the configuration overhead of explicit field mappings, while maintaining good performance through on-demand resolution.