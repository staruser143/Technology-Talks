**XFDL (Extensible Forms Description Language)** is an XML-based file format originally developed by PureEdge Solutions (later acquired by IBM) for creating, viewing, and filling out complex electronic forms. It is designed to support rich, interactive, and secure digital forms that can replicate the look and functionality of paper forms while enabling advanced features like calculations, validations, digital signatures, and data encryption.

### Key Features of XFDL:

1. **XML-Based Structure**:  
   XFDL files are written in XML, making them both human-readable (to some extent) and machine-processable.

2. **Rich Form Capabilities**:  
   Supports advanced form elements such as:
   - Text fields, checkboxes, radio buttons
   - Tables, images, and embedded objects
   - Dynamic calculations and logic (e.g., auto-calculating totals)
   - Conditional visibility and validation rules

3. **Security Features**:  
   - Built-in support for digital signatures (including multiple signatures)
   - Data encryption for sensitive information
   - Tamper-evident form design

4. **Platform Independence**:  
   Designed to render consistently across different systems, though historically required a dedicated viewer.

5. **Used in Government and Enterprise**:  
   XFDL has been widely adopted by government agencies (notably the U.S. Department of Defense and other federal entities) for secure, compliant electronic forms.

### File Extension:
- `.xfdl` (plain text XML)
- Sometimes compressed or packaged as `.p7m` or within a MIME-encoded wrapper for transmission

### Viewing and Editing XFDL Files:
- Originally required **IBM Forms Viewer** (formerly PureEdge Viewer), a proprietary application.
- Modern support is limited; IBM discontinued active development of IBM Forms, though legacy systems may still use it.
- Some third-party tools or custom XML parsers can read basic XFDL content, but full rendering and interactivity typically require the original viewer.

### Example Snippet (Simplified):
```xml
<xfdl xmlns="http://www.ibm.com/xmlns/prod/XFDL/7.7">
  <global>
    <title>Sample Form</title>
  </global>
  <page sid="page1">
    <field sid="name">
      <value/>
      <caption>Name:</caption>
    </field>
  </page>
</xfdl>
```

### Current Status:
While XFDL was innovative for its time, its usage has declined in favor of more open and web-friendly formats like HTML5 forms, PDF (with interactive features), or modern low-code form builders. However, it remains in use in certain legacy government and defense systems due to its robust security and compliance features.

Let me know if you'd like help opening an XFDL file or converting it to another format!