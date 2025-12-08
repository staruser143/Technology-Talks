Yes, you can definitely combine PDFBox-based dynamic page generation with FreeMarker HTML-based pages. Here are the main approaches:

## 1. **Merge PDFs Approach (Recommended)**
Generate both PDFs separately and merge them:

```java
// Generate HTML-based PDF via FreeMarker + openhtmltopdf
byte[] htmlPdf = htmlPdfService.renderHtmlToPdf(htmlContent);

// Generate dynamic PDF via existing PDFBox code
PDDocument pdfBoxDoc = yourExistingPdfBoxService.generateDynamicPages();

// Merge them
PDFMergerUtility merger = new PDFMergerUtility();
merger.addSource(new ByteArrayInputStream(htmlPdf));
merger.addSource(new ByteArrayInputStream(pdfBoxDoc.toByteArray()));
ByteArrayOutputStream merged = new ByteArrayOutputStream();
merger.setDestinationStream(merged);
merger.mergeDocuments(MemoryUsageSetting.setupMainMemoryOnly());

return merged.toByteArray();
```

**Pros**: Clean separation, both systems work independently, easy to maintain
**Cons**: Slight overhead from merging

## 2. **Import HTML Pages into PDFBox Document**
Convert FreeMarker HTML to PDF, then import those pages into your PDFBox document:

```java
// Generate HTML PDF
byte[] htmlPdf = htmlPdfService.renderHtmlToPdf(htmlContent);

// Load into PDFBox
PDDocument htmlDoc = PDDocument.load(new ByteArrayInputStream(htmlPdf));
PDDocument mainDoc = yourExistingPdfBoxService.generateDynamicPages();

// Import pages from HTML PDF
for (PDPage page : htmlDoc.getPages()) {
    mainDoc.addPage(page);
}

htmlDoc.close();
return mainDoc;
```

**Pros**: Single document flow, more control over page ordering
**Cons**: Similar to merge approach

## 3. **Conditional Rendering Strategy**
Use different rendering engines based on page complexity:

```java
if (pageRequiresComplexLayout(pageData)) {
    // Use FreeMarker + HTML for complex tables/layouts
    return generateHtmlBasedPage(pageData);
} else {
    // Use existing PDFBox code for dynamic/programmatic content
    return generatePdfBoxPage(pageData);
}
```

## Implementation Example

Here's how you might structure your service:

```java
@Service
public class HybridPdfService {
    
    @Autowired
    private FreemarkerService freemarkerService;
    
    @Autowired
    private HtmlPdfService htmlPdfService;
    
    @Autowired
    private LegacyPdfBoxService legacyPdfBoxService;
    
    public byte[] generateHybridPdf(Request request) throws IOException {
        PDFMergerUtility merger = new PDFMergerUtility();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        merger.setDestinationStream(output);
        
        // Add FreeMarker-based pages (e.g., healthcare member plans)
        if (request.includeHtmlPages()) {
            String html = freemarkerService.processTemplate(
                request.getTemplateName(), 
                request.getPayload()
            );
            byte[] htmlPdf = htmlPdfService.renderHtmlToPdf(html);
            merger.addSource(new ByteArrayInputStream(htmlPdf));
        }
        
        // Add PDFBox dynamic pages (e.g., charts, custom graphics)
        if (request.includeDynamicPages()) {
            PDDocument pdfBoxDoc = legacyPdfBoxService.generatePages(request);
            ByteArrayOutputStream pdfBoxOutput = new ByteArrayOutputStream();
            pdfBoxDoc.save(pdfBoxOutput);
            pdfBoxDoc.close();
            merger.addSource(new ByteArrayInputStream(pdfBoxOutput.toByteArray()));
        }
        
        merger.mergeDocuments(MemoryUsageSetting.setupMainMemoryOnly());
        return output.toByteArray();
    }
}
```

## Key Considerations

- **Page numbering**: If you need continuous page numbers, you'll need to post-process the merged PDF
- **Bookmarks/TOC**: May need to rebuild after merging
- **File size**: Merging adds minimal overhead
- **Performance**: Both generation processes can run in parallel if needed
- **Maintenance**: Keep legacy PDFBox code for what it does well (dynamic graphics, charts), use FreeMarker/HTML for complex layouts

**Recommendation**: Start with the merge approach (#1) - it's the cleanest and allows both systems to evolve independently while giving you a unified PDF output.
