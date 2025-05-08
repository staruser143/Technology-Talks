Yes! **AJV supports cross-field validations**, including **dependent fields** and **conditional relationships** between different properties in an object. Below are several ways to implement such validations:

---

## **1. Dependent Fields (Required If Another Field Exists)**
Use `dependentRequired` (JSON Schema Draft 2019-09+) to enforce that certain fields must exist if a given field is present.

### **Example: Billing Address Required if `paymentMethod` is "creditCard"**
```javascript
const Ajv = require("ajv");
const ajv = new Ajv();

const schema = {
  type: "object",
  properties: {
    paymentMethod: { type: "string", enum: ["creditCard", "paypal"] },
    billingAddress: { type: "string" },
  },
  dependentRequired: {
    creditCard: ["billingAddress"], // If paymentMethod=creditCard, billingAddress is required
  },
};

const validate = ajv.compile(schema);

// Test cases
console.log(validate({ paymentMethod: "creditCard", billingAddress: "123 St" })); // ✅ Valid
console.log(validate({ paymentMethod: "creditCard" })); // ❌ Invalid (missing billingAddress)
console.log(validate({ paymentMethod: "paypal" })); // ✅ Valid (billingAddress not required)
```

---

## **2. Conditional Validation (If-Then-Else)**
Use `if`, `then`, `else` to apply different schemas based on a condition.

### **Example: Validate `discountCode` Only if `hasDiscount` is `true`**
```javascript
const schema = {
  type: "object",
  properties: {
    hasDiscount: { type: "boolean" },
    discountCode: { type: "string" },
  },
  if: { properties: { hasDiscount: { const: true } } }, // Condition
  then: { required: ["discountCode"] }, // If true, discountCode is required
  else: { properties: { discountCode: { const: undefined } } }, // If false, discountCode must not exist
};

const validate = ajv.compile(schema);

// Test cases
console.log(validate({ hasDiscount: true, discountCode: "SAVE20" })); // ✅ Valid
console.log(validate({ hasDiscount: true })); // ❌ Invalid (missing discountCode)
console.log(validate({ hasDiscount: false })); // ✅ Valid
console.log(validate({ hasDiscount: false, discountCode: "SAVE20" })); // ❌ Invalid (discountCode must not exist)
```

---

## **3. Cross-Field Validation Using `$data` References**
Compare fields dynamically using `$data` (e.g., ensure `endDate` > `startDate`).

### **Example: Validate `endDate` > `startDate`**
```javascript
const schema = {
  type: "object",
  properties: {
    startDate: { type: "string", format: "date" },
    endDate: { type: "string", format: "date" },
  },
  required: ["startDate", "endDate"],
  if: {
    properties: {
      startDate: { format: "date" },
      endDate: { format: "date" },
    },
  },
  then: {
    properties: {
      endDate: {
        format: "date",
        // Ensure endDate > startDate
        const: { $data: "1/startDate" },
        // OR use a custom keyword for complex logic
      },
    },
  },
};

// Alternative: Custom keyword for date comparison
ajv.addKeyword({
  keyword: "dateAfter",
  type: "string",
  validate: function (schema, data, parentSchema, dataCtx) {
    const startDate = new Date(dataCtx.rootData.startDate);
    const endDate = new Date(data);
    return endDate > startDate;
  },
});

const schemaWithCustomKeyword = {
  properties: {
    startDate: { type: "string", format: "date" },
    endDate: { type: "string", format: "date", dateAfter: true },
  },
};

const validate = ajv.compile(schemaWithCustomKeyword);
console.log(validate({ startDate: "2023-01-01", endDate: "2023-01-02" })); // ✅ Valid
console.log(validate({ startDate: "2023-01-01", endDate: "2022-12-31" })); // ❌ Invalid
```

---

## **4. Property Dependencies (`dependentSchemas`)**
Use `dependentSchemas` (Draft 2019-09+) to enforce complex dependencies.

### **Example: Require `cardNumber` and `expiry` if `paymentType` is "card"**
```javascript
const schema = {
  type: "object",
  properties: {
    paymentType: { type: "string", enum: ["card", "cash"] },
    cardNumber: { type: "string" },
    expiry: { type: "string" },
  },
  dependentSchemas: {
    card: {
      // If paymentType=card, these fields are required
      required: ["cardNumber", "expiry"],
    },
  },
};

const validate = ajv.compile(schema);

console.log(validate({ paymentType: "card", cardNumber: "1234", expiry: "12/25" })); // ✅ Valid
console.log(validate({ paymentType: "card" })); // ❌ Invalid (missing card fields)
console.log(validate({ paymentType: "cash" })); // ✅ Valid
```

---

## **5. Referencing Other Fields (`$ref` + `$data`)**
Combine `$ref` and `$data` to reuse schemas dynamically.

### **Example: Ensure `confirmPassword` Matches `password`**
```javascript
const schema = {
  type: "object",
  properties: {
    password: { type: "string", minLength: 8 },
    confirmPassword: { type: "string", const: { $data: "1/password" } },
  },
  required: ["password", "confirmPassword"],
};

const validate = ajv.compile(schema);

console.log(validate({ password: "secret123", confirmPassword: "secret123" })); // ✅ Valid
console.log(validate({ password: "secret123", confirmPassword: "wrong" })); // ❌ Invalid
```

---

## **Key Takeaways**
- **`dependentRequired`**: Enforce required fields conditionally.
- **`if-then-else`**: Apply different schemas based on conditions.
- **`$data`**: Compare fields dynamically (e.g., dates, passwords).
- **`dependentSchemas`**: Define complex dependencies.
- **Custom Keywords**: For advanced cross-field logic.

AJV’s support for JSON Schema Draft 2019-09+ makes it ideal for relational validations. For more, see the [AJV documentation](https://ajv.js.org/). 🚀