Perfect — let’s now bring everything together into a **complete runnable Java application** that demonstrates:

✅ Loading a YAML mapping spec
✅ Reading JSON data
✅ Applying mapping, grouping & conditional logic
✅ Auto-generating AcroForm field names
✅ Filling a PDF form using **PDFBox 2.0.7**
✅ Saving the result to an output file

---

## 🧩 Maven Dependencies (`pom.xml`)

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
                             http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <groupId>com.example.pdfmapper</groupId>
    <artifactId>pdfmapper-demo</artifactId>
    <version>1.0.0</version>
    <properties>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
    </properties>
    <dependencies>
        <!-- PDFBox -->
        <dependency>
            <groupId>org.apache.pdfbox</groupId>
            <artifactId>pdfbox</artifactId>
            <version>2.0.7</version>
        </dependency>

        <!-- JSON handling -->
        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-databind</artifactId>
            <version>2.17.1</version>
        </dependency>

        <!-- YAML parsing -->
        <dependency>
            <groupId>org.yaml</groupId>
            <artifactId>snakeyaml</artifactId>
            <version>2.0</version>
        </dependency>
    </dependencies>
</project>
```

---

## 🧾 Sample JSON Input (`data.json`)

```json
{
  "applicantList": [
    {
      "fname": "John",
      "lname": "Doe",
      "relationship": "primary"
    },
    {
      "fname": "Jane",
      "lname": "Doe",
      "relationship": "spouse"
    },
    {
      "fname": "Chris",
      "lname": "Doe",
      "relationship": "child"
    },
    {
      "fname": "Alex",
      "lname": "Doe",
      "relationship": "child"
    }
  ]
}
```

---

## 🧾 YAML Mapping (`mapping.yaml`)

```yaml
pdf:
  mappings:
    - source: applicantList
      groupBy: relationship
      pdfFieldPattern: "{prefix}.{field}.1"
      groups:
        primary:
          targetPrefix: "PrimaryApplicant"
          fieldMap:
            fname: "FName"
            lname: "LName"
        spouse:
          targetPrefix: "Spouse"
          fieldMap:
            fname: "FName"
            lname: "LName"

    - source: applicantList
      condition: "relationship == 'child'"
      targetPrefix: "Dependent"
      pdfFieldPattern: "{prefix}.{field}.{index}"
      isArray: true
      indexed: true
      fieldMap:
        fname: "FName"
        lname: "LName"
```

---

## 🧠 Main Integration Logic (`MainApp.java`)

```java
package com.example.pdfmapper;

import com.example.pdfmapper.config.MappingSpec;
import com.example.pdfmapper.processor.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.util.*;

public class MainApp {

    public static void main(String[] args) throws Exception {
        String jsonPath = "src/main/resources/data.json";
        String yamlPath = "src/main/resources/mapping.yaml";
        String inputPdfPath = "src/main/resources/input-template.pdf";
        String outputPdfPath = "target/output-filled.pdf";

        // STEP 1: Load JSON data
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> jsonData = mapper.readValue(new File(jsonPath), Map.class);

        // STEP 2: Load YAML spec
        Yaml yaml = new Yaml();
        Map<String, Object> yamlData = yaml.load(new FileInputStream(yamlPath));

        List<MappingSpec> mappingSpecs = new ArrayList<>();
        if (yamlData.containsKey("pdf")) {
            Map<String, Object> pdfConfig = (Map<String, Object>) yamlData.get("pdf");
            List<Map<String, Object>> mappings = (List<Map<String, Object>>) pdfConfig.get("mappings");
            for (Map<String, Object> mapping : mappings) {
                mappingSpecs.add(parseMappingSpec(mapping));
            }
        }

        // STEP 3: Initialize processors
        List<MappingProcessor> processors = List.of(
                new GroupMappingProcessor(),
                new ConditionalMappingProcessor()
        );

        // STEP 4: Process all mappings
        Map<String, Object> finalFields = new LinkedHashMap<>();
        for (MappingSpec spec : mappingSpecs) {
            for (MappingProcessor processor : processors) {
                if (processor.supports(spec)) {
                    Map<String, Object> result = processor.process(jsonData, spec);
                    finalFields.putAll(result);
                }
            }
        }

        // STEP 5: Print mapped field values
        System.out.println("=== Field Mapping Result ===");
        finalFields.forEach((k, v) -> System.out.println(k + " = " + v));

        // STEP 6: Fill PDF using PDFBox
        PdfBoxFormFiller.fillPdf(inputPdfPath, outputPdfPath, finalFields);
        System.out.println("\n✅ PDF generated: " + outputPdfPath);
    }

