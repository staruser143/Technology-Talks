Excellent question — and **no, you do *not* need to pass a template ID or name** to retrieve mappings.

✅ **Your current design already handles this correctly**:  
Each `TemplateDefinition` in the YAML **contains its own list of mappings**, so when you process a template, you **use only its mappings**.

---

## 🔍 How It Works (Step by Step)

### 1. **YAML Structure**
```yaml
templates:
  - type: PDF
    templatePath: form1.pdf
    outputPath: out1.pdf
    mappings:                # ← Mappings for THIS template
      - sourceField: "user.name"
        targetField: "name.1"

  - type: PDF
    templatePath: form2.pdf
    outputPath: out2.pdf
    mappings:                # ← Mappings for THIS template
      - sourceField: "user.email"
        targetField: "email.1"

  - type: HTML
    templatePath: summary.ftl
    outputPath: summary.html
    mappings:                # ← Mappings for THIS template
      - sourceField: "user"
        targetField: "user"
```

### 2. **Processing Logic in `TemplateMergeService`**
```java
public void mergeFromJson(String json, MergeConfig config) {
    Object jsonData = parseJson(json);
    
    for (TemplateDefinition templateDef : config.getTemplates()) {
        // ✅ Use ONLY this template's mappings
        Map<String, Object> mappedData = dataMapper.mapData(jsonData, templateDef.getMappings());
        
        // ✅ Pass to correct merger (PDF or HTML)
        TemplateMerger merger = mergers.get(templateDef.getType());
        merger.merge(mappedData, templateDef);
    }
}
```

> 🔑 **Key Point**:  
> `templateDef.getMappings()` returns **only the mappings defined under that template** in YAML.

---

## ✅ Why This Is Correct

| Concern | Reality |
|--------|--------|
| **"How does it know which mappings belong to which template?"** | → Mappings are **nested under each template** in YAML |
| **"Do I need a template ID?"** | → ❌ No — structure defines ownership |
| **"Can mappings be shared?"** | → Not directly — but you can **reuse YAML anchors** (see below) |

---

## 🧩 Advanced: Reusing Mappings Across Templates (YAML Anchors)

If you have **common mappings**, use **YAML anchors**:

```yaml
common-fields: &common-fields
  - sourceField: "metadata.submissionId"
    targetField: "submission_id.1"
  - sourceField: "metadata.date"
    targetField: "date.1"

templates:
  - type: PDF
    templatePath: form1.pdf
    outputPath: out1.pdf
    mappings:
      - <<: *common-fields
      - sourceField: "user.name"
        targetField: "name.1"

  - type: PDF
    templatePath: form2.pdf
    outputPath: out2.pdf
    mappings:
      - <<: *common-fields
      - sourceField: "user.email"
        targetField: "email.1"
```

> ✅ **No code changes needed** — YAML parser handles this.

---

## 📊 Real-World Example

### Input JSON
```json
{
  "user": { "name": "John", "email": "john@example.com" },
  "metadata": { "submissionId": "APP-123", "date": "2025-01-01" }
}
```

### Processing
1. **Template 1 (form1.pdf)**:
   - Mappings: `submissionId`, `date`, `name`
   - Output: `submission_id.1 = "APP-123"`, `name.1 = "John"`

2. **Template 2 (form2.pdf)**:
   - Mappings: `submissionId`, `date`, `email`
   - Output: `submission_id.1 = "APP-123"`, `email.1 = "john@example.com"`

3. **Template 3 (summary.html)**:
   - Mappings: `user` (entire object)
   - Output: FreeMarker model with `user.name`, `user.email`

> ✅ **Each template gets exactly the data it needs**

---

## ⚠️ What If You *Want* a Template ID?

You **can** add an optional `name` or `id` field for logging/debugging:

```yaml
templates:
  - name: "primary-application"   # ← optional
    type: PDF
    templatePath: form.pdf
    outputPath: out.pdf
    mappings: [...]
```

Then in `TemplateDefinition.java`:
```java
private String name; // optional identifier
```

Use it in logs:
```java
log.info("Processing template: {}", templateDef.getName());
```

But **it’s not required for functionality**.

---

## ✅ Summary

| Question | Answer |
|--------|--------|
| **Do I need a template ID to get correct mappings?** | ❌ **No** |
| **How are mappings associated with templates?** | ✅ **By YAML structure** (nested list) |
| **Can multiple PDF/HTML templates coexist?** | ✅ **Yes** — each has its own mappings |
| **Can mappings be shared?** | ✅ **Via YAML anchors** (no code change) |
| **Is the current design sufficient?** | ✅ **Yes — it’s correct and scalable** |

Your design is **already perfect** for multi-template scenarios. No changes needed! 🎯