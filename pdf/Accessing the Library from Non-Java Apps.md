# Query
what if I have some nodejs based services which want to leverage this library. how do I support that.

# Response

Excellent question!

You’ve built a **robust, flexible Java-based template merging library** that supports:

- HTML templates → HTML/PDF
- PDF form templates → filled PDF
- JSON-driven field mapping

Now you want **Node.js services to leverage this same logic** — without rewriting everything in JavaScript.

---

## ✅ GOAL

Enable **Node.js services** to use your Java library’s functionality — **without duplicating business logic or templates**.

---

# 🚀 SOLUTION: Wrap Java Library in a Lightweight REST API (Microservice)

The cleanest, most scalable, and language-agnostic way is to expose your Java library as a **REST API service**.

Node.js (or Python, Go, C#, etc.) can then `POST` data + template + mapping → get back HTML or PDF.

---

## 🏗️ ARCHITECTURE OVERVIEW

```
Node.js Service
     │
     ▼ (HTTP POST)
TemplateMerger Microservice (Java/Spring Boot)
     │
     ▼ (uses your existing TemplateMerger class)
     ├── Merges DB data + template + mapping
     └── Returns HTML or PDF as response
```

> ✅ Reuses 100% of your existing Java code — no logic duplication.
>  
> ✅ Node.js doesn’t need JVM or Java knowledge.
>  
> ✅ Scalable, deployable, monitorable.

---

# 🧩 STEP-BY-STEP IMPLEMENTATION

## 1. 📦 Add Spring Boot (if not already using it)

Update `pom.xml`:

```xml
<dependencies>
    <!-- Spring Boot Web -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
        <version>3.2.5</version>
    </dependency>

    <!-- Your existing dependencies: Jackson, PDFBox, FreeMarker, etc. -->

    <!-- Optional: For file upload support -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
</dependencies>

<build>
    <plugins>
        <plugin>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-maven-plugin</artifactId>
        </plugin>
    </plugins>
</build>
```

---

## 2. 📄 Create Request DTO

```java
// src/main/java/com/yourcompany/dto/TemplateMergeRequest.java

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class TemplateMergeRequest {

    @NotBlank
    private String templateType; // "HTML", "HTML_TO_PDF", "PDF_FORM"

    private String templateContent; // for HTML templates
    private String templateFilePath; // for PDF_FORM (optional)

    @NotBlank
    private String mappingSpecJson;

    private Object data; // your DB data as JSON object

    // Getters & Setters
    public String getTemplateType() { return templateType; }
    public void setTemplateType(String templateType) { this.templateType = templateType; }

    public String getTemplateContent() { return templateContent; }
    public void setTemplateContent(String templateContent) { this.templateContent = templateContent; }

    public String getTemplateFilePath() { return templateFilePath; }
    public void setTemplateFilePath(String templateFilePath) { this.templateFilePath = templateFilePath; }

    public String getMappingSpecJson() { return mappingSpecJson; }
    public void setMappingSpecJson(String mappingSpecJson) { this.mappingSpecJson = mappingSpecJson; }

    public Object getData() { return data; }
    public void setData(Object data) { this.data = data; }
}
```

> 💡 Tip: For `PDF_FORM`, Node.js will send `templateFilePath` (e.g., “templates/order-form.pdf”) — this assumes the file exists on the Java service’s filesystem. Alternatively, you can accept file uploads — see enhancement below.

---

## 3. 🧑‍💻 Create REST Controller

```java
// src/main/java/com/yourcompany/controller/TemplateMergeController.java

import com.yourcompany.dto.TemplateMergeRequest;
import com.yourcompany.TemplateMerger;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/template")
public class TemplateMergeController {

    private final TemplateMerger templateMerger = new TemplateMerger();

    @PostMapping("/merge")
    public ResponseEntity<byte[]> mergeTemplate(@RequestBody TemplateMergeRequest request) {
        try {
            // Convert data to Map (assuming Jackson can handle it)
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> dataMap = (java.util.Map<String, Object>) request.getData();

            // Determine template source
            Object templateSource = "PDF_FORM".equals(request.getTemplateType())
                    ? request.getTemplateFilePath()
                    : request.getTemplateContent();

            // Determine types
            TemplateMerger.TemplateType templateType = TemplateMerger.TemplateType.valueOf(request.getTemplateType());
            TemplateMerger.OutputFormat outputFormat = switch (templateType) {
                case HTML -> TemplateMerger.OutputFormat.HTML;
                case HTML_TO_PDF, PDF_FORM -> TemplateMerger.OutputFormat.PDF;
            };

            // Execute merge
            byte[] result = templateMerger.merge(
                    templateSource,
                    dataMap,
                    request.getMappingSpecJson(),
                    outputFormat,
                    templateType
            );

            // Set response headers
            HttpHeaders headers = new HttpHeaders();
            String contentType = (outputFormat == TemplateMerger.OutputFormat.HTML)
                    ? MediaType.TEXT_HTML_VALUE
                    : MediaType.APPLICATION_PDF_VALUE;
            headers.setContentType(MediaType.parseMediaType(contentType));
            headers.setContentDispositionFormData("attachment", "output." + outputFormat.name().toLowerCase());

            return new ResponseEntity<>(result, headers, HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("Error: " + e.getMessage()).getBytes());
        }
    }
}
```

---

## 4. 🚀 Create Spring Boot Main Class

```java
// src/main/java/com/yourcompany/TemplateMergerApplication.java

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TemplateMergerApplication {
    public static void main(String[] args) {
        SpringApplication.run(TemplateMergerApplication.class, args);
    }
}
```

---

## 5. ⚙️ Configure `application.properties`

```properties
server.port=8085
spring.servlet.multipart.enabled=true
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
```

---

## 6. ▶️ Run the Service

```bash
mvn spring-boot:run
```

Service will be available at:

```
POST http://localhost:8085/api/template/merge
```

---

# 🌐 NODE.JS CLIENT EXAMPLE

Now any Node.js service can call it:

### ✅ Example: Fill PDF Form

```javascript
// fill-pdf-form.js
const axios = require('axios');
const fs = require('fs');

async function fillPdfForm() {
    const requestData = {
        templateType: "PDF_FORM",
        templateFilePath: "./templates/order-form.pdf", // ← path known to Java service
        mappingSpecJson: JSON.stringify({
            mappings: [
                { sourceField: "user_name", targetPlaceholder: "full_name" },
                { sourceField: "user_email", targetPlaceholder: "email" }
            ]
        }),
         {
            user_name: "Alice Johnson",
            user_email: "alice@example.com"
        }
    };

    try {
        const response = await axios.post(
            'http://localhost:8085/api/template/merge',
            requestData,
            {
                responseType: 'arraybuffer', // Important for binary PDF
                headers: {
                    'Content-Type': 'application/json'
                }
            }
        );

        fs.writeFileSync('output-filled.pdf', response.data);
        console.log('✅ PDF saved: output-filled.pdf');

    } catch (error) {
        console.error('❌ Error:', error.message);
    }
}

fillPdfForm();
```

### ✅ Example: Generate PDF from HTML Template

```javascript
const axios = require('axios');
const fs = require('fs');

const htmlTemplate = `
<html>
<body>
  <h1>Hello {{name}}!</h1>
  <p>Email: {{email}}</p>
</body>
</html>`;

const requestData = {
    templateType: "HTML_TO_PDF",
    templateContent: htmlTemplate,
    mappingSpecJson: JSON.stringify({
        mappings: [
            { sourceField: "user_name", targetPlaceholder: "{{name}}" },
            { sourceField: "user_email", targetPlaceholder: "{{email}}" }
        ]
    }),
     {
        user_name: "Bob Smith",
        user_email: "bob@example.com"
    }
};

axios.post('http://localhost:8085/api/template/merge', requestData, {
    responseType: 'arraybuffer'
})
.then(res => {
    fs.writeFileSync('output-from-html.pdf', res.data);
    console.log('✅ PDF from HTML saved!');
})
.catch(err => console.error(err));
```

---

# 🚀 DEPLOYMENT OPTIONS

- Run as standalone JAR: `java -jar template-merger-service.jar`
- Containerize with Docker → deploy on Kubernetes/ECS/EKS
- Deploy on serverless (AWS Lambda with Custom Runtime or GraalVM native image — advanced)

---

# ✅ ADVANTAGES

| Benefit | Description |
|--------|-------------|
| **Reuse Logic** | No duplication — Java library is source of truth |
| **Polyglot** | Any language can use it via HTTP |
| **Scalable** | Deploy behind load balancer, scale horizontally |
| **Maintainable** | Fix bugs or add features in one place |
| **Secure** | Add auth, rate limiting, logging at API gateway |

---

# 🧩 ENHANCEMENTS

### 1. ✅ Upload Template Files (instead of file paths)

Modify request to accept `multipart/form-data`:

```java
@PostMapping(path = "/merge-with-upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
public ResponseEntity<byte[]> mergeWithUpload(
        @RequestParam("template") MultipartFile templateFile,
        @RequestParam("mappingSpecJson") String mappingSpecJson,
        @RequestParam("data") String dataJson,
        @RequestParam("templateType") String templateType) { ... }
```

Node.js then uploads the PDF/HTML file directly.

---

### 2. ✅ Add API Key / JWT Authentication

```java
// In controller method
String apiKey = request.getHeader("X-API-Key");
if (!"secret123".equals(apiKey)) {
    return ResponseEntity.status(401).build();
}
```

---

### 3. ✅ Add Logging, Metrics, Tracing

Use Spring Boot Actuator + Micrometer + Zipkin.

---

### 4. ✅ Cache Templates

If same HTML/PDF template is reused, cache parsed version in memory.

---

### 5. ✅ Queue Long Operations

For batch processing, accept request → return 202 Accepted → process async → webhook/callback when done.

---

# 🔄 ALTERNATIVES (NOT RECOMMENDED)

| Approach | Why Not Recommended |
|----------|---------------------|
| ❌ Call Java from Node.js via `child_process` | Fragile, platform-dependent, hard to deploy |
| ❌ GraalVM polyglot (Node.js calling Java directly) | Experimental, complex setup, limited production use |
| ❌ Port Java logic to JavaScript | Duplicates logic, diverges over time, PDFBox not available in JS |

---

# ✅ FINAL RECOMMENDATION

> ✅ **Wrap your Java library in a Spring Boot REST API.**  
> ✅ Let Node.js (and any other service) call it via HTTP.  
> ✅ Keep business logic in one place — your battle-tested Java code.

This is the **standard, scalable, enterprise-grade approach** used by companies like Netflix, Uber, and Airbnb to share logic across polyglot microservices.

---

## 🎯 BONUS: Dockerfile for Easy Deployment

```Dockerfile
FROM eclipse-temurin:17-jre-alpine
COPY target/template-merger-*.jar /app.jar
EXPOSE 8085
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

Build & run:

```bash
mvn clean package
docker build -t template-merger-service .
docker run -p 8085:8085 -v ./templates:/templates template-merger-service
```

Now Node.js can reach it at `http://host.docker.internal:8085` or via service name in Kubernetes.

---

## ✅ You’re Done!

Your Java library is now a **reusable, language-agnostic microservice** — ready for Node.js, Python, mobile apps, or even CLI tools to consume.
 🚀
