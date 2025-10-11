Great observation! You're absolutely right to question this. Let me clarify the different approaches and why you might choose each one.

## Your Understanding is Correct

You're right that **the library module can (and often should) have Spring Boot as its parent**, while the **parent POM doesn't need Spring Boot dependencies**. Here are the two main approaches:

## Approach 1: Library Inherits Spring Boot Parent (Recommended for Libraries)

### Parent POM (`pom.xml`)
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0">
    <modelVersion>4.0.0</modelVersion>
    
    <groupId>com.yourcompany</groupId>
    <artifactId>your-library-parent</artifactId>
    <version>1.0.0</version>
    <packaging>pom</packaging>
    
    <!-- No Spring Boot here - just manages modules -->
    <modules>
        <module>library</module>
        <module>test-app</module>
    </modules>
    
    <!-- Optional: Define common properties -->
    <properties>
        <java.version>17</java.version>
        <spring-boot.version>3.1.0</spring.version>
    </properties>
</project>
```

### Library POM (`library/pom.xml`)
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0">
    <modelVersion>4.0.0</modelVersion>
    
    <!-- Library inherits Spring Boot parent directly -->
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.1.0</version>
        <relativePath/>
    </parent>
    
    <groupId>com.yourcompany</groupId>
    <artifactId>library</artifactId>
    <version>1.0.0</version>
    <packaging>jar</packaging>
    
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter</artifactId>
            <scope>provided</scope>
        </dependency>
    </dependencies>
</project>
```

### Test App POM (`test-app/pom.xml`)
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0">
    <modelVersion>4.0.0</modelVersion>
    
    <!-- Test app also inherits Spring Boot parent -->
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.1.0</version>
        <relativePath/>
    </parent>
    
    <groupId>com.yourcompany</groupId>
    <artifactId>test-app</artifactId>
    <version>1.0.0</version>
    <packaging>jar</packaging>
    
    <dependencies>
        <dependency>
            <groupId>com.yourcompany</groupId>
            <artifactId>library</artifactId>
            <version>1.0.0</version>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
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
</project>
```

## Approach 2: Parent POM Inherits Spring Boot (Alternative)

### Parent POM with Spring Boot
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0">
    <modelVersion>4.0.0</modelVersion>
    
    <!-- Parent inherits Spring Boot -->
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.1.0</version>
        <relativePath/>
    </parent>
    
    <groupId>com.yourcompany</groupId>
    <artifactId>your-library-parent</artifactId>
    <version>1.0.0</version>
    <packaging>pom</packaging>
    
    <modules>
        <module>library</module>
        <module>test-app</module>
    </modules>
</project>
```

### Child Modules (inherit from parent)
```xml
<!-- Both library and test-app would have: -->
<parent>
    <groupId>com.yourcompany</groupId>
    <artifactId>your-library-parent</artifactId>
    <version>1.0.0</version>
</parent>
```

## Why Approach 1 is Better for Libraries

### 1. **Clear Separation of Concerns**
- Parent POM only manages project structure
- Each module manages its own Spring Boot version and dependencies
- Library can be used independently of the test application

### 2. **Flexibility in Spring Boot Versions**
- Library and test app can use different Spring Boot versions if needed
- Consumers of your library aren't forced to use your parent POM's Spring Boot version

### 3. **Better for Library Distribution**
- When someone uses your library, they don't inherit your parent POM
- They only get the library JAR with its declared dependencies
- Cleaner dependency tree for consumers

### 4. **Avoids Unnecessary Inheritance**
- Parent POM doesn't need Spring Boot's opinionated defaults
- Keeps the parent POM lightweight and focused on module management

## Key Points About Maven Inheritance

### Library as Independent Artifact
Your library should be **self-contained** and not require consumers to use your parent POM. This means:

```xml
<!-- Good: Library declares its own parent -->
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.1.0</version>
</parent>

<!-- Not ideal: Library depends on your custom parent -->
<parent>
    <groupId>com.yourcompany</groupId>
    <artifactId>your-library-parent</artifactId>
    <version>1.0.0</version>
</parent>
```

### Dependency Management vs Parent Inheritance
If you want to share dependency versions without parent inheritance, use **dependencyManagement**:

```xml
<!-- In parent POM -->
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-dependencies</artifactId>
            <version>3.1.0</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

Then in child modules:
```xml
<!-- No parent inheritance needed -->
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter</artifactId>
        <!-- Version comes from dependencyManagement -->
    </dependency>
</dependencies>
```

## Recommendation

**Use Approach 1** (library directly inherits Spring Boot parent) because:

1. ✅ Your library is truly independent
2. ✅ Consumers don't need to know about your parent POM
3. ✅ Each module can evolve independently
4. ✅ Cleaner architecture and better separation of concerns
5. ✅ Follows Spring Boot library best practices

Your original instinct was correct! The parent POM should focus on **project structure management**, while individual modules handle their **framework dependencies** directly.