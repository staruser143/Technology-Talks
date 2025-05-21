```typescript
// schemas/registrationSchema.ts
export const registrationSchema = {
  type: "object",
  properties: {
    dateOfBirth: {
      type: "string",
      format: "date",
      isAdult: true
    },
    password: {
      type: "string",
      minLength: 6
    },
    confirmPassword: {
      type: "string",
      minLength: 6
    }
  },
  required: ["dateOfBirth", "password", "confirmPassword"],
  additionalProperties: false,
  passwordsMatch: true // Custom cross-field keyword
};
```

```typescript
// utils/ajvInstance.ts
import Ajv from "ajv";
import addFormats from "ajv-formats";

const ajv = new Ajv({ allErrors: true });
addFormats(ajv);

// Custom keyword: isAdult
ajv.addKeyword({
  keyword: "isAdult",
  type: "string",
  validate(schema: boolean, dob: string) {
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

// Custom keyword: passwordsMatch
ajv.addKeyword({
  keyword: "passwordsMatch",
  type: "object",
  validate(schema: boolean, data: any) {
    if (!schema) return true;
    return data.password === data.confirmPassword;
  },
  errors: false
});

export default ajv;
```
