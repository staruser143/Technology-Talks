To create a **context editor** for testing inputs in your React app, we’ll build a simple form that allows users to modify the context values dynamically. This will let you simulate different scenarios and see how your business rules respond.

---

## ✅ Step-by-Step: Context Editor Component

### 🔹 1. **Create a Context Editor Component**

```jsx
// src/components/ContextEditor.js
import React from 'react';

const ContextEditor = ({ context, setContext }) => {
  const handleChange = (key, value) => {
    setContext((prev) => ({
      ...prev,
      [key]: isNaN(value) ? value : Number(value)
    }));
  };

  return (
    <div>
      <h3>Test Context Editor</h3>
      {Object.entries(context).map(([key, value]) => (
        <div key={key} style={{ marginBottom: '0.5rem' }}>
          <label>
            {key}:&nbsp;
            <input
              type="text"
              value={value}
              onChange={(e) => handleChange(key, e.target.value)}
            />
          </label>
        </div>
      ))}
    </div>
  );
};

export default ContextEditor;
```

---

### 🔹 2. **Integrate with Your Rule Engine Component**

Update your main component to include the context editor:

```jsx
import React, { useState } from 'react';
import { evaluateRules } from '../utils/ruleEngine';
import ContextEditor from './ContextEditor';

const EditableRuleEngine = () => {
  const [rules, setRules] = useState([
    {
      name: "Premium Discount",
      condition: "membership === 'premium' && purchaseAmount > 1000",
      result: "{ discount: 0.2, message: '20% discount applied' }"
    },
    {
      name: "Free Shipping",
      condition: "country === 'India' && purchaseAmount >= 500",
      result: "{ shipping: 'free', message: 'Eligible for free shipping' }"
    }
  ]);

  const [context, setContext] = useState({
    membership: 'premium',
    purchaseAmount: 1200,
    country: 'India',
    loyaltyPoints: 1500
  });

  const [results, setResults] = useState([]);

  const handleEvaluate = async () => {
    const matched = await evaluateRules(rules, context);
    setResults(matched);
  };

  return (
    <div>
      <h2>Editable Business Rules</h2>
      <ContextEditor context={context} setContext={setContext} />

      {/* Rule editing UI here (same as before) */}

      <button onClick={handleEvaluate}>Evaluate Rules</button>

      <h3>Matched Rules</h3>
      <ul>
        {results.map((r, idx) => (
          <li key={idx}>
            <strong>{r.name}</strong>: {r.message}
            {r.discount && <div>Discount: {r.discount * 100}%</div>}
            {r.shipping && <div>Shipping: {r.shipping}</div>}
            {r.bonus && <div>Bonus: ₹{r.bonus}</div>}
          </li>
        ))}
      </ul>
    </div>
  );
};

export default EditableRuleEngine;
```

---

## 🧠 Benefits

- Test different user scenarios instantly.
- Validate rule logic against real-world inputs.
- Easily extendable to support nested objects or arrays.

Would you like to add support for adding/removing context keys dynamically or saving test contexts for reuse?
