Absolutely! Below is the **complete, production-ready implementation** of a **flexible, client-driven template merging service** that:

✅ Supports **PDF + HTML templates**  
✅ Uses **simple YAML config** (no data fetching logic)  
✅ Allows **selective template generation** by name  
✅ Uses **linear search** (no startup index)  
✅ Fully **Spring Boot compatible**

---

## 📁 Project Structure

```
src/main/java/com/example/templatemerge/
├── config/
│   └── TemplateMergeAutoConfiguration.java
├── model/
│   ├── MergeConfig.java
│   ├── TemplateDefinition.java
│   ├── TemplateType.java
│   ├── FieldMapping.java
│   ├── ItemMapping.java
│   ├── ObjectFieldMapping.java
│   ├── FilterCondition.java
│   └── TransformSpec.java
├── service/
│   ├── TemplateMergeService.java
│   ├── DataMapper.java
│   └── TemplateMerger.java
├── transformer/
│   ├── TransformerRegistry.java
│   └── CustomTransformer.java
├── util/
│   └── SimplePathResolver.java
└── exception/
    └── TemplateMergeException.java
```

---

## 🧩 1. Core Models

### `TemplateType.java`
```java
package com.example.templatemerge.model;

public enum TemplateType {
    PDF,
    HTML
}
```

### `TemplateDefinition.java`
```java
package com.example.templatemerge.model;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class TemplateDefinition {
    private String name; // unique identifier
    private TemplateType type;
    private String templatePath;
    private String outputPath; // default output path
    private List<FieldMapping> mappings = new ArrayList<>();
}
```

### `MergeConfig.java`
```java
package com.example.templatemerge.model;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class MergeConfig {
    private List<TemplateDefinition> templates = new ArrayList<>();
    
    public TemplateDefinition getTemplateByName(String name) {
        return templates.stream()
            .filter(t -> name.equals(t.getName()))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Template not found: " + name));
    }
}
```

> 💡 **No startup indexing** — uses linear search

*(Include other models like `FieldMapping`, `FilterCondition`, etc. as previously defined)*

---

## 🧠 2. `DataMapper.java` (Simplified Path Version)

*(Use the full `DataMapper` from earlier with `SimplePathResolver`)*

### `SimplePathResolver.java`
```java
package com.example.templatemerge.util;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;

public class SimplePathResolver {
    public static Object read(Object jsonContext, String simplePath) {
        if (simplePath == null || simplePath.trim().isEmpty()) {
            return null;
        }
        String jsonPath = "$." + simplePath.trim().replace("[", ".").replace("]", "");
        try {
            return JsonPath.read(jsonContext, jsonPath);
        } catch (PathNotFoundException | IllegalArgumentException e) {
            return null;
        }
    }
}
```

---

## 🖨️ 3. Template Merger Interface

### `TemplateMerger.java`
```java
package com.example.templatemerge.service;

import com.example.templatemerge.model.TemplateDefinition;
import java.io.IOException;
import java.util.Map;

public interface TemplateMerger {
    void merge(Map<String, Object> mappedData, TemplateDefinition templateDef) throws IOException;
}
```

### `PdfTemplateMerger.java`
```java
package com.example.templatemerge.service;

import com.example.templatemerge.model.TemplateDefinition;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.springframework.stereotype.Component;
import java.io.File;
import java.io.IOException;
import java.util.Map;

@Component
public class PdfTemplateMerger implements TemplateMerger {

    @Override
    public void merge(Map<String, Object> mappedData, TemplateDefinition def) throws IOException {
        try (PDDocument doc = PDDocument.load(new File(def.getTemplatePath()))) {
            PDAcroForm form = doc.getDocumentCatalog().getAcroForm();
            if (form == null) {
                throw new IllegalStateException("No AcroForm in PDF: " + def.getTemplatePath());
            }

            for (Map.Entry<String, Object> entry : mappedData.entrySet()) {
                PDField field = form.getField(entry.getKey());
                if (field != null) {
                    field.setValue(entry.getValue().toString());
                }
            }

            doc.save(def.getOutputPath());
        }
    }
}
```

