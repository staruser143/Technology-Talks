// src/main/java/com/yourcompany/EnhancedMappingResolver.java
package com.yourcompany;

import com.yourcompany.model.*;
import org.apache.commons.jexl3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

public class EnhancedMappingResolver {

    private static final Logger log = LoggerFactory.getLogger(EnhancedMappingResolver.class);
    private final JexlEngine jexl = new JexlBuilder().create();

    public Map<String, Object> resolve(Map<String, Object> sourceData, String mappingSpecJson) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        EnhancedMappingSpec spec = mapper.readValue(mappingSpecJson, EnhancedMappingSpec.class);

        Map<String, Object> workingData = new HashMap<>(sourceData);

        // Validate required fields
        validateRequiredFields(workingData, spec);

        // Process computed fields
        if (spec.getComputedFields() != null) {
            processComputedFields(spec.getComputedFields(), workingData);
        }

        Map<String, Object> result = new HashMap<>();

        // Process simple field mappings
        if (spec.getMappings() != null) {
            for (FieldMapping mapping : spec.getMappings()) {
                processFieldMapping(mapping, workingData, result);
            }
        }

        // Process list mappings
        if (spec.getListMappings() != null) {
            for (ListMapping listMapping : spec.getListMappings()) {
                processListMapping(listMapping, workingData, result);
            }
        }

        // Process conditional blocks
        if (spec.getConditionalBlocks() != null) {
            for (ConditionalBlock block : spec.getConditionalBlocks()) {
                processConditionalBlock(block, workingData, result);
            }
        }

        // Process binary mappings (images, QR codes)
        if (spec.getBinaryMappings() != null) {
            for (BinaryMapping binary : spec.getBinaryMappings()) {
                processBinaryMapping(binary, workingData, result);
            }
        }

