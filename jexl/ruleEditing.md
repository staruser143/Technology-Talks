To add **rule editing in the UI** of your React app, we’ll create a simple interface that allows users to:

1. **View existing rules**
2. **Edit rule name, condition, and result**
3. **Save changes and re-evaluate**

---

## ✅ Updated React Component with Rule Editing

Here’s a complete example:

### 🔹 1. **Editable Rule List**

```jsx
import React, { useState } from 'react';
import { evaluateRules } from '../utils/ruleEngine';

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

  const [context] = useState({
    membership: 'premium',
    purchaseAmount: 1200,
    country: 'India',
    loyaltyPoints: 1500
  });

  const [results, setResults] = useState([]);

  const handleRuleChange = (index, field, value) => {
    const updatedRules = [...rules];
    updatedRules[index][field] = value;
    setRules(updatedRules);
  };

  const handleEvaluate = async () => {
    const matched = await evaluateRules(rules, context);
    setResults(matched);
  };

  return (
    <div>
      <h2>Editable Business Rules</h2>
      {rules.map((rule, index) => (
        <div key={index} style={{ marginBottom: '1rem', padding: '1rem', border: '1px solid #ccc' }}>
          <input
            type="text"
            value={rule.name}
            onChange={(e) => handleRuleChange(index, 'name', e.target.value)}
            placeholder="Rule Name"
            style={{ width: '100%', marginBottom: '0.5rem' }}
          />
          <textarea
            value={rule.condition}
            onChange={(e) => handleRuleChange(index, 'condition', e.target.value)}
            placeholder="Condition (JEXL)"
            rows={2}
            style={{ width: '100%', marginBottom: '0.5rem' }}
          />
          <textarea
            value={rule.result}
            onChange={(e) => handleRuleChange(index, 'result', e.target.value)}
            placeholder="Result (JEXL)"
            rows={2}
            style={{ width: '100%' }}
          />
        </div>
      ))}
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

## 🧠 Next Steps

- ✅ Add **validation** for expressions before saving.
- ✅ Allow **adding/removing rules** dynamically.
- 🔄 Persist rules to **localStorage** or a **backend API**.
- 🧪 Add a **test context editor** so users can simulate different inputs.

