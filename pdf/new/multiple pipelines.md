Great question! When you have multiple pipelines with different field mappings, you need a configuration structure that can handle multiple pipeline definitions. Here are several approaches, from simple to more sophisticated:

## Approach 1: Map of Pipelines (Recommended)

### YAML Configuration Structure

```yaml
pipelines:
  customer-pipeline:
    name: Customer Data Pipeline
    source:
      type: database
      table: customers_raw
    target:
      type: database
      table: customers_processed
    field-mappings:
      - source: cust_id
        target: id
      - source: first_name  
        target: firstName
      - target: fullName
        computed: true
        sources: [first_name, last_name]
        transform: CONCAT
        separator: " "
        
  order-pipeline:
    name: Order Processing Pipeline
    source:
      type: api
      endpoint: /orders
    target:
      type: database
      table: orders_processed
    field-mappings:
      - source: order_num
        target: orderNumber
      - source: total_amt
        target: totalAmount
      - target: orderStatus
        computed: true
        sources: [status_code]
        transform: MAP
        mapping:
          "1": "PENDING"
          "2": "PROCESSING"
          "3": "COMPLETED"
          
  product-pipeline:
    name: Product Catalog Pipeline
    source:
      type: file
      path: /data/products.csv
    target:
      type: database
      table: products
    field-mappings:
      - source: prod_name
        target: productName
      - source: cat_id
        target: categoryId
```

### Java Configuration Classes

```java
@ConfigurationProperties(prefix = "pipelines")
@Data
public class PipelinesConfig {
    private Map<String, PipelineConfig> pipelines = new HashMap<>();
}

@Data
public class PipelineConfig {
    private String name;
    private SourceConfig source;
    private TargetConfig target;
    private List<FieldMapping> fieldMappings = new ArrayList<>();
}

// SourceConfig, TargetConfig, FieldMapping classes remain the same as before
```

### Usage in Services

```java
@Service
public class PipelineService {
    
    private final PipelinesConfig pipelinesConfig;
    private final FieldTransformer fieldTransformer;
    
    public PipelineService(PipelinesConfig pipelinesConfig, FieldTransformer fieldTransformer) {
        this.pipelinesConfig = pipelinesConfig;
        this.fieldTransformer = fieldTransformer;
    }
    
    public PipelineConfig getPipeline(String pipelineName) {
        PipelineConfig pipeline = pipelinesConfig.getPipelines().get(pipelineName);
        if (pipeline == null) {
            throw new IllegalArgumentException("Pipeline not found: " + pipelineName);
        }
        return pipeline;
    }
    
    public Map<String, Object> transformRecord(String pipelineName, Map<String, Object> sourceRecord) {
        PipelineConfig pipeline = getPipeline(pipelineName);
        Map<String, Object> targetRecord = new HashMap<>();
        
        for (FieldMapping mapping : pipeline.getFieldMappings()) {
            Object value = fieldTransformer.transform(mapping, sourceRecord);
            targetRecord.put(mapping.getTarget(), value);
        }
        
        return targetRecord;
    }
    
    public Set<String> getPipelineNames() {
        return pipelinesConfig.getPipelines().keySet();
    }
}
```

## Approach 2: List of Pipelines

If you prefer a list structure:

```yaml
pipelines:
  - name: customer-pipeline
    displayName: Customer Data Pipeline
    source:
      type: database
      table: customers_raw
    target:
      type: database
      table: customers_processed
    field-mappings:
      # ... mappings
      
  - name: order-pipeline  
    displayName: Order Processing Pipeline
    source:
      type: api
      endpoint: /orders
    target:
      type: database
      table: orders_processed
    field-mappings:
      # ... mappings
```

```java
@ConfigurationProperties(prefix = "pipelines")
@Data
public class PipelinesConfig {
    private List<PipelineConfig> pipelines = new ArrayList<>();
    
    // Helper method to get pipeline by name
    public PipelineConfig getPipelineByName(String name) {
        return pipelines.stream()
            .filter(p -> name.equals(p.getName()))
            .findFirst()
            .orElse(null);
    }
}
```

## Approach 3: External Configuration Files per Pipeline

For very complex scenarios, you might want separate YAML files:

### Main Configuration
```yaml
# application.yml
pipeline-configs:
  - classpath:pipelines/customer-pipeline.yml
  - classpath:pipelines/order-pipeline.yml
  - file:/etc/myapp/product-pipeline.yml
```

### Individual Pipeline Files
**customer-pipeline.yml**:
```yaml
name: customer-pipeline
displayName: Customer Data Pipeline
source:
  type: database
  table: customers_raw
target:
  type: database
  table: customers_processed
field-mappings:
  # ... mappings
```

