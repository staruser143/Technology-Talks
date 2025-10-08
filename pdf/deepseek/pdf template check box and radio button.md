When working with checkboxes and radio buttons in PDFBox, you need to handle them differently from regular text fields. Here's how to properly set values for these field types:

1. Understanding Checkboxes and Radio Buttons

Checkboxes:

· Have two states: checked (on) and unchecked (off)
· The "on" value is typically the field name or a specific value like "Yes"
· The "off" value is typically not set or null

Radio Buttons:

· Group of fields with the same name but different values
· Only one can be selected in a group

2. Enhanced PDFBox Service for Form Fields

```java
@Service
public class PdfBoxFormService {
    
    private static final Logger logger = LoggerFactory.getLogger(PdfBoxFormService.class);
    
    public byte[] fillPdfForm(byte[] templateBytes, Map<String, Object> formData) {
        try (PDDocument document = PDDocument.load(templateBytes);
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            
            PDAcroForm acroForm = document.getDocumentCatalog().getAcroForm();
            
            if (acroForm != null) {
                for (Map.Entry<String, Object> entry : formData.entrySet()) {
                    setFormFieldValue(acroForm, entry.getKey(), entry.getValue());
                }
            }
            
            document.save(baos);
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error filling PDF form", e);
        }
    }
    
    private void setFormFieldValue(PDAcroForm acroForm, String fieldName, Object value) {
        try {
            PDField field = acroForm.getField(fieldName);
            if (field == null) {
                logger.warn("Field not found: {}", fieldName);
                return;
            }
            
            if (field instanceof PDCheckBox) {
                setCheckboxValue((PDCheckBox) field, value);
            } else if (field instanceof PDRadioButton) {
                setRadioButtonValue(acroForm, fieldName, value);
            } else {
                // Handle text fields, dropdowns, etc.
                setStandardFieldValue(field, value);
            }
            
        } catch (Exception e) {
            logger.error("Error setting value for field: {}", fieldName, e);
        }
    }
    
    private void setCheckboxValue(PDCheckBox checkbox, Object value) {
        try {
            boolean shouldBeChecked = convertToBoolean(value);
            
            if (shouldBeChecked) {
                // Check the checkbox by setting its "on" value
                checkbox.check();
            } else {
                // Uncheck the checkbox
                checkbox.unCheck();
            }
            
        } catch (Exception e) {
            logger.error("Error setting checkbox value for field: {}", checkbox.getFullyQualifiedName(), e);
        }
    }
    
    private void setRadioButtonValue(PDAcroForm acroForm, String fieldName, Object value) {
        try {
            // For radio buttons, we need to set the value that matches one of the options
            String stringValue = value != null ? value.toString() : "";
            
            // Get all radio buttons in the group (same field name)
            List<PDField> radioFields = acroForm.getFields().stream()
                    .filter(f -> f.getFullyQualifiedName().equals(fieldName))
                    .collect(Collectors.toList());
            
            for (PDField radioField : radioFields) {
                if (radioField instanceof PDButton) {
                    PDButton radioButton = (PDButton) radioField;
                    
                    // Check if this radio button option matches the value we want to set
                    String optionValue = getRadioButtonValue(radioButton);
                    if (stringValue.equals(optionValue)) {
                        radioButton.setValue(stringValue);
                    }
                }
            }
            
        } catch (Exception e) {
            logger.error("Error setting radio button value for field: {}", fieldName, e);
        }
    }
    
    private void setStandardFieldValue(PDField field, Object value) {
        try {
            if (value != null) {
                field.setValue(value.toString());
            } else {
                field.setValue("");
            }
        } catch (Exception e) {
            logger.error("Error setting standard field value for field: {}", field.getFullyQualifiedName(), e);
        }
    }
    
    private boolean convertToBoolean(Object value) {
        if (value == null) return false;
        
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        
        String stringValue = value.toString().toLowerCase();
        return stringValue.equals("true") || 
               stringValue.equals("yes") || 
               stringValue.equals("on") || 
               stringValue.equals("1") ||
               stringValue.equals("checked");
    }
    
    private String getRadioButtonValue(PDButton radioButton) {
        try {
            // Radio button value is typically the export value of the widget
            if (radioButton.getWidgets().size() > 0) {
                PDAnnotationWidget widget = radioButton.getWidgets().get(0);
                // You might need to extract the export value from the widget's appearance stream
                // This is a simplified approach - you may need to adjust based on your PDF
                return radioButton.getValue();
            }
        } catch (Exception e) {
            logger.error("Error getting radio button value", e);
        }
        return radioButton.getValue();
    }
}
```

3. Utility Service to Inspect Form Fields

