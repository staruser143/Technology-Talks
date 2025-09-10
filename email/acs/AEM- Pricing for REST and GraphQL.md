Adobe Experience Manager (AEM) **does not charge separately** for using its **REST or GraphQL APIs**. These APIs are included as part of the overall **AEM as a Cloud Service** subscription. However, here are some important details to consider:

---

## ✅ **API Access in AEM**

### 🔗 **GraphQL API**
- Used for **headless delivery** of structured content via Content Fragments.
- Requires you to **enable GraphQL endpoints** in AEM [1](https://experienceleague.adobe.com/en/docs/experience-manager-cloud-service/content/headless/setup/create-api-request).
- Includes features like:
  - Schema auto-generation from Content Fragment Models
  - Support for persisted queries (for caching via CDN/Dispatcher)
  - GraphiQL IDE for query testing and debugging [2](https://experienceleague.adobe.com/en/docs/experience-manager-cloud-service/content/headless/graphql-api/content-fragments)

### 🔗 **REST API**
- Used for **CRUD operations** on assets and content fragments.
- Includes endpoints for managing templates, assets, and metadata.

Both APIs are part of the **core AEM Sites capabilities** and are **not priced separately** [3](https://business.adobe.com/products/experience-manager/sites/pricing.html).

---

## 💰 **Pricing Model Overview**

- AEM pricing is **customized** based on:
  - Number of environments (author, publish, preview)
  - Volume of content and traffic
  - Add-ons (e.g., AEM Forms, Screens, Guides)
  - SLA and compliance requirements (e.g., HIPAA, FedRAMP)

- APIs (GraphQL, REST) are included in the **base license** for AEM Sites.
- You may incur **additional costs** for:
  - CDN usage (if not using Adobe’s default)
  - Custom integrations or Adobe I/O runtime usage
  - Enhanced security or compliance packages

---

## 🔐 **Security and Compliance**
- AEM APIs are governed by the same security model:
  - Role-based access control
  - OAuth2 and token-based authentication
  - ISO-27001, SOC-2, HIPAA-ready configurations [3](https://business.adobe.com/products/experience-manager/sites/pricing.html)

---

### ✅ Summary

| Feature | AEM REST & GraphQL APIs |
|--------|--------------------------|
| **Included in Base License** | ✅ Yes |
| **Separate Pricing** | ❌ No |
| **Authentication** | OAuth2, token-based |
| **Security** | Enterprise-grade |
| **Customization** | Supported via endpoints and persisted queries |

---
