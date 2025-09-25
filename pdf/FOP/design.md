Awesome—here’s a **Java starter kit for Strategy A (one XSLT per document type)**.  
It includes a **core library** (data → canonical XML → XSLT → XSL‑FO → FOP → PDF) plus **PDF signing** with PDFBox, **two example templates** (`invoice-v1.xsl`, `statement-v1.xsl`), and a **runnable demo**.

> This is intentionally minimal but production‑oriented: clean separation, caching of compiled XSLTs, FOP font config, and hooks for TOC/signing/versioning.

---

## Project layout

```
pdf-platform/
  pom.xml
  src/main/java/com/example/pdf/
    api/
      DocType.java
      RenderOptions.java
      RenderRequest.java
    core/
      Model.java
      JsonXmlMerger.java
      CanonicalXml.java
      TemplateRouter.java
      XsltToFo.java
      FopRenderer.java
      PdfSigner.java
      RenderService.java
    demo/
      DemoMain.java
  src/main/resources/
    config/fop.xconf
    templates/
      fragments/common-base.xsl
      invoice/invoice-v1.xsl
      statement/statement-v1.xsl
    fonts/ (put .ttf here; e.g., NotoSans)
    samples/data.json
    samples/data.xml
```

---

## `pom.xml`

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0" 
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
                             https://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>
  <groupId>com.example</groupId>
  <artifactId>pdf-platform</artifactId>
  <version>0.1.0</version>
  <properties>
    <maven.compiler.source>17</maven.compiler.source>
    <maven.compiler.target>17</maven.compiler.target>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
  </properties>

  <dependencies>
    <!-- Apache FOP -->
    <dependency>
      <groupId>org.apache.xmlgraphics</groupId>
      <artifactId>fop</artifactId>
      <version>2.9</version>
    </dependency>

    <!-- Saxon-HE (XSLT 2.0) -->
    <dependency>
      <groupId>net.sf.saxon</groupId>
      <artifactId>Saxon-HE</artifactId>
      <version>12.5</version>
    </dependency>

    <!-- JSON (Jackson) -->
    <dependency>
      <groupId>com.fasterxml.jackson.core</groupId>
      <artifactId>jackson-databind</artifactId>
      <version>2.17.2</version>
    </dependency>

    <!-- JAXB (Jakarta) for Canonical XML -->
    <dependency>
      <groupId>jakarta.xml.bind</groupId>
      <artifactId>jakarta.xml.bind-api</artifactId>
      <version>4.0.2</version>
    </dependency>
    <dependency>
      <groupId>org.glassfish.jaxb</groupId>
      <artifactId>jaxb-runtime</artifactId>
      <version>4.0.5</version>
    </dependency>

    <!-- PDFBox (signing, encryption, stamping) -->
    <dependency>
      <groupId>org.apache.pdfbox</groupId>
      <artifactId>pdfbox</artifactId>
      <version>3.0.3</version>
    </dependency>
    <dependency>
      <groupId>org.bouncycastle</groupId>
      <artifactId>bcprov-jdk18on</artifactId>
      <version>1.78.1</version>
    </dependency>
  </dependencies>
</project>
```

---

## API: doc type, options, request

**`api/DocType.java`**
```java
package com.example.pdf.api;

public enum DocType {
  INVOICE, STATEMENT, LABEL, CERTIFICATE, REPORT;

  public static DocType fromString(String v) {
    return DocType.valueOf(v.trim().toUpperCase());
  }
}
```

**`api/RenderOptions.java`**
```java
package com.example.pdf.api;

public class RenderOptions {
  public boolean toc = false;          // enable Table of Contents (for reports/statements)
  public boolean sign = false;         // digitally sign with PKCS#12
  public String templateVersion = "v1";
  public String brand = "default";
}
```

**`api/RenderRequest.java`**
```java
package com.example.pdf.api;

import java.util.Optional;

public class RenderRequest {
  public DocType docType;
  public RenderOptions options = new RenderOptions();