```java
@Service
public class PdfFormInspectorService {
    
    public Map<String, Object> inspectFormFields(byte[] templateBytes) {
        Map<String, Object> fieldInfo = new HashMap<>();
        
        try (PDDocument document = PDDocument.load(templateBytes)) {
            PDAcroForm acroForm = document.getDocumentCatalog().getAcroForm();
            
            if (acroForm != null) {
                for (PDField field : acroForm.getFields()) {
                    fieldInfo.put(field.getFullyQualifiedName(), getFieldInfo(field));
                }
            }
            
        } catch (Exception e) {
            throw new RuntimeException("Error inspecting PDF form", e);
        }
        
        return fieldInfo;
    }
    
    private Map<String, Object> getFieldInfo(PDField field) {
        Map<String, Object> info = new HashMap<>();
        
        info.put("type", field.getClass().getSimpleName());
        info.put("fullyQualifiedName", field.getFullyQualifiedName());
        
        if (field instanceof PDCheckBox) {
            info.put("fieldType", "checkbox");
            info.put("currentValue", field.getValueAsString());
        } else if (field instanceof PDRadioButton) {
            info.put("fieldType", "radio");
            info.put("currentValue", field.getValueAsString());
            info.put("options", getRadioButtonOptions((PDRadioButton) field));
        } else if (field instanceof PDComboBox) {
            info.put("fieldType", "combobox");
            PDComboBox combo = (PDComboBox) field;
            info.put("options", getOptions(combo));
        } else if (field instanceof PDListBox) {
            info.put("fieldType", "listbox");
            PDListBox list = (PDListBox) field;
            info.put("options", getOptions(list));
        } else {
            info.put("fieldType", "text");
        }
        
        return info;
    }
    
    private List<String> getRadioButtonOptions(PDRadioButton radioButton) {
        List<String> options = new ArrayList<>();
        try {
            // This is a simplified approach - you may need more complex logic
            // to extract all possible values for radio buttons
            options.add(radioButton.getValue());
        } catch (Exception e) {
            logger.error("Error getting radio button options", e);
        }
        return options;
    }
    
    private List<String> getOptions(PDChoice choice) {
        List<String> options = new ArrayList<>();
        try {
            for (String option : choice.getOptions()) {
                options.add(option);
            }
        } catch (Exception e) {
            logger.error("Error getting choice options", e);
        }
        return options;
    }
}
```

4. Enhanced Controller

```java
@RestController
@RequestMapping("/api/pdf")
public class PdfFormController {
    
    @Autowired
    private PdfBoxFormService pdfFormService;
    
    @Autowired
    private PdfFormInspectorService formInspectorService;
    
    @PostMapping("/fill-form")
    public ResponseEntity<byte[]> fillPdfForm(
            @RequestParam("file") MultipartFile templateFile,
            @RequestBody Map<String, Object> formData) {
        
        try {
            byte[] templateBytes = templateFile.getBytes();
            byte[] filledPdf = pdfFormService.fillPdfForm(templateBytes, formData);
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"filled-form.pdf\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(filledPdf);
                    
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/inspect")
    public ResponseEntity<Map<String, Object>> inspectPdfForm(
            @RequestParam("file") MultipartFile templateFile) {
        
        try {
            byte[] templateBytes = templateFile.getBytes();
            Map<String, Object> fieldInfo = formInspectorService.inspectFormFields(templateBytes);
            
            return ResponseEntity.ok(fieldInfo);
                    
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
```

5. Usage Examples

Sample Data for Forms with Checkboxes and Radio Buttons

```json
{
  "templateName": "application-form",
  "fieldValues": {
    // Text fields
    "firstName": "John",
    "lastName": "Doe",
    
    // Checkboxes - use boolean or string values
    "agreeToTerms": true,
    "newsletterSubscription": "yes",
    "marketingEmails": "on",
    
    // Radio buttons - use the exact value of the option you want to select
    "gender": "male",
    "employmentStatus": "employed",
    
    // Other field types
    "country": "USA",
    "comments": "This is a comment"
  }
}
```

Client Usage

```bash
# Inspect form fields first to understand the structure
curl -X POST "http://localhost:8080/api/pdf/inspect" \
  -F "file=@/path/to/form-template.pdf"

# Fill the form with data
curl -X POST "http://localhost:8080/api/pdf/fill-form" \
  -F "file=@/path/to/form-template.pdf" \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "agreeToTerms": true,
    "newsletterSubscription": "yes",
    "gender": "male",
    "employmentStatus": "employed"
  }' \
  --output filled-form.pdf
```

Key Points to Remember:

1. Checkboxes: Use boolean values (true/false) or strings like "yes", "on", "1"
2. Radio Buttons: Use the exact value of the option you want to select
3. Inspect First: Always inspect the PDF form first to understand field names and types
4. Field Names: Use the fully qualified field names as they appear in the PDF
5. Error Handling: Some PDF forms may have complex field structures that require additional handling

This approach should handle most common PDF forms with checkboxes and radio buttons effectively.