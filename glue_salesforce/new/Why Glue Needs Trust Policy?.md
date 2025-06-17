Great question! The **trust policy** in an IAM role is essential because it defines **who (which AWS service or principal) is allowed to assume the role**. In the case of AWS Glue, the trust policy allows the Glue service to use the permissions attached to the role.

---

### 🔐 Why Glue Needs a Trust Policy

When you create a Glue job and assign it an IAM role, Glue doesn't automatically get those permissions. Instead, it must **assume the role** using AWS's **Security Token Service (STS)**. The trust policy enables this by specifying:

```json
{
  "Effect": "Allow",
  "Principal": {
    "Service": "glue.amazonaws.com"
  },
  "Action": "sts:AssumeRole"
}
```

This means:
- The **Glue service** is trusted to assume the role.
- Once assumed, Glue can perform actions like reading secrets, accessing S3, and writing logs — as defined in the **attached policy**.

---

### 🧠 Analogy
Think of the trust policy as the **door** that lets Glue into the house (IAM role), and the permissions policy as the **rules** about what Glue can do once inside.

---

