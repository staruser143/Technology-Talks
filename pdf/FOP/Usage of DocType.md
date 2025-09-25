Great question, Sridhar. The **`docType` you send in the payload** is what *routes* the request to the right **XSLT/XSL‑FO template** and toggles any **doc‑type–specific layout rules** (page masters, headers/footers, complex tables, TOC, etc.). Below is a clear, end‑to‑end view of **how that value is used** and a few **implementation patterns** you can pick from.

---

## 1) Where `docType` lives and how it flows

**Request (example):**
```json
{
  "docType": "invoice",                // invoice | statement | label | certificate | report
  "templateVersion": "v1",             // optional versioning
  "brand": "default",                  // optional brand/theme
  "options": { "sign": true, "persist": false, "toc": false },
  "data": { /* JSON or reference to XML/DB */ }
}
```

**Canonical XML (after mapping JSON/XML → POJOs → XML):**
```xml
<document type="invoice" templateVersion="v1" brand="default">
  <header>
    <docId>INV-10017</docId>
    <customerName>Acme Corp</customerName>
    <docDate>2025-09-25</docDate>
  </header>
  <items>
    <item><name>Widget A</name><quantity>3</quantity><price>120.00</price></item>
    <!-- ... -->
  </items>
  <totals>
    <subtotal>360.00</subtotal>
    <tax>18.00</tax>
    <grandTotal>378.00</grandTotal>
  </totals>
</document>
```

From here, **Saxon‑HE** applies the correct **XSLT** (selected using `docType`), producing **XSL‑FO**. **Apache FOP** renders FO → PDF.

---

## 2) Three common routing strategies (choose one)

### A) **One XSLT per document type** (simple & clear)
- `templates/invoice/invoice-v1.xsl`
- `templates/statement/statement-v1.xsl`
- …
- **Router** selects stylesheet by `docType` (+ `templateVersion`/`brand`).

**Pros:** Clean separation, easy to reason about.  
**Cons:** Shared fragments must be duplicated unless you use includes.

---

### B) **One “master” XSLT with includes/imports** (modular)
- `templates/master.xsl` includes `fragments/common/*.xsl` and `fragments/invoice/*.xsl`, etc.
- Pass `docType` as an **XSLT param** and dispatch to a named template.

**Pros:** Shared components in one place; doc‑type fragments isolated.  
**Cons:** Slightly more indirection; need a small dispatcher.

---

### C) **Template registry (DB or config)** (flexible & versioned)
- A registry maps: **(docType, version, brand)** → **stylesheet URI**.
- DevOps can “flip” versions without redeploying code.

**Pros:** Great for versioning, A/B testing, blue/green template changes.  
**Cons:** Requires a registry and cache.

---

## 3) Java implementation (router + transform)

### 3.1 Define the key types
```java
enum DocType { INVOICE, STATEMENT, LABEL, CERTIFICATE, REPORT }

record TemplateKey(DocType docType, String version, String brand) { }
```

### 3.2 Template registry with cache (precompile XSLT)
```java
public final class TemplateRegistry {
  private final Processor saxon = new Processor(false);
  private final XsltCompiler compiler = saxon.newXsltCompiler();
  private final ConcurrentMap<TemplateKey, XsltExecutable> cache = new ConcurrentHashMap<>();

  public XsltExecutable getExecutable(TemplateKey key) {
    return cache.computeIfAbsent(key, this::compile);
  }

  private XsltExecutable compile(TemplateKey key) {
    String base = "templates/";
    String path = switch (key.docType()) {
      case INVOICE     -> "%sinvoice/invoice-%s.xsl".formatted(base, key.version());
      case STATEMENT   -> "%sstatement/statement-%s.xsl".formatted(base, key.version());
      case LABEL       -> "%slabel/label-%s.xsl".formatted(base, key.version());
      case CERTIFICATE -> "%scertificate/certificate-%s.xsl".formatted(base, key.version());
      case REPORT      -> "%sreport/report-%s.xsl".formatted(base, key.version());
    };
    // Optionally add brand overlays: e.g., .../%s/%s.xsl using key.brand()
    try (InputStream is = getClass().getClassLoader().getResourceAsStream(path)) {
      if (is == null) throw new IllegalArgumentException("Template not found: " + path);
      return compiler.compile(new StreamSource(is));
    } catch (Exception e) {
      throw new RuntimeException("Failed to compile XSLT " + path, e);
    }
  }
}
```

### 3.3 Using the selected XSLT to produce XSL‑FO
```java
public byte[] toXslFo(byte[] canonicalXml, TemplateKey key, Map<String, String> params, TemplateRegistry registry) {
  try {
    XsltExecutable exec = registry.getExecutable(key);
    XsltTransformer t = exec.load();
    DocumentBuilder db = exec.getProcessor().newDocumentBuilder();
    t.setInitialContextNode(db.build(new StreamSource(new ByteArrayInputStream(canonicalXml))));

    // Pass runtime flags (e.g., TOC, watermark)
    if (params != null) {
      for (var e : params.entrySet()) {
        t.setParameter(new QName(e.getKey()), new XdmAtomicValue(e.getValue()));
      }
    }

    ByteArrayOutputStream foOut = new ByteArrayOutputStream();
    Serializer ser = exec.getProcessor().newSerializer(foOut);
    ser.setOutputProperty(Serializer.Property.METHOD, "xml");
    ser.setOutputProperty(Serializer.Property.INDENT, "yes");

    t.setDestination(ser);
    t.transform();
    return foOut.toByteArray();
  } catch (SaxonApiException e) {
    throw new RuntimeException("XSLT transform failed", e);
  }
}
```

