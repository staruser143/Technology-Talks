import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.pdmodel.interactive.form.PDNonTerminalField;

import java.io.File;
import java.util.*;

public class HybridPdfFormFiller {

    public static void main(String[] args) throws Exception {
        File pdfFile = new File("template.pdf");
        File jsonFile = new File("data.json");
        File mappingFile = new File("mapping.json");

        PDDocument document = PDDocument.load(pdfFile);
        PDAcroForm acroForm = document.getDocumentCatalog().getAcroForm();

        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(jsonFile);

        Map<String, String> customMapping = new HashMap<>();
        if (mappingFile.exists()) {
            JsonNode mappingNode = mapper.readTree(mappingFile);
            Iterator<String> keys = mappingNode.fieldNames();
            while (keys.hasNext()) {
                String jsonPath = keys.next();
                customMapping.put(jsonPath, mappingNode.get(jsonPath).asText());
            }
        }

        for (PDField field : acroForm.getFields()) {
            processFieldRecursively(field, rootNode, customMapping);
        }

        acroForm.flatten(); // Optional: make fields non-editable
        document.save("filled_form.pdf");
        document.close();
    }

    public static void processFieldRecursively(PDField field, JsonNode rootNode, Map<String, String> customMapping) {
        if (field instanceof PDNonTerminalField) {
            for (PDField child : ((PDNonTerminalField) field).getChildren()) {
                processFieldRecursively(child, rootNode, customMapping);
            }
        } else {
            String fieldName = field.getFullyQualifiedName();

            // Check custom mapping
            String jsonPath = customMapping.entrySet().stream()
                .filter(entry -> entry.getValue().equals(fieldName))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);

            JsonNode valueNode = null;

            if (jsonPath != null) {
                valueNode = resolveJsonPath(rootNode, jsonPath.split("\\."));
            } else {
                valueNode = resolveJsonPath(rootNode, fieldName.split("\\."));
            }

            if (valueNode != null && valueNode.isValueNode()) {
                try {
                    field.setValue(valueNode.asText());
                } catch (Exception e) {
                    System.err.println("Failed to set value for field: " + fieldName);
                }
            }
        }
    }

    public static JsonNode resolveJsonPath(JsonNode root, String[] pathParts) {
        JsonNode current = root;
        for (String part : pathParts) {
            if (current == null) return null;

            if (part.matches("\\d+")) {
                int index = Integer.parseInt(part) - 1;
                if (current.isArray() && index < current.size()) {
                    current = current.get(index);
                } else {
                    return null;
                }
            } else {
                current = current.get(part);
            }
        }
        return current;
    }
}
