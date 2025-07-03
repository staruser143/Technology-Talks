//const fs = require('fs');
//const path = require('path');
import { Jexl } from 'jexl';
import rules from '../src/rules.json';
const jexl = new Jexl();

// Register a regexMatch function
jexl.addFunction('regexMatch', (val: string, pattern: string) => {
  const re = new RegExp(pattern);
  return typeof val === 'string' && re.test(val);
});
//jexl.addFunction('minLength', (val: string, min: number) => typeof val === 'string' && val.length >= min);
jexl.addFunction('maxLength', (val: string, max: number) => typeof val === 'string' && val.length <= max);
jexl.addFunction('isType', (val: any, type: string) => typeof val === type);

// Example data object
const data = {
  age: 12,
  email: 'user@example.com',
  isActive: true,
  password: '0123456789',
  confirmPassword: '0123456789'
};
//
// Load rules from external JSON file
//const rulesPath = path.join(__dirname, '../src/rules.json');
//const rules1 = JSON.parse(fs.readFileSync(rulesPath, 'utf8'));
//const rules2 = rules1;

// Function to validate data
async function validate(
  data: any,
  rules: { field: string, expr: string, message: string, code: string }[]
) {
  const errors: { code: string; message: string }[] = [];
  for (const rule of rules) {
    const valid = await jexl.eval(rule.expr, data);
    if (!valid) {
      errors.push({ code: rule.code, message: rule.message });
    }
  }
  return errors;
}

// Usage
validate(data, rules).then(errors => {
  if (errors.length === 0) {
    console.log('Validation passed!');
  } else {
    console.log('Validation errors:', errors);
  }
});
