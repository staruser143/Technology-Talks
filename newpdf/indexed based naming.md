Great! You're working with a **PDF form that uses a 1-based indexed naming convention** for both **single entities** (like `PrimaryApplicant.FName.1`) and **repeatable entities** (like `Dependent.FName.1`, `Dependent.FName.2`, etc.).

This is common in government or insurance forms (e.g., IRS, healthcare applications).

---

### ✅ Goal

You want a **YAML mapping** that:
1. Fills **single-value fields** from a top-level or nested object.
2. Fills **repeating fields** (like dependents) by **iterating over a JSON array**, and **dynamically generating field names** like `Dependent.FName.1`, `Dependent.FName.2`, etc.

---

### 🧩 Strategy

Since **PDF field names are fixed**, you **cannot use a single JsonPath per repeating group**. Instead, you need to:

- Define a **repeatable block** in YAML (e.g., `repeat: true`)
- Specify the **JSON array path** (e.g., `$.dependents`)
- Define **sub-field mappings** with a **placeholder index** (e.g., `Dependent.FName.{index}`)

Then, in Java:
- For each item in the JSON array,
- Replace `{index}` with `1`, `2`, `3`, …
- Evaluate the sub-field’s JsonPath **relative to the current array item**

---

### ✅ Step 1: Enhanced YAML Mapping Format

```yaml
fields:
  # Single-value fields (non-repeating)
  - pdf_field: "PrimaryApplicant.FName.1"
    json_path: "$.primaryApplicant.firstName"
  
  - pdf_field: "PrimaryApplicant.LName.1"
    json_path: "$.primaryApplicant.lastName"

  # Repeating block: Dependents
  - repeat: true
    json_array_path: "$.dependents"
    items:
      - pdf_field_template: "Dependent.FName.{index}"
        json_path: "$.firstName"
      
      - pdf_field_template: "Dependent.LName.{index}"
        json_path: "$.lastName"
      
      - pdf_field_template: "Dependent.DOB.{index}"
        json_path: "$.dateOfBirth"
        transform: "date:MM/dd/yyyy"
```

> 🔑 Key ideas:
> - `repeat: true` marks a repeating group
> - `json_array_path` points to the array in JSON
> - `pdf_field_template` uses `{index}` as a placeholder (1-based)
> - Sub-field `json_path` is **relative to each array item**

---

### ✅ Step 2: Update Java Records to Support This

```java
public record MappingConfig(List<FieldOrBlock> fields) {}

// Union-like: either a single field OR a repeat block
public sealed interface FieldOrBlock permits SingleField, RepeatBlock {}

public record SingleField(
    String pdf_field,
    String json_path,
    String transform,
    Boolean multi,
    String delimiter
) implements FieldOrBlock {
    public SingleField {
        if (multi == null) multi = false;
        if (delimiter == null || delimiter.isEmpty()) delimiter = ", ";
    }
}

public record RepeatBlock(
    String json_array_path,
    List<SubField> items
) implements FieldOrBlock {}

public record SubField(
    String pdf_field_template,
    String json_path,
    String transform
) {}
```

> 💡 We use a **sealed interface** (`FieldOrBlock`) to model two kinds of entries.

---

### ✅ Step 3: Updated Java Logic

