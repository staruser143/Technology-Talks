Great! You want to create a **flexible, pluggable template source system** for your library. Here's a comprehensive architecture that supports multiple storage backends while keeping it clean and extensible.

## 1. Define the Core Abstraction

### Template Source Interface
```java
// Core interface for template retrieval
public interface TemplateSource {
    /**
     * Retrieve template content by name
     * @param templateName name of the template (e.g., "invoice.ftl")
     * @return template content as String
     * @throws TemplateNotFoundException if template doesn't exist
     */
    String getTemplate(String templateName) throws TemplateNotFoundException;
    
    /**
     * Check if template exists
     */
    boolean exists(String templateName);
    
    /**
     * Optional: List available templates (useful for validation)
     */
    Set<String> listTemplates();
}
```

### Custom Exception
```java
public class TemplateNotFoundException extends RuntimeException {
    public TemplateNotFoundException(String templateName) {
        super("Template not found: " + templateName);
    }
}
```

## 2. Create Implementation for Each Storage Type

### Classpath Template Source
```java
@Component
@ConditionalOnProperty(prefix = "template-library", name = "source-type", havingValue = "classpath", matchIfMissing = true)
public class ClasspathTemplateSource implements TemplateSource {
    
    private final ResourceLoader resourceLoader;
    private final String basePath;
    
    public ClasspathTemplateSource(ResourceLoader resourceLoader, 
                                 TemplateLibraryProperties properties) {
        this.resourceLoader = resourceLoader;
        this.basePath = properties.getClasspathBasePath();
    }
    
    @Override
    public String getTemplate(String templateName) {
        try {
            Resource resource = resourceLoader.getResource("classpath:" + basePath + "/" + templateName);
            if (!resource.exists()) {
                throw new TemplateNotFoundException(templateName);
            }
            return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Error reading template: " + templateName, e);
        }
    }
    
    @Override
    public boolean exists(String templateName) {
        Resource resource = resourceLoader.getResource("classpath:" + basePath + "/" + templateName);
        return resource.exists();
    }
    
    @Override
    public Set<String> listTemplates() {
        // Classpath listing is tricky - you might skip this or use ClassPathResource
        return Collections.emptySet();
    }
}
```

### File System Template Source
```java
@Component
@ConditionalOnProperty(prefix = "template-library", name = "source-type", havingValue = "filesystem")
public class FileSystemTemplateSource implements TemplateSource {
    
    private final Path basePath;
    
    public FileSystemTemplateSource(TemplateLibraryProperties properties) {
        this.basePath = Paths.get(properties.getFilesystemPath()).toAbsolutePath();
        if (!Files.exists(basePath) || !Files.isDirectory(basePath)) {
            throw new IllegalArgumentException("Template directory does not exist: " + basePath);
        }
    }
    
    @Override
    public String getTemplate(String templateName) {
        Path templatePath = basePath.resolve(templateName);
        if (!Files.exists(templatePath)) {
            throw new TemplateNotFoundException(templateName);
        }
        try {
            return Files.readString(templatePath, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Error reading template: " + templateName, e);
        }
    }
    
    @Override
    public boolean exists(String templateName) {
        return Files.exists(basePath.resolve(templateName));
    }
    
    @Override
    public Set<String> listTemplates() {
        try (Stream<Path> paths = Files.walk(basePath, 1)) {
            return paths
                .filter(Files::isRegularFile)
                .map(path -> basePath.relativize(path).toString())
                .collect(Collectors.toSet());
        } catch (IOException e) {
            throw new RuntimeException("Error listing templates", e);
        }
    }
}
```

### Cloud Storage Interface (Generic)
```java
// Generic interface for cloud storage
public interface CloudTemplateSource extends TemplateSource {
    // Could add cloud-specific methods if needed
}
```

