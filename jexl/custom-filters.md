Adding **custom filters** in JEXL is a great way to extend its functionality and make your expressions more powerful and readable. Filters in JEXL are similar to functions, but they’re applied using the pipe (`|`) syntax, which is intuitive and clean.

---

## ✅ How to Add Custom Filters in JEXL

### 1. **Basic Setup**

```js
import Jexl from 'jexl';

// Add a custom filter
Jexl.addFilter('uppercase', (val) => {
  return typeof val === 'string' ? val.toUpperCase() : val;
});
```

### 2. **Using the Filter in an Expression**

```js
const expression = "name | uppercase";
const context = { name: "john doe" };

Jexl.eval(expression, context).then(result => {
  console.log(result); // Output: "JOHN DOE"
});
```

---

## 🔧 More Examples of Custom Filters

### 🔹 Filter: `truncate`

```js
Jexl.addFilter('truncate', (val, length) => {
  return typeof val === 'string' ? val.slice(0, length) : val;
});

// Usage: "description | truncate(10)"
```

### 🔹 Filter: `currency`

```js
Jexl.addFilter('currency', (val, symbol = '$') => {
  return typeof val === 'number' ? `${symbol}${val.toFixed(2)}` : val;
});

// Usage: "price | currency('$')"
```

### 🔹 Filter: `isAdult`

```js
Jexl.addFilter('isAdult', (age) => age >= 18);

// Usage: "age | isAdult"
```

---

## 🛡️ Security Tip

When adding filters:
- Validate input types.
- Avoid exposing sensitive logic.
- Keep filters pure (no side effects).

---
