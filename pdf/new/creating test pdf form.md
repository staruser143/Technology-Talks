Absolutely! 🚀

Here’s a **step-by-step guide to create a test PDF form** with **text fields, checkboxes, radio buttons, and dropdowns** — using **free tools** (LibreOffice) and **professional tools** (Adobe Acrobat Pro).

You’ll end up with a PDF that your `PdfFormTemplateEngine` can fill correctly.

---

## 🎯 GOAL

Create a PDF form with these fields:

| Field Type | Field Name (PDF) | Sample Value |
|-----------|------------------|--------------|
| Text | `full_name` | "Alice Cooper" |
| Text | `email` | "alice@example.com" |
| Checkbox | `newsletter` | `true` → checked |
| Radio Group | `gender` | `"M"` or `"F"` |
| Dropdown | `country` | `"Canada"`, `"USA"`, `"UK"` |
| Text Area | `comments` | "Great service!" |

---

# ✅ OPTION 1: Use LibreOffice (Free & Cross-Platform)

> ✅ Best for: Simple forms, budget projects, open-source workflows

### STEP 1: Install LibreOffice
- Download: https://www.libreoffice.org/
- Works on Windows, macOS, Linux

### STEP 2: Create New Document
1. Open **LibreOffice Writer**
2. Go to `View → Toolbars → Form Controls`

### STEP 3: Add Fields

#### A. Text Field (`full_name`)
1. Click **Text Box** icon in Form Controls toolbar
2. Draw a box on the document
3. Right-click → **Control**
4. In **General** tab:
   - **Name**: `full_name`
   - (Leave other settings default)

#### B. Email Field (`email`)
- Repeat above → Name: `email`

#### C. Checkbox (`newsletter`)
1. Click **Check Box** icon
2. Draw box
3. Right-click → **Control**
4. **Name**: `newsletter`
   - ✅ LibreOffice uses `"Yes"` as the "On" value by default

#### D. Radio Buttons (`gender`)
> ⚠️ LibreOffice doesn’t have a "Radio Button" tool — use **Option Button**

1. Click **Option Button** icon
2. Draw first button → Right-click → **Control** → **Name**: `gender`
3. Draw second button → Right-click → **Control** → **Name**: `gender`
   - ⚠️ **Both must have the same name** (this creates a group)
4. To set **export values**:
   - For Male button: In **Data** tab → **Reference value**: `M`
   - For Female button: **Reference value**: `F`

> 💡 If you don’t see "Reference value", LibreOffice may not support custom export values well. In that case, it uses `"1"`, `"2"`, etc. — which is **not ideal**.

#### E. Dropdown (`country`)
1. Click **List Box** icon
2. Draw box
3. Right-click → **Control**
4. **Name**: `country`
5. In **Data** tab:
   - **List entries**:  
     ```
     Canada
     USA
     UK
     ```
   - (One per line)

#### F. Text Area (`comments`)
1. Click **Text Box** → draw larger box
2. Right-click → **Control** → **Name**: `comments`

### STEP 4: Export as PDF Form
1. `File → Export As → Export as PDF`
2. In **General** tab:
   - ✅ **Create PDF Form**
3. Click **Export**

✅ You now have a PDF with fillable fields!

> ⚠️ **Limitation**: LibreOffice’s radio buttons may not use `"M"`/`"F"` — they might use `"1"`/`"2"`. Test with the field printer (below).

---

# ✅ OPTION 2: Use Adobe Acrobat Pro (Professional)

> ✅ Best for: Production forms, full control, guaranteed compatibility

### STEP 1: Get Adobe Acrobat Pro
- Trial: https://acrobat.adobe.com/
- Paid subscription required for full features

### STEP 2: Create or Open PDF
- You can start from a blank page or existing document

### STEP 3: Prepare Form
1. Go to `Tools → Prepare Form`
2. Click **Start**

### STEP 4: Add Fields

#### A. Text Field (`full_name`)
1. Click **Text Field** tool
2. Draw box
3. In right panel:
   - **Name**: `full_name`

#### B. Checkbox (`newsletter`)
1. Click **Check Box** tool
2. Draw box
3. **Name**: `newsletter`
   - By default, "On" value = `"Yes"`

#### C. Radio Buttons (`gender`)
1. Click **Radio Button** tool
2. Draw first button → **Name**: `gender`
3. Draw second button → **Name**: `gender` (same name = group)
4. For each button:
   - Click **Options** tab
   - **Button Value**: `M` (for Male), `F` (for Female)

#### D. Dropdown (`country`)
1. Click **Drop-Down List** tool
2. Draw box
3. **Name**: `country`
4. Click **Options** tab
5. Add items:
   - **Item**: `Canada` → **Export Value**: `Canada`
   - **Item**: `USA` → **Export Value**: `USA`
   - **Item**: `UK` → **Export Value**: `UK`

