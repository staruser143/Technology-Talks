To create a library from a Spring Boot application that can be used as a Maven dependency, you need to convert your Spring Boot **application** into a **library** (JAR). Here's a step-by-step guide:

## 1. Modify Your `pom.xml`

### Remove Spring Boot Maven Plugin (or configure it properly)
The Spring Boot Maven plugin creates an executable "fat" JAR by default, which isn't suitable for libraries. You have two options:

**Option A: Remove the Spring Boot Maven Plugin entirely**
```xml
<!-- Remove this section if present -->
<!--
<plugin>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-maven-plugin</artifactId>
</plugin>
-->
```

**Option B: Configure the plugin to create both executable and plain JARs**
```xml
<plugin>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-maven-plugin</artifactId>
    <configuration>
        <classifier>exec</classifier>
    </configuration>
</plugin>
```

### Change Packaging Type (Optional)
Keep `jar` packaging (which is default):
```xml
<packaging>jar</packaging>
```

### Define Your Library Metadata
```xml
<groupId>com.yourcompany</groupId>
<artifactId>your-library-name</artifactId>
<version>1.0.0</version>
<name>Your Library Name</name>
<description>Description of your library</description>
```

## 2. Remove Spring Boot Application Class

Remove or exclude your main `@SpringBootApplication` class, as libraries shouldn't start Spring Boot contexts automatically.

```java
// Remove this class or move it to test scope
@SpringBootApplication
public class YourApplication {
    public static void main(String[] args) {
        SpringApplication.run(YourApplication.class, args);
    }
}
```

## 3. Organize Your Code for Reusability

Structure your library with clear public APIs:

```java
// Public service that consumers will use
@Service
public class YourLibraryService {
    // Your business logic here
}

// Configuration class that users can import
@Configuration
@EnableConfigurationProperties(YourLibraryProperties.class)
public class YourLibraryAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public YourLibraryService yourLibraryService() {
        return new YourLibraryService();
    }
}

// Properties class for configuration
@ConfigurationProperties(prefix = "your-library")
public class YourLibraryProperties {
    private String someProperty = "default-value";
    // getters and setters
}
```

## 4. Create `spring.factories` for Auto-Configuration (Optional)

If you want Spring Boot auto-configuration, create `src/main/resources/META-INF/spring.factories`:

```properties
org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
com.yourcompany.yourlibrary.YourLibraryAutoConfiguration
```

For Spring Boot 2.4+, you can also use `spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`:

```
com.yourcompany.yourlibrary.YourLibraryAutoConfiguration
```

## 5. Handle Dependencies Properly

Mark Spring Boot dependencies as `provided` if they'll be provided by the consuming application:

```xml
<dependencies>
    <!-- Your library's compile dependencies -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter</artifactId>
        <scope>provided</scope>
    </dependency>
    
    <!-- Dependencies that should be included in your library -->
    <dependency>
        <groupId>com.fasterxml.jackson.core</groupId>
        <artifactId>jackson-databind</artifactId>
        <scope>compile</scope>
    </dependency>
</dependencies>
```

## 6. Build and Install/Deploy

### Build the library
```bash
mvn clean install
```

### Deploy to repository (if using private/public repo)
```bash
mvn deploy
```

## 7. Using the Library in Another Project

Add the dependency to the consuming project's `pom.xml`:

```xml
<dependency>
    <groupId>com.yourcompany</groupId>
    <artifactId>your-library-name</artifactId>
    <version>1.0.0</version>
</dependency>
```

If you used the classifier approach (Option B above), specify the classifier:

```xml
<dependency>
    <groupId>com.yourcompany</groupId>
    <artifactId>your-library-name</artifactId>
    <version>1.0.0</version>
    <classifier>exec</classifier>
</dependency>
```

## Complete Example `pom.xml` for Library

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.yourcompany</groupId>
    <artifactId>spring-boot-library</artifactId>
    <version>1.0.0</version>
    <packaging>jar</packaging>

    <name>Spring Boot Library</name>
    <description>Reusable Spring Boot library</description>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.1.0</version>
        <relativePath/>
    </parent>

    <properties>
        <java.version>17</java.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter</artifactId>
            <scope>provided</scope>
        </dependency>
        
        <!-- Add other dependencies your library needs -->
    </dependencies>

    <build>
        <plugins>
            <!-- Optional: Keep this if you want both executable and plain JARs -->
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <classifier>exec</classifier>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

## Key Points to Remember

1. **No main class**: Libraries shouldn't have a main Spring Boot application class
2. **Proper dependency scoping**: Use `provided` scope for Spring Boot dependencies
3. **Auto-configuration**: Use `spring.factories` or the new auto-configuration mechanism
4. **Clean APIs**: Expose only what consumers need
5. **Versioning**: Follow semantic versioning for your library

This approach creates a clean, reusable Spring Boot library that can be easily consumed by other Spring Boot applications as a Maven dependency.