### `HtmlTemplateMerger.java`
```java
package com.example.templatemerge.service;

import com.example.templatemerge.model.TemplateDefinition;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.stereotype.Component;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

@Component
public class HtmlTemplateMerger implements TemplateMerger {

    private final Configuration fmConfig;

    public HtmlTemplateMerger() {
        fmConfig = new Configuration(Configuration.VERSION_2_3_31);
        fmConfig.setClassForTemplateLoading(HtmlTemplateMerger.class, "/templates");
        fmConfig.setDefaultEncoding("UTF-8");
    }

    @Override
    public void merge(Map<String, Object> model, TemplateDefinition def) throws IOException {
        Template template = fmConfig.getTemplate(new File(def.getTemplatePath()).getName());
        try (FileWriter out = new FileWriter(def.getOutputPath())) {
            template.process(model, out);
        }
    }
}
```

---

## 🚀 4. Main Service: `TemplateMergeService.java`

```java
package com.example.templatemerge.service;

import com.example.templatemerge.model.MergeConfig;
import com.example.templatemerge.model.TemplateDefinition;
import com.example.templatemerge.model.TemplateType;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

@Service
public class TemplateMergeService {

    private final MergeConfig mergeConfig;
    private final DataMapper dataMapper;
    private final PdfTemplateMerger pdfMerger;
    private final HtmlTemplateMerger htmlMerger;

    public TemplateMergeService(
        MergeConfig mergeConfig,
        DataMapper dataMapper,
        PdfTemplateMerger pdfMerger,
        HtmlTemplateMerger htmlMerger
    ) {
        this.mergeConfig = mergeConfig;
        this.dataMapper = dataMapper;
        this.pdfMerger = pdfMerger;
        this.htmlMerger = htmlMerger;
    }

    /**
     * Primary API: Generate a single template with provided data
     */
    public void mergeTemplate(String templateName, Object sourceData, Path outputPath) throws IOException {
        TemplateDefinition def = mergeConfig.getTemplateByName(templateName);
        TemplateDefinition defWithOutput = createDefWithOutput(def, outputPath);
        
        Map<String, Object> mappedData = dataMapper.mapData(sourceData, defWithOutput.getMappings());
        getMerger(defWithOutput.getType()).merge(mappedData, defWithOutput);
    }

    /**
     * Batch API: Generate all templates with same data (optional)
     */
    public void mergeAllTemplates(Object sourceData) throws IOException {
        for (TemplateDefinition def : mergeConfig.getTemplates()) {
            Map<String, Object> mappedData = dataMapper.mapData(sourceData, def.getMappings());
            getMerger(def.getType()).merge(mappedData, def);
        }
    }

    private TemplateMerger getMerger(TemplateType type) {
        switch (type) {
            case PDF: return pdfMerger;
            case HTML: return htmlMerger;
            default: throw new IllegalArgumentException("Unsupported template type: " + type);
        }
    }

    private TemplateDefinition createDefWithOutput(TemplateDefinition original, Path outputPath) {
        TemplateDefinition def = new TemplateDefinition();
        def.setName(original.getName());
        def.setType(original.getType());
        def.setTemplatePath(original.getTemplatePath());
        def.setMappings(original.getMappings());
        def.setOutputPath(outputPath.toString());
        return def;
    }
}
```

---

## 🔧 5. Auto-Configuration (Spring Boot Starter)

### `TemplateMergeAutoConfiguration.java`
```java
package com.example.templatemerge.config;

import com.example.templatemerge.model.MergeConfig;
import com.example.templatemerge.service.*;
import com.example.templatemerge.transformer.TransformerRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.IOException;

@Configuration
@EnableConfigurationProperties
public class TemplateMergeAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public MergeConfig mergeConfig() throws IOException {
        // Load from default location - or make configurable
        ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
        return yamlMapper.readValue(new File("config/templates.yaml"), MergeConfig.class);
    }

    @Bean
    @ConditionalOnMissingBean
    public DataMapper dataMapper(TransformerRegistry transformerRegistry) {
        return new DataMapper(transformerRegistry);
    }

    @Bean
    @ConditionalOnMissingBean
    public TemplateMergeService templateMergeService(
        MergeConfig mergeConfig,
        DataMapper dataMapper,
        PdfTemplateMerger pdfMerger,
        HtmlTemplateMerger htmlMerger
    ) {
        return new TemplateMergeService(mergeConfig, dataMapper, pdfMerger, htmlMerger);
    }
}
```