```java
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class PdfFormFiller {

    // --- Records (as above) ---
    public record MappingConfig(List<FieldOrBlock> fields) {}
    public sealed interface FieldOrBlock permits SingleField, RepeatBlock {}
    
    public record SingleField(
        String pdf_field,
        String json_path,
        String transform,
        Boolean multi,
        String delimiter
    ) implements FieldOrBlock {
        public SingleField {
            if (multi == null) multi = false;
            if (delimiter == null || delimiter.isEmpty()) delimiter = ", ";
        }
    }

    public record RepeatBlock(
        String json_array_path,
        List<SubField> items
    ) implements FieldOrBlock {}

    public record SubField(
        String pdf_field_template,
        String json_path,
        String transform
    ) {}

    // --- Main logic ---
    public static void main(String[] args) throws Exception {
        String jsonText = Files.readString(Paths.get("data.json"));
        ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
        MappingConfig config = yamlMapper.readValue(new File("mapping.yaml"), MappingConfig.class);

        try (PDDocument document = PDDocument.load(new File("template.pdf"))) {
            PDAcroForm form = document.getDocumentCatalog().getAcroForm();
            if (form == null) throw new IllegalStateException("No form in PDF");

            for (FieldOrBlock block : config.fields()) {
                if (block instanceof SingleField single) {
                    fillSingleField(form, jsonText, single);
                } else if (block instanceof RepeatBlock repeat) {
                    fillRepeatBlock(form, jsonText, repeat);
                }
            }

            document.save("filled_form.pdf");
        }
    }

    private static void fillSingleField(PDAcroForm form, String jsonText, SingleField field) {
        String value = evaluateJsonPath(jsonText, field.json_path(), field.multi());
        String transformed = applyTransform(value, field.transform());
        setPdfField(form, field.pdf_field(), transformed);
    }

    private static void fillRepeatBlock(PDAcroForm form, String jsonText, RepeatBlock block) {
        try {
            Object arrayObj = JsonPath.read(jsonText, block.json_array_path());
            if (!(arrayObj instanceof List)) {
                System.err.println("Expected array at: " + block.json_array_path());
                return;
            }
            List<?> array = (List<?>) arrayObj;

            for (int i = 0; i < array.size(); i++) {
                Object item = array.get(i);
                int oneBasedIndex = i + 1;

                // Convert item to JSON string for relative JsonPath
                String itemJson = item.toString();
                // But better: use JsonPath's inline context (see note below)

                for (SubField sub : block.items()) {
                    try {
                        // Evaluate json_path relative to current item
                        // JsonPath doesn't support "relative" easily, so we embed item in wrapper
                        String wrapperJson = "{\"item\":" + toJsonString(item) + "}";
                        Object result = JsonPath.read(wrapperJson, "$.item" + sub.json_path().substring(1));
                        String value = toStringSafe(result);
                        String transformed = applyTransform(value, sub.transform());
                        String actualPdfField = sub.pdf_field_template().replace("{index}", String.valueOf(oneBasedIndex));
                        setPdfField(form, actualPdfField, transformed);
                    } catch (Exception e) {
                        System.err.println("Error in repeat block field " + sub.pdf_field_template() + ": " + e.getMessage());
                    }
                }
            }
        } catch (PathNotFoundException e) {
            System.err.println("Array path not found: " + block.json_array_path());
        } catch (Exception e) {
            System.err.println("Error processing repeat block: " + e.getMessage());
        }
    }

    // Helper to safely convert object to JSON string
    private static String toJsonString(Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            return "\"" + obj.toString().replace("\"", "\\\"") + "\"";
        }
    }

    private static void setPdfField(PDAcroForm form, String fieldName, String value) {
        PDField field = form.getField(fieldName);
        if (field != null) {
            field.setValue(value);
        } else {
            System.err.println("PDF field not found: " + fieldName);
        }
    }

    // --- Reuse existing helpers ---
    private static String evaluateJsonPath(String json, String jsonPath, boolean multi) {
        try {
            Object result = JsonPath.read(json, jsonPath);
            if (multi) {
                List<?> list = (result instanceof List) ? (List<?>) result : Arrays.asList(result);
                return list.stream()
                    .map(PdfFormFiller::toStringSafe)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.joining("\n"));
            } else {
                if (result instanceof List) {
                    List<?> list = (List<?>) result;
                    return list.isEmpty() ? "" : toStringSafe(list.get(0));
                }
                return toStringSafe(result);
            }
        } catch (Exception e) {
            return "";
        }
    }

    private static String toStringSafe(Object obj) {
        return obj == null ? "" : obj.toString();
    }

    private static String applyTransform(String value, String transformSpec) {
        if (transformSpec == null || transformSpec.isEmpty()) return value;
        value = value.trim();

        if ("uppercase".equals(transformSpec)) {
            return value.toUpperCase();
        } else if ("lowercase".equals(transformSpec)) {
            return value.toLowerCase();
        } else if (transformSpec.startsWith("date:")) {
            String pattern = transformSpec.substring(5);
            try {
                LocalDate date = LocalDate.parse(value);
                return date.format(DateTimeFormatter.ofPattern(pattern));
            } catch (Exception e) {
                return "INVALID_DATE";
            }
        } else if (transformSpec.startsWith("currency:")) {
            String localeTag = transformSpec.substring(9);
            try {
                Number number = new BigDecimal(value);
                Locale locale = Locale.forLanguageTag(localeTag);
                return NumberFormat.getCurrencyInstance(locale).format(number);
            } catch (Exception e) {
                return "INVALID_CURRENCY";
            }
        } else if (transformSpec.startsWith("boolean:")) {
            String[] parts = transformSpec.substring(8).split("\\|", -1);
            boolean boolVal = Boolean.parseBoolean(value);
            return boolVal ? (parts.length > 0 ? parts[0] : "true") : (parts.length > 1 ? parts[1] : "false");
        }
        return value;
    }
}
```

---

### 📦 Example `data.json`

```json
{
  "primaryApplicant": {
    "firstName": "John",
    "lastName": "Doe"
  },
  "dependents": [
    {
      "firstName": "Alice",
      "lastName": "Doe",
      "dateOfBirth": "2015-03-10"
    },
    {
      "firstName": "Bob",
      "lastName": "Doe",
      "dateOfBirth": "2018-07-22"
    }
  ]
}
```

### 📄 Resulting PDF Field Population

| PDF Field Name             | Value         |
|---------------------------|---------------|
| `PrimaryApplicant.FName.1`| `JOHN`        |
| `PrimaryApplicant.LName.1`| `DOE`         |
| `Dependent.FName.1`       | `Alice`       |
| `Dependent.LName.1`       | `Doe`         |
| `Dependent.DOB.1`         | `03/10/2015`  |
| `Dependent.FName.2`       | `Bob`         |
| `Dependent.LName.2`       | `Doe`         |
| `Dependent.DOB.2`         | `07/22/2018`  |

> ✅ Handles **zero dependents** → no fields filled (safe).

---

### 🔒 Important Notes

1. **JsonPath relative evaluation**:  
   JsonPath doesn’t natively support “evaluate path relative to current object”. The workaround is to **wrap each array item in a temporary JSON object** (`{"item": ...}`) and prepend `$.item` to the sub-path.

2. **Performance**:  
   For large arrays, this is still efficient — PDF forms rarely have >10–20 repeats.

3. **Field existence**:  
   If your PDF only has fields up to `.3` (e.g., max 3 dependents), but JSON has 5, the extra ones will be **silently skipped** (since `form.getField()` returns `null`).

---

### ✅ Summary

You now have a **flexible, scalable mapping system** that supports:
- Single entities (`PrimaryApplicant`)
- Repeatable entities (`Dependent`) with **dynamic 1-based indexing**
- Full JsonPath + transforms
- Clean YAML structure

Let me know if you'd like to:
- Support **conditional repeats** (e.g., skip if field missing)
- Add **max repeat limit**
- Handle **nested repeats** (e.g., `Dependent.Addresses.1.Street.1`)

This pattern works for even the most complex government forms! 🏛️📄