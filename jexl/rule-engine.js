
// ruleEngine.js
import Jexl from 'jexl';

/**
 * Adds custom filters to JEXL if needed.
 * Example: Jexl.addFilter('uppercase', val => typeof val === 'string' ? val.toUpperCase() : val);
 */

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
export async function evaluateRules(rules, context) {
  const matchedResults = [];

  for (const rule of rules) {
    try {
      // Basic security check
      if (rule.condition.includes('__proto__') || rule.result.includes('__proto__')) {
        console.warn(`Skipping unsafe rule: ${rule.name}`);
        continue;
      }

      const conditionMet = await Jexl.eval(rule.condition, context);
      if (conditionMet) {
        const output = await Jexl.eval(rule.result, context);
        matchedResults.push({ name: rule.name, ...output });
      }
    } catch (err) {
      console.error(`Error evaluating rule "${rule.name}":`, err.message);
    }
  }

  return matchedResults;
}
