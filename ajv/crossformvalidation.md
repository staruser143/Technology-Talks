Cross-form validation — where you validate fields that exist in multiple forms or steps — is definitely possible, but it depends on how you're managing your form state.

Here's how to handle cross-form validation using @rjsf, React, and custom logic:


---

Scenario: Multi-step form or separate forms

If your application splits form fields across steps or tabs (e.g., Form Step 1: personal info, Step 2: credentials), and you want to validate between them (like email in Step 1 matches confirmEmail in Step 2), you need a shared state and a central validation function.


---

Approach: Centralized Validation Function

1. Store all form data in React state.


2. At the end of each step or on submit, run a custom validation that uses the entire form state (all steps).



Example:

const [formStep1Data, setFormStep1Data] = useState({});
const [formStep2Data, setFormStep2Data] = useState({});

Then:

const allData = {
  ...formStep1Data,
  ...formStep2Data,
};

const validateCrossForm = (allData: any) => {
  const errors: Record<string, string> = {};

  if (allData.email !== allData.confirmEmail) {
    errors.confirmEmail = "Email addresses must match.";
  }

  if (allData.agreeToTerms !== true && allData.paymentConfirmed) {
    errors.agreeToTerms = "You must agree to terms before payment.";
  }

  return errors;
};

Show these errors wherever appropriate in the UI — e.g., before submission or on the final step.


---

If using @rjsf forms per step

You can still use the validate prop in the final step (or all steps) to check the merged form data:

<Form
  schema={step2Schema}
  formData={formStep2Data}
  validate={(formData, errors) => {
    const allData = { ...formStep1Data, ...formData };

    if (allData.email !== allData.confirmEmail) {
      errors.confirmEmail?.addError("Email addresses must match.");
    }

    return errors;
  }}
/>


---

Best Practices

Centralize form data in a shared context or state.

Use a custom validation function that knows about all the steps.

You can run this validation on:

Every step (for partial checks)

Final submit (for full validation)


If you’re using a multi-step wizard, consider managing navigation using a library like formik-stepper or custom logic.



---

Would you like a working multi-step React + @rjsf example that includes cross-step validation?

