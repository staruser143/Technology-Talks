// Full Java 17 Mapping Engine Project - Option 2
// Package: com.example.mappingengine
// All classes included in one document for convenience

/* ======================
 * 1. pom.xml
 * ====================== */

// Place this in root project directory
/*
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">

    <modelVersion>4.0.0</modelVersion>
    <groupId>com.example</groupId>
    <artifactId>mappingengine</artifactId>
    <version>1.0.0</version>
    <properties>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
    </properties>

    <dependencies>
        <!-- JSON -->
        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-databind</artifactId>
            <version>2.17.0</version>
        </dependency>

        <!-- YAML -->
        <dependency>
            <groupId>org.yaml</groupId>
            <artifactId>snakeyaml</artifactId>
            <version>2.2</version>
        </dependency>

        <!-- JSONata4Java -->
        <dependency>
            <groupId>com.api.jsonata4java</groupId>
            <artifactId>jsonata4java</artifactId>
            <version>2.4.10</version>
        </dependency>
    </dependencies>
</project>
*/

/* ======================
 * 2. JsonUtils
 * ====================== */
package com.example.mappingengine;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.core.type.TypeReference;

public class JsonUtils {
    private static final ObjectMapper om = new ObjectMapper();

    public static JsonNode readTree(String json) {
        try {
            return om.readTree(json);
        } catch (Exception e) {
            throw new RuntimeException("Invalid JSON", e);
        }
    }

    public static <T> T convert(JsonNode node, Class<T> type){
        try{
            return om.treeToValue(node, type);
        } catch(Exception e){ throw new RuntimeException(e); }
    }

    public static <T> T convert(JsonNode node, TypeReference<T> ref){
        try{
            return om.convertValue(node, ref);
        } catch(Exception e){ throw new RuntimeException(e); }
    }
}

/* ======================
 * 3. ExpressionCache
 * ====================== */
package com.example.mappingengine;

import java.util.concurrent.*;
import com.api.jsonata4java.expressions.Expressions;
import com.api.jsonata4java.expressions.JsonataException;

public class ExpressionCache {
    private final ConcurrentHashMap<String, Expressions> cache = new ConcurrentHashMap<>();

    public Expressions compile(String expr) {
        return cache.computeIfAbsent(expr, k -> {
            try {
                return Expressions.parse(k);
            } catch (JsonataException e) {
                throw new RuntimeException("Invalid JSONata expression: " + k, e);
            }
        });
    }
}

/* ======================
 * 4. ExpressionEvaluator
 * ====================== */
package com.example.mappingengine;

import com.fasterxml.jackson.databind.JsonNode;
import com.api.jsonata4java.expressions.*;
import com.api.jsonata4java.expressions.utils.JsonUtils as JU;

public class ExpressionEvaluator {
    private final ExpressionCache cache = new ExpressionCache();

    public JsonNode eval(String expr, JsonNode data) {
        try {
            Expressions compiled = cache.compile(expr);
            return JU.convertValue(compiled.evaluate(data));
        } catch (Exception e) {
            throw new RuntimeException("JSONata evaluation failed: " + expr, e);
        }
    }
}

/* ======================
 * 5. MappingConfig Models
 * ====================== */
package com.example.mappingengine.config;

import java.util.*;

public class MappingConfig {
    public List<MappingGroup> groups;
}

public class MappingGroup {
    public String name;
    public String filterExpr;
    public String prefixExpr;
    public String suffixMode;   // single | increment
    public Integer maxItems;    // optional
    public Map<String,String> fields;
}

/* ======================
 * 6. MappingGroupProcessor
 * ====================== */
package com.example.mappingengine;

import com.fasterxml.jackson.databind.JsonNode;
import com.example.mappingengine.config.*;
import java.util.*;

public class MappingGroupProcessor {

    private final ExpressionEvaluator evaluator;

    public MappingGroupProcessor(ExpressionEvaluator evaluator){
        this.evaluator = evaluator;
    }

    public List<JsonNode> applyFilter(MappingGroup group, JsonNode data){
        if(group.filterExpr == null) return Collections.singletonList(data);
        JsonNode result = evaluator.eval(group.filterExpr, data);
        if(result == null || result.isNull()) return List.of();
        if(result.isArray()) return result.findValues("*");
        return List.of(result);
    }

    public String evalPrefix(String prefixExpr, JsonNode item){
        if(prefixExpr == null) return "";
        JsonNode out = evaluator.eval(prefixExpr, item);
        return out != null && out.isTextual() ? out.asText() + "." : "";
    }
}

/* ======================
 * 7. ApplicantGroupProcessor
 * Implements suffix rules
 * ====================== */
package com.example.mappingengine;

import com.fasterxml.jackson.databind.JsonNode;
import com.example.mappingengine.config.*;
import java.util.*;

public class ApplicantGroupProcessor {

    private final MappingGroupProcessor core;
    private final ExpressionEvaluator evaluator;

