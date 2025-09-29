Absolutely! Below is a **complete, working Java example** that:

1. Loads a **blank XFDL template** (with empty `<value/>` tags)  
2. Reads **external data from a JSON file**  
3. **Merges the JSON data into the XFDL** by populating the `<value>` elements  
4. Saves the **filled XFDL file**

This gives you a clean separation of **form structure** (XFDL) and **data** (JSON), which is ideal for modern integration pipelines.

---

### 📁 Project Structure
```
xfdl-json-merge/
├── pom.xml
├── src/main/java/
│   └── JsonToXfdlMerger.java
├── forms/
│   ├── registration_template.xfdl
│   └── user_data.json
└── output/
    └── filled_form.xfdl
```

---

### 1. `pom.xml` (Maven Dependencies)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.example</groupId>
    <artifactId>xfdl-json-merger</artifactId>
    <version>1.0</version>

    <properties>
        <maven.compiler.source>11</maven.compiler.source>
        <maven.compiler.target>11</maven.compiler.target>
    </properties>

    <dependencies>
        <!-- For JSON parsing -->
        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-databind</artifactId>
            <version>2.17.0</version>
        </dependency>

        <!-- For XML handling (optional, but useful) -->
        <dependency>
            <groupId>org.apache.commons</groupId>
            <artifactId>commons-text</artifactId>
            <version>1.11.0</version>
        </dependency>
    </dependencies>
</project>
```

---

### 2. `JsonToXfdlMerger.java`

```java
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

public class JsonToXfdlMerger {

    public static void main(String[] args) {
        try {
            File xfdlTemplate = new File("forms/registration_template.xfdl");
            File jsonDataFile = new File("forms/user_data.json");
            File outputXfdl = new File("output/filled_form.xfdl");

            outputXfdl.getParentFile().mkdirs();

            // Step 1: Parse JSON data
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonData = mapper.readTree(jsonDataFile);
            System.out.println("Loaded JSON data: " + jsonData.toPrettyString());

            // Step 2: Parse XFDL template
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(false); // Simplify for this example
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);

            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(xfdlTemplate);
            doc.getDocumentElement().normalize();

            // Step 3: Merge JSON into XFDL
            mergeJsonIntoXfdl(doc, jsonData);

            // Step 4: Save filled XFDL
            saveDocument(doc, outputXfdl);
            System.out.println("✅ Filled XFDL saved to: " + outputXfdl.getAbsolutePath());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Traverse all <field> elements and inject JSON values where field's 'sid' matches JSON key.
     */
    public static void mergeJsonIntoXfdl(Document xfdlDoc, JsonNode jsonData) {
        NodeList fields = xfdlDoc.getElementsByTagName("field");

        for (int i = 0; i < fields.getLength(); i++) {
            Element field = (Element) fields.item(i);
            String sid = field.getAttribute("sid");

            if (sid != null && !sid.isEmpty() && jsonData.has(sid)) {
                JsonNode valueNode = jsonData.get(sid);
                String value = (valueNode.isNull() || valueNode.isMissingNode()) ? "" : valueNode.asText();

                // Find or create <value> child
                NodeList valueNodes = field.getElementsByTagName("value");
                Element valueElement;
                if (valueNodes.getLength() > 0) {
                    valueElement = (Element) valueNodes.item(0);
                    // Clear existing content
                    while (valueElement.hasChildNodes()) {
                        valueElement.removeChild(valueElement.getFirstChild());
                    }
                } else {
                    valueElement = xfdlDoc.createElement("value");
                    field.appendChild(valueElement);
                }

                // Set new text content
                valueElement.setTextContent(value);
                System.out.println("Filled field '" + sid + "' = '" + value + "'");
            }
        }
    }

    /**
     * Save DOM Document to file with proper formatting.
     */
    public static void saveDocument(Document doc, File outputFile) throws Exception {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty(OutputKeys.STANDALONE, "yes");

        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(outputFile);
        transformer.transform(source, result);
    }
}
```

---

### 3. `registration_template.xfdl` (Blank Template)

Save as `forms/registration_template.xfdl`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<xfdl>
  <global>
    <title>User Registration</title>
  </global>
  <page sid="page1">
    <field sid="full_name">
      <value/>
    </field>
    <field sid="email">
      <value/>
    </field>
    <field sid="is_subscribed">
      <value/>
    </field>
    <field sid="country">
      <value/>
    </field>
  </page>
</xfdl>
```

> 🔸 Note: `<value/>` is empty — ready to be filled.

---

### 4. `user_data.json` (External Data)

Save as `forms/user_data.json`:

```json
{
  "full_name": "Alex Rivera",
  "email": "alex@example.com",
  "is_subscribed": "on",
  "country": "Spain"
}
```

> 💡 The **JSON keys must match XFDL `sid` attributes** exactly.

---

### ▶️ Run the Application

```bash
mvn compile
mvn exec:java -Dexec.mainClass="JsonToXfdlMerger"
```

### ✅ Output: `output/filled_form.xfdl`

```xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<xfdl>
  <global>
    <title>User Registration</title>
  </global>
  <page sid="page1">
    <field sid="full_name">
      <value>Alex Rivera</value>
    </field>
    <field sid="email">
      <value>alex@example.com</value>
    </field>
    <field sid="is_subscribed">
      <value>on</value>
    </field>
    <field sid="country">
      <value>Spain</value>
    </field>
  </page>
</xfdl>
```

This filled XFDL can now be:
- Opened in IBM Forms Viewer
- Digitally signed
- Used as input to your **PDF filling pipeline** (from earlier examples!)

---

### 🔄 Integration with PDF Filling (Optional)

You can now chain this with your PDF filler:

```java
// After generating filled_form.xfdl
XfdlToPdfFiller.fillPdfFromXfdl(
    new File("output/filled_form.xfdl"),
    new File("forms/pdf_template.pdf"),
    new File("output/final_output.pdf")
);
```

---

### ✅ Benefits of This Approach
- **Decoupled**: Form design (XFDL) ≠ data (JSON)
- **Reusable**: Same template for thousands of users
- **API-friendly**: Accept JSON from web forms, mobile apps, etc.
- **Testable**: Validate JSON independently
- **Archivable**: Final XFDL is self-contained for compliance

---

### ⚠️ Notes & Enhancements
- **Nested JSON?** Extend the merger to handle dot-notation (e.g., `"user.name"`).
- **Namespaces?** If your XFDL uses `xmlns`, enable namespace-aware parsing.
- **Validation?** Add schema validation for JSON or XFDL.
- **Performance?** For large forms, consider streaming (but XFDL is usually small).

---

Let me know if you'd like:
- Support for **nested JSON paths** (`address.city`)
- **Checkbox handling** (convert `true` → `"on"`)
- Export to **XFDF** or **CSV** as well
- A **REST API** version (e.g., Spring Boot endpoint)

This pattern is a powerful foundation for modernizing legacy XFDL workflows! 🚀