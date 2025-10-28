Great question! Using a **`static` nested class** like `FieldMapping` in Java is a common and intentional design choice — but it's worth understanding **why**, and **when you should (or shouldn’t)** use it.

---

### ✅ Short Answer

You use `static` for the nested class (`FieldMapping`) because:
- It **doesn’t need access** to the outer class (`PdfFormFiller`) instance.
- It avoids **memory overhead** and potential **memory leaks**.
- It makes the class **reusable, testable, and semantically correct**.

If you **don’t** use `static`, the nested class becomes an **inner class**, which **implicitly holds a reference** to the enclosing instance — even if it never uses it.

---

### 🔍 Deep Dive: Static vs Non-Static Nested Classes

#### 1. **Non-static nested class (inner class)**
```java
public class PdfFormFiller {
    public class FieldMapping { // ← NOT static
        public String pdf_field;
        // ...
    }
}
```
- Each `FieldMapping` instance **holds a hidden reference** to the `PdfFormFiller` instance that created it.
- You **must** instantiate it via an outer instance:
  ```java
  PdfFormFiller filler = new PdfFormFiller();
  FieldMapping fm = filler.new FieldMapping(); // awkward!
  ```
- **Wastes memory** if you don’t need the outer reference.
- Can cause **serialization issues** or **memory leaks** (e.g., if `FieldMapping` is cached or passed to another thread).

#### 2. **Static nested class**
```java
public class PdfFormFiller {
    public static class FieldMapping { // ← static
        public String pdf_field;
        // ...
    }
}
```
- **No hidden reference** to the outer class.
- Behaves like a **top-level class**, just logically grouped inside `PdfFormFiller`.
- Instantiated normally:
  ```java
  FieldMapping fm = new PdfFormFiller.FieldMapping();
  ```
- **Serializable**, **lightweight**, and **safe** to use independently.
- Perfect for **data holders** (like DTOs, config classes, mappings).

---

### 📌 In Your Use Case

Your `FieldMapping` class is a **pure data container**:
- It holds `pdf_field`, `json_path`, `transform`, etc.
- It **never accesses** methods or fields of `PdfFormFiller`.
- It’s used by **YAML parser** (SnakeYAML), which creates instances **without** an outer class context.

> ❌ If you made it non-static, **SnakeYAML would fail** with an exception like:
> ```
> Cannot construct instance of `PdfFormFiller$FieldMapping`: no suitable constructor found
> ```

Because SnakeYAML tries to call `new FieldMapping()`, but a non-static inner class **requires an enclosing instance** — which doesn’t exist during deserialization.

---

### ✅ Best Practice

> **Always use `static` for nested classes that are just data structures or helpers that don’t need the outer instance.**

This includes:
- DTOs (Data Transfer Objects)
- Configuration classes
- Record-like structures (pre-Java 14)
- Builder patterns (sometimes)

---

### 💡 Alternative: Use a Top-Level Class

You could also define `FieldMapping` in its own file:

```java
// FieldMapping.java
public class FieldMapping {
    public String pdf_field;
    public String json_path;
    public String transform;
    // ...
}
```

This is **equally valid** and often preferred for:
- Reusability across multiple classes
- Cleaner separation of concerns
- Simpler testing

But if the class is **only used inside one class** and is **simple**, a `static` nested class keeps things tidy.

---

### ✅ Summary

| Reason | Why `static`? |
|-------|----------------|
| **No outer dependency** | `FieldMapping` doesn’t use `PdfFormFiller` |
| **Memory efficiency** | No hidden reference → less memory |
| **Serialization/YAML safety** | Libraries like SnakeYAML require it |
| **Cleaner semantics** | It’s a data type, not an inner behavior |
| **Avoids runtime errors** | Non-static would break deserialization |

So yes — **using `static` here is not just correct, it’s necessary** for your code to work reliably.

Let me know if you'd like to refactor it into a record (Java 14+) or a proper POJO with getters/setters!