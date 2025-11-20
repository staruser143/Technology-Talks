You've asked a **fundamental and crucial question** that cuts to the heart of how your architecture works. Let me clarify this with concrete examples and explain the **key difference** between PDF and HTML template data requirements.

---

## 🎯 Core Principle: **PDF vs HTML Need Completely Different Data Structures**

| Aspect | PDF (AcroForm) | HTML (FreeMarker) |
|--------|----------------|------------------|
| **Data Structure** | ❌ **Flat key-value pairs** only | ✅ **Rich nested objects/lists** |
| **Field Access** | `"dependent.fname.1"` | `${dependents[0].firstName}` |
| **Repeating Data** | Must be flattened + indexed | Can use native `<#list>` |
| **Transformations** | Must output strings | Can output complex objects |
| **Flexibility** | Rigid (predefined fields) | Dynamic (logic in template) |

---

## 🔍 How Data Gets to FreeMarker Engine

### Your Current Flow:
```
YAML Config 
    → DataMapper.mapData() 
    → Map<String, Object> 
    → HtmlTemplateMerger 
    → FreeMarker.process(model, out)
```

The **`Map<String, Object>`** becomes the **FreeMarker data model**, where:
- Keys = variable names in template (`${applicants}`, `${metadata}`)
- Values = any Java object (String, List, Map, POJO)

---

## 📄 Real-World Examples

### ✅ What Works for HTML (Rich Objects)

#### YAML Configuration for HTML:
```yaml
mappings:
  # Pass entire applicants list as-is
  - sourceField: "applicants"
    targetField: "applicants"  # ← This becomes ${applicants} in FreeMarker

  # Pass filtered dependents as separate list  
  - sourceField: "applicants[relationship=dependent]"
    targetField: "dependents"  # ← This becomes ${dependents}

  # Pass metadata object
  - sourceField: "metadata"
    targetField: "metadata"    # ← This becomes ${metadata}
```

#### FreeMarker Template (`summary.ftl`):
```html
<!DOCTYPE html>
<html>
<body>
    <h1>Application Summary</h1>
    
    <!-- Access full applicant objects -->
    <h2>Primary Applicant</h2>
    <#-- Find primary applicant in list -->
    <#list applicants as app>
        <#if app.relationship == "primary">
            <p>Name: ${app.firstName} ${app.lastName}</p>
            <p>SSN: ${app.ssn}</p>
            
            <!-- Access nested addresses -->
            <#list app.addresses as addr>
                <#if addr.type == "home">
                    <p>Home: ${addr.street}, ${addr.city}</p>
                </#if>
            </#list>
        </#if>
    </#list>

    <!-- Use filtered dependents list -->
    <h2>Dependents (${dependents?size})</h2>
    <#list dependents as dep>
        <p>${dep.firstName} (Age: ${dep.age})</p>
    </#list>

    <!-- Access metadata -->
    <p>Submission ID: ${metadata.submissionId}</p>
</body>
</html>
```

#### Resulting FreeMarker Data Model:
```java
{
  "applicants": [
    { 
      "relationship": "primary",
      "firstName": "John",
      "lastName": "Doe",
      "ssn": "123-45-6789",
      "addresses": [
        { "type": "home", "street": "123 Main St", "city": "NYC" },
        { "type": "billing", "street": "456 Business Ave", "city": "NYC" }
      ]
    },
    {
      "relationship": "dependent",
      "firstName": "Alice",
      "age": 10
    }
  ],
  "dependents": [
    { "relationship": "dependent", "firstName": "Alice", "age": 10 }
  ],
  "metadata": {
    "submissionId": "APP-2025",
    "date": "2025-01-15"
  }
}
```

### ❌ What Doesn't Work for HTML (Flattened Fields)

```yaml
# WRONG for HTML - this is for PDF only!
mappings:
  - sourceField: "applicants[relationship=primary].firstName"
    targetField: "primary.fname.1"  # ❌ Don't do this for HTML
```

```html
<!-- This won't work well in HTML -->
<p>Name: ${primary.fname.1} ${primary.lname.1}</p>
<!-- You lose all the rich object structure -->
```

---

## 🧩 How Your DataMapper Handles This

### Current `DataMapper.mapData()` returns `Map<String, Object>`
```java
public Map<String, Object> mapData(Object jsonData, List<FieldMapping> mappings) {
    Map<String, Object> result = new HashMap<>();
    
    for (FieldMapping mapping : mappings) {
        if (mapping.isSingleField()) {
            // For HTML: sourceField returns entire objects/lists
            Object value = SimplePathResolver.read(jsonData, mapping.getSourceField());
            result.put(mapping.getTargetField(), value); // ← Keep as Object, not String!
        }
        // ... other mapping types
    }
    return result;
}
```

**Critical Point**: For HTML templates, your `DataMapper` should **NOT convert values to strings** — it should preserve the original object structure.

---

## 🔧 Enhanced DataMapper for HTML Templates

### Current Issue:
Your current `DataMapper` likely has this in `handleSingleValueMapping`:
```java
// ❌ Problem: Converts everything to String (good for PDF, bad for HTML)
result.put(mapping.getTargetField(), safeToString(transformed));
```

