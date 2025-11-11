Excellent ✅ — below is a **complete, production-ready** `PdfFillerService` that integrates directly with your `MappingEngine`.

It supports:

* 🔁 repeating groups with configurable per-page limits,
* 📄 automatic pagination & template replication,
* 🧩 merging filled PDFs into one output,
* 🧱 fully generic design (no hard-coded field names),
* 🧾 clean integration with Spring Boot or standalone Java.

---

### 📘 `PdfFillerService.java`

```java
package com.example.pdfmapping.service;

import com.example.pdfmapping.engine.MappingEngine;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.*;

/**
 * PdfFillerService integrates MappingEngine and PDFBox to:
 * - Evaluate YAML-based mappings on JSON source data
 * - Fill AcroForm templates
 * - Handle repeated groups with pagination
 * - Merge all pages into a single final PDF
 */
@Service
public class PdfFillerService {

    private static final Logger log = LoggerFactory.getLogger(PdfFillerService.class);
    private final MappingEngine mappingEngine = new MappingEngine();
    private final Yaml yaml = new Yaml();

    /**
     * Main pipeline entry point: processes mapping + fills PDF template.
     */
    public byte[] generateMergedPdf(InputStream yamlMappingStream, JsonNode sourceData) throws Exception {
        // 1️⃣ Parse YAML spec
        Map<String, Object> yamlMap = yaml.load(yamlMappingStream);
        if (yamlMap == null) throw new IllegalArgumentException("Invalid or empty YAML mapping");

        String templatePath = String.valueOf(yamlMap.get("template"));
        if (templatePath == null) throw new IllegalArgumentException("Missing 'template' key in YAML mapping");

        log.info("Using PDF template: {}", templatePath);

        // 2️⃣ Produce flattened map via MappingEngine
        Map<String, String> flattenedMap = mappingEngine.processMapping(
                new ByteArrayInputStream(new Yaml().dump(yamlMap).getBytes()), sourceData);

        // 3️⃣ Detect repeated groups and pagination config (optional)
        Map<String, Object> repeats = (Map<String, Object>) yamlMap.get("repeats");
        if (repeats == null) repeats = new HashMap<>();

        // Generate and merge PDFs for each repeat group or single context
        if (repeats.isEmpty()) {
            log.info("No repeat pagination config found, generating single-page PDF");
            return fillTemplate(templatePath, flattenedMap);
        }

        List<PDDocument> filledDocs = new ArrayList<>();

        for (Map.Entry<String, Object> entry : repeats.entrySet()) {
            String repeatPrefix = entry.getKey();
            Map<String, Object> repeatCfg = (Map<String, Object>) entry.getValue();
            int perPage = (int) repeatCfg.getOrDefault("perPage", 3);

            log.info("Paginating group '{}' with {} rows per page", repeatPrefix, perPage);

            List<Map<String, String>> pages = paginateRepeatedData(flattenedMap, repeatPrefix, perPage);
            for (Map<String, String> pageData : pages) {
                PDDocument filledDoc = fillTemplateDoc(templatePath, pageData);
                filledDocs.add(filledDoc);
            }
        }

        // Merge all filled PDFs into one
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        mergeDocuments(filledDocs, baos);
        return baos.toByteArray();
    }

    // ---------------------------------------------------------------------------------------------------

    /**
     * Fills a PDF template (single page) and returns the binary bytes.
     */
    private byte[] fillTemplate(String templatePath, Map<String, String> data) throws IOException {
        try (PDDocument document = fillTemplateDoc(templatePath, data);
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            document.save(baos);
            return baos.toByteArray();
        }
    }

    /**
     * Fills fields in a template and returns the PDDocument.
     */
    private PDDocument fillTemplateDoc(String templatePath, Map<String, String> data) throws IOException {
        try (InputStream templateStream = new FileInputStream(templatePath)) {
            PDDocument doc = PDDocument.load(templateStream);
            PDAcroForm form = doc.getDocumentCatalog().getAcroForm();

            if (form != null) {
                for (Map.Entry<String, String> entry : data.entrySet()) {
                    PDField field = form.getField(entry.getKey());
                    if (field != null) {
                        try {
                            field.setValue(entry.getValue());
                        } catch (Exception e) {
                            log.warn("Could not set field '{}': {}", entry.getKey(), e.getMessage());
                        }
                    }
                }
                form.flatten();
            } else {
                log.warn("Template has no AcroForm fields");
            }

            return doc;
        }
    }

    /**
     * Paginates a repeated group (like 'child.') into multiple pages.
     */
    private List<Map<String, String>> paginateRepeatedData(Map<String, String> fullMap,
                                                           String repeatPrefix,
                                                           int perPage) {
        List<Map<String, String>> pages = new ArrayList<>();

        // Group by index number (child.1, child.2, etc.)
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

            // Repeated data for current page
            List<Integer> subIndexes = allIndexes.subList(i, Math.min(i + perPage, allIndexes.size()));
            for (int idx : subIndexes) {
                pageData.putAll(grouped.get(idx));
            }

            // Also include non-repeated static fields
            fullMap.forEach((k, v) -> {
                if (!k.startsWith(repeatPrefix + ".")) {
                    pageData.put(k, v);
                }
            });

            pages.add(pageData);
        }

        log.info("Paginated {} entries into {} pages for '{}'", allIndexes.size(), pages.size(), repeatPrefix);
        return pages;
    }

    /**
     * Merge multiple PDDocument instances into a single output stream.
     */
    private void mergeDocuments(List<PDDocument> documents, OutputStream out) throws IOException {
        PDDocument merged = new PDDocument();
        for (PDDocument doc : documents) {
            for (PDPage page : doc.getPages()) {
                merged.addPage(page);
            }
        }
        merged.save(out);
        merged.close();
        for (PDDocument d : documents) d.close();
    }
}
```

