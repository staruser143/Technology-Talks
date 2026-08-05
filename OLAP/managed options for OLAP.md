## License

All three are **open source**, not proprietary:

| Engine | License | Origin |
|---|---|---|
| **Apache Druid** | Apache 2.0 | Originally built at Metamarkets |
| **Apache Pinot** | Apache 2.0 | Built at LinkedIn for user-facing analytics at hyperscale |
| **ClickHouse** | Apache 2.0 | Originally built at Yandex |

None of these are proprietary/closed — you can self-host any of them for free, inspect the source, and run them on your own infrastructure.

## Managed services

Yes, all three have managed offerings, run by the companies commercially backing each project:

- **Druid → Imply Polaris**: a fully-managed database-as-a-service from Imply, the company founded by the original creators of Apache Druid. Developers choose Imply Polaris to decrease time to market, increase developer productivity, and lower the overall cost of running Druid, and it's available on Azure as well as AWS/GCP.

- **Pinot → StarTree Cloud**: StarTree, the commercial company behind Pinot, operates StarTree Cloud — managed Apache Pinot on AWS and GCP, which collapses most of the operational surface (controllers, brokers, servers, minions, Zookeeper, deep store on S3/GCS, Kafka connections, monitoring, alerting). For most teams adopting Pinot in 2026, StarTree Cloud is described as the right starting point, since self-managed Pinot has a notably steeper operational ramp.

- **ClickHouse → ClickHouse Cloud**: ClickHouse Cloud is available on AWS, GCP, and Azure with marketplace billing integration. There are also third-party managed ClickHouse options like Tinybird and Altinity.

## Does AWS support them?

AWS doesn't offer these as **native first-party AWS services** the way it does Redshift, but they're all fully usable on AWS in a couple of ways:

1. **Self-managed on AWS infrastructure** — run any of the three on EC2/EKS/S3, which is common and well-documented (e.g., Druid using S3 for deep storage, Kinesis for ingestion)
2. **Via AWS Marketplace** — Imply (powered by Apache Druid) is available on AWS Marketplace as a fully-managed database-as-a-service, and ClickHouse Cloud and StarTree Cloud are similarly marketplace-listed, meaning you can provision and get billed through your AWS account even though Anthropic didn't build them

So the practical pattern: none of these are "AWS-native" like Redshift, but all three are well-supported *on* AWS — either self-hosted or through their vendor's managed cloud offering, billed via AWS Marketplace if you want it to show up on your AWS bill.

## For our broker use case
If we wanted to avoid running Druid/Pinot/ClickHouse ops ourself, Imply Polaris or StarTree Cloud would be the natural choice for the real-time commission dashboard path — letting your team focus on the data model and dashboards rather than cluster management, while still running inside our AWS environment via Marketplace billing.