### Fixed Version:
```java
private void handleSingleValueMapping(Object jsonData, FieldMapping mapping, Map<String, Object> result) {
    Object rawValue = SimplePathResolver.read(jsonData, mapping.getSourceField());
    if (rawValue == null) return;
    
    Object transformed = applyTransformations(rawValue, mapping.getTransforms());
    
    // ✅ For HTML: Preserve object structure
    // ✅ For PDF: Convert to String
    // How to know which? Check template type or add config flag
    
    // Option 1: Let target field name indicate intent
    if (mapping.getTargetField().endsWith(".1") || mapping.getTargetField().contains(".")) {
        // PDF-style field name → flatten to string
        result.put(mapping.getTargetField(), safeToString(transformed));
    } else {
        // HTML-style variable name → preserve structure
        result.put(mapping.getTargetField(), transformed);
    }
}
```

### Better Option: Template-Specific Mapping Engines
As discussed earlier, create separate mapping engines:

```java
// HtmlMappingEngine.java
public Map<String, Object> mapToHtmlModel(Object jsonData, List<HtmlFieldMapping> mappings) {
    Map<String, Object> model = new HashMap<>();
    for (HtmlFieldMapping mapping : mappings) {
        Object value = extractValue(jsonData, mapping.getSourceField());
        Object transformed = applyTransformations(value, mapping.getTransforms(), true); // allow complex objects
        model.put(mapping.getTargetVariable(), transformed); // ← Preserve structure!
    }
    return model;
}

// PdfMappingEngine.java  
public Map<String, String> mapToPdfFields(Object jsonData, List<PdfFieldMapping> mappings) {
    Map<String, String> fields = new HashMap<>();
    for (PdfFieldMapping mapping : mappings) {
        Object value = extractValue(jsonData, mapping.getSourceField());
        String transformed = applyTransformations(value, mapping.getTransforms(), false); // strings only
        fields.put(mapping.getTargetField(), transformed.toString());
    }
    return fields;
}
```

---

## 📄 YAML Configuration Comparison

### For PDF Templates (Flattened):
```yaml
templates:
  - name: "application-pdf"
    type: PDF
    mappings:
      - sourceField: "applicants[relationship=primary].firstName"
        targetField: "primary.fname.1"  # ← Flattened, indexed
      - sourceField: "applicants[relationship=primary].lastName"  
        targetField: "primary.lname.1"
```

### For HTML Templates (Rich Objects):
```yaml
templates:
  - name: "summary-html"
    type: HTML
    mappings:
      - sourceField: "applicants"
        targetVariable: "applicants"  # ← Rich list of objects
      - sourceField: "applicants[relationship=dependent]"
        targetVariable: "dependents"  # ← Filtered list
      - sourceField: "metadata"
        targetVariable: "metadata"    # ← Nested object
```

---

## 🔍 FreeMarker Data Access Patterns

### Pattern 1: Direct Object Access
```html
<!-- Access nested properties directly -->
<p>Primary: ${applicants[0].firstName} ${applicants[0].lastName}</p>
<p>Home Street: ${applicants[0].addresses[0].street}</p>
```

### Pattern 2: Filtering in Template
```html
<!-- Filter in template (less efficient but flexible) -->
<#list applicants as app>
  <#if app.relationship == "primary">
    <p>Primary: ${app.firstName}</p>
    
    <#-- Nested filtering -->
    <#list app.addresses as addr>
      <#if addr.type == "home">
        <p>Home: ${addr.street}</p>
      </#if>
    </#list>
  </#if>
</#list>
```

### Pattern 3: Pre-filtered Lists (Recommended)
```html
<!-- More efficient - filtering done in mapping layer -->
<#list dependents as dep>
  <p>Dependent: ${dep.firstName} (${dep.age})</p>
</#list>

<#list primaryApplicant.addresses?filter(addr -> addr.type == "home") as homeAddr>
  <p>Home: ${homeAddr.street}</p>
</#list>
```

---

## 🚀 Best Practices Summary

### For HTML Templates:
1. **✅ Use rich objects/lists** in your YAML mappings
2. **✅ Preserve object structure** in DataMapper (don't convert to strings)
3. **✅ Pre-filter lists** in mapping layer when possible (better performance)
4. **✅ Use descriptive variable names** (`applicants`, `dependents`, not `data1`)
5. **✅ Leverage FreeMarker's built-in functions** (`?filter`, `?sort`, etc.)

### YAML Field Naming Convention:
| Target Field Name | Purpose | Example |
|------------------|---------|---------|
| **No dots, no numbers** | HTML variables | `applicants`, `dependents`, `metadata` |
| **Dots and numbers** | PDF fields | `primary.fname.1`, `dependent.age.2` |

### Template-Specific Configuration:
```yaml
# Use targetVariable for HTML, targetField for PDF
mappings:
  # HTML
  - sourceField: "applicants"
    targetVariable: "applicants"  # ← HTML-specific
  
  # PDF  
  - sourceField: "applicants[relationship=primary].firstName"
    targetField: "primary.fname.1"  # ← PDF-specific
```

---

## 🔚 Final Answer

**For FreeMarker HTML templates, your YAML mappings should:**
- ✅ **Pass entire objects and lists** (not flattened fields)
- ✅ **Use simple variable names** (`applicants`, not `primary.fname.1`)
- ✅ **Preserve nested structure** (addresses inside applicants)
- ✅ **Pre-filter data** in mapping layer when possible

**The data is made available to FreeMarker as a `Map<String, Object>` where:**
- Keys become template variables (`${applicants}`)
- Values remain as rich Java objects (Lists, Maps, POJOs)
- FreeMarker can navigate the full object graph (`${applicants[0].addresses[0].street}`)

This gives you the **flexibility and power** of FreeMarker while keeping your configuration clean and maintainable. The key insight is that **HTML and PDF have fundamentally different data requirements**, and your YAML configuration should reflect that difference explicitly.