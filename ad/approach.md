# Using Azure AD Workload Identity to enable a service running in an AKS pod to access Azure Blob Storage.
## Scenario: 
You have an application running in an AKS pod that needs to read data from a specific Azure Blob Storage container. We'll use Azure AD Workload Identity to grant this pod the necessary permissions without managing any secrets within the pod.

## Prerequisites:
 * AKS Cluster: You need an existing AKS cluster with Azure AD Workload Identity enabled. If you don't have one, you can create one or enable it on an existing cluster using the Azure CLI.
# Create a new AKS cluster with workload identity enabled
```shell script
az aks create -g <resource-group-name> -n <cluster-name> --enable-workload-identity --node-count 1
```
# Enable workload identity on an existing AKS cluster
```
az aks update -g <resource-group-name> -n <cluster-name> --enable-workload-identity
```
 * Azure Container Registry (ACR): You'll need an ACR to store your application's container image.
 * Azure Blob Storage Account and Container: You need an Azure Storage Account and a Blob container within it that your application will access.
 * Azure CLI: Ensure you have the Azure CLI installed and configured.
 * kubectl: Ensure you have kubectl installed and configured to connect to your AKS cluster.

   
## Detailed Steps:
Step 1: Create an Azure AD Application
We need an Azure AD application that will represent the identity of our pod.
# Replace with your desired application name

```
APPLICATION_NAME="my-aks-app-identity"
```
# Create the Azure AD application
```
az ad app create --display-name $APPLICATION_NAME
```
Take note of the appId (also known as the client ID) from the output. We'll need this later. Let's store it in a variable:
```
CLIENT_ID="<your-app-id>"
```

Step 2: Create a Federated Identity Credential
Now, we'll create a federated identity credential between our Azure AD application and a Kubernetes service account in our AKS cluster. This establishes the trust relationship.
 * Namespace: Choose the Kubernetes namespace where your pod will run (e.g., default).
 * Service Account Name: Choose a name for the Kubernetes service account that your pod will use (e.g., my-app-sa).
First, create the Kubernetes service account if it doesn't exist:
```
NAMESPACE="default"
SERVICE_ACCOUNT_NAME="my-app-sa"
```
```
kubectl create serviceaccount $SERVICE_ACCOUNT_NAME -n $NAMESPACE
```
Now, create the federated identity credential. Replace the placeholders with your actual values:
```
AKS_RESOURCE_GROUP="<your-aks-resource-group>"
AKS_CLUSTER_NAME="<your-aks-cluster-name>"
SUBSCRIPTION_ID="<your-azure-subscription-id>"
```

```
az identity federated-credential create --name "aks-workload-identity" \
    --identity-id $CLIENT_ID \
    --issuer "https://oidc.prod-eastus2.workload.azure.com/$SUBSCRIPTION_ID/oidc" \
    --subject system:serviceaccount:$NAMESPACE:$SERVICE_ACCOUNT_NAME \
    --audience api://AzureADTokenExchange
```
Explanation of Parameters:
 * --name: A name for your federated identity credential.
 * --identity-id: The appId (client ID) of the Azure AD application we created.
 * --issuer: The OIDC issuer URL for Azure AD Workload Identity in your subscription's region. This specific URL (https://oidc.prod-eastus2.workload.azure.com/$SUBSCRIPTION_ID/oidc) is a general endpoint. AKS handles the specific tenant discovery.
 * --subject: This specifies the Kubernetes service account that will be allowed to use this Azure AD application. The format is system:serviceaccount:<namespace>:<serviceaccount-name>.
 * --audience: This should always be api://AzureADTokenExchange for Azure AD Workload Identity.
Step 3: Grant Permissions to the Azure AD Application
Now, we need to grant the Azure AD application the necessary permissions to access the Azure Blob Storage. We'll assign the "Storage Blob Data Reader" role to the application on the specific Blob container.
STORAGE_ACCOUNT_NAME="<your-storage-account-name>"
CONTAINER_NAME="<your-container-name>"