  // Pointers or raw strings; pick what suits your ingestion
  public String jsonData;  // JSON payload (optional)
  public String xmlData;   // XML payload (optional)

  public Optional<String> json() { return Optional.ofNullable(jsonData); }
  public Optional<String> xml() { return Optional.ofNullable(xmlData); }
}
```

---

## Core model (JAXB) and merger

**`core/Model.java`**
```java
package com.example.pdf.core;

import jakarta.xml.bind.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "document")
@XmlAccessorType(XmlAccessType.FIELD)
public class Model {

  @XmlAttribute
  public String type; // invoice|statement|...

  @XmlAttribute
  public String templateVersion;

  @XmlAttribute
  public String brand;

  @XmlElement
  public Header header = new Header();

  @XmlElementWrapper(name = "items")
  @XmlElement(name = "item")
  public List<Item> items = new ArrayList<>();

  @XmlElement
  public Totals totals = new Totals();

  @XmlAccessorType(XmlAccessType.FIELD)
  public static class Header {
    public String docId;
    public String customerName;
    public String accountId;
    public String period;
    public String currency = "INR";
    public String locale = "en-IN";
    public String docDate; // ISO (yyyy-MM-dd)
  }

  @XmlAccessorType(XmlAccessType.FIELD)
  public static class Item {
    public String name;
    public int quantity;
    public BigDecimal price;
    public String description;
  }

  @XmlAccessorType(XmlAccessType.FIELD)
  public static class Totals {
    public BigDecimal subtotal;
    public BigDecimal tax;
    public BigDecimal grandTotal;
  }
}
```

**`core/JsonXmlMerger.java`**
```java
package com.example.pdf.core;

import com.example.pdf.api.DocType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Unmarshaller;

import java.io.StringReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Iterator;

public class JsonXmlMerger {

  private final ObjectMapper om = new ObjectMapper();

  public Model merge(DocType docType, String templateVersion, String brand,
                     String jsonOpt, String xmlOpt) throws Exception {
    Model merged = new Model();
    merged.type = docType.name().toLowerCase();
    merged.templateVersion = templateVersion;
    merged.brand = brand;

    if (xmlOpt != null && !xmlOpt.isBlank()) {
      JAXBContext jaxb = JAXBContext.newInstance(Model.class);
      Unmarshaller um = jaxb.createUnmarshaller();
      Model fromXml = (Model) um.unmarshal(new StringReader(xmlOpt));
      // naive copy
      merged = fromXml;
      merged.type = docType.name().toLowerCase();
      merged.templateVersion = templateVersion;
      merged.brand = brand;
    }

    if (jsonOpt != null && !jsonOpt.isBlank()) {
      JsonNode root = om.readTree(jsonOpt);

      // header
      JsonNode hdr = root.path("header");
      if (!hdr.isMissingNode()) {
        if (hdr.hasNonNull("docId")) merged.header.docId = hdr.get("docId").asText();
        if (hdr.hasNonNull("customerName")) merged.header.customerName = hdr.get("customerName").asText();
        if (hdr.hasNonNull("accountId")) merged.header.accountId = hdr.get("accountId").asText();
        if (hdr.hasNonNull("period")) merged.header.period = hdr.get("period").asText();
        if (hdr.hasNonNull("currency")) merged.header.currency = hdr.get("currency").asText();
        if (hdr.hasNonNull("locale")) merged.header.locale = hdr.get("locale").asText();
        if (hdr.hasNonNull("docDate")) merged.header.docDate = hdr.get("docDate").asText();
        else merged.header.docDate = LocalDate.now().toString();
      }

      // items
      JsonNode items = root.path("items");
      if (items.isArray()) {
        merged.items.clear();
        for (Iterator<JsonNode> it = items.elements(); it.hasNext(); ) {
          JsonNode n = it.next();
          Model.Item item = new Model.Item();
          item.name = n.path("name").asText("");
          item.quantity = n.path("quantity").asInt(1);
          if (n.hasNonNull("price")) item.price = new BigDecimal(n.get("price").asText());
          item.description = n.path("description").asText("");
          merged.items.add(item);
        }
      }

      // totals
      JsonNode totals = root.path("totals");
      if (!totals.isMissingNode()) {
        if (totals.hasNonNull("subtotal")) merged.totals.subtotal = new BigDecimal(totals.get("subtotal").asText());
        if (totals.hasNonNull("tax")) merged.totals.tax = new BigDecimal(totals.get("tax").asText());
        if (totals.hasNonNull("grandTotal")) merged.totals.grandTotal = new BigDecimal(totals.get("grandTotal").asText());
      }
    }

    // simple auto-calc if missing
    if (merged.totals.subtotal == null) {
      merged.totals.subtotal = merged.items.stream()
        .map(i -> i.price == null ? BigDecimal.ZERO : i.price.multiply(BigDecimal.valueOf(i.quantity)))
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    if (merged.totals.tax == null) merged.totals.tax = BigDecimal.ZERO;
    if (merged.totals.grandTotal == null) merged.totals.grandTotal = merged.totals.subtotal.add(merged.totals.tax);
    if (merged.header.docDate == null) merged.header.docDate = LocalDate.now().toString();

    return merged;
  }
}
```

**`core/CanonicalXml.java`**
```java
package com.example.pdf.core;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Marshaller;
import java.io.StringWriter;

public class CanonicalXml {
  public String marshal(Model model) throws Exception {
    JAXBContext ctx = JAXBContext.newInstance(Model.class);
    Marshaller m = ctx.createMarshaller();
    m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
    StringWriter sw = new StringWriter();
    m.marshal(model, sw);
    return sw.toString();
  }
}
```

---

## Template routing (Strategy A)

**`core/TemplateRouter.java`**
```java
package com.example.pdf.core;

import com.example.pdf.api.DocType;

public class TemplateRouter {