    @SuppressWarnings("unchecked")
    private static MappingSpec parseMappingSpec(Map<String, Object> map) {
        MappingSpec spec = new MappingSpec();
        spec.setSource((String) map.get("source"));
        spec.setGroupBy((String) map.get("groupBy"));
        spec.setCondition((String) map.get("condition"));
        spec.setTargetPrefix((String) map.get("targetPrefix"));
        spec.setPdfFieldPattern((String) map.getOrDefault("pdfFieldPattern", "{prefix}.{field}.{index}"));
        spec.setIndexed(Boolean.TRUE.equals(map.get("indexed")));
        spec.setArray(Boolean.TRUE.equals(map.get("isArray")));

        if (map.containsKey("groups")) {
            Map<String, Object> groups = (Map<String, Object>) map.get("groups");
            Map<String, com.example.pdfmapper.config.GroupSpec> groupSpecs = new LinkedHashMap<>();
            for (var entry : groups.entrySet()) {
                Map<String, Object> groupMap = (Map<String, Object>) entry.getValue();
                com.example.pdfmapper.config.GroupSpec gs = new com.example.pdfmapper.config.GroupSpec();
                gs.setTargetPrefix((String) groupMap.get("targetPrefix"));
                gs.setFieldMap((Map<String, String>) groupMap.get("fieldMap"));
                groupSpecs.put(entry.getKey().toLowerCase(), gs);
            }
            spec.setGroups(groupSpecs);
        }

        if (map.containsKey("fieldMap")) {
            spec.setFieldMap((Map<String, String>) map.get("fieldMap"));
        }
        return spec;
    }
}
```

---

## ✅ Example Console Output

```
=== Field Mapping Result ===
PrimaryApplicant.FName.1 = John
PrimaryApplicant.LName.1 = Doe
Spouse.FName.1 = Jane
Spouse.LName.1 = Doe
Dependent.FName.1 = Chris
Dependent.LName.1 = Doe
Dependent.FName.2 = Alex
Dependent.LName.2 = Doe

✅ PDF generated: target/output-filled.pdf
```

---

## 📘 Folder Structure

```
pdfmapper-demo/
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/com/example/pdfmapper/
│   │   │   ├── MainApp.java
│   │   │   ├── config/
│   │   │   │   ├── MappingSpec.java
│   │   │   │   ├── GroupSpec.java
│   │   │   └── processor/
│   │   │       ├── MappingProcessor.java
│   │   │       ├── GroupMappingProcessor.java
│   │   │       ├── ConditionalMappingProcessor.java
│   │   │       ├── PdfFieldNameGenerator.java
│   │   │       └── PdfBoxFormFiller.java
│   │   └── resources/
│   │       ├── data.json
│   │       ├── mapping.yaml
│   │       └── input-template.pdf
└── target/
    └── output-filled.pdf
```

---

✅ **You can now run:**

```bash
mvn clean compile exec:java -Dexec.mainClass="com.example.pdfmapper.MainApp"
```

And you’ll get your **populated PDF** with correct field naming patterns.

---

Would you like me to extend this next so it can **generate missing AcroForm fields automatically in the PDF** (if not already present), rather than just logging a warning?
