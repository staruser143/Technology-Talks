That's a common challenge when bridging complex data structures (like nested arrays and objects) with the flat nature of AcroForm fields in PDF templates. Since PDFBox works directly with AcroForm field names, you can't use the simple dot-notation or array indexing that Freemarker allows.
Here is the recommended approach to handle nested arrays and objects when filling PDF AcroForm templates using PDFBox.
💡 Key Approach: Data Flattening and Convention-Based Mapping
The most robust solution is to flatten your nested data into a simple map of key-value pairs that directly correspond to your AcroForm field names. You'll need to define a convention for naming your PDF fields to accommodate array indexes and nested structures.
1. 📋 Define a Naming Convention for AcroForm Fields
The PDF form fields need to be named in a predictable way to represent your nested data.
| Data Structure | Example Data Field | AcroForm Field Name Convention |
|---|---|---|
| Simple Object | user.name | user_name or userName |
| Array Item | items[0].product | items_0_product |
| Nested Array | sections[1].photos[2].url | sections_1_photos_2_url |
> 📝 Recommendation: Use an underscore (_) as a consistent separator instead of dots (.) or brackets ([]) in your PDF field names, as dots can have special meaning in AcroForm field hierarchy, and brackets aren't standard.
> 
2. 🧹 Flatten the Nested Data (Pre-processing)
You need a recursive function to convert your source data (objects and arrays) into a simple, flat map of (AcroFormFieldName, Value).
Example Flattening Logic
Assume your source data looks like this (in JSON):
{
  "user": {
    "id": 101,
    "name": "Alice"
  },
  "items": [
    { "product": "Pencil", "qty": 5 },
    { "product": "Eraser", "qty": 1 }
  ]
}

The flattening function should generate a Map<String, String> like this:
| Key (AcroForm Field Name) | Value |
|---|---|
| user_id | "101" |
| user_name | "Alice" |
| items_0_product | "Pencil" |
| items_0_qty | "5" |
| items_1_product | "Eraser" |
| items_1_qty | "1" |
How to Implement the Flattening Function
You can write a simple recursive function (e.g., in Java, assuming your data is a Map<String, Object>):
public Map<String, String> flattenData(Map<String, Object> sourceData) {
    Map<String, String> flatMap = new HashMap<>();
    flatten("", sourceData, flatMap);
    return flatMap;
}

private void flatten(String currentPath, Object data, Map<String, String> flatMap) {
    if (data instanceof Map) {
        Map<String, Object> map = (Map<String, Object>) data;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String newPath = currentPath.isEmpty() ? entry.getKey() : currentPath + "_" + entry.getKey();
            flatten(newPath, entry.getValue(), flatMap);
        }
    } else if (data instanceof List) {
        List<Object> list = (List<Object>) data;
        for (int i = 0; i < list.size(); i++) {
            String newPath = currentPath + "_" + i;
            flatten(newPath, list.get(i), flatMap);
        }
    } else if (data != null) {
        // Base case: store the value using the constructed path as the key
        flatMap.put(currentPath, data.toString());
    }
}

3. 💾 Apply Data to the PDF (PDFBox)
Once you have your flatMap, the PDFBox part becomes straightforward:
 * Load the PDF template.
 * Get the PDAcroForm.
 * Iterate over your flatMap.
 * For each key-value pair, look up the field by the key (which is now the AcroForm field name) and set its value.
<!-- end list -->
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;

// ...

public void fillPdf(String templatePath, String outputPath, Map<String, String> flatData) throws IOException {
    try (PDDocument pdfDocument = PDDocument.load(new File(templatePath))) {
        PDAcroForm acroForm = pdfDocument.getDocumentCatalog().getAcroForm();

        // 1. Iterate over the flattened data
        for (Map.Entry<String, String> entry : flatData.entrySet()) {
            String fieldName = entry.getKey();
            String fieldValue = entry.getValue();

            // 2. Look up the field by the flattened name
            PDField field = acroForm.getField(fieldName);

            if (field != null) {
                // 3. Set the value
                field.setValue(fieldValue);
            } else {
                System.out.println("Warning: AcroForm field not found for key: " + fieldName);
            }
        }

        pdfDocument.save(outputPath);
    }
}

This approach decouples your complex data structure from the flat field structure of AcroForms, making your overall system much more predictable and maintainable.
Would you like me to elaborate on the designing of the PDF form template in Adobe Acrobat (or similar tool) to match this naming convention?