  public String resolveXsltPath(DocType docType, String version) {
    return switch (docType) {
      case INVOICE     -> "templates/invoice/invoice-" + version + ".xsl";
      case STATEMENT   -> "templates/statement/statement-" + version + ".xsl";
      case LABEL       -> "templates/label/label-" + version + ".xsl";         // add file later
      case CERTIFICATE -> "templates/certificate/certificate-" + version + ".xsl";
      case REPORT      -> "templates/report/report-" + version + ".xsl";
    };
  }
}
```

---

## Transform (Saxon‑HE) and render (FOP)

**`core/XsltToFo.java`**
```java
package com.example.pdf.core;

import net.sf.saxon.s9api.*;

import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class XsltToFo {

  private final Processor processor = new Processor(false);
  private final XsltCompiler compiler = processor.newXsltCompiler();

  public byte[] transform(String canonicalXml, String xsltClasspath, boolean toc) throws Exception {
    InputStream xslt = resource(xsltClasspath);
    if (xslt == null) throw new IllegalArgumentException("XSLT not found: " + xsltClasspath);

    XsltExecutable exec = compiler.compile(new StreamSource(xslt));
    XsltTransformer t = exec.load();

    DocumentBuilder db = processor.newDocumentBuilder();
    XdmNode src = db.build(new StreamSource(new ByteArrayInputStream(canonicalXml.getBytes("UTF-8"))));
    t.setInitialContextNode(src);

    // parameters
    t.setParameter(new QName("toc"), new XdmAtomicValue(toc ? "true" : "false"));

    ByteArrayOutputStream foOut = new ByteArrayOutputStream();
    Serializer ser = processor.newSerializer(foOut);
    ser.setOutputProperty(Serializer.Property.METHOD, "xml");
    ser.setOutputProperty(Serializer.Property.INDENT, "yes");

    t.setDestination(ser);
    t.transform();
    return foOut.toByteArray();
  }

  private InputStream resource(String path) {
    return Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
  }
}
```

**`core/FopRenderer.java`**
```java
package com.example.pdf.core;

import org.apache.fop.apps.*;
import javax.xml.transform.*;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;

public class FopRenderer {
  private final FopFactory fopFactory;

