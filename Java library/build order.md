Yes, **you should build the library module first** before building/running the application module, but there are smart ways to handle this automatically. Here's what you need to know:

## Why Build Order Matters

When your test application depends on your library module, Maven needs the library's compiled classes and JAR file to be available before it can compile the application.

## Best Approaches

### 1. **Use Maven Reactor with `-am` (Recommended)**

The `-am` flag (also-make) automatically builds dependent modules:

```bash
# Build library and all modules that depend on it
mvn clean install -pl library -am

# Then run your test application
mvn spring-boot:run -pl test-app
```

Or in one command:
```bash
# Build everything and run test app
mvn clean install && mvn spring-boot:run -pl test-app
```

### 2. **Build the Entire Project**

Simplest approach for development:

```bash
# Builds library first, then test-app (Maven handles order automatically)
mvn clean install

# Run the test application
mvn spring-boot:run -pl test-app
```

Maven's **reactor** automatically determines the correct build order based on module dependencies.

### 3. **IDE Integration (Most Convenient for Development)**

Most IDEs (IntelliJ IDEA, Eclipse, VS Code) handle this automatically:

- **IntelliJ IDEA**: When you run the test application, it automatically compiles changed library classes
- **Eclipse**: Automatically builds dependent projects
- **VS Code**: With Java extensions, handles incremental compilation

**No manual building needed** when using your IDE's run/debug features!

### 4. **Development Script Approach**

Create a script that handles the build order:

**`dev.sh` (Linux/Mac):**
```bash
#!/bin/bash
echo "Building library..."
mvn clean install -pl library -am -q

echo "Starting test application..."
mvn spring-boot:run -pl test-app
```

**`dev.bat` (Windows):**
```batch
@echo off
echo Building library...
mvn clean install -pl library -am -q

echo Starting test application...
mvn spring-boot:run -pl test-app
```

## Maven Reactor Smart Ordering

Maven automatically determines build order based on:
- Module dependencies in `pom.xml`
- The `<modules>` section in parent POM

So if your structure is:
```
parent
├── library
└── test-app (depends on library)
```

Running `mvn clean install` will **always** build `library` before `test-app`.

## Quick Development Workflow

### During Active Development:
1. **Use your IDE** to run the test application directly (no manual builds needed)
2. **Make changes to library code**
3. **IDE automatically recompiles** and hot-reloads (with DevTools)

### Before Committing/Testing:
```bash
# Full clean build to ensure everything works
mvn clean install

# Run tests to verify
mvn test
```

### When You Need Fresh Builds:
```bash
# Force rebuild library and dependent modules
mvn clean install -pl library -am

# Run test application
mvn spring-boot:run -pl test-app
```

## Pro Tips

### Skip Tests During Development (Faster Builds)
```bash
mvn clean install -DskipTests
```

### Use `-pl` and `-am` Together
- `-pl library` = build only the library module
- `-am` = also build modules that depend on library
- This is faster than building the entire project

### Enable Hot Reload
Add Spring Boot DevTools to your test application:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-devtools</artifactId>
    <scope>runtime</scope>
    <optional>true</optional>
</dependency>
```

This enables automatic restart when library classes change (in IDE development mode).

## Summary

- **Yes**, build library before application, but **Maven handles this automatically** in most cases
- **During development**: Use your IDE's run feature (no manual builds needed)
- **For command line**: Use `mvn clean install` (builds in correct order) or `mvn clean install -pl library -am`
- **Best practice**: Always do a full `mvn clean install` before committing to ensure everything works together

The key is understanding that Maven's reactor is smart about build order, so you don't need to manually manage it in most scenarios!