#### E. Text Area (`comments`)
1. Click **Text Field** → draw large box
2. **Name**: `comments`
3. In **Appearance** tab → set **Scrolling** if needed

### STEP 5: Save
- `File → Save As → form-test.pdf`

✅ Perfectly structured PDF form!

---

# 🔍 STEP 5: Validate Your PDF Form

Use this Java helper to **print all field names and types**:

```java
// PdfFieldInspector.java
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.pdmodel.interactive.form.PDChoiceField;
import org.apache.pdfbox.pdmodel.interactive.form.PDCheckbox;

import java.io.File;
import java.util.Set;

public class PdfFieldInspector {
    public static void main(String[] args) throws Exception {
        String pdfPath = "form-test.pdf"; // ← your PDF path
        try (PDDocument doc = PDDocument.load(new File(pdfPath))) {
            PDAcroForm form = doc.getDocumentCatalog().getAcroForm();
            if (form == null) {
                System.out.println("No form found!");
                return;
            }

            System.out.println("PDF Form Fields:");
            System.out.println("=================");
            for (PDField field : form.getFields()) {
                String name = field.getFullyQualifiedName();
                String type = field.getClass().getSimpleName();
                String value = field.getValueAsString();

                System.out.println("Name: " + name);
                System.out.println("  Type: " + type);
                System.out.println("  Current Value: " + value);

                if (field instanceof PDCheckbox cb) {
                    try {
                        System.out.println("  On Value: " + cb.getOnValue());
                    } catch (Exception e) {
                        System.out.println("  On Value: (default 'Yes')");
                    }
                }

                if (field instanceof PDChoiceField choice) {
                    try {
                        Set<String> options = choice.getOptions();
                        System.out.println("  Options: " + options);
                    } catch (Exception e) {
                        System.out.println("  Options: (unable to read)");
                    }
                }
                System.out.println();
            }
        }
    }
}
```

Run it to see:
- Exact field names
- Checkbox "On" values
- Dropdown options

---

# 🧪 STEP 6: Test with Your Engine

### Sample Data

```java
Map<String, Object> testData = Map.of(
    "full_name", "Alice Cooper",
    "email", "alice@example.com",
    "newsletter", true,          // → checkbox
    "gender", "F",               // → radio button
    "country", "Canada",         // → dropdown
    "comments", "Great service!"
);
```

### Mapping Spec

```json
{
  "mappings": [
    { "sourceField": "full_name", "targetPlaceholder": "full_name" },
    { "sourceField": "email", "targetPlaceholder": "email" },
    { "sourceField": "newsletter", "targetPlaceholder": "newsletter" },
    { "sourceField": "gender", "targetPlaceholder": "gender" },
    { "sourceField": "country", "targetPlaceholder": "country" },
    { "sourceField": "comments", "targetPlaceholder": "comments" }
  ]
}
```

### Run Merge

```java
byte[] filledPdf = merger.merge(
    new LocalFileTemplateSource(Paths.get("form-test.pdf")),
    testData,
    mappingSpecJson,
    OutputFormat.PDF,
    TemplateType.PDF_FORM
);
Files.write(Paths.get("filled-test.pdf"), filledPdf);
```

✅ Open `filled-test.pdf` — all fields should be correctly filled!

---

# 📌 BEST PRACTICES

1. **Use simple field names**: `full_name`, not `Customer Full Name (Required)*`
2. **For radios**: Use short export values (`M`/`F`, not `Male`/`Female`)
3. **For checkboxes**: Stick with `"Yes"`/`"Off"` unless you control the PDF
4. **Test early**: Use the field inspector before integrating
5. **Avoid spaces/special chars** in field names

---

# 🚀 BONUS: Download a Pre-Made Test PDF

I can’t attach files here, but you can:

1. **Create one using steps above**, or
2. Use this **public test PDF**:  
   [https://www.pdfscripting.com/public/ExamplePDFs/CheckBoxesAndRadioButtons.pdf](https://www.pdfscripting.com/public/ExamplePDFs/CheckBoxesAndRadioButtons.pdf)  
   (Inspect it with the field printer to see field names)

---

## ✅ SUMMARY

You now know how to:

- ✅ Create PDF forms with **all field types** using LibreOffice or Acrobat
- ✅ Set correct **field names and export values**
- ✅ **Validate** your PDF with Java
- ✅ **Test** with your enhanced `PdfFormTemplateEngine`

This ensures your system works reliably with **real-world PDF forms** used in applications, contracts, and government documents.

Let me know if you want:
- A **Dockerized test environment**
- A **Postman collection** to test the REST API
- A **Node.js test script**

I’ll generate it for you! 🛠️📄