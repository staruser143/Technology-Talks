Great question! Let me clarify where and when to use `@EnableConfigurationProperties` vs `@Component` for your configuration properties classes.

## Option 1: Using `@Component` (Simpler approach)

Add `@Component` directly to your configuration properties class:

```java
// MyAppProperties.java
@ConfigurationProperties(prefix = "myapp")
@Component  // 👈 Add @Component here
public class MyAppProperties {
    private String name;
    private int port;
    // getters and setters
}
```

**Where to put it**: Directly on the configuration properties class itself.

**Pros**: Simple, automatic bean registration
**Cons**: Tightly couples your configuration class to Spring

## Option 2: Using `@EnableConfigurationProperties` (Recommended approach)

### Step 1: Create your configuration properties class WITHOUT `@Component`

```java
// MyAppProperties.java
@ConfigurationProperties(prefix = "myapp")
public class MyAppProperties {  // 👈 No @Component here
    private String name;
    private int port;
    // getters and setters
}
```

### Step 2: Enable it in your configuration class

You have several options for where to put `@EnableConfigurationProperties`:

#### Option 2A: On your main application class (most common)

```java
// Application.java
@SpringBootApplication
@EnableConfigurationProperties(MyAppProperties.class)  // 👈 Here
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

#### Option 2B: On a dedicated configuration class

```java
// AppConfig.java
@Configuration
@EnableConfigurationProperties(MyAppProperties.class)  // 👈 Here
public class AppConfig {
    // other configuration beans
}
```

#### Option 2C: Enable multiple configuration properties classes

```java
@SpringBootApplication
@EnableConfigurationProperties({
    MyAppProperties.class,
    DatabaseProperties.class,
    CacheProperties.class
})
public class Application {
    // ...
}
```

## Complete Working Example

**custom-config.yml**:
```yaml
myapp:
  name: MyApplication
  port: 8081
database:
  url: jdbc:postgresql://localhost:5432/mydb
  username: admin
```

**MyAppProperties.java**:
```java
@ConfigurationProperties(prefix = "myapp")
public class MyAppProperties {
    private String name;
    private int port;
    
    // getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getPort() { return port; }
    public void setPort(int port) { this.port = port; }
}
```

**DatabaseProperties.java**:
```java
@ConfigurationProperties(prefix = "database")
public class DatabaseProperties {
    private String url;
    private String username;
    
    // getters and setters
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
}
```

**Application.java**:
```java
@SpringBootApplication
@EnableConfigurationProperties({
    MyAppProperties.class,
    DatabaseProperties.class
})
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

**Usage in a service**:
```java
@Service
public class MyService {
    
    private final MyAppProperties appProperties;
    private final DatabaseProperties dbProperties;
    
    // Constructor injection (recommended)
    public MyService(MyAppProperties appProperties, DatabaseProperties dbProperties) {
        this.appProperties = appProperties;
        this.dbProperties = dbProperties;
    }
    
    public void doSomething() {
        System.out.println("App name: " + appProperties.getName());
        System.out.println("DB URL: " + dbProperties.getUrl());
    }
}
```

## Key Recommendations:

1. **Use `@EnableConfigurationProperties`** (Option 2) rather than `@Component` because:
   - It's more explicit about your intent
   - Better separation of concerns
   - Easier to test (your properties class isn't tied to Spring)
   - Follows Spring Boot best practices

2. **Place `@EnableConfigurationProperties` on your main application class** if you have just a few configuration classes, or create a dedicated configuration class if you have many.

3. **Always use constructor injection** to get your configuration properties in your services (as shown above).

4. **Don't forget** that you still need your custom `YamlPropertySourceFactory` if you're loading a custom YAML file instead of `application.yml`.

The `@EnableConfigurationProperties` approach is the **officially recommended way** by Spring Boot documentation and provides better maintainability and testability.