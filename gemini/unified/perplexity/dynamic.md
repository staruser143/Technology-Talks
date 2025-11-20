We are considering to use FreeMarker based html templates for building pages with content and finally converting them to pdf. Since, the content is dynamic and also the layout changes based on data we are seeing a need to create many templates to handle varied requirements. For some, we need 3 column layout tables, for others 2 column and for others multi-level tables. Is there a better way handle this dynamic set of layout and data when using freemarker.

When using FreeMarker to handle dynamic layouts and data for PDF generation from HTML templates, a more scalable and maintainable approach than creating many different templates is to design a flexible template architecture that:

- Uses template composition by splitting the layout into reusable sub-templates or macros for common blocks such as table structures, rows, columns, and sections.
- Embeds conditional logic and dynamic iteration inside the template to switch between different layouts (e.g., 2-column, 3-column, multi-level tables) based on the input data.
- Employs macros extensively to encapsulate reusable template fragments, which reduces redundancy and centralizes layout control.
- Passes structured data models where each data piece carries information about its content, layout requirements (such as column counts), and hierarchical structure, so the single template can adapt dynamically.
- Applies CSS styling and class toggling dynamically in the FreeMarker template to adjust visual presentation based on the data-driven layout decisions.

This approach significantly reduces the number of full template files by enabling a single, parameterized FreeMarker template to flexibly render diverse page layouts and complex table structures dynamically at runtime.

For example, you can:

- Define macros for different table layouts.
- Use <#if> conditions to select layout styles.
- Use <#list> to render rows and nested rows.
- Control column spans and nesting by data attributes.

This strategy of combining template modularization, macros, and dynamic data-driven conditions is considered best practice for handling highly variable document layouts with FreeMarker, avoiding template explosion while maintaining clarity and reusability [1][2][3][4].

If your use case involves generating PDF afterwards, ensure that the generated HTML is well-structured and uses CSS for layout, so that PDF conversion tools can produce consistent results from this dynamic HTML.

Would you like a sample approach or example FreeMarker template structure illustrating this best practice?

Citations:
[1] Dynamic HTML templates with FreeMarker and Vaadin https://vaadin.com/blog/dynamic-html-templates-with-freemarker-and-vaadin
[2] Best practices - 3.1 Freemarker and templating https://docs.squiz.net/funnelback-15/latest/best-practice-guide/best-practices-3.1-freemarker-and-templating.html
[3] Using FreeMarker templates (FTL)- Tutorial https://www.vogella.com/tutorials/FreeMarker/article.html
[4] Dynamic and complex configurations with FreeMarker https://labs.consol.de/development/2018/03/26/dynamic-and-complex-configurations-with-freemarker.html
[5] How to build a FTL template for a dynamic layout? https://stackoverflow.com/questions/21419974/how-to-build-a-ftl-template-for-a-dynamic-layout
[6] Dynamic HTML Templates With FreeMarker and Vaadin https://dzone.com/articles/dynamic-html-templates-with-freemarker-and-vaadin-1
[7] Using variables and expressions in FreeMarker templates https://docs.digital.ai/deploy/docs/how-to/using-variables-and-expressions-in-freemarker-templates
[8] Seldom used and expert built-ins - Apache FreeMarker Manual https://freemarker.apache.org/docs/ref_builtins_expert.html
[9] What's the best practice to put freemarker template files https://stackoverflow.com/questions/51691462/whats-the-best-practice-to-put-freemarker-template-files
[10] The template at a glance - Apache FreeMarker Manual https://freemarker.apache.org/docs/dgui_quickstart_template.html
