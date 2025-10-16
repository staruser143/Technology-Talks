That's an excellent follow-up question. Handling radio buttons and checkboxes requires a slight adjustment to the convention, as these field types don't just accept any string value; they accept a specific **export value** defined in the PDF template.

Here is the breakdown of how to map your data to **Radio Buttons** and **Check Boxes** using the recommended flattening convention.

-----

## 1\. ✅ Check Boxes

### The Principle

A PDF Check Box has two possible states: **Checked** and **Unchecked**. When checked, it holds a specific **Export Value** (often `"Yes"` or `"On"`). When unchecked, it holds the value `"Off"` or `"No"`.

Your job is to ensure the **value in your flattened data map** matches the Check Box's **Export Value** when it should be selected.

### Convention and Mapping

| Check Box Goal | AcroForm Field Name (Convention) | Field Export Value (PDF Design) | Flattened Data Value (Input) | PDFBox Action |
| :--- | :--- | :--- | :--- | :--- |
| **Checked** | `user_is_subscribed` | `"Yes"` | `"Yes"` | `field.setValue("Yes")` |
| **Unchecked** | `user_is_subscribed` | `"Yes"` | `"No"`, `"False"`, or `""` (Empty String) | `field.setValue("Off")` or `field.setValue("No")` |

#### Data Flattening Logic Adjustment

When flattening a boolean from your source data, you must convert it to the exact **Export Value** string used in your PDF.

| Source Boolean Value | Desired Output String in `flatMap` |
| :--- | :--- |
| `true` | `"Yes"` (or whatever you set the Export Value to) |
| `false` | `"Off"` (or `"No"`) |

#### PDFBox Action

PDFBox handles this by setting the field value. To set a Check Box to its checked state, you pass its defined **Export Value**. To uncheck it, you pass `"Off"` (or the specified unselected value).

```java
// Assuming the Check Box export value is "Yes"
PDField field = acroForm.getField("user_is_subscribed");

if (dataValue.equals("Yes")) {
    field.setValue("Yes"); // Checks the box
} else {
    field.setValue("Off"); // Unchecks the box
}
```

-----

## 2\. 🔘 Radio Buttons (Radio Button Groups)

### The Principle

Radio buttons always belong to a **group**. Only one button in the group can be selected at a time. The entire group shares the **same AcroForm Field Name**.

The button that is selected is the one whose individual **Export Value** matches the **Value** set for the entire group field.

### Convention and Mapping

| Radio Group Goal | AcroForm Field Name (Shared by all buttons) | Button Export Values (PDF Design) | Flattened Data Value (Input) | PDFBox Action |
| :--- | :--- | :--- | :--- | :--- |
| Select "Option 1" | `user_payment_method` | Button 1: `"Card"`, Button 2: `"Cash"` | `"Card"` | `field.setValue("Card")` |

#### PDF Design Steps

1.  **Create the Group:** Create all the radio buttons for a single question (e.g., "Payment Method").
2.  **Set the Shared Name:** In the Properties panel for **all** buttons in the group, set the **Name** to the same flattened name, e.g., **`user_payment_method`**.
3.  **Set Unique Export Values:** For *each individual button*, set a unique **Export Value** (e.g., Button 1 = `"Card"`, Button 2 = `"Cash"`). This is what your data must match.

#### Data Flattening Logic

The flattened map key should be the shared group name, and the value should be the specific **Export Value** of the option you want to select.

| Source Data Field | Flattened Key | Flattened Value |
| :--- | :--- | :--- |
| `user.paymentMethod` = `"Card"` | **`user_payment_method`** | `"Card"` |

#### PDFBox Action

You set the value of the shared group field to the desired option's export value.

```java
// "user_payment_method" is the name of the radio button group
PDField field = acroForm.getField("user_payment_method");

// This sets the selected button to the one whose Export Value is "Cash"
field.setValue("Cash");
```

-----

## 3\. 🎯 Handling Nested Arrays with Radio/Check

The same principles apply when nested: you simply ensure your full flattened name includes the index.

| Field Type | Nested Data Path | AcroForm Field Name | Export Value | Flattened Data Value |
| :--- | :--- | :--- | :--- | :--- |
| **Check Box** | `items[1].isHazardous` | **`items_1_isHazardous`** | `"H"` | `"H"` (to check) |
| **Radio Group** | `items[2].fulfillment` | **`items_2_fulfillment`** | `"Ship"` / `"Pickup"` | `"Ship"` (to select Ship) |

By setting up your PDF form fields this way, your single flattening logic can consistently handle simple text fields, nested arrays, and complex interactive elements like radio buttons and checkboxes.

Would you like me to provide a quick example of a JSON structure that includes booleans and option fields and how they map to the flattened structure?