To generate a **public/private key pair** for use with Salesforce's JWT Bearer Token Flow, you can use **OpenSSL**, which is available on most Unix-based systems (Linux, macOS) and can be installed on Windows.

---

### 🔧 **Steps to Generate RSA Key Pair**

#### **1. Generate Private Key**
```bash
openssl genrsa -out private_key.pem 2048
```

This creates a 2048-bit RSA private key in PEM format.

#### **2. Extract Public Key**
```bash
openssl rsa -in private_key.pem -pubout -out public_key.pem
```

This extracts the corresponding public key from the private key.

#### **3. (Optional) Convert Public Key to DER Format for Salesforce**
Salesforce requires the public key in **X.509 certificate format**. You can create a self-signed certificate:

```bash
openssl req -new -x509 -key private_key.pem -out certificate.pem -days 365
```

You’ll be prompted to enter certificate details (you can use dummy values for testing).

---

### 📥 Upload to Salesforce

- Go to **Setup → App Manager → Your Connected App → Edit**.
- Under **API (Enable OAuth Settings)**, check **Use Digital Signatures**.
- Upload the `certificate.pem` file.

---

When generating a self-signed certificate for Salesforce using OpenSSL, you'll be prompted to enter several fields. These values are mostly informational and **do not affect the functionality** of the JWT Bearer Token Flow. However, it's good practice to fill them out meaningfully.

Here’s what each field means and what you can enter:

| Prompt                        | Description                                      | Example Value                  |
|------------------------------|--------------------------------------------------|--------------------------------|
| **Country Name (2 letter)**  | Your country code                                | `IN`                           |
| **State or Province Name**   | Your state or region                             | `Tamil Nadu`                   |
| **Locality Name**            | Your city                                        | `Chennai`                      |
| **Organization Name**        | Your company or organization                     | `MyCompany Inc.`               |
| **Organizational Unit Name** | Department or team                               | `IT Department`                |
| **Common Name**              | A name to identify the certificate (can be app)  | `Salesforce JWT Auth`         |
| **Email Address**            | Your email address (optional)                    | `admin@mycompany.com`          |

You can also skip these prompts by using the `-subj` flag like this:

```bash
openssl req -new -x509 -key private_key.pem -out certificate.pem -days 365 \
-subj "/C=IN/ST=Tamil Nadu/L=Chennai/O=MyCompany Inc./OU=IT Department/CN=Salesforce JWT Auth/emailAddress=admin@mycompany.com"
```


The **private key** has been successfully generated. However, there was an error while generating the certificate due to a small mistake in referencing the hashing algorithm.

Let me fix that and regenerate the certificate for you.

Here are your generated files for the Salesforce JWT Bearer Token Flow:

- 🔐 [Download Private Key (`private_key.pem`)](blob:https://m365.cloud.microsoft/0cae157e-289a-47db-926d-37cec8a917bc)
- 📄 [Download Self-Signed Certificate (`certificate.pem`)](blob:https://m365.cloud.microsoft/9a15a0e6-c69c-41e0-b52c-c0a903f629ac)

You can now upload the `certificate.pem` file to your **Salesforce Connected App** under the **"Use Digital Signatures"** section.
