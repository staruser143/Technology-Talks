Below is a **clean, production-grade medical enrollment YAML** that supports a **dynamic number of dependents**, without requiring any conditional logic inside YAML.

This YAML uses:

### ✔ `expr:` based on JSONPath

### ✔ `safeIndex` transform → prevents errors and returns blank when dependent doesn’t exist

### ✔ Uniform “Dependent N” mapping (1..3 or 1..5 based on form template)

This is the **recommended best-practice pattern** for AcroForm PDF mapping.

---

# ✅ **Medical Enrollment YAML (Dynamic Dependents with Safe Index Transform)**

```yaml
template:
  file: "medical_enrollment.pdf"
  location: "classpath:templates/"

mapping:

  # -------------------------
  # Subscriber (Primary Member)
  # -------------------------
  subscriberFirstName:
    expr: "$.subscriber.name.first"

  subscriberLastName:
    expr: "$.subscriber.name.last"

  subscriberDOB:
    expr: "$.subscriber.dob"

  subscriberSSN:
    expr: "$.subscriber.ssn"
    transform: ["mask:ssn"]

  subscriberGender:
    expr: "$.subscriber.gender"

  subscriberPhone:
    expr: "$.contacts.phone"

  subscriberEmail:
    expr: "$.contacts.email"

  # -------------------------
  # Address
  # -------------------------
  addressLine1:
    expr: "$.address.line1"

  addressLine2:
    expr: "$.address.line2"

  city:
    expr: "$.address.city"

  state:
    expr: "$.address.state"

  zip:
    expr: "$.address.zip"

  # -------------------------
  # Coverage Type
  # -------------------------
  planName:
    expr: "$.coverage.planName"

  planStartDate:
    expr: "$.coverage.startDate"
    transform: ["date:yyyy-MM-dd->MM/dd/yyyy"]

  # -------------------------
  # Dynamic Dependents
  # -------------------------
  # Form supports up to 3 dependents (can change to 5 if needed)
  # safeIndex: ensures missing dependents yield blank output

  dependent1FirstName:
    expr: "$.dependents"
    transform:
      - "safeIndex:0"
      - "extract:$.name.first"

  dependent1LastName:
    expr: "$.dependents"
    transform:
      - "safeIndex:0"
      - "extract:$.name.last"

  dependent1DOB:
    expr: "$.dependents"
    transform:
      - "safeIndex:0"
      - "extract:$.dob"

  dependent1Relationship:
    expr: "$.dependents"
    transform:
      - "safeIndex:0"
      - "extract:$.relationship"

  # -------------------------
  # Dependent 2
  # -------------------------
  dependent2FirstName:
    expr: "$.dependents"
    transform:
      - "safeIndex:1"
      - "extract:$.name.first"

  dependent2LastName:
    expr: "$.dependents"
    transform:
      - "safeIndex:1"
      - "extract:$.name.last"

  dependent2DOB:
    expr: "$.dependents"
    transform:
      - "safeIndex:1"
      - "extract:$.dob"

  dependent2Relationship:
    expr: "$.dependents"
    transform:
      - "safeIndex:1"
      - "extract:$.relationship"

  # -------------------------
  # Dependent 3
  # -------------------------
  dependent3FirstName:
    expr: "$.dependents"
    transform:
      - "safeIndex:2"
      - "extract:$.name.first"

  dependent3LastName:
    expr: "$.dependents"
    transform:
      - "safeIndex:2"
      - "extract:$.name.last"

  dependent3DOB:
    expr: "$.dependents"
    transform:
      - "safeIndex:2"
      - "extract:$.dob"

  dependent3Relationship:
    expr: "$.dependents"
    transform:
      - "safeIndex:2"
      - "extract:$.relationship"
```

---

# 🧩 **How This YAML Works:**

### 🔹 `expr: "$.dependents"`

Returns the **entire dependents array**, regardless of count.

### 🔹 `safeIndex:N` (built-in transform implemented in Java)

```java
// behavior
safeIndex(array, index):
    if array is null → return null
    if index >= array.length → return null
    else return array[index]
```

This means:

* If there are **0 dependents** → all dependent fields become blank (expected for PDFs)
* If there is **1 dependent** → Dependent 1 fills, Dependent 2 & 3 blank
* If there are **3 dependents** → all three fill cleanly
* No JSONPath errors, no array-out-of-bound issues

### 🔹 `extract:$.name.first`

Runs an *inner JSONPath query* on the object returned by `safeIndex`.

This gives clean, declarative YAML:

```
$.dependents → safeIndex(i) → extract one field
```

---

# ⭐ **Why This Approach Is Best**

### ✔ Clean YAML

### ✔ Fully dynamic dependent count

### ✔ No JSONPath complexity

### ✔ No conditional logic

### ✔ Works perfectly with PDFs (blank if missing)

### ✔ Safe and predictable

---

# 👉 Want More?

I can also provide:

### 🔹 The **Java implementation** of:

* `safeIndex` transform
* `extract` transform
* `mask:ssn` transform
* `date:` transform

### 🔹 A sample enrollment JSON with:

* 0 dependents
* 1 dependent
* 3 dependents

### 🔹 An extended YAML supporting 5 dependents instead of 3

### 🔹 A more compact YAML using anchors & anchors reuse

### 🔹 A JSONata version of the same mapping

Tell me what you'd like next.
