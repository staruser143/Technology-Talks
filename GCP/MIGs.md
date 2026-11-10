# Google Cloud Managed Instance Groups (MIGs)
- Collections of identical Compute Engine virtual machines that we can manage, scale, and heal as a single entity. [1, 2] 

## Key Features

* **Core Capacity**: Manages up to 4,000 identically configured VMs using an initial instance template. [1] 
* **Autoscaling**: Automatically adds or removes VMs based on traffic demand, CPU utilization, or custom metrics. [3, 4] 
* **Autohealing**: Detects and recreates unhealthy or crashed VMs using application-based health checks. [1, 3] 
* **High Availability**: Supports multi-zonal and regional deployments to spread workloads across failure domains. [1, 3] 
* **Stateful Workloads**: Preserves unique instance data, persistent disks, custom names, and metadata for stateful applications like databases (note: stateful MIGs do not support autoscaling). [5] 
* **Pricing**: There is no extra service charge for using MIGs; you pay only for the underlying Compute Engine VM resources. [1] 

## Types of MIGs

* **Stateless MIGs**: Identical VMs used for web servers, microservices, and batch processing that support full autoscaling and load balancing. [3, 5] 
* **Stateful MIGs**: VMs that retain persistent disks, IP addresses, and metadata across repairs or updates, ideal for legacy or database setups. [5] 

[1] [https://cloud.google.com](https://cloud.google.com/instance-groups)
[2] [https://docs.cloud.google.com](https://docs.cloud.google.com/compute/docs/instance-groups)
[3] [https://www.youtube.com](https://www.youtube.com/watch?v=COC143ckAFM&t=5)
[4] [https://www.skills.google](https://www.skills.google/focuses/611?parent=catalog)
[5] [https://dev.to](https://dev.to/latchudevops/part-37-google-compute-engine-managed-instance-groups-stateful-in-google-cloud-platform-gcp-2ib4)
