FreeMarker macros allow defining reusable blocks of FTL (FreeMarker Template Language) code, similar to functions or subroutines in programming languages. They help in organizing templates, promoting code reuse, and making templates more readable. 
1. Defining a Macro: 
A macro is defined using the &lt;#macro&gt; directive, followed by the macro name and optional parameters in parentheses. The macro's body contains the FTL code that will be executed when the macro is called. 
<#macro greet name>
  <p>Hello, ${name}!</p>
</#macro>

2. Calling a Macro: 
Macros are invoked using the &lt;@&gt; syntax. The macro name is followed by parentheses containing the values for its parameters. 
<@greet name="Alice"/>
<@greet name="Bob"/>

3. Macros with Default Parameter Values: 
You can specify default values for macro parameters, which will be used if the parameter is not provided during the macro call. 
<#macro sayHello name="Guest">
  <p>Hello, ${name}!</p>
</#macro>

<@sayHello/>        <#-- Output: Hello, Guest! -->
<@sayHello name="Charlie"/> <#-- Output: Hello, Charlie! -->

4. Macros with Nested Content: 
Macros can also accept nested content, which is the FTL code placed between the opening and closing macro tags. The nested directive inside the macro body renders this content. 
<#macro box>
  <div style="border: 1px solid black; padding: 10px;">
    <#nested>
  </div>
</#macro>

<@box>
  <p>This content is inside the box.</p>
</@box>

5. Importing Macros from Other Files: 
Macros can be defined in separate FTL files and then imported into other templates using the &lt;#import&gt; directive. This promotes modularity and organization. 
<#-- macros.ftl -->
<#macro myMacro>...</#macro>

<#-- main.ftl -->
<#import "macros.ftl" as m>
<@m.myMacro/>

6. Best Practices: 

• Meaningful Names: Give your macros and parameters clear, descriptive names. 
• Documentation: Add comments to explain what your macros do, especially for complex ones. 
• Modularity: Group related macros into separate files for better organization. 
• Reusability: Design macros to be as generic and reusable as possible. 
• Built-in vs. Custom: Utilize FreeMarker's built-in directives and functions when appropriate, and create custom macros for specific, repetitive tasks. 

AI responses may include mistakes.

