import { evaluateRules } from './ruleEngine.js';

const rules = [
  {
    name: "Premium Discount",
    condition: "membership === 'premium' && purchaseAmount > 1000",
    result: "{ discount: 0.2, message: '20% discount applied' }"
  }
];

const context = {
  membership: 'premium',
  purchaseAmount: 1200
};

evaluateRules(rules, context).then(results => {
  console.log(results);
});
