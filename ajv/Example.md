```sh
npm install ajv
npm install --save-dev @types/ajv
```

```
// schema.ts
export const dobSchema = {
  type: "object",
  properties: {
    dateOfBirth: {
      type: "string",
      format: "date",
      isAdult: true // <-- custom keyword
    }
  },
  required: ["dateOfBirth"],
  additionalProperties: false
};
```

```
// validator.ts
import Ajv, { JSONSchemaType } from "ajv";
import addFormats from "ajv-formats";

const ajv = new Ajv();
addFormats(ajv); // Adds support for "date", "email", etc.

// Custom keyword to validate if DOB is at least 18 years ago
ajv.addKeyword({
  keyword: "isAdult",
  type: "string",
  validate: function (schema: boolean, dob: string) {
    if (!schema) return true;

    const dobDate = new Date(dob);
    const today = new Date();
    const eighteenYearsAgo = new Date(
      today.getFullYear() - 18,
      today.getMonth(),
      today.getDate()
    );

    return dobDate <= eighteenYearsAgo;
  },
  errors: false
});

export default ajv;
```

```
// index.ts
import ajv from "./validator";
import { dobSchema } from "./schema";

const validate = ajv.compile(dobSchema);

const data = {
  dateOfBirth: "2008-05-21"
};

if (validate(data)) {
  console.log("Valid: User is at least 18.");
} else {
  console.log("Invalid:", validate.errors);

```
}
