// PdfController.java
package com.example.fopdemo.controller;

import com.example.fopdemo.service.PdfGenerationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pdf")
public class PdfController {

    @Autowired
    private PdfGenerationService pdfService;

    @PostMapping("/invoice")
    public ResponseEntity<byte[]> generateInvoicePdf(@RequestBody String xmlData) {
        try {
            byte[] pdfBytes = pdfService.generatePdfFromXml(xmlData, "templates/invoice.xsl");

            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", "inline; filename=invoice.pdf");
            headers.add("Content-Type", "application/pdf");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}