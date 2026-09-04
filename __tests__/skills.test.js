const { calculateNumbers } = require('../skills');

describe('calculateNumbers', () => {
  test('adds two positive integers', () => {
    expect(calculateNumbers(2, 3)).toBe(5);
  });

  test('adds two negative integers', () => {
    expect(calculateNumbers(-2, -3)).toBe(-5);
  });

  test('adds a positive and a negative integer', () => {
    expect(calculateNumbers(5, -3)).toBe(2);
  });

  test('adds zero to a number', () => {
    expect(calculateNumbers(0, 7)).toBe(7);
    expect(calculateNumbers(7, 0)).toBe(7);
  });

  test('adds two zeros', () => {
    expect(calculateNumbers(0, 0)).toBe(0);
  });

  test('adds floating point numbers', () => {
    expect(calculateNumbers(1.5, 2.3)).toBeCloseTo(3.8);
  });

  test('adds large numbers', () => {
    expect(calculateNumbers(1000000, 2000000)).toBe(3000000);
  });

  test('concatenates strings when passed strings', () => {
    expect(calculateNumbers('hello', ' world')).toBe('hello world');
  });

  test('handles string and number addition (type coercion)', () => {
    expect(calculateNumbers('5', 3)).toBe('53');
  });
});
