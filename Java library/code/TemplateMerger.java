package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import freemarker.template.Configuration;
import freemarker.template.Template;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class TemplateMerger {

    @Autowired
    private Configuration freemarkerConfig;

    private final Templates templates;

    public TemplateMerger(Templates templates) {
        this.templates = templates;
    }

    // method to merge templates with a specifc templateName with data
    public String mergeTemplate(String templateName, java.util.Map<String, Object> data) {
        // Implement merging logic here

        // Get the template by name (this is just a placeholder, actual implementation may vary)
         Templates.DocumentTemplate docTemplate = templates.getTemplates().get(templateName);
            if (docTemplate == null) {
                throw new IllegalArgumentException("Template not found: " + templateName);
            }
            // Perform merging logic using the template and data
            // do Merge template with data
             // Load template
             try {
                    Template template = freemarkerConfig.getTemplate(docTemplate.getTemplateFile());

                    // Merge template with data.
                    StringWriter writer = new StringWriter();
                    template.process(data, writer);
                    System.out.println(writer.toString());
                    return writer.toString(); // Returns merged HTML
                } catch (Exception e) {
                e.printStackTrace();
                return "Error during template merging: " + e.getMessage();
        }   
    }
}



      

  

