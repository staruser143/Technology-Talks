FreeMarker's sub-templates are primarily implemented using the &lt;#include&gt; and &lt;#import&gt; directives. These directives enable modularity and code reuse in FreeMarker templates. 
1. &lt;#include&gt; Directive: 
The &lt;#include&gt; directive allows the insertion of content from another FreeMarker template file into the current template at the point where the directive is placed. syntax. 
    <#include "path/to/subtemplate.ftl">

• Functionality: 
	• The content of subtemplate.ftl is directly inserted into the main template. 
	• Variables defined in the main template are accessible within the included sub-template. 
	• Changes to variables within the included sub-template will affect the main template after the include. 
	• Useful for including common elements like headers, footers, or navigation menus across multiple pages. 

• Example: 
	• header.ftl: 

        <!DOCTYPE html>
        <html>
        <head>
            <title>${pageTitle}</title>
        </head>
        <body>

main_page.ftl. 
        <#assign pageTitle = "My Website">
        <#include "header.ftl">
        <h1>Welcome!</h1>
        <p>This is the main content.</p>
        </body>
        </html>

2. &lt;#import&gt; Directive: 
The &lt;#import&gt; directive allows the import of a template as a "namespace," making its macros and functions available for use in the current template. syntax. 
    <#import "path/to/library.ftl" as myLib>

• Functionality: 
	• Macros and functions defined within library.ftl are made available under the myLib namespace. 
	• This prevents naming conflicts if multiple imported templates have macros with the same name. 
	• Variables defined in the imported template are not directly accessible in the main template unless explicitly passed. 
	• Useful for creating libraries of reusable macros and functions. 

• Example: 
	• macros.ftl: 

        <#macro greet name>
            Hello, ${name}!
        </#macro>

main_page.ftl. 
        <#import "macros.ftl" as myMacros>
        <@myMacros.greet name="Alice"/>

Key Differences: 

• &lt;#include&gt; inserts the content directly, sharing the same variable scope. 
• &lt;#import&gt; creates a separate namespace for macros and functions, promoting better organization and preventing conflicts. 

By utilizing these directives, FreeMarker templates can be structured efficiently, promoting reusability and maintainability in web applications or other text generation tasks. 

AI responses may include mistakes.