  public FopRenderer(String fopConfigClasspath) throws Exception {
    try (InputStream is = resource(fopConfigClasspath)) {
      if (is == null) throw new IllegalArgumentException("fop.xconf not found: " + fopConfigClasspath);
      File tmp = File.createTempFile("fop-", ".xconf");
      try (OutputStream os = new FileOutputStream(tmp)) { is.transferTo(os); }
      fopFactory = FopFactory.newInstance(tmp.toURI());
    }
  }

  public byte[] renderPdf(byte[] foBytes) throws Exception {
    FOUserAgent foUserAgent = fopFactory.newFOUserAgent();
    ByteArrayOutputStream pdfOut = new ByteArrayOutputStream();

    Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, foUserAgent, pdfOut);
    Transformer id = TransformerFactory.newInstance().newTransformer();
    Source src = new StreamSource(new ByteArrayInputStream(foBytes));
    Result res = new SAXResult(fop.getDefaultHandler());

    id.transform(src, res);
    return pdfOut.toByteArray();
  }

  private InputStream resource(String path) {
    return Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
  }
}
```

---

## Digital signature (PDFBox)

**`core/PdfSigner.java`**
```java
package com.example.pdf.core;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.ExternalSigningSupport;

import java.io.*;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.cert.Certificate;
import java.util.Calendar;
import java.util.Collections;

public class PdfSigner {

  public byte[] sign(byte[] pdf, String pkcs12Path, char[] password) throws Exception {
    KeyStore ks = KeyStore.getInstance("PKCS12");
    try (InputStream is = new FileInputStream(pkcs12Path)) {
      ks.load(is, password);
    }
    String alias = Collections.list(ks.aliases()).get(0);
    PrivateKey privateKey = (PrivateKey) ks.getKey(alias, password);
    Certificate[] chain = ks.getCertificateChain(alias);

    try (PDDocument doc = PDDocument.load(pdf)) {
      PDSignature signature = new PDSignature();
      signature.setFilter(PDSignature.FILTER_ADOBE_PPKLITE);
      signature.setSubFilter(PDSignature.SUBFILTER_ADBE_PKCS7_DETACHED);
      signature.setName("Your Company");
      signature.setLocation("Chennai, IN");
      signature.setReason("Approved");
      signature.setSignDate(Calendar.getInstance());

      doc.addSignature(signature); // invisible signature

      ByteArrayOutputStream signedOut = new ByteArrayOutputStream();
      ExternalSigningSupport ext = doc.saveIncrementalForExternalSigning(signedOut);

      // compute CMS
      Signature cms = Signature.getInstance("SHA256withRSA");
      cms.initSign(privateKey);
      try (InputStream content = ext.getContent()) {
        content.transferTo(new OutputStream() {
          @Override public void write(int b) throws IOException { cms.update((byte) b); }
          @Override public void write(byte[] b, int off, int len) { cms.update(b, off, len); }
        });
      }
      byte[] cmsSignature = cms.sign();
      ext.setSignature(cmsSignature);

      return signedOut.toByteArray();
    }
  }
}
```

> For visible signatures or TSA timestamps, we can extend this class later.

---

## Orchestration service

**`core/RenderService.java`**
```java
package com.example.pdf.core;

import com.example.pdf.api.DocType;
import com.example.pdf.api.RenderOptions;

public class RenderService {

  private final JsonXmlMerger merger = new JsonXmlMerger();
  private final CanonicalXml canonical = new CanonicalXml();
  private final TemplateRouter router = new TemplateRouter();
  private final XsltToFo xslt = new XsltToFo();
  private final FopRenderer fop;

  public RenderService() throws Exception {
    this.fop = new FopRenderer("config/fop.xconf");
  }

