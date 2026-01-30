In this architecture, **Bouncy Castle** acts as the engine that creates the "seal," while `saveIncremental` is the method used to apply that seal without breaking the document's structure.

---

### 1. The Role of Bouncy Castle: The "Black Box" of Crypto

While **PDFBox** understands the structure of a PDF (pages, fonts, images), it does not natively know how to perform complex cryptography. **Bouncy Castle** fills this gap.

When you sign a document, you aren't just "pasting an image." You are performing a mathematical operation. Bouncy Castle handles:

* **Generating the CMS/PKCS#7 Container:** This is a digital "envelope" that contains your certificate, the document hash, and the timestamp.
* **Asymmetric Encryption:** It uses your **Private Key** (which only your service knows) to encrypt the document's SHA-256 hash.
* **Certificate Handling:** It ensures the "Public Key" (which anyone can use to verify the signature) is correctly attached so a PDF viewer (like Adobe) knows the signature is trusted.

**The Workflow:** PDFBox says, *"Here is the hash of the document,"* and Bouncy Castle responds, *"Here is the encrypted, legally-compliant digital signature block for that hash."*

---

### 2. What is `saveIncremental`?

In a standard file save, a program often rewrites the entire file from scratch. **This is fatal for digital signatures.** If you change a single byte of a signed PDF, the signature becomes "Invalid." To solve this, PDFBox uses **Incremental Saving**.

#### How it works:

Instead of rewriting the file, `saveIncremental` appends the signature and the metadata to the **end of the file**. The original bytes of the document remain completely untouched.

**Why this is critical for your custom control:**

1. **Tamper Evidence:** If someone tries to edit the text after it's signed, they would have to add *another* incremental update. PDF viewers will see this and flag: *"Document has been modified since signature was applied."*
2. **Multi-Signer Support:** It allows Person A to sign, then Person B to sign. Each signature is simply "appended" to the end without disturbing the previous one.
3. **Efficiency:** Since your existing service uses **OpenHTMLtoPDF**, you generate the "base" document once, and then use `saveIncremental` to quickly snap on the signature layer without re-rendering the HTML.

---

### 3. Putting it together in your Pipeline

Since you already have a generation service, your Java logic will look like this:

1. **Generate:** OpenHTMLtoPDF creates `Draft.pdf`.
2. **Prepare:** PDFBox opens `Draft.pdf` and calculates the "Byte Range" (everything except the space where the signature will go).
3. **Sign (Bouncy Castle):** You pass that byte range to Bouncy Castle to create the cryptographic block.
4. **Seal (`saveIncremental`):** PDFBox appends that block to the end of the file and saves the final `Signed.pdf`.

### Potential Risk: Private Key Management

To avoid vendor lock-in, you will likely store your signing certificates in a **Java KeyStore (JKS)** or a **Cloud KMS** (like AWS KMS or Azure Key Vault). Bouncy Castle is the bridge that allows your Java code to talk to these keys.

**Would you like to see a sample `SignatureInterface` implementation showing exactly where Bouncy Castle is called inside a PDFBox workflow?**
