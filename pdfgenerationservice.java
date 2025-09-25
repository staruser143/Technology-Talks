// PdfGenerationService.java
package com.example.fopdemo.service;

import org.apache.fop.apps.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class PdfGenerationService {

    public byte[] generatePdfFromXml(String xmlContent, String xsltPath) throws Exception {
        // 1. Setup FOP
        FopFactory fopFactory = FopFactory.newInstance(new File("fop-config.xml").toURI()); // optional config
        // Or use default config:
        // FopFactory fopFactory = FopFactory.newInstance();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, out);

        // 2. Setup Transformer
        TransformerFactory factory = TransformerFactory.newInstance();
        Source xslt = new StreamSource(new ClassPathResource(xsltPath).getInputStream());
        Transformer transformer = factory.newTransformer(xslt);

        // 3. Set input (XML as String)
        Source src = new StreamSource(new StringReader(xmlContent));

        // 4. Result
        Result res = new SAXResult(fop.getDefaultHandler());

        // 5. Transform (XML + XSLT → XSL-FO → PDF)
        transformer.transform(src, res);

        return out.toByteArray();
    }
}