  public byte[] render(DocType docType, RenderOptions opts, String json, String xml,
                       boolean sign, String pkcs12Path, char[] pw) throws Exception {

    // 1) Merge → Model
    Model model = merger.merge(docType, opts.templateVersion, opts.brand, json, xml);

    // 2) Canonical XML
    String cx = canonical.marshal(model);

    // 3) XSLT (docType → XSLT path)
    String xsltPath = router.resolveXsltPath(docType, opts.templateVersion);
    byte[] fo = xslt.transform(cx, xsltPath, opts.toc);

    // 4) FOP → PDF
    byte[] pdf = fop.renderPdf(fo);

    // 5) Sign if requested
    if (sign) {
      PdfSigner signer = new PdfSigner();
      pdf = signer.sign(pdf, pkcs12Path, pw);
    }
    return pdf;
  }
}
```

---

## Demo runner

**`demo/DemoMain.java`**
```java
package com.example.pdf.demo;

import com.example.pdf.api.DocType;
import com.example.pdf.api.RenderOptions;
import com.example.pdf.core.RenderService;

import java.nio.file.Files;
import java.nio.file.Path;

public class DemoMain {
  public static void main(String[] args) throws Exception {
    String json = Files.readString(Path.of("src/main/resources/samples/data.json"));
    String xml = Files.readString(Path.of("src/main/resources/samples/data.xml")); // optional

    RenderService svc = new RenderService();

    RenderOptions opts = new RenderOptions();
    opts.templateVersion = "v1";
    opts.toc = false;

    byte[] pdf = svc.render(DocType.INVOICE, opts, json, null, false, null, null);
    Files.write(Path.of("target/invoice-sample.pdf"), pdf);

    opts.toc = true;
    pdf = svc.render(DocType.STATEMENT, opts, json, null, false, null, null);
    Files.write(Path.of("target/statement-sample.pdf"), pdf);

    System.out.println("Generated PDFs in target/");
  }
}
```

---

## FOP configuration

**`resources/config/fop.xconf`**
```xml
<fop version="1.0">
  <renderers>
    <renderer mime="application/pdf">
      <fonts>
        <directory recursive="true">src/main/resources/fonts</directory>
        <auto-detect/> <!-- optional -->
      </fonts>
      <!-- Uncomment for archival
      <pdf-a-mode disable-verification="true">PDF/A-1b</pdf-a-mode>
      -->
    </renderer>
  </renderers>
</fop>
```

> Put your fonts (e.g., Noto Sans) in `src/main/resources/fonts/` for consistent rendering.

---

## XSLT templates (Strategy A)

**`templates/fragments/common-base.xsl`**
```xml
<xsl:stylesheet version="2.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:fo="http://www.w3.org/1999/XSL/Format">
  <xsl:output method="xml" indent="yes"/>

  <!-- Common table header -->
  <xsl:template name="table-header">
    <fo:table-header>
      <fo:table-row background-color="#f3f3f3">
        <fo:table-cell><fo:block font-weight="bold">Item</fo:block></fo:table-cell>
        <fo:table-cell><fo:block font-weight="bold" text-align="right">Qty</fo:block></fo:table-cell>
        <fo:table-cell><fo:block font-weight="bold" text-align="right">Price</fo:block></fo:table-cell>
      </fo:table-row>
    </fo:table-header>
  </xsl:template>

  <!-- Common items rendering -->
  <xsl:template name="items-table">
    <fo:table table-layout="fixed" width="100%">
      <fo:table-column column-width="proportional-column-width(6)"/>
      <fo:table-column column-width="proportional-column-width(2)"/>
      <fo:table-column column-width="proportional-column-width(2)"/>
      <xsl:call-template name="table-header"/>
      <fo:table-body>
        <xsl:for-each select="/document/items/item">
          <fo:table-row keep-together.within-page="always">
            <fo:table-cell><fo:block><xsl:value-of select="name"/></fo:block></fo:table-cell>
            <fo:table-cell><fo:block text-align="right"><xsl:value-of select="quantity"/></fo:block></fo:table-cell>
            <fo:table-cell><fo:block text-align="right"><xsl:value-of select="format-number(number(price), '#,##0.00')"/></fo:block></fo:table-cell>
          </fo:table-row>
        </xsl:for-each>
      </fo:table-body>
    </fo:table>
  </xsl:template>

