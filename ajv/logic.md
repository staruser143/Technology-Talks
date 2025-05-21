Great question — and you're absolutely right to point this out.

By default, AJV doesn't rename fields — so the property name in your form data (e.g., dob) must exactly match the key in the schema (e.g., dateOfBirth), or vice versa.


---

Options to handle this cleanly:

Option 1: Match field name in form data to schema

Make sure your form data uses dateOfBirth as the key:

const formData = {
  dateOfBirth: "2000-01-01", // must match schema property
  password: "abc123",
  confirmPassword: "abc123"
};

Then in your custom keyword (like minAge), you'll access data.dateOfBirth directly — no remapping needed.

Option 2: Add minAge to a specific field

Your minAge keyword is defined at the field level, not at the object level — so you don't actually need to reference the property name (dob or dateOfBirth) in the keyword implementation. AJV automatically passes in the value for the field where minAge is used.

Example:

If your schema has:

"dateOfBirth": {
  "type": "string",
  "format": "date",
  "minAge": 18
}

Then your keyword implementation works like this:

validate(minAge: number, dob: string) {
  // `dob` here is automatically the value of `data.dateOfBirth`
}

So no need to reference the field name in the keyword — AJV handles that internally when validating a field-level keyword.


---

Summary

AJV passes the value of dateOfBirth to the validate() function — no need to access by key like data["dateOfBirth"].

Just make sure your form data keys match the schema exactly.

If you must map different field names, you'll need a preprocessing step to rename fields in your data before validating.


Would you like an example where the form uses dob but the schema uses dateOfBirth, and we map between them?

