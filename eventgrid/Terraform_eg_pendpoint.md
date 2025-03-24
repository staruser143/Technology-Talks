# Provision Azure Event Grid Topic
We can provision an Azure Event Grid topic with a private endpoint using Terraform.

### Example Terraform Script

1. **Terraform Configuration**:
   - Create a Terraform configuration file (e.g., `main.tf`) with the necessary resources to create an Event Grid topic, a private endpoint, and the required networking components.

   ```hcl
   provider "azurerm" {
     features {}
   }

   resource "azurerm_resource_group" "example" {
     name     = "example-resources"
     location = "West Europe"
   }

   resource "azurerm_virtual_network" "example" {
     name                = "example-vnet"
     address_space       = ["10.0.0.0/16"]
     location            = azurerm_resource_group.example.location
     resource_group_name = azurerm_resource_group.example.name
   }

   resource "azurerm_subnet" "example" {
     name                 = "example-subnet"
     resource_group_name  = azurerm_resource_group.example.name
     virtual_network_name = azurerm_virtual_network.example.name
     address_prefixes     = ["10.0.1.0/24"]
   }

   resource "azurerm_eventgrid_topic" "example" {
     name                = "example-topic"
     resource_group_name = azurerm_resource_group.example.name
     location            = azurerm_resource_group.example.location
   }

   resource "azurerm_private_endpoint" "example" {
     name                = "example-private-endpoint"
     location            = azurerm_resource_group.example.location
     resource_group_name = azurerm_resource_group.example.name
     subnet_id           = azurerm_subnet.example.id

     private_service_connection {
       name                           = "example-privateserviceconnection"
       private_connection_resource_id = azurerm_eventgrid_topic.example.id
       subresource_names              = ["topic"]
       is_manual_connection           = false
     }
   }

   resource "azurerm_private_dns_zone" "example" {
     name                = "privatelink.eventgrid.azure.net"
     resource_group_name = azurerm_resource_group.example.name
   }

   resource "azurerm_private_dns_zone_virtual_network_link" "example" {
     name                  = "example-dnszone-vnet-link"
     resource_group_name   = azurerm_resource_group.example.name
     private_dns_zone_name = azurerm_private_dns_zone.example.name
     virtual_network_id    = azurerm_virtual_network.example.id
   }

   resource "azurerm_private_dns_a_record" "example" {
     name                = "example-topic"
     zone_name           = azurerm_private_dns_zone.example.name
     resource_group_name = azurerm_resource_group.example.name
     ttl                 = 300
     records             = [azurerm_private_endpoint.example.private_ip_address]
   }
   ```

2. **Initialize and Apply Terraform Configuration**:
   - Initialize the Terraform configuration:
     ```bash
     terraform init
     ```
   - Apply the configuration to create the resources:
     ```bash
     terraform apply
     ```

### Explanation:
- **azurerm_virtual_network** and **azurerm_subnet**: Create a virtual network and subnet for the private endpoint.
- **azurerm_eventgrid_topic**: Creates the Event Grid topic.
- **azurerm_private_endpoint**: Creates a private endpoint for the Event Grid topic.
- **azurerm_private_dns_zone** and **azurerm_private_dns_a_record**: Configure a private DNS zone and DNS record to resolve the private endpoint within the virtual network.

This setup ensures that your Event Grid topic is accessible via a private endpoint, enhancing security by keeping traffic within your virtual network.

