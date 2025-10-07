For a data pipeline with field mapping and transformations, you can create a flexible YAML configuration that handles direct mappings, renamed fields, and computed fields. Here's a comprehensive approach:

## YAML Configuration Structure

```yaml
pipeline:
  name: customer-data-pipeline
  source:
    type: database
    table: customers_raw
  target:
    type: database  
    table: customers_processed
  
  field-mappings:
    # Direct mapping (same field names)
    - source: id
      target: id
      
    - source: email
      target: email
      
    # Renamed fields
    - source: first_name
      target: firstName
      
    - source: last_name  
      target: lastName
      
    # Computed fields with transformations
    - target: fullName
      computed: true
      sources: [first_name, last_name]
      transform: CONCAT
      separator: " "
      
    - target: accountStatus
      computed: true
      sources: [status_code]
      transform: MAP
      mapping:
        "1": "ACTIVE"
        "2": "INACTIVE"
        "3": "SUSPENDED"
        "default": "UNKNOWN"
        
    - target: customerTier
      computed: true
      sources: [annual_spend, membership_years]
      transform: CUSTOM
      function: calculateCustomerTier
      
    - target: registrationDate
      computed: true
      sources: [created_timestamp]
      transform: DATE_FORMAT
      pattern: "yyyy-MM-dd"
      
    - target: contactInfo
      computed: true
      sources: [email, phone, address]
      transform: JSON_OBJECT
      keys: [email, phoneNumber, address]
      
    # Conditional mapping
    - target: preferredContact
      computed: true
      sources: [email, phone]
      transform: CONDITIONAL
      condition: "email != null ? email : phone"
```

## Java Configuration Classes

```java
@ConfigurationProperties(prefix = "pipeline")
public class PipelineConfig {
    private String name;
    private SourceConfig source;
    private TargetConfig target;
    private List<FieldMapping> fieldMappings = new ArrayList<>();
    
    // getters and setters
}

@Data
public class SourceConfig {
    private String type;
    private String table;
    // other source properties
}

@Data
public class TargetConfig {
    private String type;
    private String table;
    // other target properties
}
```

## Field Mapping Configuration

```java
@Data
public class FieldMapping {
    private String source;           // single source field (for direct mapping)
    private String target;           // target field name
    private Boolean computed = false; // whether this is a computed field
    
    // For computed fields
    private List<String> sources;    // multiple source fields
    private String transform;        // transformation type
    private String separator;        // for concatenation
    private Map<String, String> mapping; // for MAP transform
    private String function;         // for custom functions
    private String pattern;          // for date formatting
    private List<String> keys;       // for JSON object keys
    private String condition;        // for conditional logic
}
```

## Transformation Engine

```java
@Component
public class FieldTransformer {
    
    private final Map<String, Function<List<Object>, Object>> transformers = new HashMap<>();
    
    public FieldTransformer() {
        initializeTransformers();
    }
    
    private void initializeTransformers() {
        transformers.put("CONCAT", this::concatenate);
        transformers.put("MAP", this::mapValues);
        transformers.put("DATE_FORMAT", this::formatDate);
        transformers.put("JSON_OBJECT", this::createJsonObject);
        transformers.put("CONDITIONAL", this::evaluateCondition);
        // Add more as needed
    }
    
    public Object transform(FieldMapping mapping, Map<String, Object> sourceData) {
        if (!mapping.getComputed()) {
            // Direct mapping
            return sourceData.get(mapping.getSource());
        }
        
        // Get source values
        List<Object> sourceValues = mapping.getSources().stream()
            .map(sourceData::get)
            .collect(Collectors.toList());
            
        Function<List<Object>, Object> transformer = transformers.get(mapping.getTransform());
        if (transformer == null) {
            throw new IllegalArgumentException("Unknown transform: " + mapping.getTransform());
        }
        
        return transformer.apply(sourceValues);
    }
    
    private Object concatenate(List<Object> values) {
        String separator = Optional.ofNullable(((FieldMapping) values).getSeparator()).orElse("");
        return values.stream()
            .filter(Objects::nonNull)
            .map(Object::toString)
            .collect(Collectors.joining(separator));
    }
    
    private Object mapValues(List<Object> values) {
        // This would need access to the mapping configuration
        // Implementation depends on how you pass the mapping
        return null;
    }
    
    private Object formatDate(List<Object> values) {
        // Implementation for date formatting
        return null;
    }
    
    private Object createJsonObject(List<Object> values) {
        // Implementation for JSON object creation
        return null;
    }
    
    private Object evaluateCondition(List<Object> values) {
        // Implementation for conditional logic (could use scripting engine)
        return null;
    }
}
```

