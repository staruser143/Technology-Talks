// =========================
// Part 2 — FULL GENERIC MAPPING ENGINE (Option A)
// Java 17 — No domain references — JSONata4Java
// Package: com.example.mapping
// =========================

package com.example.mapping;

// --------------------------------------------------
// JsonataEvaluator Interface
// --------------------------------------------------
public interface JsonataEvaluator {
    Object evaluate(String expression, Object input);
}

// --------------------------------------------------
// Jsonata4JavaEvaluator (uses JSONata4Java)
// --------------------------------------------------
import com.api.jsonata4java.Expression;
import com.api.jsonata4java.expressions.JsonataException;
import com.api.jsonata4java.expressions.PathExpression;
import com.api.jsonata4java.mapper.JacksonMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Jsonata4JavaEvaluator implements JsonataEvaluator {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public Object evaluate(String expression, Object input) {
        try {
            JsonNode inputNode = MAPPER.valueToTree(input);
            Expression expr = Expression.jsonata(expression);
            JsonNode result = expr.evaluate(inputNode);
            return JacksonMapper.convertJsonNode(result);
        } catch (JsonataException ex) {
            throw new RuntimeException("JSONata evaluation failed: " + expression, ex);
        }
    }
}

// --------------------------------------------------
// JsonataExpressionCache
// --------------------------------------------------
import java.util.concurrent.ConcurrentHashMap;

public class JsonataExpressionCache {
    private final ConcurrentHashMap<String, Expression> cache = new ConcurrentHashMap<>();

    public Expression getOrCompile(String expr) {
        return cache.computeIfAbsent(expr, Expression::jsonata);
    }
}

// --------------------------------------------------
// Config Models
// --------------------------------------------------
import java.util.List;
import java.util.Map;

public class MappingConfig {
    private String templateName;
    private String templatePath;
    private List<MappingGroup> groups;

    public String getTemplateName() { return templateName; }
    public String getTemplatePath() { return templatePath; }
    public List<MappingGroup> getGroups() { return groups; }
}

public class MappingGroup {
    private String name;
    private String source;
    private SuffixRules suffix;
    private List<FieldMapping> fields;

    public String getName() { return name; }
    public String getSource() { return source; }
    public SuffixRules getSuffix() { return suffix; }
    public List<FieldMapping> getFields() { return fields; }
}

public class FieldMapping {
    private String target;
    private String expr;

    public String getTarget() { return target; }
    public String getExpr() { return expr; }
}

public enum SuffixMode {
    none,
    index
}

public class SuffixRules {
    private SuffixMode mode = SuffixMode.none;
    private Integer start = 1;

    public SuffixMode getMode() { return mode; }
    public Integer getStart() { return start; }
}

// --------------------------------------------------
// FieldNameBuilder
// --------------------------------------------------
public class FieldNameBuilder {

    public static String build(String base, int index, SuffixRules rules) {
        if (rules == null || rules.getMode() == SuffixMode.none) {
            return base;
        }

        int start = (rules.getStart() != null) ? rules.getStart() : 1;
        int computedIndex = start + index;

        if (rules.getMode() == SuffixMode.index) {
            return base + "_" + computedIndex;
        }

        return base;
    }
}

// --------------------------------------------------
// MappingContext
// --------------------------------------------------
import java.util.HashMap;

public class MappingContext {
    private final JsonataEvaluator evaluator;
    private final MappingConfig config;
    private final Map<String, Object> sourceData;

    public MappingContext(JsonataEvaluator evaluator, MappingConfig config, Map<String, Object> sourceData) {
        this.evaluator = evaluator;
        this.config = config;
        this.sourceData = sourceData;
    }

    public JsonataEvaluator evaluator() { return evaluator; }
    public MappingConfig config() { return config; }
    public Map<String, Object> source() { return sourceData; }
}

// --------------------------------------------------
// MappingResult
// --------------------------------------------------
import java.util.LinkedHashMap;
import java.util.Map;

public class MappingResult {

    private final Map<String, Object> flat = new LinkedHashMap<>();

    public void put(String field, Object value) {
        flat.put(field, value);
    }

    public Map<String, Object> getFlat() {
        return flat;
    }
}

// --------------------------------------------------
// MappingEngine (MAIN ENGINE)
// --------------------------------------------------
import java.util.Collections;

public class MappingEngine {

    public MappingResult process(MappingContext ctx) {
        MappingResult result = new MappingResult();

        for (MappingGroup group : ctx.config().getGroups()) {

            Object groupInput = (group.getSource() == null)
                    ? ctx.source()
                    : ctx.evaluator().evaluate(group.getSource(), ctx.source());

            if (groupInput == null) continue;

            if (groupInput instanceof List<?> list) {
                int idx = 0;
                for (Object item : list) {
                    applyGroup(ctx, group, item, idx, result);
                    idx++;
                }
            } else {
                applyGroup(ctx, group, groupInput, 0, result);
            }
        }
        return result;
    }

    private void applyGroup(MappingContext ctx, MappingGroup group, Object item, int index, MappingResult result) {
        for (FieldMapping fm : group.getFields()) {
            Object value = ctx.evaluator().evaluate(fm.getExpr(), item);

            String finalName = FieldNameBuilder.build(
                    fm.getTarget(),
                    index,
                    group.getSuffix()
            );

            result.put(finalName, value);
        }
    }
}