```
# Get the resource ID of the Blob container
```
CONTAINER_ID=$(az storage container show --account-name $STORAGE_ACCOUNT_NAME --name $CONTAINER_NAME --query "id" -o tsv)
```
# Get the object ID of the Azure AD application
```
OBJECT_ID=$(az ad app show --id $CLIENT_ID --query "objectId" -o tsv)
```
# Assign the "Storage Blob Data Reader" role to the Azure AD application on the container
```
az role assignment create --assignee $OBJECT_ID \
    --role "Storage Blob Data Reader" \
    --scope $CONTAINER_ID
```
Step 4: Deploy Your Application to AKS
Now, let's define a Kubernetes deployment for your application. The key here is to associate the pod with the Kubernetes service account we created earlier.
```
apiVersion: apps/v1
kind: Deployment
metadata:
  name: my-app-deployment
  namespace: default
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
      serviceAccountName: my-app-sa # Associate the pod with the service account
      containers:
      - name: my-app-container
        image: <your-acr-name>.azurecr.io/<your-app-image>:<tag>
        # Ensure your application uses an Azure SDK that supports Azure AD authentication
        # (e.g., Azure.Storage.Blobs for .NET, @azure/storage-blob for Node.js, etc.)
        env:
        - name: STORAGE_ACCOUNT_NAME
          value: "<your-storage-account-name>"
        # Your application code will use the managed identity to authenticate
        # It should not require any connection strings or secrets here
```
Replace the placeholders in the image and env sections with your actual values.
Apply this deployment to your AKS cluster:
```
kubectl apply -f your-deployment.yaml
```
Step 5: Application Code
Your application code running in the pod needs to use an Azure SDK that supports Azure AD authentication. The SDK will automatically discover the necessary credentials provided by Azure AD Workload Identity and use them to authenticate to Azure Blob Storage.
Here's a conceptual example in Python using the azure-storage-blob library:
```python
from azure.identity import DefaultAzureCredential
from azure.storage.blob import BlobServiceClient

storage_account_name = os.environ.get("STORAGE_ACCOUNT_NAME")
container_name = "<your-container-name>"

# Use DefaultAzureCredential, which automatically handles Workload Identity
credential = DefaultAzureCredential()

blob_service_client = BlobServiceClient(f"https://{storage_account_name}.blob.core.windows.net", credential=credential)
container_client = blob_service_client.get_container_client(container_name)

try:
    blob_list = container_client.list_blobs()
    print("Blobs in the container:")
    for blob in blob_list:
        print(f"- {blob.name}")
except Exception as ex:
    print(f"Error accessing Blob Storage: {ex}")
```

In this code:
 * We import DefaultAzureCredential from the azure.identity library. This credential type automatically handles authentication using various Azure identity providers, including Azure AD Workload Identity when running in an AKS pod with it configured.
 * We don't need to provide any explicit credentials (like connection strings or access keys). DefaultAzureCredential handles the token acquisition behind the scenes.
Verification:
 * Once your pod is running, check its logs to see if it can successfully connect to Azure Blob Storage and list the blobs in the container.
 * If you encounter issues, double-check the following:
   * The Azure AD application ID (CLIENT_ID) is correct.
   * The federated identity credential is configured correctly with the correct issuer, subject (namespace and service account), and audience.
   * The Azure AD application has the necessary RBAC role assigned to the correct Blob Storage resource.
   * Your pod's serviceAccountName is correctly set in the deployment.
   * Your application code is using an Azure SDK that supports Azure AD authentication and DefaultAzureCredential (or a similar mechanism).
By following these steps, you've successfully enabled your application running in an AKS pod to access Azure Blob Storage securely using Azure AD Workload Identity, without the need to manage any secrets within your Kubernetes environment. This significantly enhances the security posture of your AKS applications.
