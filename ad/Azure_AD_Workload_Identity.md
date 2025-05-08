# Using Microsoft Entra Workload ID for Secure Authentication

To provide access to an **AKS pod** for Azure Services we can use **Microsoft Entra Workload ID** for secure authentication. Here’s a detailed step-by-step flow:

---

### **1. Enable Workload Identity Federation in AKS**
First, ensure the  AKS cluster supports **OIDC federation**:
```sh
az aks update --resource-group myResourceGroup --name myAKSCluster --enable-workload-identity --enable-oidc-issuer
```
This enables **OIDC issuer** for AKS, allowing service accounts to authenticate with Microsoft Entra ID.
---
## We can fetch the OIDCIssuerUrl as below
```sh
az aks show --name myAKSCluster --resource-group myResourceGroup --query "oidcIssuerProfile.issuerUrl" -o tsv
```
### **2. Create a Microsoft Entra Workload ID**
Register an **application** in Microsoft Entra ID and configure it for workload identity federation:
```sh
az ad app create --display-name "AKS Workload Identity"
```
## Create  a Service Account
```sh
apiVersion: v1
kind: ServiceAccount
metadata:
  name: my-service-account
  annotations:
    azure.workload.identity/client-id: "<app-id>"
```

## Then, create a **federated credential** for the  AKS service account:
A federated credential for an AKS service account allows Kubernetes workloads to authenticate with Microsoft Entra ID using OIDC federation, eliminating the need for managing secrets manually.

```sh
az ad app federated-credential create --id <app-id> --issuer <oidc-issuer-url> --subject "system:serviceaccount:default:my-service-account"
```

---

### **3. Assign Permissions to Azure Blob Storage**
Grant the **Storage Blob Data Contributor** role to the workload identity:
```sh
az role assignment create --assignee <app-id> --role "Storage Blob Data Contributor" --scope /subscriptions/<subscription-id>/resourceGroups/<resource-group>/providers/Microsoft.Storage/storageAccounts/<storage-account>
```

---

### **4. Assign Permissions to Azure Event Hub**
Grant the **Azure Event Hubs Data Sender** role:
```sh
az role assignment create --assignee <app-id> --role "Azure Event Hubs Data Sender" --scope /subscriptions/<subscription-id>/resourceGroups/<resource-group>/providers/Microsoft.EventHub/namespaces/<eventhub-namespace>
```

---

### **5. Configure Kubernetes Service Account**
Annotate the **service account** with the workload identity details:
```yaml
apiVersion: v1
kind: ServiceAccount
metadata:
  name: my-service-account
  annotations:
    azure.workload.identity/client-id: "<app-id>"
```

---

### **6. Deploy a Pod with Access to Azure Services**
Now, deploy a pod that uses the workload identity to access **Blob Storage** and **Event Hub**:
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: my-app
spec:
  replicas: 1
  selector:
    matchLabels:
      app: my-app
  template:
    metadata:
      labels:
        app: my-app
    spec:
      serviceAccountName: my-service-account  # Only reference the Service Account
      containers:
      - name: my-container
        image: my-app-image

```
## How Authentication Works Inside the Pod
* The pod runs with the Service Account, which is linked to Microsoft Entra ID.
* Kubernetes automatically mounts the OIDC token inside the pod.
* The application can use Azure SDKs to authenticate without needing to manually specify the app-id.

---

### **7. Use Azure SDKs in the Application**
Inside the application, use **Azure SDKs** to authenticate and interact with Azure services:

#### **Blob Storage Access (Python Example)**
```python
from azure.identity import DefaultAzureCredential
from azure.storage.blob import BlobServiceClient

credential = DefaultAzureCredential()
blob_service_client = BlobServiceClient(account_url="https://<storage-account>.blob.core.windows.net", credential=credential)
```

#### **Event Hub Access (Python Example)**
```python
from azure.identity import DefaultAzureCredential
from azure.eventhub import EventHubProducerClient

credential = DefaultAzureCredential()
producer = EventHubProducerClient(fully_qualified_namespace="<eventhub-namespace>.servicebus.windows.net", eventhub_name="<eventhub-name>", credential=credential)
```

## References
* https://learn.microsoft.com/en-us/azure/aks/workload-identity-overview?tabs=javascript
* https://learn.microsoft.com/en-us/azure/aks/workload-identity-deploy-cluster

---