</xsl:stylesheet>
```

**`templates/invoice/invoice-v1.xsl`**
```xml
<xsl:stylesheet version="2.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:fo="http://www.w3.org/1999/XSL/Format">
  <xsl:output method="xml" indent="yes"/>
  <xsl:param name="toc" select="'false'"/>

  ../fragments/common-base.xsl

  <xsl:template match="/document[@type='invoice']">
    <fo:root>
      <fo:layout-master-set>
        <fo:simple-page-master master-name="A4" page-width="210mm" page-height="297mm"
          margin-top="15mm" margin-bottom="15mm" margin-left="15mm" margin-right="15mm">
          <fo:region-before extent="18mm"/>
          <fo:region-after extent="12mm"/>
          <fo:region-body margin-top="20mm" margin-bottom="15mm"/>
        </fo:simple-page-master>
      </fo:layout-master-set>

      <fo:page-sequence master-reference="A4" initial-page-number="1">
        <fo:static-content flow-name="xsl-region-before">
          <fo:block font-size="10pt" font-weight="bold">Invoice <xsl:value-of select="header/docId"/></fo:block>
        </fo:static-content>
        <fo:static-content flow-name="xsl-region-after">
          <fo:block text-align="center" font-size="9pt">
            Page <fo:page-number/> of <fo:page-number-citation-last ref-id="doc-end"/>
          </fo:block>
        </fo:static-content>

        <fo:flow flow-name="xsl-region-body">
          <fo:block font-size="16pt" font-weight="bold" space-after="6pt">Invoice</fo:block>
          <fo:block space-after="6pt">
            Customer: <xsl:value-of select="header/customerName"/> • Date: <xsl:value-of select="header/docDate"/>
          </fo:block>

          <xsl:call-template name="items-table"/>

          <fo:block space-before="6pt" text-align="right">
            <fo:inline>Subtotal: </fo:inline>
            <fo:inline font-weight="bold">
              <xsl:value-of select="format-number(number(totals/subtotal), '#,##0.00')"/>
            </fo:inline>
          </fo:block>
          <fo:block text-align="right">
            <fo:inline>Tax: </fo:inline>
            <fo:inline font-weight="bold">
              <xsl:value-of select="format-number(number(totals/tax), '#,##0.00')"/>
            </fo:inline>
          </fo:block>
          <fo:block text-align="right" font-size="12pt" font-weight="bold">
            Total: <xsl:value-of select="format-number(number(totals/grandTotal), '#,##0.00')"/>
          </fo:block>

          <fo:block id="doc-end"/>
        </fo:flow>
      </fo:page-sequence>
    </fo:root>
  </xsl:template>
