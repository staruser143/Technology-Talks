# Granting permissions to the Azure AD application
In the context of an AKS pod needing to publish and consume messages from an Azure Service Bus topic when using Azure AD Workload Identity.

## Understanding the Goal:
Our objective is to grant the Azure AD application (that our pod will assume the identity of) the necessary permissions to interact with a specific Azure Service Bus topic. This involves assigning Azure RBAC roles that authorize the application to perform send and receive operations on the topic.

### Detailed Steps:
 * **Identify the Target Service Bus Namespace and Topic**:
   * We'll need the name of the Azure Service Bus namespace and the name of the specific topic the pod will interact with.
 * **Determine the Required Azure RBAC Roles**:
   * Azure Service Bus defines several built-in roles. For a pod that needs to both publish and consume messages, we'll typically need to assign the following roles to the Azure AD application on the scope of the specific Service Bus topic:
     * **Azure Service Bus Data Sender**: Allows sending messages to the topic.
     * **Azure Service Bus Data Receiver**: Allows receiving messages from subscriptions to the topic.
   * **Important Scope**: It's best practice to grant these roles at the topic level rather than the namespace level to adhere to the principle of least privilege. Granting at the namespace level would give the application permissions to all queues, topics, and subscriptions within that namespace.
 * **Get the Resource ID of the Service Bus Topic**:
   * We'll need the unique identifier (Resource ID) of the target Service Bus topic to scope the role assignment correctly. We this using the Azure CLI
```
SERVICE_BUS_NAMESPACE="<your-servicebus-namespace-name>"
TOPIC_NAME="<your-topic-name>"
RESOURCE_GROUP="<your-resource-group-name>"

TOPIC_ID=$(az servicebus topic show --namespace-name $SERVICE_BUS_NAMESPACE --name $TOPIC_NAME --resource-group $RESOURCE_GROUP --query "id" -o tsv)

echo "Service Bus Topic Resource ID: $TOPIC_ID"
```
   * Replace the placeholders with your actual Service Bus namespace name, topic name, and resource group name.
 * **Get the Object ID of the Azure AD Application**:
   * We'll need the **Object ID** of the Azure AD application we created earlier (the one we linked to the pod's Kubernetes Service Account via the federated identity credential). We can retrieve this using the Azure CLI, using the **appId (client ID)**:

```
CLIENT_ID="<your-azure-ad-app-client-id>"
OBJECT_ID=$(az ad app show --id $CLIENT_ID --query "objectId" -o tsv)
echo "Azure AD Application Object ID: $OBJECT_ID"
```
   * Replace <your-azure-ad-app-client-id> with the appId you noted when creating the Azure AD application.
 * Assign the Azure RBAC Roles to the Azure AD Application on the Topic:
   * Now, use the az role assignment create command to grant the necessary roles to the Azure AD application, scoped to the specific Service Bus topic. You'll need to run this command twice, once for each role:
     # Assign the "Azure Service Bus Data Sender" role
```
az role assignment create --assignee $OBJECT_ID \
    --role "Azure Service Bus Data Sender" \
    --scope $TOPIC_ID

echo "Assigned 'Azure Service Bus Data Sender' role to the Azure AD application on the topic."
```

# Assign the "Azure Service Bus Data Receiver" role
```
az role assignment create --assignee $OBJECT_ID \
    --role "Azure Service Bus Data Receiver" \
    --scope $TOPIC_ID

echo "Assigned 'Azure Service Bus Data Receiver' role to the Azure AD application on the topic."
```

### Why this approach is better:
 * **Principle of Least Privilege**: By granting permissions specifically at the topic level, we ensure that the pod's identity only has the necessary access to that particular Service Bus resource and not the entire namespace or other resources.
 * **Security:** Limiting the scope of permissions reduces the potential impact if the pod's identity were ever compromised.
 * **Clarity**: It's easier to understand and audit which applications have access to specific Service Bus resources.


In summary, granting permissions to the Azure AD application for Service Bus involves identifying the required roles, scoping the assignment to the specific topic, and using Azure RBAC via the Azure CLI (or other Azure management tools) to link the Azure AD application's object ID to the necessary built-in Service Bus roles on that topic's Resource ID.

Remember that your application code running in the pod will then use the DefaultAzureCredential (or a similar Azure Identity SDK mechanism) to authenticate to Azure Service Bus. 
The SDK will automatically discover the token associated with the pod's Azure AD identity (thanks to Azure AD Workload Identity and the federated identity credential) and use it to authorize the publish and consume operations. We won't need to manage any connection strings or secrets within your pod.
