Great question! Apache FOP **only accepts XSL-FO (an XML-based format)** as input — it **cannot process JSON directly**. However, you can still generate PDFs from JSON data in a Spring Boot application by **converting JSON → XML → XSL-FO → PDF**.

Here’s how to do it securely and efficiently:

---

## 🔁 Overall Workflow with JSON

```
JSON Data 
   ↓ (Convert to XML)
XML 
   ↓ (Apply XSLT)
XSL-FO 
   ↓ (Render with FOP)
PDF
```

You **skip the native XML source**, but everything else stays the same.

---

## ✅ Step-by-Step Implementation

### 1. **Add Dependencies**

In `pom.xml`:

```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>com.fasterxml.jackson.dataformat</groupId>
        <artifactId>jackson-dataformat-xml</artifactId>
    </dependency>
    <dependency>
        <groupId>org.apache.xmlgraphics</groupId>
        <artifactId>fop</artifactId>
        <version>2.9</version>
    </dependency>
</dependencies>
```

> 💡 `jackson-dataformat-xml` lets you convert Java objects ↔ XML easily.

---

### 2. **Define a Java Model (POJO)**

This bridges JSON and XML:

```java
// Invoice.java
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

@JacksonXmlRootElement(localName = "invoice")
public class Invoice {
    @JacksonXmlProperty(localName = "customer")
    private String customer;

    @JacksonXmlProperty(localName = "amount")
    private Double amount;

    @JacksonXmlProperty(localName = "date")
    private String date;

    // Constructors, getters, setters
    public Invoice() {}
    public Invoice(String customer, Double amount, String date) {
        this.customer = customer;
        this.amount = amount;
        this.date = date;
    }

    // Getters and setters...
}
```

> ✅ Jackson will serialize this to:
> ```xml
> <invoice>
>   <customer>Alice</customer>
>   <amount>199.99</amount>
>   <date>2024-06-01</date>
> </invoice>
> ```

---

### 3. **Reuse Your XSLT (from earlier)**

Your `invoice.xsl` remains **unchanged** — it expects the same XML structure.

---

### 4. **Update the PDF Service to Handle JSON**

```java
// PdfGenerationService.java
@Service
public class PdfGenerationService {

    private final ObjectMapper jsonMapper = new ObjectMapper();
    private final XmlMapper xmlMapper = new XmlMapper();

    // Secure TransformerFactory (as discussed earlier)
    private final TransformerFactory transformerFactory;

    public PdfGenerationService() throws Exception {
        this.transformerFactory = createSecureTransformerFactory();
    }

    private TransformerFactory createSecureTransformerFactory() throws TransformerConfigurationException {
        TransformerFactory factory = TransformerFactory.newInstance();
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        return factory;
    }

    public byte[] generatePdfFromJson(String jsonInput, String xsltPath) throws Exception {
        // 1. Parse JSON → Java Object
        Invoice invoice = jsonMapper.readValue(jsonInput, Invoice.class);

        // 2. Convert Java Object → XML String
        String xmlString = xmlMapper.writeValueAsString(invoice);

        // 3. Now use the same FOP pipeline as before
        Source xslt = new StreamSource(new ClassPathResource(xsltPath).getInputStream());
        Transformer transformer = transformerFactory.newTransformer(xslt);

        Source src = new StreamSource(new StringReader(xmlString));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Fop fop = FopFactory.newInstance().newFop(MimeConstants.MIME_PDF, out);
        Result res = new SAXResult(fop.getDefaultHandler());

        transformer.transform(src, res);
        return out.toByteArray();
    }
}
```

---

### 5. **Controller Accepts JSON**

```java
@RestController
@RequestMapping("/api/pdf")
public class PdfController {

    @Autowired
    private PdfGenerationService pdfService;

    @PostMapping("/invoice")
    public ResponseEntity<byte[]> generateInvoiceFromJson(@RequestBody String jsonBody) {
        try {
            byte[] pdf = pdfService.generatePdfFromJson(jsonBody, "templates/invoice.xsl");

            return ResponseEntity.ok()
                .header("Content-Disposition", "inline; filename=invoice.pdf")
                .contentType(org.springframework.http.MediaType.APPLICATION_PDF)
                .body(pdf);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
}
```

---

### 6. **Test with JSON**

Send a POST request to `/api/pdf/invoice` with **JSON body**:

```json
{
  "customer": "Bob Johnson",
  "amount": 249.99,
  "date": "2024-06-20"
}
```

✅ Result: A properly formatted PDF!

---

## 🔁 Alternative: Skip Java Model (Direct JSON → XML)

If you don’t want POJOs, you can convert JSON → XML **generically**:

```java
public String jsonToXml(String json) throws Exception {
    ObjectMapper jsonMapper = new ObjectMapper();
    JsonNode jsonNode = jsonMapper.readTree(json);

    XmlMapper xmlMapper = new XmlMapper();
    return xmlMapper.writeValueAsString(jsonNode);
}
```

> ⚠️ But this produces generic XML like:
> ```xml
> <ObjectNode>
>   <customer>Bob</customer>
>   ...
> </ObjectNode>
> ```
> You’d need to adjust your XSLT accordingly.

**Recommendation**: Use **POJOs** for predictable, clean XML structure.

---

## 🛡️ Security Still Applies!

Even though input is JSON:
- The **XML generated from JSON** is still processed by the XSLT/FOP pipeline.
- If you ever allow **user-controlled XML tags** (e.g., via dynamic keys), you could reintroduce risks.
- **Always use the secure `TransformerFactory`** as shown.

> ✅ Since you’re generating XML **programmatically** (not parsing user-provided XML), XXE risk is **very low** — but defense in depth is good practice.

---

## 🚀 Bonus: Use Thymeleaf or Mustache for Simpler Cases?

If your layout is simple, consider:
- Convert JSON → Java object
- Use **Thymeleaf** to render **XSL-FO as a template** (instead of XSLT)
- Feed the resulting XSL-FO string directly to FOP

Example Thymeleaf template (`invoice.fo`):

```xml
<fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format">
  <fo:layout-master-set>...</fo:layout-master-set>
  <fo:page-sequence master-reference="A4">
    <fo:flow flow-name="xsl-region-body">
      <fo:block>Customer: [[${invoice.customer}]]</fo:block>
      <fo:block>Amount: $[[${invoice.amount}]]</fo:block>
    </fo:flow>
  </fo:page-sequence>
</fo:root>
```

Then render with Thymeleaf → String → FOP.

> This avoids XSLT entirely and may be easier for developers familiar with HTML templates.

---

## ✅ Summary

| Approach | When to Use |
|--------|-------------|
| **JSON → POJO → XML → XSLT → FOP** | You already use XSLT or need complex formatting |
| **JSON → Thymeleaf (XSL-FO template) → FOP** | Simpler layouts, prefer template engines over XSLT |
| **Stick with XML input** | If your data source is already XML |

Both approaches let you **leverage Apache FOP’s powerful layout engine** while accepting modern **JSON APIs**.

You get the best of both worlds: **RESTful JSON input** + **print-quality PDF output**. 🎯