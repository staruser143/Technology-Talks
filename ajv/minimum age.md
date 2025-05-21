```typescript
// schemas/registrationSchema.ts
export const registrationSchema = {
  type: "object",
  properties: {
    dateOfBirth: {
      type: "string",
      format: "date",
      minAge: 18 // <-- declarative min age
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
  passwordsMatch: true
};
```

```typescript

// utils/ajvInstance.ts
import Ajv from "ajv";
import addFormats from "ajv-formats";

const ajv = new Ajv({ allErrors: true });
addFormats(ajv);

// Custom keyword: minAge
ajv.addKeyword({
  keyword: "minAge",
  type: "string",
  schemaType: "number",
  validate(minAge: number, dob: string) {
    if (!dob) return false;
    const birthDate = new Date(dob);
    const today = new Date();
    const ageDiff = today.getFullYear() - birthDate.getFullYear();
    const m = today.getMonth() - birthDate.getMonth();
    const age =
      m < 0 || (m === 0 && today.getDate() < birthDate.getDate())
        ? ageDiff - 1
        : ageDiff;
    return age >= minAge;
  },
  errors: false
});

// passwordsMatch remains the same
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
