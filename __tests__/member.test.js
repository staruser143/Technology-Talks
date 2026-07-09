const { skillsMember } = require('../member');

describe('skillsMember', () => {
  test('returns the expected member string', () => {
    expect(skillsMember()).toBe('I am a member of the skills.js module');
  });

  test('returns a string type', () => {
    expect(typeof skillsMember()).toBe('string');
  });

  test('does not return undefined or null', () => {
    expect(skillsMember()).toBeDefined();
    expect(skillsMember()).not.toBeNull();
  });

  test('contains "skills.js" in the return value', () => {
    expect(skillsMember()).toContain('skills.js');
  });

  test('contains "member" in the return value', () => {
    expect(skillsMember()).toContain('member');
  });
});