### Custom Configuration Loader

```java
@Component
@ConfigurationProperties(prefix = "pipeline-configs")
public class ExternalPipelineConfigLoader {
    
    private List<String> configLocations = new ArrayList<>();
    private final Map<String, PipelineConfig> pipelines = new HashMap<>();
    
    @PostConstruct
    public void loadExternalConfigs() {
        ResourceLoader resourceLoader = new DefaultResourceLoader();
        
        for (String location : configLocations) {
            try {
                Resource resource = resourceLoader.getResource(location);
                if (resource.exists()) {
                    YamlPropertiesFactoryBean yaml = new YamlPropertiesFactoryBean();
                    yaml.setResources(resource);
                    Properties props = yaml.getObject();
                    
                    // Convert properties to PipelineConfig
                    PipelineConfig pipeline = convertPropertiesToPipelineConfig(props);
                    pipelines.put(pipeline.getName(), pipeline);
                }
            } catch (Exception e) {
                log.error("Failed to load pipeline config: " + location, e);
            }
        }
    }
    
    private PipelineConfig convertPropertiesToPipelineConfig(Properties props) {
        // Implementation to convert flat properties to nested objects
        // This can be complex; consider using ObjectMapper instead
        ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
        try {
            return objectMapper.convertValue(props, PipelineConfig.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert properties to PipelineConfig", e);
        }
    }
    
    public PipelineConfig getPipeline(String name) {
        return pipelines.get(name);
    }
}
```

## Approach 4: Profile-Specific Pipelines

Use Spring profiles to activate different pipeline configurations:

### customer-pipeline.yml
```yaml
# src/main/resources/application-customer.yml
pipelines:
  customer-pipeline:
    # ... customer pipeline config
```

### order-pipeline.yml  
```yaml
# src/main/resources/application-order.yml
pipelines:
  order-pipeline:
    # ... order pipeline config
```

### Activate via profiles:
```bash
# Run with customer pipeline
java -jar myapp.jar --spring.profiles.active=customer

# Run with multiple pipelines
java -jar myapp.jar --spring.profiles.active=customer,order
```

## Approach 5: Database-Backed Pipeline Configuration

For dynamic pipeline management:

```java
@Entity
@Table(name = "pipeline_configs")
public class PipelineConfigEntity {
    @Id
    private String pipelineName;
    private String configJson; // Store YAML/JSON as string
    
    @Convert(converter = PipelineConfigConverter.class)
    private PipelineConfig pipelineConfig;
}

@Component
public class DatabasePipelineService {
    
    @Autowired
    private PipelineConfigRepository repository;
    
    private final Map<String, PipelineConfig> cache = new ConcurrentHashMap<>();
    
    public PipelineConfig getPipeline(String pipelineName) {
        return cache.computeIfAbsent(pipelineName, name -> {
            PipelineConfigEntity entity = repository.findById(name)
                .orElseThrow(() -> new IllegalArgumentException("Pipeline not found"));
            return entity.getPipelineConfig();
        });
    }
}
```

## Recommendation

For most use cases, **Approach 1 (Map of Pipelines)** is the best choice because it:

- ✅ Keeps all configuration in one place
- ✅ Is easy to maintain and version control
- ✅ Provides type safety with Java configuration classes
- ✅ Allows easy programmatic access by pipeline name
- ✅ Works well with Spring Boot's configuration binding
- ✅ Supports both simple and complex pipeline definitions

### Complete Working Example

**application.yml**:
```yaml
pipelines:
  customer-sync:
    name: Customer Sync Pipeline
    enabled: true
    field-mappings:
      - source: customer_id
        target: id
      - source: cust_email
        target: email
      - target: status
        computed: true
        sources: [active_flag]
        transform: MAP
        mapping:
          "Y": "ACTIVE"
          "N": "INACTIVE"
          
  order-processing:
    name: Order Processing Pipeline  
    enabled: true
    field-mappings:
      - source: order_id
        target: orderId
      - target: priority
        computed: true
        sources: [order_amount, customer_tier]
        transform: CUSTOM
        function: calculateOrderPriority
```

**Usage**:
```java
@RestController
public class PipelineController {
    
    private final PipelineService pipelineService;
    
    @PostMapping("/transform/{pipelineName}")
    public Map<String, Object> transformRecord(
            @PathVariable String pipelineName,
            @RequestBody Map<String, Object> sourceRecord) {
        return pipelineService.transformRecord(pipelineName, sourceRecord);
    }
    
    @GetMapping("/pipelines")
    public Set<String> listPipelines() {
        return pipelineService.getPipelineNames();
    }
}
```

This approach gives you maximum flexibility while keeping your configuration clean and maintainable!