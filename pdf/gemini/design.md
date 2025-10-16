I'd be happy to detail how to design your PDF form template to match the recommended **flattening and convention-based naming** for nested data structures.

The goal is to name your **AcroForm fields** within the PDF design tool (like Adobe Acrobat Pro or LibreOffice Draw) using the `parent_index_child` convention.

## 📝 Key Steps for Designing the PDF Form

### 1. Identify Required Repeatable Sections

First, determine the maximum number of times an array (like `items` or `sections`) can repeat on a single page. If your JSON has a maximum of 5 items in the `items` array, you need 5 dedicated rows/sets of fields in your PDF.

### 2. Name Fields Using the Convention

When adding or editing a form field in your PDF design software, you will set its full name to match the flattened key.

| Data Path (JSON) | Array Index | AcroForm Field Name |
| :--- | :--- | :--- |
| `user.name` | N/A | `user_name` |
| `items.product` | Index 0 | **`items_0_product`** |
| `items.qty` | Index 0 | **`items_0_qty`** |
| `items.product` | Index 1 | **`items_1_product`** |
| `items.qty` | Index 1 | **`items_1_qty`** |
| `sections.photos.url` | Section Index 1, Photo Index 2 | **`sections_1_photos_2_url`** |

### 3. Practical Implementation (Using Adobe Acrobat Pro Example)

1.  **Open the PDF:** Open your base PDF (which may contain static text and table layouts) in Acrobat Pro and enter **Form Editing** mode.
2.  **Add/Select Field:** Add a new Text Field (or Check Box/Radio Button) where you want the data to appear.
3.  **Access Field Properties:** Double-click the field to open the **Properties** window.
4.  **Set the Name:** In the **General** tab, use the **Name** property to enter the specific, index-based field name.



**Example: The First Item**
* For the product name in the first row, set the field **Name** to `items_0_product`.
* For the quantity in the first row, set the field **Name** to `items_0_qty`.

**Example: The Second Item**
* For the product name in the second row, set the field **Name** to `items_1_product`.
* For the quantity in the second row, set the field **Name** to `items_1_qty`.

---

## 📐 Template Design Considerations

### Static Repeating Elements (Tables)

Since PDF AcroForms are static structures, you need to **manually create the fields** for every possible item.

* If your design is a table, you must draw **$N$ rows** in the PDF design (e.g., a table with 5 rows for 5 items).
* In row 1, use fields with index `_0_`.
* In row 2, use fields with index `_1_`.
* ...and so on, up to the maximum index you require.

### Handling Empty Arrays

If your data only contains 2 items but your template has 5 field sets (up to `items_4_...`), **PDFBox will only fill the fields that exist in your flattened data**. The remaining fields (`items_2_...`, `items_3_...`, `items_4_...`) will simply remain blank or hold their default value in the PDF, which is the desired outcome.

By adhering to this fixed naming convention in the template design, your data flattening logic becomes the reliable bridge between your complex data model and the PDF's field structure.

Would you like me to clarify how to handle specific field types, like **Radio Buttons** or **Check Boxes**, with this convention?