## Enhanced Field Mapping with Builder Pattern

For more complex scenarios, consider this enhanced structure:

```yaml
pipeline:
  field-mappings:
    # Simple direct mapping
    - from: id
      to: id
      
    # Renamed field
    - from: cust_email
      to: email
      
    # Computed field with expression
    - to: displayName
      expression: "TRIM(firstName) + ' ' + TRIM(lastName)"
      
    # Computed field with function reference
    - to: riskScore
      function: calculateRiskScore
      inputs: [credit_score, income, debt_ratio]
      
    # Conditional mapping
    - to: contactMethod
      conditions:
        - when: "phone != null && phoneVerified == true"
          then: "PHONE"
        - when: "email != null && emailVerified == true"  
          then: "EMAIL"
        - else: "UNKNOWN"
          
    # Array/Collection transformation
    - to: tags
      transform: SPLIT
      source: tag_string
      delimiter: ","
```

## Custom Function Registry

```java
@Component
public class CustomFunctionRegistry {
    private final Map<String, CustomFunction> functions = new HashMap<>();
    
    @PostConstruct
    public void registerFunctions() {
        functions.put("calculateCustomerTier", this::calculateCustomerTier);
        functions.put("calculateRiskScore", this::calculateRiskScore);
    }
    
    public Object execute(String functionName, Map<String, Object> inputs) {
        CustomFunction func = functions.get(functionName);
        if (func == null) {
            throw new IllegalArgumentException("Function not found: " + functionName);
        }
        return func.execute(inputs);
    }
    
    private Object calculateCustomerTier(Map<String, Object> inputs) {
        Double annualSpend = (Double) inputs.get("annual_spend");
        Integer membershipYears = (Integer) inputs.get("membership_years");
        
        if (annualSpend > 10000 && membershipYears > 2) {
            return "PREMIUM";
        } else if (annualSpend > 5000) {
            return "GOLD";
        }
        return "STANDARD";
    }
    
    @FunctionalInterface
    private interface CustomFunction {
        Object execute(Map<String, Object> inputs);
    }
}
```

## Usage in Data Pipeline

```java
@Service
public class DataPipelineService {
    
    private final PipelineConfig pipelineConfig;
    private final FieldTransformer fieldTransformer;
    private final CustomFunctionRegistry functionRegistry;
    
    public DataPipelineService(PipelineConfig pipelineConfig, 
                              FieldTransformer fieldTransformer,
                              CustomFunctionRegistry functionRegistry) {
        this.pipelineConfig = pipelineConfig;
        this.fieldTransformer = fieldTransformer;
        this.functionRegistry = functionRegistry;
    }
    
    public Map<String, Object> transformRecord(Map<String, Object> sourceRecord) {
        Map<String, Object> targetRecord = new HashMap<>();
        
        for (FieldMapping mapping : pipelineConfig.getFieldMappings()) {
            Object value;
            if (mapping.getComputed() && mapping.getFunction() != null) {
                // Handle custom function
                Map<String, Object> functionInputs = new HashMap<>();
                for (String inputField : mapping.getSources()) {
                    functionInputs.put(inputField, sourceRecord.get(inputField));
                }
                value = functionRegistry.execute(mapping.getFunction(), functionInputs);
            } else {
                // Handle standard transformation
                value = fieldTransformer.transform(mapping, sourceRecord);
            }
            
            targetRecord.put(mapping.getTarget(), value);
        }
        
        return targetRecord;
    }
}
```

## Key Benefits of This Approach:

1. **Flexible**: Handles direct mappings, renames, and complex computations
2. **Extensible**: Easy to add new transformation types
3. **Maintainable**: Configuration is externalized in YAML
4. **Type-safe**: Java classes provide structure and validation
5. **Testable**: Each transformation can be unit tested independently

This structure gives you a powerful yet maintainable way to configure your data pipeline mappings while keeping the complexity manageable through well-defined transformation types and custom functions.