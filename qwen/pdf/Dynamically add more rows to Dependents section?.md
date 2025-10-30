**Short answer**:  
> ❌ **No — you cannot dynamically add new AcroForm fields to a PDF template using Apache PDFBox alone.**

AcroForm fields are **statically defined** in the PDF. If your template only has `dependent.name.1`, `dependent.name.2`, `dependent.name.3`, then **you cannot create `dependent.name.4` at runtime** without **modifying the PDF structure**.

---

## 🔍 Why This Is Hard

### 1. **AcroForm Fields Are Fixed**
- Fields are defined in the PDF’s **form dictionary**
- Each field has a **name**, **rectangle (position)**, **font**, **appearance**, etc.
- PDFBox can **fill** existing fields, but **not create new visual fields**

### 2. **"Rows" = Visual + Logical**
- A "row" isn’t just a field name — it’s:
  - Text fields (`name`, `age`)
  - Labels
  - Lines/borders
  - Possibly checkboxes, dropdowns
- These are **drawn as content** (not form fields)

> 🚫 **PDFBox cannot "clone" a row of visual elements** — it’s not a layout engine.

---

## ✅ Practical Workarounds

### 🔹 Option 1: **Design Template with Max Rows** (Recommended)
- Create PDF with **maximum possible rows** (e.g., 10)
- Hide unused rows via:
  - **Default blank values**
  - **Print logic** (user prints only filled pages)
- In your YAML:
  ```yaml
  maxRepetitions: 10  # cap to template limit
  ```

> ✅ **Pros**: Simple, reliable, works with any PDF tool  
> ❌ **Cons**: Wastes space if max rows >> actual

---

### 🔹 Option 2: **Generate Additional Pages** (Advanced)
If you **must** support unlimited rows:

1. **Design a "row template"** as a separate PDF (1 row)
2. **Stamp/overlay** it onto a new page for overflow
3. **Create new AcroForm fields** programmatically on new pages

#### Example with PDFBox:
```java
// Clone a row from page 1 to page 2
PDPage templatePage = doc.getPage(0);
PDPage newPage = new PDPage();
doc.addPage(newPage);

// Copy visual content (hard!)
// Then create new fields:
PDTextField nameField = new PDTextField(doc);
nameField.setPartialName("dependent.name.4");
// Set position, font, etc.
form.getFields().add(nameField);
```

> ⚠️ **Challenges**:
> - Positioning must be **pixel-perfect**
> - Fonts, colors, borders must match
> - **No built-in "row cloning"** in PDFBox
> - **Fragile** — breaks if template changes

> 📌 **Only feasible if you control the PDF creation process**

---

### 🔹 Option 3: **Use HTML + Convert to PDF** (Best for Dynamic Content)
- Generate **HTML** with repeating sections (easy in FreeMarker)
- Convert to PDF using **Flying Saucer** or **Puppeteer**

```java
// In your TemplateMerger
String html = processFreeMarkerTemplate(data);
OutputStream pdfOut = new FileOutputStream("output.pdf");
HtmlConverter.convertToPdf(html, pdfOut);
```

> ✅ **Pros**: Unlimited rows, full layout control  
> ❌ **Cons**: Not AcroForm (no fillable fields), different workflow

---

### 🔹 Option 4: **Hybrid Approach**
- Use **AcroForm PDF** for first N rows (e.g., 3)
- Append a **non-interactive table** (as content) for overflow rows
- Render overflow data as **text** (not form fields)

```java
try (PDPageContentStream cs = new PDPageContentStream(doc, page, ...)) {
    cs.beginText();
    cs.setFont(PDType1Font.HELVETICA, 10);
    cs.newLineAtOffset(x, y);
    cs.showText("Alice, 10");
    cs.endText();
}
```

> ✅ **Pros**: Works with existing PDF  
> ❌ **Cons**: Overflow data is **not editable**

---

## 📊 Recommendation by Use Case

| Use Case | Best Approach |
|--------|--------------|
| **Govt form with max 5 dependents** | ✅ **Option 1** (pre-built rows) |
| **Unlimited line items (invoices)** | ✅ **Option 3** (HTML → PDF) |
| **Must have fillable fields + overflow** | ⚠️ **Option 4** (fillable + static overflow) |
| **You control PDF creation** | ⚠️ **Option 2** (programmatic row cloning) |

---

## 🔒 Reality Check

> **AcroForm is not designed for dynamic row insertion.**  
> It’s a **static form technology** from the 1990s.

If you need **truly dynamic forms**, consider:
- **HTML5 + JavaScript** (for web)
- **PDF with embedded JavaScript** (limited support)
- **Non-interactive PDFs** (generated from HTML)

---

## ✅ What You Should Do

1. **Determine max realistic rows** (e.g., 99% of cases ≤ 5 dependents)
2. **Update PDF template** to include that many rows
3. **Use `maxRepetitions`** in YAML to cap data:
   ```yaml
   - sourceArray: "dependents"
     maxRepetitions: 5  # match PDF template
   ```
4. **Validate upstream**: reject submissions with >5 dependents (if business allows)

This is the **most reliable, maintainable, and widely compatible** approach.

---

## 🚫 What Not to Do

- Don’t try to **parse PDF layout** to "clone" rows — it’s error-prone
- Don’t assume **all PDF viewers** support dynamic AcroForm changes
- Don’t over-engineer — **95% of forms have a reasonable row limit**

---

## ✅ Summary

| Question | Answer |
|--------|--------|
| Can I add new AcroForm rows at runtime? | ❌ **No** (without complex PDF manipulation) |
| Should I try? | ❌ **Not recommended** for production |
| Best alternative? | ✅ **Pre-build max rows in PDF template** |
| For unlimited data? | ✅ **Use HTML → PDF conversion** |

Stick with **static templates + reasonable limits** — it’s the standard in enterprise PDF processing. 🛡️