### S3 Template Source
```java
@Component
@ConditionalOnProperty(prefix = "template-library", name = "source-type", havingValue = "s3")
@ConditionalOnClass(name = "software.amazon.awssdk.services.s3.S3Client")
public class S3TemplateSource implements CloudTemplateSource {
    
    private final S3Client s3Client;
    private final String bucketName;
    private final String basePath;
    
    public S3TemplateSource(TemplateLibraryProperties properties) {
        this.bucketName = properties.getS3().getBucketName();
        this.basePath = properties.getS3().getBasePath();
        this.s3Client = S3Client.builder()
            .region(Region.of(properties.getS3().getRegion()))
            .build();
    }
    
    @Override
    public String getTemplate(String templateName) {
        try {
            GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(basePath + "/" + templateName)
                .build();
            
            ResponseInputStream<GetObjectResponse> response = s3Client.getObject(request);
            return new String(response.readAllBytes(), StandardCharsets.UTF_8);
        } catch (S3Exception e) {
            if (e.statusCode() == 404) {
                throw new TemplateNotFoundException(templateName);
            }
            throw new RuntimeException("Error reading S3 template: " + templateName, e);
        } catch (IOException e) {
            throw new RuntimeException("Error reading S3 template: " + templateName, e);
        }
    }
    
    @Override
    public boolean exists(String templateName) {
        try {
            HeadObjectRequest request = HeadObjectRequest.builder()
                .bucket(bucketName)
                .key(basePath + "/" + templateName)
                .build();
            s3Client.headObject(request);
            return true;
        } catch (S3Exception e) {
            return e.statusCode() != 404;
        }
    }
    
    @Override
    public Set<String> listTemplates() {
        ListObjectsV2Request request = ListObjectsV2Request.builder()
            .bucket(bucketName)
            .prefix(basePath + "/")
            .build();
        
        return s3Client.listObjectsV2(request)
            .contents()
            .stream()
            .map(S3Object::key)
            .map(key -> key.substring(basePath.length() + 1))
            .collect(Collectors.toSet());
    }
}
```

### MongoDB Template Source
```java
@Component
@ConditionalOnProperty(prefix = "template-library", name = "source-type", havingValue = "mongodb")
@ConditionalOnClass(name = "org.springframework.data.mongodb.core.MongoTemplate")
public class MongoTemplateSource implements TemplateSource {
    
    private final MongoTemplate mongoTemplate;
    private final String collectionName;
    
    public MongoTemplateSource(MongoTemplate mongoTemplate, TemplateLibraryProperties properties) {
        this.mongoTemplate = mongoTemplate;
        this.collectionName = properties.getMongo().getCollectionName();
    }
    
    @Override
    public String getTemplate(String templateName) {
        TemplateDocument doc = mongoTemplate.findById(templateName, TemplateDocument.class, collectionName);
        if (doc == null) {
            throw new TemplateNotFoundException(templateName);
        }
        return doc.getContent();
    }
    
    @Override
    public boolean exists(String templateName) {
        return mongoTemplate.exists(Query.query(Criteria.where("_id").is(templateName)), 
                                   TemplateDocument.class, collectionName);
    }
    
    @Override
    public Set<String> listTemplates() {
        return mongoTemplate.findAll(TemplateDocument.class, collectionName)
            .stream()
            .map(TemplateDocument::getId)
            .collect(Collectors.toSet());
    }
    
    // MongoDB document structure
    @Document
    static class TemplateDocument {
        @Id
        private String id;
        private String content;
        private LocalDateTime lastModified;
        
        // constructors, getters, setters
    }
}
```

## 3. Configuration Properties

```java
@ConfigurationProperties(prefix = "template-library")
public class TemplateLibraryProperties {
    
    private String sourceType = "classpath"; // default
    
    // Classpath settings
    private String classpathBasePath = "templates";
    
    // Filesystem settings
    private String filesystemPath = "/opt/templates";
    
    // S3 settings
    private S3Config s3 = new S3Config();
    
    // MongoDB settings
    private MongoConfig mongo = new MongoConfig();
    
    // Getters and setters
    
    public static class S3Config {
        private String bucketName;
        private String basePath = "";
        private String region = "us-east-1";
        // getters/setters
    }
    
    public static class MongoConfig {
        private String collectionName = "templates";
        // getters/setters
    }
}
```

