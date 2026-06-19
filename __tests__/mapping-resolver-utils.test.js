/**
 * Tests for the utility functions from pdf/EnhancedMappingResolver.java.
 * The pure utility methods are ported to JavaScript and tested independently.
 * This covers: getNestedValue, applyFormatter, toTitleCase, renderTemplate,
 * jsonString, and fmt (from FormToJson.java).
 */

// --- Ported utility functions from EnhancedMappingResolver.java ---

function getNestedValue(data, fieldPath) {
  if (fieldPath === null || fieldPath === undefined || fieldPath.trim() === '') return null;

  const parts = fieldPath.split('.');
  let current = data;

  for (const part of parts) {
    if (current !== null && current !== undefined && typeof current === 'object') {
      current = current[part];
    } else {
      return null;
    }
  }
  return current !== undefined ? current : null;
}

function toTitleCase(input) {
  if (input === null || input === undefined || input === '') return input;
  let result = '';
  let nextTitleCase = true;
  for (const c of input) {
    if (c === ' ') {
      nextTitleCase = true;
      result += c;
    } else if (nextTitleCase) {
      result += c.toUpperCase();
      nextTitleCase = false;
    } else {
      result += c;
    }
  }
  return result;
}

function applyFormatter(value, formatterSpec) {
  try {
    if (formatterSpec.startsWith('date:')) {
      // Simplified date formatting for JS
      const pattern = formatterSpec.substring(5);
      const date = value instanceof Date ? value : new Date(typeof value === 'number' ? value : parseInt(value));
      if (pattern === 'yyyy-MM-dd') {
        return date.toISOString().split('T')[0];
      }
      return date.toISOString();
    } else if (formatterSpec.startsWith('currency:')) {
      const currencyCode = formatterSpec.substring(9);
      if (typeof value === 'number') {
        return value.toFixed(2).replace(/\B(?=(\d{3})+(?!\d))/g, ',') + ' ' + currencyCode;
      }
    } else {
      switch (formatterSpec) {
        case 'UPPERCASE': return value.toString().toUpperCase();
        case 'LOWERCASE': return value.toString().toLowerCase();
        case 'TITLECASE': return toTitleCase(value.toString());
        default: return value.toString();
      }
    }
  } catch (e) {
    // Return original on error
  }
  return value;
}

function renderTemplate(template, data) {
  let result = template;
  for (const [key, val] of Object.entries(data)) {
    result = result.split(key).join(val !== null && val !== undefined ? val.toString() : '');
  }
  return result;
}

// From FormToJson.java
function jsonString(s) {
  if (s === null || s === undefined) return 'null';
  let out = '"';
  for (let i = 0; i < s.length; i++) {
    const c = s.charAt(i);
    switch (c) {
      case '"': out += '\\"'; break;
      case '\\': out += '\\\\'; break;
      case '\b': out += '\\b'; break;
      case '\f': out += '\\f'; break;
      case '\n': out += '\\n'; break;
      case '\r': out += '\\r'; break;
      case '\t': out += '\\t'; break;
      default:
        if (c.charCodeAt(0) < 0x20) {
          out += '\\u' + c.charCodeAt(0).toString(16).padStart(4, '0');
        } else {
          out += c;
        }
    }
  }
  out += '"';
  return out;
}

function fmt(v) {
  if (Math.abs(v - Math.round(v)) < 1e-6) {
    return Math.round(v).toString();
  }
  return v.toString();
}

// --- Tests ---

describe('getNestedValue', () => {
  test('gets top-level value', () => {
    expect(getNestedValue({ name: 'John' }, 'name')).toBe('John');
  });

  test('gets deeply nested value', () => {
    const data = { user: { address: { city: 'NYC' } } };
    expect(getNestedValue(data, 'user.address.city')).toBe('NYC');
  });

  test('returns null for missing path', () => {
    expect(getNestedValue({ name: 'John' }, 'age')).toBeNull();
  });

  test('returns null for missing nested path', () => {
    const data = { user: { name: 'John' } };
    expect(getNestedValue(data, 'user.address.city')).toBeNull();
  });

  test('returns null for null fieldPath', () => {
    expect(getNestedValue({ a: 1 }, null)).toBeNull();
  });

  test('returns null for empty fieldPath', () => {
    expect(getNestedValue({ a: 1 }, '')).toBeNull();
  });

  test('returns null for whitespace-only fieldPath', () => {
    expect(getNestedValue({ a: 1 }, '   ')).toBeNull();
  });

  test('handles numeric values', () => {
    expect(getNestedValue({ count: 42 }, 'count')).toBe(42);
  });

  test('handles boolean values', () => {
    expect(getNestedValue({ active: true }, 'active')).toBe(true);
  });

  test('handles array values at leaf', () => {
    const data = { items: [1, 2, 3] };
    expect(getNestedValue(data, 'items')).toEqual([1, 2, 3]);
  });

  test('returns null when intermediate is not an object', () => {
    const data = { name: 'John' };
    expect(getNestedValue(data, 'name.first')).toBeNull();
  });
});

describe('toTitleCase', () => {
  test('capitalizes first letter of each word', () => {
    expect(toTitleCase('hello world')).toBe('Hello World');
  });

  test('handles single word', () => {
    expect(toTitleCase('hello')).toBe('Hello');
  });

  test('handles empty string', () => {
    expect(toTitleCase('')).toBe('');
  });

  test('handles null', () => {
    expect(toTitleCase(null)).toBeNull();
  });

  test('handles already title-cased text', () => {
    expect(toTitleCase('Hello World')).toBe('Hello World');
  });

  test('handles all uppercase text', () => {
    expect(toTitleCase('HELLO WORLD')).toBe('HELLO WORLD');
  });

  test('handles multiple spaces between words', () => {
    expect(toTitleCase('hello  world')).toBe('Hello  World');
  });

  test('handles single character', () => {
    expect(toTitleCase('a')).toBe('A');
  });

  test('handles mixed case', () => {
    expect(toTitleCase('jOHN dOE')).toBe('JOHN DOE');
  });
});

