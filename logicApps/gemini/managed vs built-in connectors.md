In Azure Logic Apps, both Managed Connectors and Built-in Connectors serve the purpose of integrating your workflows with external services, applications, and systems. However, they differ significantly in how they are hosted, their performance characteristics, cost implications, and the Logic App plans they are most commonly associated with.
Here's a detailed breakdown of the differences:
Managed Connectors
 * Hosting:
   * External and Shared: Managed connectors are deployed, hosted, and managed by Microsoft in a shared, multi-tenant infrastructure within Azure. They essentially act as a proxy or wrapper around the API of the underlying service (e.g., Office 365, Salesforce, SharePoint).
   * When your Logic App workflow uses a managed connector, it makes an API call to this external connector service hosted by Microsoft, which then communicates with the target service.
 * Performance:
   * Network Latency: Since they are hosted externally, using managed connectors involves network hops and external API calls. This can introduce some latency into your workflow execution.
 * Cost:
   * Pay-per-execution: For Consumption Logic Apps, managed connector actions are typically billed per execution. This means you pay for each time your workflow calls an action from a managed connector. For Standard Logic Apps, while the compute (App Service Plan) is a fixed cost, calls to managed connectors still incur a separate usage charge.
 * Availability:
   * Extensive Catalog: There's a vast and constantly growing catalog of managed connectors for hundreds of services (SaaS, PaaS, on-premises systems via data gateway).
 * Usage:
   * Consumption and Standard: Managed connectors are available in both Consumption and Standard Logic Apps.
 * Authentication:
   * Usually requires you to create a "connection" resource in Azure, which stores authentication details (e.g., API keys, OAuth tokens) for the target service.
 * Examples: Office 365 Outlook, Salesforce, Twitter, Dropbox, SQL Server (when connecting externally), Azure Key Vault, Azure Blob Storage (when using the managed version).
Built-in Connectors
 * Hosting:
   * In-Process (Native): Built-in connectors run directly and natively within the Azure Logic Apps runtime itself. For Standard Logic Apps, this means they operate within the same App Service Plan and process as your workflow.
   * They are "built into" the Logic Apps engine rather than being external services.
 * Performance:
   * Faster and Lower Latency: Because they run in-process, built-in connectors eliminate external network hops and API calls to a separate connector service. This results in significantly lower latency and higher performance.
 * Cost:
   * Included in Compute (Standard): For Standard Logic Apps, operations performed by built-in connectors are generally included in the cost of your App Service Plan. You don't pay per-action execution fees for built-in connector operations (though you might still pay for the underlying service calls if they have their own billing, e.g., Azure Service Bus). This can make them more cost-effective for high-frequency operations.
   * Limited availability in Consumption: Consumption Logic Apps have a smaller set of built-in connectors, primarily for core operations like HTTP, Request, Schedule, and some Azure-specific services.
 * Availability:
   * Core Operations & Specific Services: The built-in connector set includes fundamental operations (e.g., HTTP, Request, Schedule, XML transformations) and built-in versions for common Azure services (e.g., Azure Service Bus, Azure Event Hubs, SQL Server, FTP, SFTP) in Standard Logic Apps.
 * Usage:
   * Predominantly Standard (especially the service-specific ones): While some basic built-in connectors (like HTTP or Recurrence) are available in Consumption, the powerful, service-specific built-in connectors (e.g., for Service Bus, SQL Server) are a hallmark of the Standard plan.
 * Authentication:
   * Authentication details (connection strings, credentials) are often stored directly within the Logic App's configuration or app settings, or in local.settings.json for local development.
 * Examples:
   * General: Recurrence (Scheduler), Request (HTTP listener), HTTP (generic HTTP calls), XML operations (Compose, Transform XML, Validate XML), Data Operations (Compose, Join, Select, etc.).
   * Standard specific (often called "Service Providers"): Azure Service Bus, Azure Event Hubs, SQL Server (in-process connectivity), FTP, SFTP, MQ.
Summary of Key Differences:
| Feature | Managed Connectors | Built-in Connectors |
|---|---|---|
| Hosting | External, Microsoft-managed (shared multi-tenant) | In-process, runs directly within Logic App runtime (especially for Standard) |
| Performance | Can introduce network latency | Faster, lower latency (no external API calls for the connector operation itself) |
| Cost | Billed per action execution (Consumption & Standard) | Generally included in App Service Plan cost (Standard); limited per-action in Consumption |
| Catalog | Vast and growing (hundreds of services) | Core operations + optimized versions for key Azure/on-premises services (Standard) |
| Logic App Type | Available in both Consumption and Standard | Basic ones in Consumption; comprehensive, service-specific ones in Standard |
| Network | Requires internet access to reach the managed connector | Can run in isolated networks (VNet integration, ASEv3) without public internet access for the connector operation |
| Customization | Limited to connector configuration | For Standard, can extend with custom built-in connectors (based on Azure Functions) |
When to Use Which:
 * Managed Connectors are your go-to for integrating with a wide variety of third-party SaaS applications (Salesforce, ServiceNow, etc.), Microsoft 365 services (SharePoint, Outlook), and other Azure services where a built-in equivalent isn't available or doesn't meet your needs. They offer simplicity and a quick way to connect.
 * Built-in Connectors are preferred for:
   * Performance-sensitive scenarios: When low latency and high throughput are critical, especially for frequently executed actions.
   * Cost optimization at scale: For high-volume workloads in Standard Logic Apps, using built-in connectors can significantly reduce overall costs by avoiding per-action billing.
   * Network isolation requirements: When connecting to resources in private networks (VNet, on-premises) without exposing them to the public internet for the connector operation.
   * Core workflow control: For operations like scheduling, HTTP requests, and data manipulation that are fundamental to almost any workflow.
In essence, Built-in connectors (especially in Standard Logic Apps) represent a more performant, often more cost-effective, and more deeply integrated way to connect to core Azure services and handle fundamental workflow operations, leveraging the dedicated nature of the Standard plan. Managed connectors, on the other hand, provide unparalleled breadth of connectivity to a vast ecosystem of cloud and on-premises applications, acting as a convenient proxy service.
