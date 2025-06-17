We have chosen MongoDB Atlas as the datastore for event and domain collections for the event driven system we are building based on Event Sourcing and CQRS and want to use Mongo Change Streams in a microservice to build Read Model in domain collections based on projections from change streams. Our Web workloads are running in AKS in Azure. We run multiple instances of microservice for HA, but we see that the changes are sent and processed by all instances of microservices which results in duplicate processing and performance degradation.We are looking for options to address this challenge. 

We have considered the 5 approaches as below. I want to know pros and cons of each approach and which one could be a better approach to handle the challenges we are facing.
Please consider each of the approach and let me know. If there is any other approach other than what I have presented do also let me know on that.

1.Customize Mongo Change Streams with custom code which addresses some of the limitations in Mongo Change Streams.
2.Leverage Mongo Atlas Stream Processing which enhances Mongo Change Streams either with or without Kafka/Azure Event Hubs
3.Use Kafka along with Atlas MongoDB to publish events to Kafka and from there process it accordinglly using multiple consumers in a load balanced manner
4.Use Azure Event Hubs with Atlas MongoDB , similar to Kafka but as a Managed Service than OpenSource Kafka which we have to manage ourselves, but provides interoperabiity because web workloads are already in AKS in Azure
5.Use Confluent Kafka along with Atlas MongoDB, similar to Azure Event Hubs, but not without vendor-lock-in like Azure Event Hubs.
