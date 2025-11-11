Excellent 👏 — this is a **real-world** and very important requirement for dynamic PDF population.
Let’s unpack it step-by-step and then I’ll show you exactly how to implement it cleanly with **PDFBox + your MappingEngine**.

---

## 🧠 Scenario Recap

* Your **template PDF** (AcroForm-based) has a **fixed number of rows**, e.g.
  `child[1]`, `child[2]`, `child[3]`.
* But your source JSON might have **more dependents** — say 5, 7, or 10.

You want:

1. To **fill up** the first 3 dependents in the **first page** (template instance).
2. Then **clone/append** the same template page for the remaining dependents.
3. Keep doing that until all dependents are rendered.
4. Finally, **merge all pages** into a single final PDF.

---

## 🧩 Conceptual Design

You can extend your mapping pipeline to include a **pagination layer** for repeating groups.

### Steps

1. **MappingEngine output**
   Produces all flattened fields (including all children, `child.1`, `child.2`, ...).

2. **Repeat Pagination Layer**

   * Detects repeating prefixes (`child.`).
   * Chunks the repeated field data into pages of N rows (based on your template limit, e.g., 3).
   * Produces one `Map<String, String>` per page.

3. **PdfFillerService**

   * Loads the template (using PDFBox).
   * For each page’s data map, fills the form fields.
   * Appends all filled pages into a single merged PDF output stream.

---

## 🧮 Example

If you have **5 children** and **template supports 3**,
you get:

| Page   | Fields Filled                   |
| ------ | ------------------------------- |
| Page 1 | child.1.*, child.2.*, child.3.* |
| Page 2 | child.4.*, child.5.*            |

---

## ⚙️ Implementation Overview

Here’s what we’ll do:

### 1. Add pagination logic in the PDF merging step.

```java
private List<Map<String, String>> paginateRepeatedData(Map<String, String> fullMap, String repeatPrefix, int perPage) {
    List<Map<String, String>> pages = new ArrayList<>();

    // Group by index number (e.g. child.1, child.2)
    Map<Integer, Map<String, String>> grouped = new TreeMap<>();
    for (Map.Entry<String, String> entry : fullMap.entrySet()) {
        String key = entry.getKey();
        if (key.startsWith(repeatPrefix + ".")) {
            String[] parts = key.split("\\.");
            if (parts.length > 2) {
                try {
                    int index = Integer.parseInt(parts[1]);
                    grouped.computeIfAbsent(index, i -> new LinkedHashMap<>()).put(key, entry.getValue());
                } catch (NumberFormatException ignored) {}
            }
        }
    }

    // Split into pages
    List<Integer> allIndexes = new ArrayList<>(grouped.keySet());
    for (int i = 0; i < allIndexes.size(); i += perPage) {
        Map<String, String> pageData = new LinkedHashMap<>();
        List<Integer> subIndexes = allIndexes.subList(i, Math.min(i + perPage, allIndexes.size()));
        for (int idx : subIndexes) {
            pageData.putAll(grouped.get(idx));
        }
        // also include static (non-child) fields for every page
        fullMap.forEach((k, v) -> {
            if (!k.startsWith(repeatPrefix + ".")) {
                pageData.put(k, v);
            }
        });
        pages.add(pageData);
    }

    return pages;
}
```

---

### 2. Integrate with PDFBox filler

```java
public byte[] fillAndMergePdf(String templatePath, Map<String, String> flattenedMap,
                              String repeatPrefix, int repeatPerPage) throws IOException {
    List<Map<String, String>> pageDataSets = paginateRepeatedData(flattenedMap, repeatPrefix, repeatPerPage);
    List<PDDocument> filledDocs = new ArrayList<>();

    for (Map<String, String> pageData : pageDataSets) {
        try (InputStream templateStream = new FileInputStream(templatePath);
             PDDocument templateDoc = PDDocument.load(templateStream)) {

            PDAcroForm form = templateDoc.getDocumentCatalog().getAcroForm();
            if (form != null) {
                for (Map.Entry<String, String> entry : pageData.entrySet()) {
                    PDField field = form.getField(entry.getKey());
                    if (field != null) {
                        field.setValue(entry.getValue());
                    }
                }
                form.flatten();
            }

            filledDocs.add(templateDoc);
        }
    }

    // Merge all pages into one
    PDDocument merged = new PDDocument();
    for (PDDocument doc : filledDocs) {
        for (PDPage page : doc.getPages()) {
            merged.addPage(page);
        }
    }

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    merged.save(baos);
    merged.close();
    for (PDDocument doc : filledDocs) doc.close();
    return baos.toByteArray();
}
```

---

### 3. Example Controller Usage

```java
@PostMapping(value = "/mergePdf", produces = MediaType.APPLICATION_PDF_VALUE)
public ResponseEntity<byte[]> mergePdf(@RequestParam("yaml") MultipartFile yamlFile,
                                       @RequestParam("json") MultipartFile jsonFile) throws Exception {
    JsonNode source = objectMapper.readTree(jsonFile.getInputStream());
    Map<String, String> flatMap = mappingEngine.processMapping(yamlFile.getInputStream(), source);

    byte[] mergedPdf = pdfFillerService.fillAndMergePdf(
        "templates/enrollment_form.pdf",
        flatMap,
        "child",  // repeat group prefix
        3         // rows per page in template
    );

    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=merged.pdf")
        .contentType(MediaType.APPLICATION_PDF)
        .body(mergedPdf);
}
```

---

## ✅ Result

For any number of dependents, the output PDF:

* Fills as many as possible per template page (based on `repeatPerPage`)
* Automatically appends extra pages for overflow
* Preserves all single-value fields across every page
* Produces a seamless, merged, printable PDF

---

## 💡 Bonus Extensions

You can easily enhance this with:

* `repeatConfig` in YAML:

  ```yaml
  repeats:
    child:
      perPage: 3
  ```

  Then auto-load `repeatPrefix` and `repeatPerPage` dynamically.
* Multi-group handling (`plan`, `spouse`, etc.).
* Page footers like “Page 2 of 4” automatically via PDFBox text overlays.

---

Would you like me to provide the **complete `PdfFillerService` class** next (with this pagination + merging logic built-in and integrated with your existing `MappingEngine`)?