---

## 📄 6. Example YAML Config (`config/templates.yaml`)

```yaml
templates:
  - name: "application-pdf"
    type: PDF
    templatePath: "classpath:templates/app_form.pdf"
    outputPath: "/tmp/app.pdf"  # default, overridden by client
    mappings:
      - sourceObject: "applicants"
        itemFilters:
          - field: "relationship"
            operator: EQ
            value: "primary"
        fieldMappings:
          - sourceField: "firstName"
            targetField: "primary.fname.1"
          - sourceField: "lastName"
            targetField: "primary.lname.1"

  - name: "summary-html"
    type: HTML
    templatePath: "classpath:templates/summary.ftl"
    outputPath: "/tmp/summary.html"
    mappings:
      - sourceField: "applicants"
        targetField: "applicants"
      - sourceField: "metadata.submissionId"
        targetField: "submissionId"
```

---

## 🧪 7. Client Usage Example

```java
@Service
public class DocumentGenerationService {

    private final TemplateMergeService templateMergeService;
    private final ApplicantRepository applicantRepository;

    public DocumentGenerationService(
        TemplateMergeService templateMergeService,
        ApplicantRepository applicantRepository
    ) {
        this.templateMergeService = templateMergeService;
        this.applicantRepository = applicantRepository;
    }

    public void generateDocuments(String appId) throws IOException {
        // 1. Fetch data (client decides what to fetch)
        Object applicationData = applicantRepository.getFullApplicationData(appId);

        // 2. Generate PDF
        templateMergeService.mergeTemplate(
            "application-pdf", 
            applicationData, 
            Paths.get("/output/app_" + appId + ".pdf")
        );

        // 3. Generate HTML summary
        templateMergeService.mergeTemplate(
            "summary-html",
            applicationData,
            Paths.get("/output/summary_" + appId + ".html")
        );
    }
}
```

---

## ✅ Key Design Benefits

| Feature | Implementation |
|--------|---------------|
| **Selective template generation** | `mergeTemplate(templateName, data, outputPath)` |
| **Client-driven data fetching** | Client passes `Object data` |
| **No startup indexing** | Linear search in `getTemplateByName()` |
| **Template-agnostic** | Same `DataMapper` for PDF/HTML |
| **Spring Boot native** | Auto-configuration, component scanning |
| **YAML simplicity** | No data source logic in config |

---

## 📦 Dependencies (`pom.xml`)

```xml
<dependencies>
    <!-- Core -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter</artifactId>
    </dependency>
    
    <!-- PDF -->
    <dependency>
        <groupId>org.apache.pdfbox</groupId>
        <artifactId>pdfbox</artifactId>
        <version>2.0.27</version>
    </dependency>
    
    <!-- HTML -->
    <dependency>
        <groupId>org.freemarker</groupId>
        <artifactId>freemarker</artifactId>
    </dependency>
    
    <!-- JSON/YAML -->
    <dependency>
        <groupId>com.fasterxml.jackson.dataformat</groupId>
        <artifactId>jackson-dataformat-yaml</artifactId>
    </dependency>
    
    <!-- JsonPath -->
    <dependency>
        <groupId>com.jayway.jsonpath</groupId>
        <artifactId>json-path</artifactId>
        <version>2.9.0</version>
    </dependency>
</dependencies>
```

---

This implementation gives you a **production-ready, flexible, and maintainable** template merging library that handles all your requirements:

- ✅ PDF + HTML templates  
- ✅ Complex data mapping with filters/transforms  
- ✅ Selective template generation  
- ✅ Client-controlled data fetching  
- ✅ Clean separation of concerns  

You can now publish this as a **Spring Boot starter** and use it across multiple applications! 🚀