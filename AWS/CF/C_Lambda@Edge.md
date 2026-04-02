This is a **classic AWS exam / architecture question**, and the key phrases that drive the answer are:

*   **Least operational effort**
*   **Simple redirects only**
*   **Multiple domains**
*   **Redirect mapping defined in a JSON document**
*   **Must support HTTP and HTTPS**
*   **DNS already in Route 53**

***

## ✅ Best-Fit Solution (Lowest Ops)

### **Amazon CloudFront + Lambda\@Edge (Viewer Request) + Route 53**

This solution is **serverless, highly scalable, HTTPS-native**, and avoids managing EC2, containers, or ALBs.

***

## ✅ High‑Level Architecture

    Route 53 (10 domains)
            ↓ (Alias)
    CloudFront Distribution
            ↓
    Lambda@Edge (Viewer Request)
            ↓
    HTTP 301/302 Redirect

***

## ✅ Why This Is the Correct Answer

| Requirement          | How It’s Met                        |
| -------------------- | ----------------------------------- |
| Redirect per domain  | Lambda\@Edge inspects `Host` header |
| JSON‑based mapping   | JSON embedded or fetched from S3    |
| HTTP + HTTPS         | CloudFront terminates SSL           |
| Low ops              | No servers, no scaling, no patching |
| Route 53 managed DNS | Route 53 aliases to CloudFront      |

***

## ✅ Detailed Steps (What the exam expects)

### **1. Create a CloudFront Distribution**

*   Use **any origin** (can be S3 or dummy since Lambda returns response)
*   Enable **Viewer Protocol Policy: Redirect HTTP to HTTPS**
*   Attach **ACM certificate** covering all 10 domains
    *   Wildcard or SAN certificate

***

### **2. Implement Lambda\@Edge (Viewer Request)**

*   Trigger: **Viewer Request**
*   Logic:
    *   Read `event.request.headers.host`
    *   Look up target URL from JSON map
    *   Return **301/302 redirect response**

#### Example JSON mapping

```json
{
  "example1.com": "https://www.company.com/page1",
  "example2.com": "https://campaign.company.com"
}
```

#### Lambda response (simplified)

```json
{
  "status": "301",
  "statusDescription": "Moved Permanently",
  "headers": {
    "location": [
      { "key": "Location", "value": "https://target-url" }
    ]
  }
}
```

✅ No origin hit  
✅ No backend infra

***

### **3. (Optional) Store JSON in S3**

*   If mappings change often:
    *   Store JSON in S3
    *   Cache in Lambda\@Edge memory
*   Still very low operational overhead

***

### **4. Configure Route 53**

*   For each domain:
    *   Create **A/AAAA Alias record**
    *   Point to the CloudFront distribution

✅ No IP management  
✅ Fully managed DNS

***

## 🚫 Why Other Options Are Worse

### ❌ Application Load Balancer

*   Needs VPC
*   Needs listeners & rules
*   Overkill for simple redirects

### ❌ EC2 / ECS

*   Servers to patch and scale
*   Costly and unnecessary

### ❌ S3 Website Redirect Alone

*   **Does NOT support HTTPS**
*   Requires CloudFront anyway → Lambda\@Edge still needed

***

## ✅ Final Exam‑Style Answer

> **Use Amazon CloudFront with a Lambda\@Edge function on viewer requests to perform HTTP 301 redirects based on domain-to-URL mappings defined in a JSON document. Configure Route 53 alias records to point all domains to the CloudFront distribution and use ACM for HTTPS support.**

***

## 🧠 Exam Tip (Why this always wins)

When you see:

*   *multiple domains*
*   *redirect logic*
*   *HTTPS*
*   *JSON mapping*
*   *least operational effort*

👉 **CloudFront + Lambda\@Edge** is almost always the intended answer.


