Yes, you can provide custom error messages in AJV (with TypeScript + React) using one of these approaches:


---

Option 1: Use ajv-errors plugin (most flexible)

This allows custom error messages directly in your JSON schema using the errorMessage keyword.

Setup

1. Install the plugin:



npm install ajv-errors

2. Use it with your AJV instance:



import Ajv from "ajv";
import ajvErrors from "ajv-errors";

const ajv = new Ajv({ allErrors: true }); // allErrors is needed!
ajvErrors(ajv); // add plugin


---

Schema with Custom Error Messages

{
  "type": "object",
  "properties": {
    "dateOfBirth": {
      "type": "string",
      "format": "date",
      "minAge": 18,
      "errorMessage": {
        "format": "Date of birth must be a valid date.",
        "minAge": "You must be at least 18 years old."
      }
    },
    "password": {
      "type": "string",
      "minLength": 6,
      "errorMessage": {
        "minLength": "Password must be at least 6 characters long."
      }
    },
    "confirmPassword": {
      "type": "string",
      "minLength": 6,
      "errorMessage": {
        "minLength": "Confirm password must be at least 6 characters long."
      }
    }
  },
  "required": ["dateOfBirth", "password", "confirmPassword"],
  "errorMessage": {
    "required": {
      "dateOfBirth": "Date of birth is required.",
      "password": "Password is required.",
      "confirmPassword": "Please confirm your password."
    }
  },
  "passwordsMatch": true
}

> Note: minAge and passwordsMatch are assumed to be custom keywords that you implement, and you can attach errorMessage to them as well.




---

Option 2: Handle errors manually in code (for advanced logic)

If you're using custom keywords or dynamic conditions, you can:

Parse validate.errors array from AJV.

Replace default messages with your own based on keyword, instancePath, or params.


const errorMap: Record<string, string> = {
  "minAge": "You must be at least 18 years old.",
  "passwordsMatch": "Passwords do not match."
};

const messages = (validate.errors || []).map(err => {
  return errorMap[err.keyword] || err.message;
});


---

Summary

Approach	Best For

ajv-errors plugin + errorMessage in schema	Clean, schema-driven error messages
Manual mapping via validate.errors	Full control, dynamic errors, localization



---

Would you like me to show how to display these errors below each field in a React form using something like react-hook-form or Formik?

