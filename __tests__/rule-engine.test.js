const { evaluateRules, validate } = require('../jexl/rule-engine.cjs');

describe('evaluateRules', () => {
  test('returns matched rules when condition is met', async () => {
    const rules = [
      {
        name: 'Premium Discount',
        condition: "membership == 'premium' && purchaseAmount > 1000",
        result: "{ discount: 0.2, message: '20% discount applied' }"
      }
    ];

    const context = {
      membership: 'premium',
      purchaseAmount: 1200
    };

    const results = await evaluateRules(rules, context);
    expect(results).toHaveLength(1);
    expect(results[0].name).toBe('Premium Discount');
    expect(results[0].discount).toBe(0.2);
  });

  test('returns empty array when no conditions are met', async () => {
    const rules = [
      {
        name: 'Premium Discount',
        condition: "membership == 'premium' && purchaseAmount > 1000",
        result: "{ discount: 0.2 }"
      }
    ];

    const context = {
      membership: 'basic',
      purchaseAmount: 500
    };

    const results = await evaluateRules(rules, context);
    expect(results).toHaveLength(0);
  });

  test('evaluates multiple rules and returns all matches', async () => {
    const rules = [
      {
        name: 'Age Check',
        condition: 'age >= 18',
        result: "{ status: 'adult' }"
      },
      {
        name: 'Active Check',
        condition: 'isActive == true',
        result: "{ access: 'granted' }"
      },
      {
        name: 'Minor Check',
        condition: 'age < 18',
        result: "{ status: 'minor' }"
      }
    ];

    const context = { age: 25, isActive: true };

    const results = await evaluateRules(rules, context);
    expect(results).toHaveLength(2);
    expect(results[0].name).toBe('Age Check');
    expect(results[0].status).toBe('adult');
    expect(results[1].name).toBe('Active Check');
    expect(results[1].access).toBe('granted');
  });

  test('skips rules with __proto__ in condition (security check)', async () => {
    const warnSpy = jest.spyOn(console, 'warn').mockImplementation(() => {});

    const rules = [
      {
        name: 'Unsafe Rule',
        condition: "__proto__.polluted == true",
        result: "{ hacked: true }"
      }
    ];

    const context = {};
    const results = await evaluateRules(rules, context);

    expect(results).toHaveLength(0);
    expect(warnSpy).toHaveBeenCalledWith(expect.stringContaining('Skipping unsafe rule'));

    warnSpy.mockRestore();
  });

  test('skips rules with __proto__ in result (security check)', async () => {
    const warnSpy = jest.spyOn(console, 'warn').mockImplementation(() => {});

    const rules = [
      {
        name: 'Unsafe Result',
        condition: 'true',
        result: "{ __proto__: { polluted: true } }"
      }
    ];

    const context = {};
    const results = await evaluateRules(rules, context);

    expect(results).toHaveLength(0);
    expect(warnSpy).toHaveBeenCalledWith(expect.stringContaining('Skipping unsafe rule'));

    warnSpy.mockRestore();
  });

  test('handles errors in rule evaluation gracefully', async () => {
    const errorSpy = jest.spyOn(console, 'error').mockImplementation(() => {});

    const rules = [
      {
        name: 'Bad Condition',
        condition: '!!!invalid syntax###',
        result: "{ x: 1 }"
      }
    ];

    const context = {};
    const results = await evaluateRules(rules, context);

    expect(results).toHaveLength(0);
    expect(errorSpy).toHaveBeenCalled();

    errorSpy.mockRestore();
  });

  test('handles empty rules array', async () => {
    const results = await evaluateRules([], { foo: 'bar' });
    expect(results).toHaveLength(0);
  });

  test('handles numeric comparisons', async () => {
    const rules = [
      {
        name: 'Price Tier',
        condition: 'price > 100 && price <= 500',
        result: "{ tier: 'mid' }"
      }
    ];

    const context = { price: 250 };
    const results = await evaluateRules(rules, context);
    expect(results).toHaveLength(1);
    expect(results[0].tier).toBe('mid');
  });

  test('handles boolean context values', async () => {
    const rules = [
      {
        name: 'Active User',
        condition: 'isActive',
        result: "{ label: 'active' }"
      }
    ];

    const results1 = await evaluateRules(rules, { isActive: true });
    expect(results1).toHaveLength(1);

    const results2 = await evaluateRules(rules, { isActive: false });
    expect(results2).toHaveLength(0);
  });
});

describe('validate', () => {
  test('returns empty array when all rules pass', async () => {
    const data = { age: 25, email: 'user@example.com' };
    const rules = [
      { field: 'age', expr: 'age >= 18', message: 'Must be 18+', code: 'AGE_INVALID' }
    ];

    const errors = await validate(data, rules);
    expect(errors).toHaveLength(0);
  });

  test('returns errors when validation fails', async () => {
    const data = { age: 12, email: 'user@example.com' };
    const rules = [
      { field: 'age', expr: 'age >= 18', message: 'Must be 18+', code: 'AGE_INVALID' }
    ];

    const errors = await validate(data, rules);
    expect(errors).toHaveLength(1);
    expect(errors[0].code).toBe('AGE_INVALID');
    expect(errors[0].message).toBe('Must be 18+');
  });

  test('validates multiple rules', async () => {
    const data = { age: 12, email: '' };
    const rules = [
      { field: 'age', expr: 'age >= 18', message: 'Must be 18+', code: 'AGE_INVALID' },
      { field: 'email', expr: 'email != ""', message: 'Email required', code: 'EMAIL_REQUIRED' }
    ];

    const errors = await validate(data, rules);
    expect(errors).toHaveLength(2);
  });

  test('handles empty rules array', async () => {
    const errors = await validate({ name: 'test' }, []);
    expect(errors).toHaveLength(0);
  });

  test('validates numeric ranges', async () => {
    const data = { score: 85 };
    const rules = [
      { field: 'score', expr: 'score >= 0 && score <= 100', message: 'Score out of range', code: 'SCORE_RANGE' }
    ];

    const errors = await validate(data, rules);
    expect(errors).toHaveLength(0);
  });

  test('catches score out of range', async () => {
    const data = { score: 150 };
    const rules = [
      { field: 'score', expr: 'score >= 0 && score <= 100', message: 'Score out of range', code: 'SCORE_RANGE' }
    ];

    const errors = await validate(data, rules);
    expect(errors).toHaveLength(1);
    expect(errors[0].code).toBe('SCORE_RANGE');
  });

  test('validates string equality', async () => {
    const data = { status: 'active' };
    const rules = [
      { field: 'status', expr: "status == 'active' || status == 'pending'", message: 'Invalid status', code: 'STATUS_INVALID' }
    ];

    const errors = await validate(data, rules);
    expect(errors).toHaveLength(0);
  });

  test('validates boolean fields', async () => {
    const data = { termsAccepted: false };
    const rules = [
      { field: 'termsAccepted', expr: 'termsAccepted == true', message: 'Must accept terms', code: 'TERMS_REQUIRED' }
    ];

    const errors = await validate(data, rules);
    expect(errors).toHaveLength(1);
    expect(errors[0].code).toBe('TERMS_REQUIRED');
  });
});
