package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@SpringBootApplication
public class PDFGeneratorApplication {

	private final Templates templates;

	private final TemplateMerger templateMerger;
	
	public PDFGeneratorApplication(Templates templates, TemplateMerger templateMerger) {
		this.templates = templates;
		this.templateMerger = templateMerger;
	}
	public static void main(String[] args) {
		SpringApplication.run(PDFGeneratorApplication.class, args);
	}

	@GetMapping("/")
	String home() {
		return "Hello World!";
	}

	@GetMapping("/templates")
	Templates listTemplates() {
		return templates;
		
	}

	@GetMapping("/merge/{templateName}")
	String mergeTemplateWithData(@org.springframework.web.bind.annotation.PathVariable String templateName) {
		java.util.Map<String, Object> data;
		data = new java.util.HashMap<>();
		data.put("name", "Sample Document");
		

		return templateMerger.mergeTemplate(templateName, data);

		
	}
	@GetMapping("/templates/{templateName}")
	Templates.DocumentTemplate getTemplateByName(@org.springframework.web.bind.annotation.PathVariable String templateName) {
			return templates.getTemplates().get(templateName);
		
	}


}
