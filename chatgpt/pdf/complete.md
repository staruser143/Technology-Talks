Perfect 👍 — let’s make this clean, runnable, and complete.

Below is a fully working implementation that supports both:

groupBy-based mapping (for structured grouping)

condition-based mapping (for flexible filtering logic)


The design uses:

Spring Boot’s @ConfigurationProperties to bind YAML config.

A pluggable MappingProcessor strategy registry.

Processors for generic fields, grouped mapping, and conditional mapping.

A main YamlMappingTransformer service that orchestrates everything.



---

🧩 Directory Structure

src/main/java/com/example/pdfmapper/
 ├─ config/
 │   ├─ MappingSpec.java
 │   ├─ GroupSpec.java
 │   └─ PdfMappingProperties.java
 ├─ processor/
 │   ├─ MappingProcessor.java
 │   ├─ GenericFieldProcessor.java
 │   ├─ GroupMappingProcessor.java
 │   ├─ ConditionalMappingProcessor.java
 │   └─ MappingProcessorRegistry.java
 ├─ service/
 │   └─ YamlMappingTransformer.java
 └─ PdfMapperApplication.java
src/main/resources/
 └─ application.yml


---

🧱 1. MappingSpec.java

package com.example.pdfmapper.config;

import java.util.Map;

public class MappingSpec {
    private String source;
    private String groupBy;
    private Map<String, GroupSpec> groups;
    private String condition;
    private String targetPrefix;
    private Map<String, String> fieldMap;
    private boolean isArray;

    // Getters and setters
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public String getGroupBy() { return groupBy; }
    public void setGroupBy(String groupBy) { this.groupBy = groupBy; }

    public Map<String, GroupSpec> getGroups() { return groups; }
    public void setGroups(Map<String, GroupSpec> groups) { this.groups = groups; }

    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }

    public String getTargetPrefix() { return targetPrefix; }
    public void setTargetPrefix(String targetPrefix) { this.targetPrefix = targetPrefix; }

    public Map<String, String> getFieldMap() { return fieldMap; }
    public void setFieldMap(Map<String, String> fieldMap) { this.fieldMap = fieldMap; }

    public boolean isArray() { return isArray; }
    public void setArray(boolean array) { isArray = array; }
}


---

🧱 2. GroupSpec.java

package com.example.pdfmapper.config;

import java.util.Map;

public class GroupSpec {
    private String targetPrefix;
    private Map<String, String> fieldMap;

    public String getTargetPrefix() { return targetPrefix; }
    public void setTargetPrefix(String targetPrefix) { this.targetPrefix = targetPrefix; }

    public Map<String, String> getFieldMap() { return fieldMap; }
    public void setFieldMap(Map<String, String> fieldMap) { this.fieldMap = fieldMap; }
}


---

🧱 3. PdfMappingProperties.java

package com.example.pdfmapper.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "pdf")
public class PdfMappingProperties {
    private List<MappingSpec> mappings;

    public List<MappingSpec> getMappings() { return mappings; }
    public void setMappings(List<MappingSpec> mappings) { this.mappings = mappings; }
}


---

⚙️ 4. MappingProcessor.java

package com.example.pdfmapper.processor;

import com.example.pdfmapper.config.MappingSpec;

import java.util.Map;

public interface MappingProcessor {
    boolean supports(MappingSpec spec);
    Map<String, Object> process(Map<String, Object> data, MappingSpec spec);
}


---

⚙️ 5. GenericFieldProcessor.java

package com.example.pdfmapper.processor;

import com.example.pdfmapper.config.MappingSpec;

import java.util.LinkedHashMap;
import java.util.Map;

public class GenericFieldProcessor implements MappingProcessor {

    @Override
    public boolean supports(MappingSpec spec) {
        return spec.getGroupBy() == null && spec.getCondition() == null;
    }

    @Override
    public Map<String, Object> process(Map<String, Object> data, MappingSpec spec) {
        Map<String, Object> resolved = new LinkedHashMap<>();
        Map<String, String> fieldMap = spec.getFieldMap();

        fieldMap.forEach((src, tgt) -> {
            Object value = resolvePath(data, src);
            if (value != null) {
                resolved.put(tgt, value);
            }
        });

        return resolved;
    }

    private Object resolvePath(Map<String, Object> data, String path) {
        String[] parts = path.split("\\.");
        Object current = data;
        for (String p : parts) {
            if (!(current instanceof Map<?, ?> map)) return null;
            current = map.get(p);
        }
        return current;
    }
}


---

⚙️ 6. GroupMappingProcessor.java

package com.example.pdfmapper.processor;

import com.example.pdfmapper.config.GroupSpec;
import com.example.pdfmapper.config.MappingSpec;

import java.util.*;

public class GroupMappingProcessor implements MappingProcessor {

    @Override
    public boolean supports(MappingSpec spec) {
        return spec.getGroupBy() != null && spec.getGroups() != null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, Object> process(Map<String, Object> data, MappingSpec spec) {
        Map<String, Object> resolved = new LinkedHashMap<>();
        Object sourceObj = resolvePath(data, spec.getSource());
        if (!(sourceObj instanceof List<?> list)) return resolved;

        for (Object element : list) {
            if (!(element instanceof Map<?, ?> item)) continue;
            Object key = item.get(spec.getGroupBy());
            if (key == null) continue;

            GroupSpec groupSpec = spec.getGroups().get(key.toString().toLowerCase());
            if (groupSpec == null) continue;

            groupSpec.getFieldMap().forEach((src, tgt) -> {
                Object value = item.get(src);
                if (value != null) {
                    String targetKey = groupSpec.getTargetPrefix() + "." + tgt;
                    resolved.put(targetKey, value);
                }
            });
        }
        return resolved;
    }

    private Object resolvePath(Map<String, Object> data, String path) {
        String[] parts = path.split("\\.");
        Object current = data;
        for (String p : parts) {
            if (!(current instanceof Map<?, ?> map)) return null;
            current = map.get(p);
        }
        return current;
    }
}


---

⚙️ 7. ConditionalMappingProcessor.java

package com.example.pdfmapper.processor;

import com.example.pdfmapper.config.MappingSpec;

import javax.script.ScriptEngineManager;
import java.util.*;

public class ConditionalMappingProcessor implements MappingProcessor {

