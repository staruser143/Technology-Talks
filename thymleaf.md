Great! Using **Thymeleaf to generate XSL-FO** (instead of XSLT) is a clean, developer-friendly alternative—especially if you're already using Thymeleaf in your Spring Boot app or prefer HTML-like templating over XSLT.

Here’s how to do it step by step:

---

## 🎯 Goal
Convert **JSON → Java Object → Thymeleaf (XSL-FO template) → Apache FOP → PDF**

No XSLT needed!

---

## ✅ Step 1: Add Dependencies

In `pom.xml`:

```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-thymeleaf</artifactId>
    </dependency>
    <dependency>
        <groupId>org.apache.xmlgraphics</groupId>
        <artifactId>fop</artifactId>
        <version>2.9</version>
    </dependency>
</dependencies>
```

> 💡 Thymeleaf is now ready to render templates.

---

## 📁 Step 2: Create an XSL-FO Template with Thymeleaf

Place your template under:  
`src/main/resources/templates/invoice.fo`

> 🔸 **Important**: Use `.fo` extension (not `.html`) so Thymeleaf doesn’t auto-escape XML.

### `invoice.fo`
```xml
<?xml version="1.0" encoding="UTF-8"?>
<fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format"
         xmlns:th="http://www.thymeleaf.org">

  <fo:layout-master-set>
    <fo:simple-page-master master-name="A4" page-height="29.7cm" page-width="21cm" margin="1cm">
      <fo:region-body margin-top="2cm"/>
      <fo:region-before extent="2cm"/>
    </fo:simple-page-master>
  </fo:layout-master-set>

  <fo:page-sequence master-reference="A4">
    <fo:static-content flow-name="xsl-region-before">
      <fo:block font-size="18pt" font-weight="bold" text-align="center">
        INVOICE
      </fo:block>
    </fo:static-content>

    <fo:flow flow-name="xsl-region-body">
      <fo:block font-size="12pt" space-after="10px">
        <fo:inline font-weight="bold">Customer:</fo:inline>
        <fo:inline th:text="${invoice.customer}">John Doe</fo:inline>
      </fo:block>

      <fo:block font-size="12pt" space-after="10px">
        <fo:inline font-weight="bold">Amount:</fo:inline>
        <fo:inline th:text="|${#numbers.formatDecimal(invoice.amount, 1, 'COMMA', 2, 'POINT')}|">$199.99</fo:inline>
      </fo:block>

      <fo:block font-size="12pt">
        <fo:inline font-weight="bold">Date:</fo:inline>
        <fo:inline th:text="${invoice.date}">2024-06-01</fo:inline>
      </fo:block>
    </fo:flow>
  </fo:page-sequence>
</fo:root>
```

> ✅ Thymeleaf expressions like `${invoice.customer}` will be replaced at render time.

---

## 🧱 Step 3: Create a Java Model (POJO)

```java
// Invoice.java
public class Invoice {
    private String customer;
    private Double amount;
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

---

## 🛠️ Step 4: Create a Service to Render PDF

```java
// PdfGenerationService.java
import org.apache.fop.apps.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.StringReader;
import java.io.ByteArrayOutputStream;

@Service
public class PdfGenerationService {

    @Autowired
    private SpringTemplateEngine templateEngine; // Auto-configured by Spring Boot

    public byte[] generatePdfFromInvoice(Invoice invoice) throws Exception {
        // 1. Prepare Thymeleaf context
        Context ctx = new Context();
        ctx.setVariable("invoice", invoice);

        // 2. Render XSL-FO template to String
        String foContent = templateEngine.process("invoice", ctx); // matches invoice.fo

        // 3. Feed XSL-FO string to Apache FOP
        FopFactory fopFactory = FopFactory.newInstance();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, out);

        // Transform FO string → PDF
        javax.xml.transform.TransformerFactory factory = 
            javax.xml.transform.TransformerFactory.newInstance();
        factory.setFeature(javax.xml.XMLConstants.FEATURE_SECURE_PROCESSING, true);

        javax.xml.transform.Source src = new javax.xml.transform.stream.StreamSource(new StringReader(foContent));
        javax.xml.transform.Result res = new javax.xml.transform.sax.SAXResult(fop.getDefaultHandler());

        factory.newTransformer().transform(src, res);

        return out.toByteArray();
    }
}
```

> ✅ No XSLT! Just Thymeleaf → XSL-FO string → FOP.

---

## 🌐 Step 5: Create a REST Controller

```java
@RestController
@RequestMapping("/api/pdf")
public class PdfController {

    @Autowired
    private PdfGenerationService pdfService;

    @PostMapping("/invoice")
    public ResponseEntity<byte[]> generateInvoice(@RequestBody Invoice invoice) {
        try {
            byte[] pdfBytes = pdfService.generatePdfFromInvoice(invoice);

            return ResponseEntity.ok()
                .header("Content-Disposition", "inline; filename=invoice.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
```

---

## 🧪 Step 6: Test It

Send a **POST** request to `/api/pdf/invoice` with JSON:

```json
{
  "customer": "Alice Cooper",
  "amount": 299.5,
  "date": "2024-06-25"
}
```

✅ You’ll receive a beautifully formatted PDF!

---

## 🔒 Security Note

Since you’re **generating XSL-FO from a template** (not parsing user XML), **XXE risk is eliminated**.  
However, still use `FEATURE_SECURE_PROCESSING` when creating the `Transformer` (as shown) for defense in depth.

---

## 🎨 Tips & Best Practices

### 1. **Use Thymeleaf Utility Objects**
- `#dates`, `#numbers`, `#strings` work in `.fo` templates.
- Example:  
  ```html
  <fo:inline th:text="${#dates.format(invoice.date, 'dd/MM/yyyy')}"/>
  ```

### 2. **Organize Templates**
- Put `.fo` files in `src/main/resources/templates/`
- Spring Boot auto-configures Thymeleaf to look there.

### 3. **Handle Errors Gracefully**
- Validate `Invoice` fields (use `@Valid` in controller)
- Catch `TemplateInputException` if template is missing

### 4. **Caching**
- Thymeleaf caches templates in production (good for performance)
- Disable in dev with:  
  ```yaml
  # application.yml
  spring:
    thymeleaf:
      cache: false
  ```

### 5. **Complex Layouts?**
- You can still use **page sequences**, **tables**, **images**, etc. in XSL-FO
- Thymeleaf just fills in the data — layout logic stays in the `.fo` file

---

## 🆚 Thymeleaf vs XSLT for FOP

| Feature | Thymeleaf + XSL-FO | XSLT + XSL-FO |
|--------|--------------------|---------------|
| Learning curve | Easier (HTML-like) | Steeper (functional, XML-centric) |
| Debugging | Simple (just view rendered FO) | Harder (XSLT trace needed) |
| Logic in template | Yes (`th:if`, `th:each`) | Yes (but verbose) |
| Best for | Devs familiar with web templates | XML/XSLT experts, DocBook pipelines |

---

## ✅ Summary

With Thymeleaf:
- You **skip XSLT entirely**
- Write **clean, readable XSL-FO templates** with Thymeleaf expressions
- Accept **JSON → POJO → PDF** in a modern Spring Boot way
- Maintain **full control over PDF layout** via XSL-FO

This approach is **ideal for most business applications** (invoices, reports, certificates) where you want simplicity without sacrificing print-quality output.

🚀 Now go generate some PDFs!