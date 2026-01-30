To implement this in your Java service, you need to create a class that implements PDFBox's `SignatureInterface`. This is where the "handshake" between your document and the cryptography engine (**Bouncy Castle**) happens.

### 1. The Maven Dependencies

Add these to your `pom.xml`. Since you already use PDFBox and OpenHTMLtoPDF, you likely have the first one. Bouncy Castle provides the PKCS7 logic.

```xml
<dependencies>
    <dependency>
        <groupId>org.apache.pdfbox</groupId>
        <artifactId>pdfbox</artifactId>
        <version>3.0.0</version>
    </dependency>
    <dependency>
        <groupId>org.bouncycastle</groupId>
        <artifactId>bcmail-jdk18on</artifactId>
        <version>1.76</version>
    </dependency>
</dependencies>

```

---

### 2. The Implementation Code

This class takes the raw document bytes provided by PDFBox, wraps them in a CMS (Cryptographic Message Syntax) container using Bouncy Castle, and returns the signature block.

```java
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.SignatureInterface;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;

import java.io.InputStream;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.util.Arrays;

public class BouncyCastleSigner implements SignatureInterface {
    private final PrivateKey privateKey;
    private final Certificate[] certChain;

    public BouncyCastleSigner(PrivateKey privateKey, Certificate[] certChain) {
        this.privateKey = privateKey;
        this.certChain = certChain;
    }

    @Override
    public byte[] sign(InputStream content) {
        try {
            // 1. Setup the Bouncy Castle Signer
            List<Certificate> certList = Arrays.asList(certChain);
            JcaCertStore certs = new JcaCertStore(certList);
            CMSSignedDataGenerator gen = new CMSSignedDataGenerator();
            
            ContentSigner sha256Signer = new JcaContentSignerBuilder("SHA256withRSA")
                                            .setProvider("BC").build(privateKey);

            gen.addSignerInfoGenerator(new JcaSignerInfoGeneratorBuilder(
                    new JcaDigestCalculatorProviderBuilder().build())
                    .build(sha256Signer, (X509Certificate) certChain[0]));
            
            gen.addCertificates(certs);

            // 2. Process the PDF content (the byte range)
            CMSProcessableInputStream msg = new CMSProcessableInputStream(content);
            CMSSignedData signedData = gen.generate(msg, false);

            // 3. Return the CMS block to PDFBox
            return signedData.getEncoded();
        } catch (Exception e) {
            throw new RuntimeException("Signing failed", e);
        }
    }
}

```

---

### 3. Integrating with your Pipeline

Since your existing service generates the PDF, you can simply "pipe" its output into this signing method.

```java
public void finalizeAndSign(InputStream generatedPdf, OutputStream finalOutput) {
    try (PDDocument doc = Loader.loadPDF(generatedPdf)) {
        PDSignature signature = new PDSignature();
        signature.setFilter(PDSignature.FILTER_ADOBE_PPKLITE);
        signature.setSubFilter(PDSignature.SUBFILTER_ADBE_PKCS7_DETACHED);
        signature.setName("My Custom Service");
        signature.setReason("Agreed to Terms");

        // The "saveIncremental" call is what appends the signature 
        // without breaking the existing composition.
        doc.addSignature(signature, new BouncyCastleSigner(myKey, myCerts));
        doc.saveIncremental(finalOutput);
    }
}

```

### Why this works for you:

* **Role of Bouncy Castle:** It takes the `InputStream content` (which PDFBox identifies as the document bytes to be protected) and turns it into a `CMSSignedData` object. This object is the "seal."
* **Role of `saveIncremental`:** It ensures that your **AcroForm** data or **OpenHTMLtoPDF** layout is preserved. PDFBox writes the original file, then appends the signature block at the end. If you used a standard `save()`, it would re-index the file and invalidate the signature you just created.

**Would you like me to explain how to store your Private Keys securely (e.g., in a KeyStore or Cloud Vault) so they aren't hardcoded in your Java service?**
