Azure Event Grid provides two access keys—**primary** and **secondary**—for each topic or domain. These keys are used to authenticate and authorize clients to publish events to the Event Grid resource.

### Usage of Primary and Secondary Keys:

1. **Authentication**:
   - **Purpose**: Both keys are used to authenticate clients that publish events to the Event Grid topic or domain.
   - **Scenario**: When a client needs to send events, it includes one of these keys in the request header to authenticate the request[1](https://learn.microsoft.com/en-us/purview/sit-defn-azure-eventgrid-access-key).

2. **Key Rotation**:
   - **Purpose**: Having two keys allows for seamless key rotation without downtime.
   - **Scenario**: You can regenerate one key (e.g., the secondary key) while continuing to use the other key (e.g., the primary key) for active operations. Once the new key is in place, you can switch to using it and then regenerate the other key[2](https://learn.microsoft.com/en-us/azure/event-grid/get-access-keys).

3. **Security**:
   - **Purpose**: Regularly rotating keys enhances security by minimizing the risk of key compromise.
   - **Scenario**: By periodically regenerating and updating the keys, you ensure that even if a key is compromised, it will only be valid for a limited time[2](https://learn.microsoft.com/en-us/azure/event-grid/get-access-keys).

### Example Workflow for Key Rotation:
1. **Initial Setup**:
   - Use the primary key for all publishing operations.
   - Store both keys securely, such as in Azure Key Vault.

2. **Key Rotation**:
   - Regenerate the secondary key using the Azure portal, CLI, or PowerShell.
   - Update the stored secondary key in Azure Key Vault.
   - Switch your application to use the new secondary key.
   - Regenerate the primary key.
   - Update the stored primary key in Azure Key Vault.
   - Switch back to using the primary key for publishing operations.

### How to Regenerate Keys:
- **Azure Portal**:
  - Navigate to your Event Grid topic or domain.
  - Go to the **Access keys** section.
  - Click on **Regenerate Key** for the key you want to regenerate.

- **Azure CLI**:
  ```bash
  az eventgrid topic key regenerate --resource-group <RESOURCE_GROUP_NAME> --name <TOPIC_NAME> --key-name key1
  ```

- **Azure PowerShell**:
  ```powershell
  New-AzEventGridTopicKey -ResourceGroupName <RESOURCE_GROUP_NAME> -Name <TOPIC_NAME> -KeyName key1


[1](https://learn.microsoft.com/en-us/purview/sit-defn-azure-eventgrid-access-key): [Get access keys for an Event Grid resource](https://learn.microsoft.com/en-us/azure/event-grid/get-access-keys)
[2](https://learn.microsoft.com/en-us/azure/event-grid/get-access-keys): [Rotation tutorial for resources with two sets of credentials](https://learn.microsoft.com/en-us/azure/key-vault/secrets/tutorial-rotation-dual)
