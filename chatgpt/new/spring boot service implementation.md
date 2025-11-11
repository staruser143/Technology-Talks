Perfect — here’s the full **Spring Boot microservice version** of the generic **Mapping Engine**,
with a `/transform` endpoint that accepts a JSON payload and a YAML mapping spec, applies JSONata transformations, and returns flattened results.

---

## 🧩 Project Structure

```
mapping-engine-service/
 ├── src/
 │   ├── main/java/com/example/mapping/
 │   │   ├── MappingEngine.java        <-- generic reusable engine
 │   │   ├── MappingService.java       <-- wrapper service
 │   │   ├── MappingController.java    <-- REST API endpoint
 │   │   └── MappingEngineApplication.java
 │   └── main/resources/
 │       └── application.yml
 └── pom.xml
```

---

## 🧱 `pom.xml`

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
                             http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <groupId>com.example</groupId>
    <artifactId>mapping-engine-service</artifactId>
    <version>1.0.0</version>
    <packaging>jar</packaging>

    <properties>
        <java.version>17</java.version>
        <spring.boot.version>3.3.2</spring.boot.version>
    </properties>

    <dependencies>
        <!-- Spring Boot Web -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
            <version>${spring.boot.version}</version>
        </dependency>

        <!-- Jackson for JSON -->
        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-databind</artifactId>
        </dependency>

        <!-- YAML parser -->
        <dependency>
            <groupId>org.yaml</groupId>
            <artifactId>snakeyaml</artifactId>
            <version>2.2</version>
        </dependency>

        <!-- JSONata for Java -->
        <dependency>
            <groupId>com.api.jsonata4java</groupId>
            <artifactId>jsonata4java</artifactId>
            <version>2.4.8</version>
        </dependency>

        <!-- Dev tools -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-devtools</artifactId>
            <optional>true</optional>
        </dependency>

        <!-- Testing -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <version>${spring.boot.version}</version>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <version>${spring.boot.version}</version>
            </plugin>
        </plugins>
    </build>
</project>
```

---

## 🚀 `MappingEngineApplication.java`

```java
package com.example.mapping;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MappingEngineApplication {
    public static void main(String[] args) {
        SpringApplication.run(MappingEngineApplication.class, args);
    }
}
```

---

## ⚙️ `MappingService.java`

```java
package com.example.mapping;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service
public class MappingService {

    private final ObjectMapper mapper = new ObjectMapper();
    private final MappingEngine engine = new MappingEngine();

    public Map<String, String> transform(String jsonSource, String yamlSpec) throws Exception {
        JsonNode source = mapper.readTree(jsonSource);
        try (ByteArrayInputStream yamlStream =
                     new ByteArrayInputStream(yamlSpec.getBytes(StandardCharsets.UTF_8))) {
            return engine.transform(source, yamlStream);
        }
    }
}
```

---

## 🌐 `MappingController.java`

```java
package com.example.mapping;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/mapping")
public class MappingController {

    private final MappingService mappingService;

    public MappingController(MappingService mappingService) {
        this.mappingService = mappingService;
    }

    @PostMapping(value = "/transform", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, String> transform(
            @RequestPart("source") String sourceJson,
            @RequestPart("mapping") String yamlSpec
    ) throws Exception {
        return mappingService.transform(sourceJson, yamlSpec);
    }
}
```

---

## 🧠 Example Request (via Postman / cURL)

### Endpoint

```
POST http://localhost:8080/api/mapping/transform
```

### Content-Type

`multipart/form-data`

### Form fields:

| Key       | Type | Description       |
| --------- | ---- | ----------------- |
| `source`  | Text | JSON input        |
| `mapping` | Text | YAML mapping spec |

---

### Example JSON (`source` field)

```json
{
  "applicants": [
    {
      "type": "PrimaryApplicant",
      "demographic": { "firstName": "John", "lastName": "Doe" },
      "products": [
        {
          "name": "Medical",
          "plans": [
            { "planName": "Med A", "startDate": "2024-01-01" },
            { "planName": "Med B", "startDate": "2024-06-01" }
          ]
        }
      ]
    },
    {
      "type": "Child",
      "demographic": { "firstName": "Alice", "lastName": "Doe" }
    }
  ]
}
```

---

### Example YAML (`mapping` field)

```yaml
contexts:
  primaryApplicant: "$applicants[type='PrimaryApplicant']"
  childApplicants: "$applicants[type='Child']"

mappings:
  primary:
    context: "$primaryApplicant"
    fields:
      demographic:
        firstName: "$demographic.firstName"
        lastName: "$demographic.lastName"
      products:
        context: "$products"
        repeat: true
        fields:
          name: "$name"
          plans:
            context: "$plans"
            repeat: true
            fields:
              planName: "$planName"
              startDate: "$startDate"

  child:
    context: "$childApplicants"
    repeat: true
    fields:
      demographic:
        firstName: "$demographic.firstName"
        lastName: "$demographic.lastName"
```

---

### Example Output JSON

```json
{
  "primary.demographic.firstName.1": "John",
  "primary.demographic.lastName.1": "Doe",
  "primary.products.name.1.1": "Medical",
  "primary.products.plans.planName.1.1.1": "Med A",
  "primary.products.plans.startDate.1.1.1": "2024-01-01",
  "primary.products.plans.planName.1.1.2": "Med B",
  "primary.products.plans.startDate.1.1.2": "2024-06-01",
  "child.demographic.firstName.1": "Alice",
  "child.demographic.lastName.1": "Doe"
}
```

---

## 🧩 Why This Design Works for Microservices

| Feature             | Description                                  |
| ------------------- | -------------------------------------------- |
| **Self-contained**  | One `/transform` endpoint for all mappings   |
| **Generic engine**  | Reusable across any JSON & YAML combination  |
| **Safe evaluation** | JSONata expressions isolated from runtime    |
| **Cloud ready**     | Easy to deploy in containerized environments |
| **Extensible**      | Add caching, audit, or validation layers     |

---

## 🚀 Next Optional Enhancements

If you like, I can show:

1. A **REST model version** that takes YAML + JSON as files and returns a downloadable PDF directly.
2. Integration with your existing **PDF merging engine** (using field names from this output map).
3. Or a **mapping repository** that stores reusable YAML specs per client/service.

---

Would you like me to extend this service to **generate and return a merged PDF** using this mapping output (i.e., full end-to-end pipeline: JSON → mapping → field map → filled PDF → return file)?
