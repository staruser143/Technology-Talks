To integrate **JEXL with a backend**, you’ll want to build a system where:

- Rules are stored and managed on the backend (e.g., in a database).
- The backend exposes APIs to **create, read, update, delete (CRUD)** rules.
- The backend can **evaluate rules** using JEXL against incoming data (context).
- Optionally, the frontend can send context and get rule evaluation results.

---

## ✅ Backend Integration Architecture

### 🔹 1. **Rule Storage**
Store rules in a database like MongoDB or PostgreSQL with fields like:
```json
{
  "name": "Premium Discount",
  "condition": "membership === 'premium' && purchaseAmount > 1000",
  "result": "{ discount: 0.2, message: '20% discount applied' }"
}
```

---

### 🔹 2. **Node.js Backend Setup (Express + JEXL)**

#### 📦 Install dependencies
```bash
npm install express jexl body-parser mongoose
```

#### 🧩 Sample API Server

```js
// server.js
import express from 'express';
import bodyParser from 'body-parser';
import Jexl from 'jexl';
import mongoose from 'mongoose';

const app = express();
app.use(bodyParser.json());

// Connect to MongoDB
mongoose.connect('mongodb://localhost:27017/rules', { useNewUrlParser: true, useUnifiedTopology: true });

const RuleSchema = new mongoose.Schema({
  name: String,
  condition: String,
  result: String
});
const Rule = mongoose.model('Rule', RuleSchema);

// Evaluate rules endpoint
app.post('/evaluate', async (req, res) => {
  const context = req.body.context;
  const rules = await Rule.find();

  const results = [];

  for (const rule of rules) {
    try {
      const conditionMet = await Jexl.eval(rule.condition, context);
      if (conditionMet) {
        const output = await Jexl.eval(rule.result, context);
        results.push({ name: rule.name, ...output });
      }
    } catch (err) {
      console.error(`Error in rule "${rule.name}":`, err.message);
    }
  }

  res.json(results);
});

// CRUD endpoints for rules (optional)
app.get('/rules', async (req, res) => {
  const rules = await Rule.find();
  res.json(rules);
});

app.post('/rules', async (req, res) => {
  const newRule = new Rule(req.body);
  await newRule.save();
  res.json(newRule);
});

app.listen(3000, () => console.log('Rule engine backend running on port 3000'));
```

---

### 🔹 3. **Frontend Integration**

From your React app, you can:
- Fetch rules from `/rules`
- Send context to `/evaluate`
- Display matched results

```js
const evaluateRulesFromBackend = async (context) => {
  const response = await fetch('http://localhost:3000/evaluate', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ context })
  });
  return await response.json();
};
```

---

## 🧠 Benefits of Backend Integration

- Centralized rule management
- Dynamic updates without redeploying frontend
- Secure and scalable evaluation
- Easy to audit and log rule usage

---
