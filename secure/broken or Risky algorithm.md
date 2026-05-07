## Use of Broken or Risky Cryptographic Algorithm

This vulnerability (CWE-327) occurs when software uses cryptographic algorithms that are outdated, weak, or known to be insecure — making encrypted data susceptible to attacks.

---

### Why It's Dangerous

Weak cryptography can be broken by attackers to:
- Decrypt sensitive data (passwords, PII, financial info)
- Forge digital signatures or certificates
- Perform man-in-the-middle attacks
- Bypass authentication mechanisms

---

### Common Broken/Risky Algorithms

| Category | Broken/Risky | Secure Alternative |
|---|---|---|
| **Hashing** | MD5, SHA-1 | SHA-256, SHA-3 |
| **Symmetric Encryption** | DES, 3DES, RC4, Blowfish | AES-256-GCM, ChaCha20-Poly1305 |
| **Asymmetric Encryption** | RSA-1024, DSA-1024 | RSA-2048+, ECC (P-256/P-384) |
| **Key Exchange** | Diffie-Hellman <2048-bit | ECDH, X25519 |
| **Password Hashing** | MD5/SHA for passwords | bcrypt, Argon2, scrypt, PBKDF2 |
| **TLS/SSL** | SSLv2, SSLv3, TLS 1.0, TLS 1.1 | TLS 1.2 (minimum), TLS 1.3 |

---

### Vulnerable Code Examples & Fixes

**❌ Vulnerable — MD5 for password hashing (Python)**
```python
import hashlib
hashed = hashlib.md5(password.encode()).hexdigest()
```
**✅ Secure — Use bcrypt**
```python
import bcrypt
hashed = bcrypt.hashpw(password.encode(), bcrypt.gensalt(rounds=12))
```

---

**❌ Vulnerable — DES encryption (Java)**
```java
Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
```
**✅ Secure — Use AES-GCM**
```java
Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
```

---

**❌ Vulnerable — Weak RSA key (JavaScript/Node)**
```javascript
crypto.generateKeyPair('rsa', { modulusLength: 512 });
```
**✅ Secure — Use 2048-bit minimum**
```javascript
crypto.generateKeyPair('rsa', { modulusLength: 2048 });
```

---

### Remediation Checklist

**1. Audit & Inventory**
- Identify all cryptographic operations in your codebase
- Flag any use of MD5, SHA-1, DES, RC4, or SSL/TLS < 1.2

**2. Replace Weak Algorithms**
- Use vetted libraries (OpenSSL, libsodium, Bouncy Castle)
- Never implement custom/home-grown cryptography

**3. Key Management**
- Use sufficiently long key sizes (AES-256, RSA-2048+)
- Rotate keys regularly and store them in a secrets manager (e.g., AWS KMS, HashiCorp Vault)

**4. Enforce Secure TLS**
- Disable SSLv2/v3, TLS 1.0, TLS 1.1 on all servers
- Enforce TLS 1.3 where possible

**5. Use Authenticated Encryption**
- Prefer AEAD modes like **AES-GCM** or **ChaCha20-Poly1305** over CBC/ECB which lack integrity checks

**6. Static Analysis**
- Integrate SAST tools (Semgrep, Checkmarx, SonarQube) to detect weak crypto in CI/CD pipelines

---

### Relevant Standards & References
- **OWASP**: A02:2021 – Cryptographic Failures
- **CWE**: CWE-327 (Broken Algorithm), CWE-328 (Weak Hash), CWE-326 (Inadequate Key Strength)
- **NIST**: SP 800-131A – Transitioning cryptographic algorithms and key lengths