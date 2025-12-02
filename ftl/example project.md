Awesome—here’s a **ready-to-run starter project** that demonstrates dynamic **N‑column layouts** and **data-driven content** with FreeMarker and Spring Boot. I’ve packaged everything (code + templates + assets) for you to download and run locally.

**Download the project:**  
[freemarker-grid-starter.zip](blob:https://m365.cloud.microsoft/3320e3b6-dcf2-4656-803b-929524675196)

***

## What’s inside

    freemarker-grid-starter/
    ├─ pom.xml
    ├─ README.md
    ├─ src/
       ├─ main/java/com/example/freemarkergrid/
       │  ├─ FreeMarkerGridStarterApplication.java
       │  ├─ config/FreemarkerConfig.java          ← auto-escaping, multi-template loader (classpath + CMS)
       │  └─ web/HomeController.java               ← sample pages + data-driven blocks
       └─ main/resources/
          ├─ application.yml
          ├─ messages.properties                   ← i18n strings
          ├─ templates/
          │  ├─ layouts/
          │  │  ├─ base.ftl                        ← site-wide layout + named regions
          │  │  ├─ regions.ftl                     ← region macro (fallback or injected content)
          │  │  └─ grid.ftl                        ← N-column grid macro (column-1..N)
          │  ├─ components/
          │  │  ├─ header.ftl
          │  │  ├─ footer.ftl
          │  │  ├─ card.ftl
          │  │  └─ table.ftl
          │  ├─ pages/
          │  │  ├─ home.ftl                        ← base layout + components
          │  │  └─ dynamic-grid.ftl                ← blocks rendered per column via macros
          │  └─ i18n/i18n.ftl                      ← simple i18n helper macro
          └─ static/assets/
             ├─ app.css
             └─ app.js

***

## How to run

> **Prereqs:** Java 17+ and Maven 3.8+

1.  Unzip the file.
2.  From the project folder, run:

```bash
mvn spring-boot:run
```

3.  Open:

*   **Home:** `http://localhost:8080/home`  
    Shows the base layout, a cards grid, and a sidebar region (with fallback).
*   **Dynamic Grid:** `http://localhost:8080/dynamic`  
    Renders **3 columns** with blocks placed by data (`column-1`, `column-2`, `column-3`).  
    Change to **2** or **N** columns just by editing the controller data (no template edits).

***

## Key design points (mapped to files)

*   **Dynamic N-column layout**: `templates/layouts/grid.ftl`  
    Macro `<@grid.gridLayout columns=...>` renders any number of columns with named regions `column-1..N`.
*   **Named regions & fallbacks**: `templates/layouts/regions.ftl`  
    Macro `<@slots.region id="...">...</@slots.region>` inserts controller-provided content if available; otherwise renders the fallback body.
*   **Base layout & composition**: `templates/layouts/base.ftl`  
    Site-wide header/footer, flash area, sidebar, and `<#nested>` for page content.
*   **Reusable components**: `templates/components/*.ftl`  
    Macros for `card`, `table`, etc. You can add more without changing the layout.
*   **Data-driven blocks**: `src/main/java/.../web/HomeController.java`  
    The `/dynamic` endpoint builds `pageLayout` (e.g., `columns=3`) and a `blocks` list (type, region, order, visible). It **groups blocks by region** so templates remain declarative.

***

## Customize quickly

*   **Change column count**: In `HomeController#dynamic`, edit:
    ```java
    pageLayout.put("columns", 2); // or 3, 4, N
    ```
*   **Place content in specific columns**: Set block `region` to `"column-1"`, `"column-2"`, …
*   **Control ordering/visibility**: Use `order` (int) and `visible` (boolean) fields; controller sorts & filters before passing to the template.
*   **Add new components**: Drop a macro under `templates/components/` and add a `case` to `dynamic-grid.ftl`.
*   **Inject CMS/DB fragments**: Use the provided `StringTemplateLoader` in `FreemarkerConfig`; populate fragments at runtime and reference via `<#include>` or render via `regions` with **server-side sanitized HTML** (`?no_esc` used only for trusted content).

***

## Why this works well for complex, dynamic layouts

*   **Data decides layout**, templates remain simple and declarative.
*   **Regions are named slots**, so the controller (or CMS) can target content into specific columns without template branching.
*   **Component macros standardize markup** and ensure consistent rendering across pages.
*   **Auto-escaping enabled** in `FreemarkerConfig` for safety; raw HTML injection is explicit and gated.

***

## Next steps (if you want me to extend this for you)

*   Wire to **Bootstrap/Tailwind**: Map `columns` → responsive classes for tighter control over breakpoints.
*   Add a **component registry** file to avoid `#switch` and make components pluggable.
*   Include **JUnit renderer tests** that render each page with fixture data to catch template regressions.
*   Add an example of **CMS-managed fragment**, e.g., an announcement bar updated at runtime.

Would you like me to integrate **Tailwind (or Bootstrap)** and add a couple of extra components (banner, chart stub) to demonstrate richer pages?