        return result;
    }

    private void validateRequiredFields(Map<String, Object> data, EnhancedMappingSpec spec) {
        if (spec.getRequiredFields() != null) {
            for (String field : spec.getRequiredFields()) {
                if (getNestedValue(data, field) == null) {
                    throw new IllegalArgumentException("Required field missing: " + field);
                }
            }
        }
    }

    private void processComputedFields(List<ComputedField> computedFields, Map<String, Object> data) {
        JexlContext context = new MapContext();
        data.forEach(context::set);

        for (ComputedField field : computedFields) {
            try {
                JexlExpression e = jexl.createExpression(field.getExpression());
                Object value = e.evaluate(context);
                data.put(field.getName(), value);
            } catch (Exception e) {
                log.warn("Failed to compute field: " + field.getName(), e);
            }
        }
    }

    private void processFieldMapping(FieldMapping mapping, Map<String, Object> data, Map<String, Object> result) {
        // Skip if condition not met
        if (mapping.getCondition() != null && !evaluateCondition(mapping.getCondition(), data)) {
            return;
        }

        Object value = getNestedValue(data, mapping.getSourceField());
        if (value == null && mapping.getDefaultValue() != null) {
            value = mapping.getDefaultValue();
        }

        if (value != null && mapping.getFormatter() != null) {
            value = applyFormatter(value, mapping.getFormatter());
        }

        if (value != null) {
            result.put(mapping.getTargetPlaceholder(), value);
        }
    }

    private void processListMapping(ListMapping listMapping, Map<String, Object> data, Map<String, Object> result) {
        Object listObj = getNestedValue(data, listMapping.getSourceField());
        if (!(listObj instanceof List)) {
            log.warn("Expected list for: " + listMapping.getSourceField());
            return;
        }

        List<Map<String, Object>> items = (List<Map<String, Object>>) listObj;
        List<String> itemResults = new ArrayList<>();

        for (Map<String, Object> item : items) {
            Map<String, Object> itemContext = new HashMap<>(data);
            itemContext.putAll(item); // Make item fields available

            Map<String, Object> itemResult = new HashMap<>();
            if (listMapping.getItemMappings() != null) {
                for (FieldMapping itemMapping : listMapping.getItemMappings()) {
                    processFieldMapping(itemMapping, itemContext, itemResult);
                }
            }

            // If itemTemplate is provided, render it
            if (listMapping.getItemTemplate() != null) {
                String rendered = renderTemplate(listMapping.getItemTemplate(), itemResult);
                itemResults.add(rendered);
            } else {
                // Assume template engine handles block — just provide data
                // This mode requires template engine (FreeMarker/Handlebars) to process {{#each}}
                result.put(listMapping.getTargetPlaceholder(), items); // Pass raw list
                return;
            }
        }

        result.put(listMapping.getTargetPlaceholder(), String.join("", itemResults));
    }

    private void processConditionalBlock(ConditionalBlock block, Map<String, Object> data, Map<String, Object> result) {
        boolean conditionMet = block.getCondition() == null || evaluateCondition(block.getCondition(), data);

        if (conditionMet) {
            if (block.getContent() != null) {
                result.put(block.getBlockPlaceholder(), block.getContent());
                if (block.getEndPlaceholder() != null) {
                    result.put(block.getEndPlaceholder(), "");
                }
            }

            if (block.getMappings() != null) {
                for (FieldMapping mapping : block.getMappings()) {
                    processFieldMapping(mapping, data, result);
                }
            }
        } else {
            // Remove placeholders if condition not met
            result.put(block.getBlockPlaceholder(), "");
            if (block.getEndPlaceholder() != null) {
                result.put(block.getEndPlaceholder(), "");
            }
        }
    }

    private void processBinaryMapping(BinaryMapping binary, Map<String, Object> data, Map<String, Object> result) {
        Object value = getNestedValue(data, binary.getSourceField());
        if (value == null && binary.getFallback() != null) {
            value = binary.getFallback();
        }

        if (value == null) return;

        try {
            String dataUri = switch (binary.getType()) {
                case IMAGE -> convertImageUrlToDataUri(value.toString());
                case QRCODE -> generateQrCodeDataUri(value.toString(), binary.getSize());
            };
            result.put(binary.getTargetPlaceholder(), dataUri);
        } catch (Exception e) {
            log.warn("Failed to process binary mapping: " + binary.getTargetPlaceholder(), e);
            if (binary.getFallback() != null) {
                result.put(binary.getTargetPlaceholder(), binary.getFallback());
            }
        }
    }

    private boolean evaluateCondition(String condition, Map<String, Object> data) {
        try {
            JexlContext context = new MapContext();
            data.forEach(context::set);
            JexlExpression e = jexl.createExpression(condition);
            Object result = e.evaluate(context);
            return Boolean.TRUE.equals(result);
        } catch (Exception e) {
            log.warn("Failed to evaluate condition: " + condition, e);
            return false;
        }
    }

    private Object getNestedValue(Map<String, Object> data, String fieldPath) {
        if (fieldPath == null || fieldPath.trim().isEmpty()) return null;

        String[] parts = fieldPath.split("\\.");
        Object current = data;

        for (String part : parts) {
            if (current instanceof Map) {
                current = ((Map<?, ?>) current).get(part);
            } else {
                return null;
            }
        }
        return current;
    }

    private Object applyFormatter(Object value, String formatterSpec) {
        try {
            if (formatterSpec.startsWith("date:")) {
                String pattern = formatterSpec.substring(5);
                SimpleDateFormat sdf = new SimpleDateFormat(pattern);
                if (value instanceof Date) {
                    return sdf.format(value);
                } else if (value instanceof Long || value instanceof String) {
                    long time = value instanceof Long ? (Long) value : Long.parseLong((String) value);
                    return sdf.format(new Date(time));
                }
            } else if (formatterSpec.startsWith("currency:")) {
                String currencyCode = formatterSpec.substring(9);
                DecimalFormat df = new DecimalFormat("#,##0.00");
                if (value instanceof Number) {
                    return df.format(value) + " " + currencyCode;
                }
            } else {
                return switch (formatterSpec) {
                    case "UPPERCASE" -> value.toString().toUpperCase();
                    case "LOWERCASE" -> value.toString().toLowerCase();
                    case "TITLECASE" -> toTitleCase(value.toString());
                    default -> value.toString();
                };
            }
        } catch (Exception e) {
            log.warn("Formatting failed for: " + value + " with " + formatterSpec, e);
        }
        return value;
    }

    private String toTitleCase(String input) {
        if (input == null || input.isEmpty()) return input;
        StringBuilder sb = new StringBuilder();
        boolean nextTitleCase = true;
        for (char c : input.toCharArray()) {
            if (Character.isSpaceChar(c)) {
                nextTitleCase = true;
            } else if (nextTitleCase) {
                c = Character.toTitleCase(c);
                nextTitleCase = false;
            }
            sb.append(c);
        }
        return sb.toString();
    }

    private String convertImageUrlToDataUri(String url) throws IOException {
        BufferedImage img = ImageIO.read(new URL(url));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, "png", baos);
        byte[] bytes = baos.toByteArray();
        return "image/png;base64," + Base64.getEncoder().encodeToString(bytes);
    }

    private String generateQrCodeDataUri(String content, String sizeSpec) throws Exception {
        int width = 200, height = 200;
        if (sizeSpec != null && sizeSpec.contains("x")) {
            String[] parts = sizeSpec.split("x");
            width = Integer.parseInt(parts[0]);
            height = Integer.parseInt(parts[1]);
        }

        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, width, height);
        BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "png", baos);
        byte[] bytes = baos.toByteArray();

        return "image/png;base64," + Base64.getEncoder().encodeToString(bytes);
    }

    private String renderTemplate(String template, Map<String, Object> data) {
        // Simple string replacement for now - replace with FreeMarker if needed
        String result = template;
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            result = result.replace(entry.getKey(), entry.getValue() != null ? entry.getValue().toString() : "");
        }
        return result;
    }

    // Helper class for Jackson
    private static class ObjectMapper extends com.fasterxml.jackson.databind.ObjectMapper {
        public ObjectMapper() {
            // Configure as needed
        }
    }
}