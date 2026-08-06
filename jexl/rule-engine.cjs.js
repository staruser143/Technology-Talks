// CommonJS version of rule-engine.js for testing
// Original: jexl/rule-engine.js (ES module)

const Jexl = require('jexl');

/**
 * Evaluates a list of structured rules against a given context.
 * Each rule should have:
 * - name: string
 * - condition: JEXL expression that returns a boolean
 * - result: JEXL expression that returns structured output
 *
 * @param {Array} rules - Array of rule objects
 * @param {Object} context - Data context for evaluation
 * @returns {Promise<Array>} - Array of matched rules with their structured outputs
 */
async function evaluateRules(rules, context) {
  const jexl = new Jexl.Jexl();
  const matchedResults = [];

  for (const rule of rules) {
    try {
      // Basic security check
      if (rule.condition.includes('__proto__') || rule.result.includes('__proto__')) {
        console.warn(`Skipping unsafe rule: ${rule.name}`);
        continue;
      }

      const conditionMet = await jexl.eval(rule.condition, context);
      if (conditionMet) {
        const output = await jexl.eval(rule.result, context);
        matchedResults.push({ name: rule.name, ...output });
      }
    } catch (err) {
      console.error(`Error evaluating rule "${rule.name}":`, err.message);
    }
  }

  return matchedResults;
}

/**
 * Validates data against a set of JEXL-based validation rules.
 * Each rule should have:
 * - field: string (field name being validated)
 * - expr: JEXL expression that returns boolean (true = valid)
 * - message: error message if validation fails
 * - code: error code
 *
 * @param {Object} data - Data to validate
 * @param {Array} rules - Array of validation rule objects
 * @returns {Promise<Array>} - Array of validation error objects
 */
async function validate(data, rules) {
  const jexl = new Jexl.Jexl();
  const errors = [];
  for (const rule of rules) {
    const valid = await jexl.eval(rule.expr, data);
    if (!valid) {
      errors.push({ code: rule.code, message: rule.message });
    }
  }
  return errors;
}

module.exports = { evaluateRules, validate };