## 4. Main Service Using TemplateSource

```java
@Service
public class TemplateMerger {
    
    private final TemplateSource templateSource;
    private final Configuration freemarkerConfig;
    
    public TemplateMerger(TemplateSource templateSource, Configuration freemarkerConfig) {
        this.templateSource = templateSource;
        this.freemarkerConfig = freemarkerConfig;
    }
    
    public String mergeTemplate(String templateName, Map<String, Object> model) {
        String templateContent = templateSource.getTemplate(templateName);
        
        try {
            Template template = new Template(templateName, 
                new StringReader(templateContent), freemarkerConfig);
            
            StringWriter writer = new StringWriter();
            template.process(model, writer);
            return writer.toString();
        } catch (TemplateException | IOException e) {
            throw new RuntimeException("Error merging template: " + templateName, e);
        }
    }
}
```

## 5. Auto-Configuration

```java
@Configuration
@EnableConfigurationProperties(TemplateLibraryProperties.class)
public class TemplateLibraryAutoConfiguration {
    
    @Bean
    @ConditionalOnMissingBean
    public TemplateMerger templateMerger(TemplateSource templateSource, 
                                       Configuration freemarkerConfig) {
        return new TemplateMerger(templateSource, freemarkerConfig);
    }
    
    // Freemarker configuration bean (if needed)
    @Bean
    @ConditionalOnMissingBean
    public Configuration freemarkerConfiguration() {
        Configuration config = new Configuration(Configuration.VERSION_2_3_31);
        config.setDefaultEncoding("UTF-8");
        config.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        return config;
    }
}
```

## 6. Usage Examples

### Classpath (Default)
```yaml
# application.yml
template-library:
  source-type: classpath
  classpath-base-path: templates
```

### File System
```yaml
template-library:
  source-type: filesystem
  filesystem-path: /var/myapp/templates
```

### S3
```yaml
template-library:
  source-type: s3
  s3:
    bucket-name: my-templates-bucket
    base-path: prod/templates
    region: us-west-2
```

### MongoDB
```yaml
template-library:
  source-type: mongodb
  mongo:
    collection-name: app_templates
```

## 7. Making it Extensible

### Custom Template Source Interface
```java
// Allow consumers to implement their own sources
public interface CustomTemplateSource extends TemplateSource {
    String getSourceType();
}
```

### Auto-discovery for Custom Sources
```java
@Configuration
public class CustomTemplateSourceConfiguration {
    
    @Bean
    @ConditionalOnBean(CustomTemplateSource.class)
    public TemplateSource customTemplateSource(List<CustomTemplateSource> customSources,
                                             TemplateLibraryProperties properties) {
        return customSources.stream()
            .filter(source -> source.getSourceType().equals(properties.getSourceType()))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("No custom template source found for type: " + properties.getSourceType()));
    }
}
```

## 8. Dependency Management

In your library's `pom.xml`, make cloud dependencies **optional**:

```xml
<dependencies>
    <!-- Required dependencies -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-freemarker</artifactId>
    </dependency>
    
    <!-- Optional dependencies -->
    <dependency>
        <groupId>software.amazon.awssdk</groupId>
        <artifactId>s3</artifactId>
        <optional>true</optional>
    </dependency>
    
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-mongodb</artifactId>
        <optional>true</optional>
    </dependency>
</dependencies>
```

## Key Benefits of This Approach

1. **Pluggable Architecture**: Easy to add new storage types
2. **Conditional Loading**: Only loads implementations when configured
3. **Cloud Agnostic**: Same interface for all storage types
4. **Extensible**: Consumers can implement custom sources
5. **Spring Boot Friendly**: Uses standard Spring Boot patterns
6. **Optional Dependencies**: Library doesn't force cloud dependencies on consumers

This architecture gives you maximum flexibility while maintaining clean, testable code that follows Spring Boot best practices.