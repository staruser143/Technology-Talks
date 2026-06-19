/**
 * Tests for the CommandRouter matching logic from "nestjs handlers.js".
 * The core matching/routing algorithm is extracted and tested independently
 * without NestJS dependencies.
 */

// Extracted pure functions from nestjs handlers.js
function matches(config, requestAttributes) {
  return Object.entries(config.conditions).every(([key, value]) => {
    if (Array.isArray(value)) {
      return value.includes(requestAttributes[key]);
    }
    return requestAttributes[key] === value;
  });
}

function findMatchingHandler(handlerConfigs, requestAttributes) {
  return handlerConfigs
    .filter(config => matches(config, requestAttributes))
    .sort((a, b) => b.priority - a.priority)
    .map(config => config.name)[0] || null;
}

describe('CommandRouter matching logic', () => {
  describe('matches', () => {
    test('matches exact string conditions', () => {
      const config = {
        name: 'handler1',
        priority: 1,
        conditions: { requestType: 'create', requestSource: 'web' }
      };
      const attributes = { requestType: 'create', requestSource: 'web' };
      expect(matches(config, attributes)).toBe(true);
    });

    test('fails when a string condition does not match', () => {
      const config = {
        name: 'handler1',
        priority: 1,
        conditions: { requestType: 'create', requestSource: 'web' }
      };
      const attributes = { requestType: 'create', requestSource: 'mobile' };
      expect(matches(config, attributes)).toBe(false);
    });

    test('matches array conditions (value in list)', () => {
      const config = {
        name: 'handler2',
        priority: 2,
        conditions: { userRole: ['admin', 'manager'] }
      };
      const attributes = { userRole: 'admin' };
      expect(matches(config, attributes)).toBe(true);
    });

    test('fails when value not in array condition', () => {
      const config = {
        name: 'handler2',
        priority: 2,
        conditions: { userRole: ['admin', 'manager'] }
      };
      const attributes = { userRole: 'viewer' };
      expect(matches(config, attributes)).toBe(false);
    });

    test('matches with mixed string and array conditions', () => {
      const config = {
        name: 'handler3',
        priority: 3,
        conditions: {
          requestType: 'update',
          userRole: ['admin', 'editor']
        }
      };
      const attributes = { requestType: 'update', userRole: 'editor' };
      expect(matches(config, attributes)).toBe(true);
    });

    test('fails when attribute is missing', () => {
      const config = {
        name: 'handler1',
        priority: 1,
        conditions: { requestType: 'create' }
      };
      const attributes = {};
      expect(matches(config, attributes)).toBe(false);
    });

    test('matches with empty conditions', () => {
      const config = {
        name: 'catchAll',
        priority: 0,
        conditions: {}
      };
      const attributes = { requestType: 'anything' };
      expect(matches(config, attributes)).toBe(true);
    });

    test('matches numeric conditions', () => {
      const config = {
        name: 'handler4',
        priority: 1,
        conditions: { priority: 1 }
      };
      const attributes = { priority: 1 };
      expect(matches(config, attributes)).toBe(true);
    });

    test('fails on type mismatch (string vs number)', () => {
      const config = {
        name: 'handler4',
        priority: 1,
        conditions: { priority: 1 }
      };
      const attributes = { priority: '1' };
      expect(matches(config, attributes)).toBe(false);
    });

    test('matches boolean conditions', () => {
      const config = {
        name: 'handler5',
        priority: 1,
        conditions: { isActive: true }
      };
      const attributes = { isActive: true };
      expect(matches(config, attributes)).toBe(true);
    });
  });

  describe('findMatchingHandler', () => {
    const handlerConfigs = [
      {
        name: 'AdminHandler',
        priority: 10,
        conditions: { userRole: 'admin', requestType: 'create' }
      },
      {
        name: 'ManagerHandler',
        priority: 5,
        conditions: { userRole: ['admin', 'manager'], requestType: 'create' }
      },
      {
        name: 'DefaultHandler',
        priority: 1,
        conditions: { requestType: 'create' }
      }
    ];

    test('returns highest priority matching handler', () => {
      const result = findMatchingHandler(handlerConfigs, {
        userRole: 'admin',
        requestType: 'create'
      });
      expect(result).toBe('AdminHandler');
    });

    test('returns second-highest when top does not match', () => {
      const result = findMatchingHandler(handlerConfigs, {
        userRole: 'manager',
        requestType: 'create'
      });
      expect(result).toBe('ManagerHandler');
    });

    test('falls back to lowest priority handler', () => {
      const result = findMatchingHandler(handlerConfigs, {
        userRole: 'viewer',
        requestType: 'create'
      });
      expect(result).toBe('DefaultHandler');
    });

    test('returns null when no handler matches', () => {
      const result = findMatchingHandler(handlerConfigs, {
        userRole: 'admin',
        requestType: 'delete'
      });
      expect(result).toBeNull();
    });

    test('returns null for empty handler configs', () => {
      const result = findMatchingHandler([], { requestType: 'create' });
      expect(result).toBeNull();
    });

    test('handles single handler config', () => {
      const configs = [
        { name: 'OnlyHandler', priority: 1, conditions: { type: 'test' } }
      ];
      const result = findMatchingHandler(configs, { type: 'test' });
      expect(result).toBe('OnlyHandler');
    });

    test('priority ordering is descending (highest first)', () => {
      const configs = [
        { name: 'Low', priority: 1, conditions: {} },
        { name: 'High', priority: 100, conditions: {} },
        { name: 'Mid', priority: 50, conditions: {} }
      ];
      const result = findMatchingHandler(configs, { anything: 'value' });
      expect(result).toBe('High');
    });
  });
});
