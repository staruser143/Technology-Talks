Great observation! You're absolutely right to wonder about this. There are several ways Spring Boot can discover and inject your library classes **without explicit auto-configuration**. Let me explain how this works:

## 1. **Component Scanning (Most Likely Reason)**

### How it works:
When you run your test application with `@SpringBootApplication`, Spring Boot automatically scans for components in:
- The package containing your main application class
- **All sub-packages** of that package

### Example:
```java
// Test Application - in package com.example.testapp
@SpringBootApplication
public class TestApplication {
    // Component scanning includes: com.example.testapp.*
}

// Library classes - if they're in com.example.testapp.lib (sub-package)
@Service
public class YourLibraryService { } // This gets picked up automatically!
```

**If your library classes are in a sub-package of your test application's package**, they get discovered automatically!

## 2. **Explicit Component Scanning**

You might have explicitly told Spring where to look:

```java
@SpringBootApplication
@ComponentScan(basePackages = {"com.example.testapp", "com.yourcompany.library"})
public class TestApplication {
    // Now Spring scans your library package too
}
```

## 3. **Manual Bean Registration in Test Application**

You might have created beans manually in your test application:

```java
@SpringBootApplication
public class TestApplication {
    
    @Bean
    public YourLibraryService yourLibraryService() {
        return new YourLibraryService();
    }
}
```

## 4. **Testing with Direct Package Structure**

If you're developing both library and test app in the same project (not as separate modules), your library classes might actually be in the same source tree:

```
src/main/java/
├── com/example/testapp/           # Test application
│   └── TestApplication.java
└── com/example/library/           # Library classes (same project!)
    └── YourLibraryService.java
```

In this case, they're **part of the same application**, not a true library dependency.

## How to Verify What's Happening

### Check your package structure:
```java
// In your test application
@SpringBootApplication
public class TestApplication {
    // What package is this in? e.g., com.example.testapp
}

// In your library service
@Service
public class YourLibraryService {
    // What package is this in? 
    // If it's com.example.testapp.something → auto-discovered
    // If it's com.yourcompany.library → should NOT be auto-discovered
}
```

### Check component scan paths:
Add this to see what Spring is scanning:
```java
@SpringBootApplication
public class TestApplication implements CommandLineRunner {
    
    @Autowired
    private ApplicationContext applicationContext;
    
    @Override
    public void run(String... args) {
        String[] beanNames = applicationContext.getBeanDefinitionNames();
        Arrays.sort(beanNames);
        for (String beanName : beanNames) {
            if (beanName.contains("YourLibrary")) {
                System.out.println("Found bean: " + beanName);
                System.out.println("Class: " + applicationContext.getBean(beanName).getClass());
            }
        }
    }
}
```

## Why This Matters for Real Library Usage

### The Problem:
If your library works **only because of component scanning** in your test app, it **won't work** when someone else uses it as a real dependency!

### Real-world scenario:
```java
// Someone else's application
package com.theircompany.theirapp;

@SpringBootApplication
public class TheirApplication { } // Scans com.theircompany.theirapp.*

// Your library is in com.yourcompany.library
// → NOT discovered by their component scan!
```

## The Correct Approach: Add Auto-Configuration

Even if it works now, you should add proper auto-configuration to make your library **truly reusable**:

### 1. Create Auto-Configuration Class:
```java
// In your library: com.yourcompany.library.YourLibraryAutoConfiguration
@Configuration
public class YourLibraryAutoConfiguration {
    
    @Bean
    @ConditionalOnMissingBean
    public YourLibraryService yourLibraryService() {
        return new YourLibraryService();
    }
}
```

### 2. Register it in `spring.factories`:
Create `src/main/resources/META-INF/spring.factories`:
```properties
org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
com.yourcompany.library.YourLibraryAutoConfiguration
```

### 3. Or use the new approach (Spring Boot 2.4+):
Create `src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`:
```
com.yourcompany.library.YourLibraryAutoConfiguration
```

## Testing the Real Scenario

To verify your library works like a real dependency:

1. **Install your library**: `mvn clean install -pl library`
2. **Create a completely separate Spring Boot application** (different project, different package)
3. **Add your library as a dependency**
4. **Try to use it without any special configuration**

If it works → your auto-configuration is correct ✅  
If it doesn't work → you need proper auto-configuration ❌

## Summary

- **Current behavior**: Your library classes are probably being discovered through component scanning because they're in sub-packages of your test app
- **For real libraries**: You need explicit auto-configuration so it works regardless of package structure
- **Best practice**: Always add proper auto-configuration to make your library truly reusable

Add the auto-configuration now, even if it seems to work without it, because it will break when used in real-world scenarios!