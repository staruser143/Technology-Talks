# Step-by-step guide to setting up private endpoints for an Azure Event Grid topic using the Azure portal:

### Steps to Set Up Private Endpoints:

1. **Sign in to Azure Portal**:
   - Go to the Azure portal and sign in with your Azure account.

2. **Create or Navigate to Your Event Grid Topic**:
   - If you are creating a new topic, follow the steps to create a custom topic. On the **Basics** page of the Create topic wizard, select **Next: Networking** after filling in the required fields.
   - If you are configuring an existing topic, navigate to your Event Grid topic.

3. **Configure Networking**:
   - On the **Networking** page, select the **Private access** option to allow access via a private endpoint.
   - In the **Private endpoint connections** section, click on **+ Private endpoint**.

4. **Create Private Endpoint**:
   - On the **Create private endpoint** page, follow these steps:
     - **Name**: Enter a name for the private endpoint.
     - **Subscription**: Select your Azure subscription.
     - **Resource Group**: Choose an existing resource group or create a new one.
     - **Region**: Select the region for the private endpoint. It must be in the same region as your virtual network.
     - **Virtual Network**: Select the virtual network where the private endpoint will be created.
     - **Subnet**: Choose a subnet within the selected virtual network.
     - **Private DNS Integration**: Decide whether to integrate the private endpoint with a private DNS zone. This helps with name resolution within the virtual network.

5. **Review and Create**:
   - After configuring the private endpoint, click **Review + create**.
   - Review your settings and click **Create** to create the private endpoint.

6. **Verify Configuration**:
   - Once the private endpoint is created, navigate to the **Networking** tab of your Event Grid topic.
   - Ensure that the private endpoint is listed under the **Private endpoint connections** section.

### Example Use Case:
- **Internal Applications**: If you have an internal application hosted within an Azure Virtual Network (VNet) that needs to securely publish events to an Event Grid topic without exposing the traffic to the public internet, setting up a private endpoint ensures that all communication stays within the VNet.