    public ApplicantGroupProcessor(ExpressionEvaluator evaluator){
        this.evaluator = evaluator;
        this.core = new MappingGroupProcessor(evaluator);
    }

    public Map<String,String> process(MappingGroup group, JsonNode data){
        List<JsonNode> items = core.applyFilter(group, data);
        Map<String,String> out = new LinkedHashMap<>();

        int idx = 1;
        for(JsonNode item : items){
            String prefix = core.evalPrefix(group.prefixExpr, item);
            String suffix = switch(group.suffixMode){
                case "single" -> "1";
                case "increment" -> String.valueOf(idx);
                default -> "";
            };

            for(var e : group.fields.entrySet()){
                JsonNode val = evaluator.eval(e.getValue(), item);
                String finalKey = prefix + e.getKey() + "." + suffix;
                out.put(finalKey, val != null && val.isValueNode() ? val.asText() : "");
            }

            if("increment".equals(group.suffixMode)) idx++;
        }

        return out;
    }
}

/* ======================
 * 8. CoverageInfoProcessor
 * Supports: group-level aggregation
 * ====================== */
package com.example.mappingengine;

import com.fasterxml.jackson.databind.JsonNode;
import com.example.mappingengine.config.*;
import java.util.*;

public class CoverageInfoProcessor {
    private final MappingGroupProcessor core;
    private final ExpressionEvaluator evaluator;

    public CoverageInfoProcessor(ExpressionEvaluator evaluator){
        this.evaluator = evaluator;
        this.core = new MappingGroupProcessor(evaluator);
    }

    public Map<String,String> process(MappingGroup group, JsonNode data){
        List<JsonNode> applicants = core.applyFilter(group, data);
        Map<String,String> out = new LinkedHashMap<>();

        int idx = 1;
        for(JsonNode applicant : applicants){
            if(group.maxItems != null && idx > group.maxItems) break;

            // Always prefixExpr per applicant
            String prefix = core.evalPrefix(group.prefixExpr, applicant);

            // Always increment suffix
            String suffix = String.valueOf(idx);

            for(var e : group.fields.entrySet()){
                JsonNode val = evaluator.eval(e.getValue(), applicant);
                String finalKey = prefix + e.getKey() + "." + suffix;
                out.put(finalKey, val != null && val.isValueNode() ? val.asText() : "");
            }
            idx++;
        }
        return out;
    }
}

/* ======================
 * 9. TargetFieldFlattener
 * ====================== */
package com.example.mappingengine;

import java.util.*;

public class TargetFieldFlattener {
    public Map<String,String> flatten(List<Map<String,String>> list){
        Map<String,String> out = new LinkedHashMap<>();
        for(var m : list) out.putAll(m);
        return out;
    }
}

/* ======================
 * 10. MappingEngine (Main)
 * ====================== */
package com.example.mappingengine;

import com.example.mappingengine.config.*;
import com.fasterxml.jackson.databind.JsonNode;
import org.yaml.snakeyaml.Yaml;
import java.io.*;
import java.util.*;

public class MappingEngine {

    private final ExpressionEvaluator evaluator = new ExpressionEvaluator();
    private final ApplicantGroupProcessor applicantProcessor = new ApplicantGroupProcessor(evaluator);
    private final CoverageInfoProcessor coverageProcessor = new CoverageInfoProcessor(evaluator);
    private final TargetFieldFlattener flattener = new TargetFieldFlattener();

    public MappingConfig loadYaml(String yamlText){
        return new Yaml().loadAs(yamlText, MappingConfig.class);
    }

    public Map<String,String> execute(MappingConfig cfg, JsonNode data){

        List<Map<String,String>> chunks = new ArrayList<>();

        for(MappingGroup g : cfg.groups){
            if(g.name.equals("coverageinfo")){
                chunks.add(coverageProcessor.process(g, data));
            } else {
                chunks.add(applicantProcessor.process(g, data));
            }
        }

        return flattener.flatten(chunks);
    }
}

/* ======================
 * 11. Example YAML (Place in resource or external)
 * ====================== */
/*

# Example mapping.yaml

groups:

  - name: applicantFields
    filterExpr: "applicants[$relationshipType in ['Primary','Spouse','Dependent']]"
    prefixExpr: "relationshipType ~> $lowercase()"   # e.g. primary, spouse, dependent
    suffixMode: "increment"
    fields:
      firstName: "firstName"
      lastName: "lastName"


  - name: coverageinfo
    filterExpr: "applicants"
    prefixExpr: "'coverageinfo.currentcoverage.'"
    suffixMode: "increment"
    maxItems: 5
    fields:
      applicantFirstName: "firstName"
      insurerName: "coverage[0].currentCoverage.insurerName"
      planName: "coverage[0].currentCoverage.planName"

*/

// END OF FILE

# Unit Tests

// Full Option D test suite will be added here.
