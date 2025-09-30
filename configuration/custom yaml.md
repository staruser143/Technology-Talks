There are several ways to use a custom YAML file for configuration in Spring Boot instead of (or in addition to) the default `application.yml`. Here are the main approaches:

## 1. Using `@PropertySource` with YAML Support

**Important**: `@PropertySource` doesn't natively support YAML in Spring Boot, so you need a custom factory.

### Step 1: Create a YAML Property Source Factory

```java
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertySourceFactory;

import java.util.Properties;

public class YamlPropertySourceFactory implements PropertySourceFactory {
    @Override
    public PropertySource<?> createPropertySource(String name, EncodedResource resource) {
        YamlPropertiesFactoryBean factory = new YamlPropertiesFactoryBean();
        factory.setResources(resource.getResource());
        Properties properties = factory.getObject();
        return new PropertiesPropertySource(resource.getResource().getFilename(), properties);
    }
}
```

### Step 2: Use it with `@PropertySource`

```java
@Configuration
@PropertySource(value = "classpath:custom-config.yml", factory = YamlPropertySourceFactory.class)
public class CustomConfig {
    // Your configuration beans
}
```

## 2. Using `@ConfigurationProperties` with Custom YAML

This is the **recommended approach** for type-safe configuration.

### Step 1: Create a Configuration Properties Class

```java
@ConfigurationProperties(prefix = "myapp")
@Component
public class MyAppProperties {
    private String name;
    private int port;
    private Database database = new Database();
    
    // Getters and setters
    
    public static class Database {
        private String url;
        private String username;
        private String password;
        
        // Getters and setters
    }
}
```

### Step 2: Load the custom YAML file programmatically

```java
@Configuration
public class CustomYamlConfig {
    
    @Bean
    @ConfigurationProperties(prefix = "myapp")
    public MyAppProperties myAppProperties() {
        return new MyAppProperties();
    }
    
    @PostConstruct
    public void loadCustomYaml() {
        // This approach requires additional setup
    }
}
```

## 3. Programmatic Loading with `YamlPropertiesFactoryBean`

```java
@Configuration
public class CustomConfigLoader {
    
    @Bean
    public static PropertySourcesPlaceholderConfigurer properties() {
        PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer = 
            new PropertySourcesPlaceholderConfigurer();
        
        YamlPropertiesFactoryBean yaml = new YamlPropertiesFactoryBean();
        yaml.setResources(new ClassPathResource("custom-config.yml"));
        propertySourcesPlaceholderConfigurer.setProperties(yaml.getObject());
        
        return propertySourcesPlaceholderConfigurer;
    }
}
```

## 4. Using Spring Boot's Built-in Profile Support

You can create profile-specific YAML files:

- `application-dev.yml`
- `application-prod.yml`
- `application-custom.yml`

Then activate the profile:

```yaml
# application.yml
spring:
  profiles:
    active: custom
```

Or via command line:
```bash
java -jar myapp.jar --spring.profiles.active=custom
```

## 5. Loading External YAML Files

For external files (outside the classpath):

```java
@Configuration
public class ExternalYamlConfig {
    
    @Bean
    public PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        PropertySourcesPlaceholderConfigurer configurer = 
            new PropertySourcesPlaceholderConfigurer();
        
        YamlPropertiesFactoryBean yaml = new YamlPropertiesFactoryBean();
        yaml.setResources(new FileSystemResource("/path/to/external-config.yml"));
        configurer.setProperties(yaml.getObject());
        
        return configurer;
    }
}
```

## 6. Complete Example with Custom YAML

**custom-config.yml** (in `src/main/resources`):
```yaml
myapp:
  name: MyCustomApp
  port: 8081
  database:
    url: jdbc:postgresql://localhost:5432/mydb
    username: admin
    password: secret
```

**Configuration class**:
```java
@Configuration
@PropertySource(value = "classpath:custom-config.yml", factory = YamlPropertySourceFactory.class)
@ConfigurationProperties(prefix = "myapp")
@Data // Lombok annotation for getters/setters
public class MyAppConfig {
    private String name;
    private int port;
    private Database database;
    
    @Data
    public static class Database {
        private String url;
        private String username;
        private String password;
    }
}
```

## Key Points:

1. **`@ConfigurationProperties` is preferred** over `@Value` for complex configurations
2. **Enable configuration properties** by adding `@EnableConfigurationProperties` or using `@Component`
3. **Validation**: You can add JSR-303 validation annotations to your configuration properties
4. **Relaxed binding**: Spring Boot supports flexible property binding (kebab-case, camelCase, etc.)
5. **Type safety**: `@ConfigurationProperties` provides compile-time type safety

Choose the approach that best fits your use case. For most scenarios, combining the custom `YamlPropertySourceFactory` with `@ConfigurationProperties` provides the best balance of flexibility and type safety.