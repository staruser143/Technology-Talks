# GCP NEGs
- A Google Cloud Platform (GCP) Network Endpoint Group (NEG) is a configuration object that specifies a group of backend IP address and port combinations, allowing Google Cloud load balancers and Cloud Service Mesh to route traffic at a granular, service- or pod-level rather than just VM-to-VM. [1, 2, 3] 
## Core Types of NEGs

* **Zonal NEGs**: Groups of IP and port combinations (such as GKE pods or microservices running inside virtual machines within a specific zone). [1, 2] 
* **Serverless NEGs**: Directs load balancer traffic to serverless services like Cloud Run, Cloud Functions, and App Engine. [4, 5] 
* **Internet NEGs**: Points traffic to external backends located outside of GCP (on-premises data centers or other cloud providers) via a fully qualified domain name (FQDN) or public IP. [6, 7] 
* **Hybrid NEGs**: Connects on-premises or hybrid-cloud services to GCP load balancers. [6, 8] 
* **Private Service Connect (PSC) NEGs**: Routes traffic to services running across different GCP networks or projects. [6] 

## Key Benefits

* **Granular Routing**: Send traffic directly to specific container pods in GKE (bypassing node-level proxies like kube-proxy for lower latency). [1, 9] 
* **Serverless Integration**: Expose serverless applications safely behind Google Cloud's external or internal Application Load Balancers. [4, 5] 
* **Global Load Balancing & Security**: Combine external backends with Google Cloud features like Cloud CDN, Cloud Armor (DDoS/WAF protection), and global load balancing. [6, 7] 

[1] [https://docs.cloud.google.com](https://docs.cloud.google.com/load-balancing/docs/negs)
[2] [https://registry.terraform.io](https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/compute_network_endpoint_group)
[3] [https://www.linkedin.com](https://www.linkedin.com/posts/sumit-kumar-ab2059132_16-introduction-to-network-endpoint-groups-activity-7135187263252049920-xoKc)
[4] [https://www.youtube.com](https://www.youtube.com/watch?v=0CHDPaOIBks&t=17)
[5] [https://www.cloudthat.com](https://www.cloudthat.com/resources/blog/understanding-network-endpoint-groups-negs-in-google-cloud-load-balancing)
[6] [https://medium.com](https://medium.com/google-cloud/nuts-and-bolts-of-negs-network-endpoint-groups-in-gcp-35b0d06f4691)
[7] [https://www.youtube.com](https://www.youtube.com/watch?v=cFRKmkptnmc&t=39)
[8] [https://cloud.google.com](https://cloud.google.com/load-balancing)
[9] [https://www.youtube.com](https://www.youtube.com/watch?v=JFKBWAops1s&t=83)
