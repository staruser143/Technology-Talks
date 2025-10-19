import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AdvancedConfigDrivenPdfMapper {

    private final ObjectMapper mapper = new ObjectMapper();

    public void fillPdf(PDDocument document, JsonNode payload, JsonNode config) throws Exception {
        PDAcroForm acroForm = document.getDocumentCatalog().getAcroForm();
        if (acroForm == null) return;

        String sourcePath = config.get("source").asText();
        String criteriaFieldPath = config.get("criteriaField").asText();
        JsonNode criteriaValues = config.get("criteriaValues");

        JsonNode sourceArray = resolveJsonPath(payload, sourcePath.split("\\."));
        if (sourceArray == null || !sourceArray.isArray()) return;

        for (int i = 0; i < sourceArray.size(); i++) {
            JsonNode item = sourceArray.get(i);
            JsonNode criteriaValueNode = resolveJsonPath(item, criteriaFieldPath.split("\\."));
            if (criteriaValueNode == null || !criteriaValueNode.isValueNode()) continue;

            String criteriaValue = criteriaValueNode.asText();
            JsonNode fieldMappings = criteriaValues.get(criteriaValue);
            if (fieldMappings == null || fieldMappings.get("fields") == null) continue;

            JsonNode fields = fieldMappings.get("fields");

            for (Iterator<String> it = fields.fieldNames(); it.hasNext(); ) {
                String jsonPath = it.next();
                String fieldTemplate = fields.get(jsonPath).asText();

                List<String> resolvedPaths = resolveFilteredPaths(item, jsonPath);
                for (int j = 0; j < resolvedPaths.size(); j++) {
                    JsonNode valueNode = resolveJsonPath(item, resolvedPaths.get(j).split("\\."));
                    if (valueNode != null && valueNode.isValueNode()) {
                        String fieldName = fieldTemplate
                                .replace("{index}", String.valueOf(i + 1))
                                .replace("{planIndex}", String.valueOf(j + 1))
                                .replace("{addressIndex}", String.valueOf(j + 1))
                                .replace("{criteriaValue}", criteriaValue);

                        PDField field = acroForm.getField(fieldName);
                        if (field != null) {
                            field.setValue(valueNode.asText());
                        }
                    }
                }
            }
        }
    }

    private List<String> resolveFilteredPaths(JsonNode root, String path) {
        List<String> paths = new ArrayList<>();
        Pattern pattern = Pattern.compile("(.+?)\\[\\?(.+?)==\"(.+?)\"\\]\\.(.+)");
        Matcher matcher = pattern.matcher(path);

        if (matcher.matches()) {
            String arrayPath = matcher.group(1);
            String filterField = matcher.group(2);
            String filterValue = matcher.group(3);
            String subPath = matcher.group(4);

            JsonNode arrayNode = resolveJsonPath(root, arrayPath.split("\\."));
            if (arrayNode != null && arrayNode.isArray()) {
                for (int i = 0; i < arrayNode.size(); i++) {
                    JsonNode element = arrayNode.get(i);
                    JsonNode filterNode = element.get(filterField);
                    if (filterNode != null && filterNode.asText().equals(filterValue)) {
                        paths.add(arrayPath + "." + i + "." + subPath);
                    }
                }
            }
        } else {
            paths = resolvePaths(root, path);
        }

        return paths;
    }

    private List<String> resolvePaths(JsonNode root, String path) {
        List<String> paths = new ArrayList<>();
        if (path.contains("[]")) {
            String[] parts = path.split("\\[]");
            JsonNode arrayNode = resolveJsonPath(root, parts[0].split("\\."));
            if (arrayNode != null && arrayNode.isArray()) {
                for (int i = 0; i < arrayNode.size(); i++) {
                    paths.add(parts[0] + "." + i + "." + parts[1]);
                }
            }
        } else {
            paths.add(path);
        }
        return paths;
    }

    private JsonNode resolveJsonPath(JsonNode root, String[] pathParts) {
        JsonNode current = root;
        for (String part : pathParts) {
            if (current == null) return null;
            if (part.matches("\\d+")) {
                int index = Integer.parseInt(part);
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
