// TemplatesProperties.java
package com.example.demo;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.List;
import lombok.Data;

@Component
@ConfigurationProperties
@Data
public class Templates {
    private Map<String, DocumentTemplate> templates;

    // getters and setters

    @Data
    public static class DocumentTemplate {
        private String description;
        private String templateType;
        private String templateFile;
        private List<FieldMapping> fieldMappings;
        private List<Page> pages;
        private List<MergesFile> mergesFiles;
        // getters and setters
    }

    @Data
    public static class FieldMapping {
        private String source;
        private String target;
        // getters and setters
    }

    @Data
    public static class Page {
        private String name;
        private String content;
        // getters and setters
    }
    @Data
    public static class MergesFile {
        private String name;
        private String file;
        // getters and setters
    }
}