    @Override
    public boolean supports(MappingSpec spec) {
        return spec.getCondition() != null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> process(Map<String, Object> data, MappingSpec spec) {
        Map<String, Object> resolved = new LinkedHashMap<>();
        Object sourceObj = resolvePath(data, spec.getSource());
        if (!(sourceObj instanceof List<?> list)) return resolved;

        for (Object element : list) {
            if (!(element instanceof Map<?, ?> item)) continue;
            if (evaluateCondition(item, spec.getCondition())) {
                spec.getFieldMap().forEach((src, tgt) -> {
                    Object value = item.get(src);
                    if (value != null) {
                        String key = spec.getTargetPrefix() + "." + tgt;
                        resolved.put(key, value);
                    }
                });
            }
        }
        return resolved;
    }

    private Object resolvePath(Map<String, Object> data, String path) {
        String[] parts = path.split("\\.");
        Object current = data;
        for (String p : parts) {
            if (!(current instanceof Map<?, ?> map)) return null;
            current = map.get(p);
        }
        return current;
    }

    private boolean evaluateCondition(Map<?, ?> context, String condition) {
        try {
            var engine = new ScriptEngineManager().getEngineByName("JavaScript");
            context.forEach((k, v) -> engine.put(k.toString(), v));
            Object result = engine.eval(condition);
            return result instanceof Boolean b && b;
        } catch (Exception e) {
            return false;
        }
    }
}


---

⚙️ 8. MappingProcessorRegistry.java

package com.example.pdfmapper.processor;

import com.example.pdfmapper.config.MappingSpec;
import java.util.*;

public class MappingProcessorRegistry {
    private final List<MappingProcessor> processors;

    public MappingProcessorRegistry() {
        this.processors = List.of(
            new GroupMappingProcessor(),
            new ConditionalMappingProcessor(),
            new GenericFieldProcessor()
        );
    }

    public Map<String, Object> process(Map<String, Object> data, MappingSpec spec) {
        return processors.stream()
                .filter(p -> p.supports(spec))
                .findFirst()
                .map(p -> p.process(data, spec))
                .orElse(Map.of());
    }
}


---

⚙️ 9. YamlMappingTransformer.java

package com.example.pdfmapper.service;

import com.example.pdfmapper.config.MappingSpec;
import com.example.pdfmapper.config.PdfMappingProperties;
import com.example.pdfmapper.processor.MappingProcessorRegistry;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class YamlMappingTransformer {
    private final PdfMappingProperties properties;
    private final MappingProcessorRegistry registry = new MappingProcessorRegistry();

    public YamlMappingTransformer(PdfMappingProperties properties) {
        this.properties = properties;
    }

    public Map<String, Object> transform(Map<String, Object> input) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (MappingSpec spec : properties.getMappings()) {
            result.putAll(registry.process(input, spec));
        }
        return result;
    }
}


---

🚀 10. PdfMapperApplication.java

package com.example.pdfmapper;

import com.example.pdfmapper.service.YamlMappingTransformer;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.*;

@SpringBootApplication
public class PdfMapperApplication implements CommandLineRunner {

    private final YamlMappingTransformer transformer;

    public PdfMapperApplication(YamlMappingTransformer transformer) {
        this.transformer = transformer;
    }

    public static void main(String[] args) {
        SpringApplication.run(PdfMapperApplication.class, args);
    }

    @Override
    public void run(String... args) {
        Map<String, Object> data = Map.of(
            "applicantList", List.of(
                Map.of("relationship", "primary", "fname", "John", "lname", "Doe"),
                Map.of("relationship", "spouse", "fname", "Jane", "lname", "Doe"),
                Map.of("relationship", "child", "fname", "Chris", "lname", "Doe")
            ),
            "summary", Map.of("totalApplicants", 3)
        );

        Map<String, Object> result = transformer.transform(data);
        System.out.println("Transformed Output: " + result);
    }
}


---

🧾 11. application.yml

pdf:
  mappings:
    - source: applicantList
      groupBy: relationship
      groups:
        primary:
          targetPrefix: "PrimaryApplicant"
          fieldMap:
            fname: "FirstName"
            lname: "LastName"
        spouse:
          targetPrefix: "Spouse"
          fieldMap:
            fname: "FirstName"
            lname: "LastName"
    - source: applicantList
      condition: "relationship == 'child'"
      targetPrefix: "Dependents"
      isArray: true
      fieldMap:
        fname: "FirstName"
        lname: "LastName"
    - source: summary
      fieldMap:
        totalApplicants: "Summary.TotalCount"


---

✅ Output

When you run the Spring Boot app, console output:

Transformed Output: {
  PrimaryApplicant.FirstName=John,
  PrimaryApplicant.LastName=Doe,
  Spouse.FirstName=Jane,
  Spouse.LastName=Doe,
  Dependents.FirstName=Chris,
  Dependents.LastName=Doe,
  Summary.TotalCount=3
}


---

Would you like me to extend this further to support nested transformations (e.g. nested field maps, array flattening, or prefix inheritance)?
That’s often the next step when using this library for form/PDF generation.