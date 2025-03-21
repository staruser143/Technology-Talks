You're absolutely correct! The `CDCConfig` class is not being used in the implementations, and I didn't provide details on how to populate it from a configuration file (e.g., YAML). Let me address these issues and provide a complete solution.

---

### **1. Define the `CDCConfig` Class**
The `CDCConfig` class will hold configuration options for CDC, such as:
- **Operations to listen for** (e.g., `insert`, `update`, `delete`).
- **Filters** (e.g., specific field values to filter changes by).

Here’s how you can define the `CDCConfig` class:
```java
import java.util.List;
import java.util.Map;

public class CDCConfig {
    private List<String> operations; // e.g., ["insert", "update", "delete"]
    private Map<String, Object> filters; // e.g., {"status": "active"}

    // Getters and setters
    public List<String> getOperations() {
        return operations;
    }

    public void setOperations(List<String> operations) {
        this.operations = operations;
    }

    public Map<String, Object> getFilters() {
        return filters;
    }

    public void setFilters(Map<String, Object> filters) {
        this.filters = filters;
    }
}
```

---

### **2. Populate `CDCConfig` from a YAML File**
To load configuration from a YAML file, you can use a library like **SnakeYAML**. Add the following dependency to your `pom.xml`:
```xml
<dependency>
    <groupId>org.yaml</groupId>
    <artifactId>snakeyaml</artifactId>
    <version>2.0</version>
</dependency>
```

Then, create a utility class to load the YAML configuration:
```java
import org.yaml.snakeyaml.Yaml;
import java.io.InputStream;
import java.util.Map;

public class ConfigLoader {
    public static CDCConfig loadCDCConfig() {
        Yaml yaml = new Yaml();
        InputStream inputStream = ConfigLoader.class
                .getClassLoader()
                .getResourceAsStream("application.yaml");

        Map<String, Object> config = yaml.load(inputStream);
        Map<String, Object> cdcConfigMap = (Map<String, Object>) config.get("cdc");

        CDCConfig cdcConfig = new CDCConfig();
        cdcConfig.setOperations((List<String>) cdcConfigMap.get("operations"));
        cdcConfig.setFilters((Map<String, Object>) cdcConfigMap.get("filters"));

        return cdcConfig;
    }
}
```

---

### **3. Update the YAML Configuration File**
Here’s an example `application.yaml` file:
```yaml
database:
  type: "mongodb" # or "cosmosdb"
  mongodb:
    uri: "mongodb+srv://<username>:<password>@cluster0.mongodb.net/<dbname>?retryWrites=true&w=majority"
    databaseName: "testdb"
    collectionName: "testcollection"
  cosmosdb:
    uri: "<cosmosdb_connection_string>"
    databaseName: "testdb"
    containerName: "testcontainer"
cdc:
  operations:
    - "insert"
    - "update"
  filters:
    status: "active"
```

---

### **4. Use `CDCConfig` in the Implementations**
Update the `MongoDBChangeStream` and `CosmosDBChangeFeed` classes to use the `CDCConfig` for filtering and operation-specific logic.

#### **MongoDB Implementation**
```java
@Override
public void listenForChanges(Consumer<ChangeEvent> callback, CDCConfig config) {
    List<String> operations = config.getOperations();
    Map<String, Object> filters = config.getFilters();

    collection.watch().forEach(change -> {
        // Filter by operation type
        if (operations != null && !operations.contains(change.getOperationType().getValue())) {
            return;
        }

        // Filter by field values
        if (filters != null) {
            Document fullDocument = change.getFullDocument();
            if (fullDocument == null || !filters.entrySet().stream()
                    .allMatch(entry -> entry.getValue().equals(fullDocument.get(entry.getKey())))) {
                return;
            }
        }

        ChangeEvent event = new ChangeEvent();
        event.setOperationType(change.getOperationType().getValue());
        event.setDocument(change.getFullDocument());
        callback.accept(event);
    });
}
```

#### **Cosmos DB Implementation**
```java
@Override
public void listenForChanges(Consumer<ChangeEvent> callback, CDCConfig config) {
    List<String> operations = config.getOperations();
    Map<String, Object> filters = config.getFilters();

    CosmosChangeFeedRequestOptions options = CosmosChangeFeedRequestOptions.createForProcessingFromNow();
    Iterable<FeedResponse<Document>> changeFeed = container.queryChangeFeed(options, Document.class);

    for (FeedResponse<Document> page : changeFeed) {
        for (Document document : page.getResults()) {
            // Infer operation type (Cosmos DB does not provide this directly)
            String operationType = document.containsKey("_ts") ? "update" : "insert";

            // Filter by operation type
            if (operations != null && !operations.contains(operationType)) {
                continue;
            }

            // Filter by field values
            if (filters != null && !filters.entrySet().stream()
                    .allMatch(entry -> entry.getValue().equals(document.get(entry.getKey())))) {
                continue;
            }

            ChangeEvent event = new ChangeEvent();
            event.setOperationType(operationType);
            event.setDocument(document);
            callback.accept(event);
        }
    }
}
```

---

### **5. Update the Main Application**
Load the `CDCConfig` from the YAML file and pass it to the `listenForChanges` method:
```java
public class CDCApplication {
    public static void main(String[] args) {
        CDCInterface cdcInstance = CDCFactory.getCDCInstance();
        cdcInstance.connect();

        CDCConfig cdcConfig = ConfigLoader.loadCDCConfig();
        cdcInstance.listenForChanges(change -> {
            System.out.println("Change detected: " + change);
        }, cdcConfig);

        // Keep the application running
        while (true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
```

---

### **6. Summary**
- The `CDCConfig` class is now used to pass configuration options (e.g., operations and filters) to the CDC implementations.
- The configuration is loaded from a YAML file using **SnakeYAML**.
- The `MongoDBChangeStream` and `CosmosDBChangeFeed` classes use the `CDCConfig` to filter changes based on operations and field values.

This approach ensures that the framework is **configuration-driven** and **extensible**, allowing you to easily switch between MongoDB Atlas and Azure Cosmos DB for NoSQL API with minimal changes.