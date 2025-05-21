To generate a UI form dynamically from a JSON Schema including validations in a React + TypeScript app, you can use libraries that:

1. Render form components based on JSON Schema.


2. Automatically handle validations (AJV under the hood).


3. Support custom formats, widgets, and error messages.




---

Recommended Library: @rjsf/core (React JSONSchema Form)

Features

Renders dynamic forms from JSON Schema + optional UI Schema.

Uses AJV for validation (including custom keywords and error messages).

Supports custom widgets, themes (Material-UI, Bootstrap), field ordering.



---

Step-by-Step Setup

1. Install

npm install @rjsf/core ajv ajv-formats ajv-errors

If you want styling with Material UI:

npm install @rjsf/material-ui @mui/material @emotion/react @emotion/styled


---

2. Basic Example with Schema and Validation

import Form from "@rjsf/material-ui";
import { JSONSchema7 } from "json-schema";

const schema: JSONSchema7 = {
  type: "object",
  properties: {
    dateOfBirth: {
      type: "string",
      format: "date",
      title: "Date of Birth",
      minAge: 18
    },
    password: { type: "string", title: "Password", minLength: 6 },
    confirmPassword: { type: "string", title: "Confirm Password", minLength: 6 }
  },
  required: ["dateOfBirth", "password", "confirmPassword"],
  passwordsMatch: true
};

const uiSchema = {
  password: { "ui:widget": "password" },
  confirmPassword: { "ui:widget": "password" }
};

export default function DynamicForm() {
  const handleSubmit = ({ formData }: any) => {
    console.log("Submitted:", formData);
  };

  return <Form schema={schema} uiSchema={uiSchema} onSubmit={handleSubmit} />;
}


---

3. Add Custom Validation (like minAge, passwordsMatch)

Create a customValidate function:

const customValidate = (formData: any, errors: any) => {
  const birthDate = new Date(formData.dateOfBirth);
  const age = new Date().getFullYear() - birthDate.getFullYear();
  if (age < 18) {
    errors.dateOfBirth.addError("You must be at least 18 years old.");
  }

  if (formData.password !== formData.confirmPassword) {
    errors.confirmPassword.addError("Passwords must match.");
  }

  return errors;
};

Then plug it into your form:

<Form
  schema={schema}
  uiSchema={uiSchema}
  onSubmit={handleSubmit}
  validate={customValidate}
/>


---

Bonus: External JSON Schema File

You can load your schema from a .json file:

import registrationSchema from "../schemas/registrationSchema.json";

And pass it to the form:

<Form schema={registrationSchema} />


---

Summary

Tool	Purpose

@rjsf/core	Auto-generates React forms from JSON Schema
uiSchema	Controls widget rendering and layout
validate prop	Adds custom cross-field or computed validation logic
JSON Schema	Defines field types, required fields, and validations



---

Would you like me to generate a working StackBlitz/CodeSandbox example for you to experiment with?