Now your **`docType`** (with optional **version**/**brand**) deterministically chooses the **XSLT** that generates the right **XSL‑FO**.

---

## 4) XSLT side: how `docType` actually shapes the FO

You can handle doc‑type logic in two ways:

### 4.1 Separate stylesheets (simplest)
**`templates/invoice/invoice-v1.xsl`:**
```xml
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:fo="http://www.w3.org/1999/XSL/Format">
  <xsl:output method="xml" indent="yes"/>

  <!-- Optional switches -->
  <xsl:param name="toc" select="'false'"/>
  <xsl:param name="watermark" select="''"/>

  <!-- Import shared fragments -->
  ../fragments/common-base.xsl

  <xsl:template match="/document[@type='invoice']">
    <fo:root>
      <!-- Page masters with invoice-specific header/footer -->
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
          <fo:block font-size="10pt" font-weight="bold">
            Invoice <xsl:value-of select="header/docId"/>
          </fo:block>
        </fo:static-content>
        <fo:static-content flow-name="xsl-region-after">
          <fo:block text-align="center" font-size="9pt">
            Page <fo:page-number/> of <fo:page-number-citation-last ref-id="doc-end"/>
          </fo:block>
        </fo:static-content>

        <fo:flow flow-name="xsl-region-body">
          <!-- Optional watermark -->
          <xsl:if test="$watermark != ''">
            <fo:block color="#cccccc" font-size="60pt" text-align="center" space-after="10pt">
              <xsl:value-of select="$watermark"/>
            </fo:block>
          </xsl:if>

          <!-- Body: complex table -->
          <xsl:call-template name="invoice-body"/>
          <fo:block id="doc-end"/>
        </fo:flow>
      </fo:page-sequence>
    </fo:root>
  </xsl:template>
</xsl:stylesheet>
```

**Key point:** Because this file is *only for `invoice`*, you’re free to define **invoice‑specific** page masters, headers/footers, and table layouts.

---

### 4.2 One master stylesheet (docType param dispatch)
**`templates/master/master-v1.xsl`:**
```xml
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:fo="http://www.w3.org/1999/XSL/Format">
  <xsl:output method="xml" indent="yes"/>
  <xsl:param name="docType" required="yes"/>
  <xsl:param name="toc" select="'false'"/>

  ../fragments/common-base.xsl
  ../fragments/invoice/invoice-body.xsl
  ../fragments/statement/statement-body.xsl
  <!-- include other doc types -->

  <xsl:template match="/document">
    <xsl:choose>
      <xsl:when test="$docType='invoice'">
        <xsl:call-template name="render-invoice"/>
      </xsl:when>
      <xsl:when test="$docType='statement'">
        <xsl:call-template name="render-statement"/>
      </xsl:when>
      <!-- others -->
      <xsl:otherwise>
        <xsl:message terminate="yes">Unsupported docType: <xsl:value-of select="$docType"/></xsl:message>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
</xsl:stylesheet>
```

And in Java, pass the parameter:
```java
t.setParameter(new QName("docType"), new XdmAtomicValue(docType.name().toLowerCase()));
```

---

## 5) How doc‑type drives common features

- **Headers/Footers**: different **page masters** per doc type; statements may require “running balance” in footer; labels often have no footer.
- **Tables**: invoices focus on itemized tables with totals; statements on transaction ledger tables with group headers and `keep-*` rules.
- **TOC**: enabled for multi‑section reports; usually disabled for invoices/labels. Pass `$toc='true'` for reports.
- **Bookmarks**: generate bookmarks per section for reports and per month for statements.
- **N‑up / Grid (labels)**: docType=label picks a template that lays out a grid using a table or absolute `block-container`.
- **Backgrounds/Certificates**: certificate doc type uses static background image and absolute containers for seals/signatures.

---

## 6) Versioning, brand, and A/B safety

- **Version**: Add `templateVersion` to the **TemplateKey** so you can update template logic without breaking older outputs.
- **Brand**: Resolve brand‑specific assets (logos, colors) by overlay includes, e.g. `fragments/brand/default/colors.xsl`.
- **Security (whitelist!)**: Never concatenate raw `docType` into file paths from the request; map to an **enum** and **known paths** (as in the router code) to avoid path traversal.

---

## 7) Error handling & observability

- If `docType` is unknown → **HTTP 400** with a clear message.
- Log the `(docType, version, brand)` and the **template path** chosen.
- Emit metrics for **transform time** and **render time** per doc type (helps capacity planning).

---

## 8) End‑to‑end mini example

**Step 1** – Payload:
```json
{ "docType":"statement", "templateVersion":"v2", "options":{"toc": true}, "data":{ /* ... */ } }
```

**Step 2** – Router selects:
```
templates/statement/statement-v2.xsl
```

**Step 3** – XSLT produces **XSL-FO** that:
- Declares a page master with **ledger-style header/footer**
- Builds a **TOC** using markers and `page-number-citation`
- Renders **transaction groups** with repeating headers

**Step 4** – FOP renders FO → **PDF**, PDFBox signs/encrypts if requested.

---

## What would you like to use?
Given your scope and the need for multiple document families, I recommend:

- **Start with Strategy A** (one XSLT per doc type), plus a small **common fragments** library.
- Add a **registry + cache** when you need versioning or brand overlays at scale.

If you want, I can scaffold a **working example** with:
- The Java router and cache,
- `invoice-v1.xsl` and `statement-v1.xsl` (with headers/footers/complex tables/TOC),
- A basic **Spring Boot** endpoint, and
- Unit tests that golden‑compare the resulting PDFs.  

Which strategy (A/B/C) shall I generate as a starter?
