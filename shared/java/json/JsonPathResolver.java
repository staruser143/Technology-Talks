package shared.json;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Shared utility for resolving dot-separated JSON paths against a Jackson JsonNode tree.
 *
 * Consolidates the duplicated resolveJsonPath() method from:
 *   try/AdvancedConfigDrivenPdfMapper.java (lines 105-122) - uses 0-based array indexing
 *   try/HybridPdfFormFiller.java (lines 76-93)             - uses 1-based array indexing
 *
 * Callers choose their indexing convention via the `zeroBased` parameter.
 */
public final class JsonPathResolver {

    private JsonPathResolver() {}

    /**
     * Resolves a dot-separated path against a JsonNode tree.
     *
     * @param root      the root node to traverse
     * @param pathParts the path segments (e.g., "member.addresses.0.city".split("\\."))
     * @param zeroBased true for 0-based array indexing (AdvancedConfigDrivenPdfMapper style),
     *                  false for 1-based (HybridPdfFormFiller style)
     * @return the resolved node, or null if any segment is missing
     */
    public static JsonNode resolve(JsonNode root, String[] pathParts, boolean zeroBased) {
        JsonNode current = root;
        for (String part : pathParts) {
            if (current == null) return null;

            if (part.matches("\\d+")) {
                int index = Integer.parseInt(part);
                if (!zeroBased) {
                    index = index - 1;
                }
                if (current.isArray() && index >= 0 && index < current.size()) {
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

    /**
     * Resolves with 0-based array indexing (default convention).
     */
    public static JsonNode resolve(JsonNode root, String[] pathParts) {
        return resolve(root, pathParts, true);
    }
}
