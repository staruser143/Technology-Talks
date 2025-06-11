You're absolutely right to raise that point—**Azure Cosmos DB for NoSQL is also mutable by default**, just like MongoDB. Here's a direct comparison and clarification:

---

## 🔄 **Mutability in Cosmos DB vs MongoDB Atlas**

| Feature                     | **Azure Cosmos DB (NoSQL API)**         | **MongoDB Atlas**                          |
|----------------------------|------------------------------------------|--------------------------------------------|
| **Default Behavior**       | Mutable documents                        | Mutable documents                          |
| **Event Immutability**     | Must be enforced at application level    | Must be enforced at application level      |
| **Write Operations**       | Supports insert, update, delete          | Supports insert, update, delete            |
| **Native Immutability Support** | ❌ No built-in immutability enforcement | ❌ No built-in immutability enforcement     |

---

## 🧠 Implications for Event Sourcing

In both systems:
- **Events must be treated as append-only**.
- **Application logic or access control** must prevent updates or deletions of event documents.
- **Immutability is a design discipline**, not a built-in feature.

---

## ✅ How to Enforce Immutability in Both

### **1. Application-Level Enforcement**
- Only allow `insert` operations for event collections.
- Disable or avoid `update` and `delete` operations on event containers.

### **2. Role-Based Access Control**
- Use **Cosmos DB RBAC** or **MongoDB Atlas roles** to restrict write permissions.
- Assign roles that allow only `create` operations on event collections.

### **3. Schema Validation (MongoDB only)**
- MongoDB supports **JSON Schema validation** to enforce structure and prevent updates to certain fields.
- Cosmos DB does not support schema validation natively.

### **4. Use Append-Only Patterns**
- Design event documents with **versioning** or **timestamp-based IDs**.
- Never overwrite existing documents—always insert new ones.

---

So yes, **both databases require careful design to ensure immutability**, and neither enforces it out of the box. The key difference is that **MongoDB offers more tools (like schema validation)** to help enforce it, while **Cosmos DB relies more on external enforcement** via application logic and access control.

Would you like a **code snippet or policy example** showing how to enforce immutability in either Cosmos DB or MongoDB?