---

### 🧾 Example YAML Mapping

```yaml
template: templates/enrollment_form.pdf

contexts:
  primaryApplicant: "applicants[type='PrimaryApplicant']"
  childApplicants: "applicants[type='Child']"

mappings:
  primary:
    context: "primaryApplicant"
    fields:
      demographic:
        firstName: "demographic.firstName"
        lastName: "demographic.lastName"
      product:
        name: "products[0].name"

  child:
    context: "childApplicants"
    repeat: true
    fields:
      demographic:
        firstName: "demographic.firstName"
        lastName: "demographic.lastName"
      address:
        homeCity: "addresses[type='Home'].city"
        homeState: "addresses[type='Home'].state"

repeats:
  child:
    perPage: 3
```

---

### 🧩 Controller Example

```java
@RestController
@RequestMapping("/api/mapping")
public class PdfMappingController {

    private final PdfFillerService pdfFillerService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public PdfMappingController(PdfFillerService pdfFillerService) {
        this.pdfFillerService = pdfFillerService;
    }

    @PostMapping(value = "/mergePdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> mergePdf(@RequestParam("yaml") MultipartFile yamlFile,
                                           @RequestParam("json") MultipartFile jsonFile) throws Exception {

        JsonNode source = objectMapper.readTree(jsonFile.getInputStream());
        byte[] pdfBytes = pdfFillerService.generateMergedPdf(yamlFile.getInputStream(), source);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=merged.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }
}
```

---

### ✅ Features Recap

| Feature                    | Description                        |
| -------------------------- | ---------------------------------- |
| **Template-driven**        | Reads template path from YAML      |
| **JSONata-driven mapping** | Powered by `MappingEngine`         |
| **Pagination**             | Breaks repeating groups into pages |
| **Dynamic merging**        | Combines all filled templates      |
| **Static + repeated data** | Keeps single fields on every page  |
| **No hard-coding**         | Works for any mapping spec         |
| **Uses PDFBox**            | No commercial library dependency   |

---

Would you like me to add an **example runnable Spring Boot project structure** (with dependencies and sample files) so you can just clone and test the end-to-end pipeline directly?