describe('applyFormatter', () => {
  test('formats UPPERCASE', () => {
    expect(applyFormatter('hello world', 'UPPERCASE')).toBe('HELLO WORLD');
  });

  test('formats LOWERCASE', () => {
    expect(applyFormatter('HELLO WORLD', 'LOWERCASE')).toBe('hello world');
  });

  test('formats TITLECASE', () => {
    expect(applyFormatter('hello world', 'TITLECASE')).toBe('Hello World');
  });

  test('formats currency with USD', () => {
    expect(applyFormatter(1234.5, 'currency:USD')).toBe('1,234.50 USD');
  });

  test('formats currency with EUR', () => {
    expect(applyFormatter(999, 'currency:EUR')).toBe('999.00 EUR');
  });

  test('formats currency with large numbers', () => {
    expect(applyFormatter(1234567.89, 'currency:USD')).toBe('1,234,567.89 USD');
  });

  test('formats date with yyyy-MM-dd pattern', () => {
    const date = new Date('2024-03-15T00:00:00Z');
    expect(applyFormatter(date, 'date:yyyy-MM-dd')).toBe('2024-03-15');
  });

  test('returns value.toString() for unknown formatter', () => {
    expect(applyFormatter(42, 'unknown')).toBe('42');
  });

  test('handles number as string in UPPERCASE', () => {
    expect(applyFormatter(123, 'UPPERCASE')).toBe('123');
  });
});

describe('renderTemplate', () => {
  test('replaces single placeholder', () => {
    expect(renderTemplate('Hello {{name}}', { '{{name}}': 'World' })).toBe('Hello World');
  });

  test('replaces multiple placeholders', () => {
    const template = '{{greeting}} {{name}}!';
    const data = { '{{greeting}}': 'Hello', '{{name}}': 'John' };
    expect(renderTemplate(template, data)).toBe('Hello John!');
  });

  test('replaces null values with empty string', () => {
    expect(renderTemplate('Hello {{name}}', { '{{name}}': null })).toBe('Hello ');
  });

  test('replaces undefined values with empty string', () => {
    expect(renderTemplate('Hello {{name}}', { '{{name}}': undefined })).toBe('Hello ');
  });

  test('handles template with no placeholders', () => {
    expect(renderTemplate('Hello World', {})).toBe('Hello World');
  });

  test('handles empty template', () => {
    expect(renderTemplate('', { key: 'value' })).toBe('');
  });

  test('replaces multiple occurrences of same placeholder', () => {
    expect(renderTemplate('{{x}} and {{x}}', { '{{x}}': 'A' })).toBe('A and A');
  });

  test('handles numeric values', () => {
    expect(renderTemplate('Count: {{count}}', { '{{count}}': 42 })).toBe('Count: 42');
  });
});

describe('jsonString (from FormToJson.java)', () => {
  test('wraps simple string in quotes', () => {
    expect(jsonString('hello')).toBe('"hello"');
  });

  test('returns null for null input', () => {
    expect(jsonString(null)).toBe('null');
  });

  test('returns null for undefined input', () => {
    expect(jsonString(undefined)).toBe('null');
  });

  test('escapes double quotes', () => {
    expect(jsonString('say "hello"')).toBe('"say \\"hello\\""');
  });

  test('escapes backslashes', () => {
    expect(jsonString('path\\to\\file')).toBe('"path\\\\to\\\\file"');
  });

  test('escapes newlines', () => {
    expect(jsonString('line1\nline2')).toBe('"line1\\nline2"');
  });

  test('escapes tabs', () => {
    expect(jsonString('col1\tcol2')).toBe('"col1\\tcol2"');
  });

  test('escapes carriage returns', () => {
    expect(jsonString('line1\rline2')).toBe('"line1\\rline2"');
  });

  test('escapes backspace', () => {
    expect(jsonString('text\bmore')).toBe('"text\\bmore"');
  });

  test('escapes form feed', () => {
    expect(jsonString('text\fmore')).toBe('"text\\fmore"');
  });

  test('handles empty string', () => {
    expect(jsonString('')).toBe('""');
  });

  test('handles control characters', () => {
    const result = jsonString('\x01\x02');
    expect(result).toBe('"\\u0001\\u0002"');
  });

  test('handles mixed special characters', () => {
    expect(jsonString('"hello\n\\world"')).toBe('"\\"hello\\n\\\\world\\""');
  });
});

describe('fmt (from FormToJson.java)', () => {
  test('formats integer float as integer string', () => {
    expect(fmt(5.0)).toBe('5');
  });

  test('formats non-integer float as float string', () => {
    expect(fmt(5.5)).toBe('5.5');
  });

  test('formats zero', () => {
    expect(fmt(0)).toBe('0');
  });

  test('formats negative integer', () => {
    expect(fmt(-3.0)).toBe('-3');
  });

  test('formats negative float', () => {
    expect(fmt(-3.14)).toBe('-3.14');
  });

  test('formats very small decimal as integer', () => {
    expect(fmt(100.0000001)).toBe('100');
  });

  test('formats large integer', () => {
    expect(fmt(1000000.0)).toBe('1000000');
  });
});