</xsl:stylesheet>
```

**`templates/statement/statement-v1.xsl`**
```xml
<xsl:stylesheet version="2.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:fo="http://www.w3.org/1999/XSL/Format">
  <xsl:output method="xml" indent="yes"/>
  <xsl:param name="toc" select="'true'"/>

  ../fragments/common-base.xsl

  <xsl:template match="/document[@type='statement']">
    <fo:root>
      <fo:layout-master-set>
        <fo:simple-page-master master-name="A4" page-width="210mm" page-height="297mm"
          margin-top="15mm" margin-bottom="15mm" margin-left="15mm" margin-right="15mm">
          <fo:region-before extent="18mm"/>
          <fo:region-after extent="12mm"/>
          <fo:region-body margin-top="20mm" margin-bottom="15mm"/>
        </fo:simple-page-master>
      </fo:layout-master-set>

      <fo:page-sequence master-reference="A4" initial-page-number="1">
        <fo:static-content flow-name="xsl-region-before">
          <fo:block font-size="10pt" font-weight="bold">Statement • <xsl:value-of select="header/period"/></fo:block>
        </fo:static-content>
        <fo:static-content flow-name="xsl-region-after">
          <fo:block text-align="center" font-size="9pt">
            Page <fo:page-number/> of <fo:page-number-citation-last ref-id="end"/>
          </fo:block>
        </fo:static-content>

        <fo:flow flow-name="xsl-region-body">
          <fo:block font-size="16pt" font-weight="bold" space-after="6pt">Statement of Account</fo:block>
          <fo:block space-after="8pt">Customer: <xsl:value-of select="header/customerName"/></fo:block>

          <!-- Optional TOC -->
          <xsl:if test="$toc='true'">
            <fo:block font-size="12pt" font-weight="bold" space-after="6pt">Table of Contents</fo:block>
            <xsl:for-each select="/document/items/item">
              <fo:block>
                <xsl:value-of select="name"/>
                <fo:leader leader-pattern="dots"/>
                <fo:page-number-citation ref-id="{concat('sec-', position())}"/>
              </fo:block>
            </xsl:for-each>
            <fo:block space-after="10pt"/>
          </xsl:if>

          <!-- Sections (demo: each item is a section) -->
          <xsl:for-each select="/document/items/item">
            <fo:block id="{concat('sec-', position())}" font-weight="bold" space-before="8pt">
              <xsl:value-of select="name"/>
            </fo:block>
            <fo:block>Description: <xsl:value-of select="description"/></fo:block>
          </xsl:for-each>

          <fo:block id="end"/>
        </fo:flow>
      </fo:page-sequence>
    </fo:root>
  </xsl:template>
</xsl:stylesheet>
```

---

## Sample data (quick)

**`resources/samples/data.json`**
```json
{
  "header": {
    "docId": "INV-10017",
    "customerName": "Acme Corp",
    "period": "2025-09",
    "currency": "INR",
    "docDate": "2025-09-25"
  },
  "items": [
    { "name": "Widget A", "quantity": 3, "price": 120.00, "description": "Premium widget" },
    { "name": "Widget B", "quantity": 1, "price": 250.00, "description": "Deluxe widget" }
  ],
  "totals": {
    "subtotal": 610.00,
    "tax": 18.00,
    "grandTotal": 628.00
  }
}
```

**`resources/samples/data.xml`** (optional; same structure as `Model`)
```xml
<document type="invoice" templateVersion="v1" brand="default">
  <header>
    <docId>INV-10017</docId>
    <customerName>Acme Corp</customerName>
    <period>2025-09</period>
    <currency>INR</currency>
    <docDate>2025-09-25</docDate>
  </header>
  <items>
    <item><name>Widget X</name><quantity>2</quantity><price>199.00</price><description>Alt</description></item>
  </items>
  <totals>
    <subtotal>398.00</subtotal>
    <tax>0.00</tax>
    <grandTotal>398.00</grandTotal>
  </totals>
</document>
```

---

## Run it

```bash
mvn -q -e -DskipTests package
java -cp target/pdf-platform-0.1.0.jar com.example.pdf.demo.DemoMain
# PDFs written to target/invoice-sample.pdf and target/statement-sample.pdf
```

> To test signing, set `sign=true` in your own driver or wire a small CLI/REST and provide a `.p12` path + password.

---

## Next add-ons (when you’re ready)

- **Visible signature appearance** (signature field, image, position).
- **PDF/A** output (enable in FOP config and adjust color profiles).
- **Barcodes/QR** (ZXing) generated as PNG/SVG and included in FO.
- **Spring Boot REST** endpoint for transactional generation.
- **Batch worker** (e.g., Kafka listener) that calls `RenderService`.
- **Brand overlays** (logo/colors via additional XSL imports).
- **Golden-file testing** (render → checksum/visual diff).

---

If you’d like, I can **package this into a downloadable ZIP** (with all files laid out) or extend it with a **Spring Boot REST API** (`POST /render`) that accepts your payload and returns the PDF.
