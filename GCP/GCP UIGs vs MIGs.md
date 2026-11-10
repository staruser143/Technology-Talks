# GCP MIGs vs UIGs
- Google Cloud Platform (GCP) distinguishes between Managed Instance Groups (MIGs) and Unmanaged Instance Groups (UIGs or UMIGs) based on how virtual machines (VMs) are deployed, scaled, and maintained. [1, 2] 

## Key Differences
* **Definition & Structure**
    * **MIGs**: A collection of identical (or stateful) VMs created from a single instance template and managed as a single entity.
    * **UIGs**: A loose, manual grouping of individual VMs that do not need to be identical or share a common template. [2, 3, 4] 
* **Auto-Healing & Fault Tolerance**
    * **MIGs**: Automatically detect failed or crashed VMs (via basic VM status or application-based health checks) and recreate them using the original template and settings.
    * **UIGs**: Do not provide auto-healing. If a VM crashes, the load balancer simply stops sending traffic to it, and capacity remains reduced until a human manually intervenes. [1, 2, 5] 
* **Scalability & Updates**
    * **MIGs**: Support automatic scaling based on demand (such as CPU utilization or custom metrics), plus rolling and canary updates for software deployments.
    * **UIGs**: No built-in autoscaling or automated rolling updates; every VM must be configured and scaled manually. [2, 3, 6] 
* Multi-Zone & Regional Support
    * **MIGs**: Can be regional, spanning multiple zones for high availability against zonal outages.
    * **UIGs**: Limited to single-zone manual management. [5, 7] 
* Use Cases
    * **MIGs**: Ideal for scalable web apps, microservices, stateful databases, and batch/GPU workloads.
    * **UIGs**: Reserved mostly for legacy third-party virtual appliances (like specialized firewalls or routers) that run their own internal clustering logic and would conflict with GCP's automated VM recreation. Note that Network Endpoint Groups (NEGs) have largely replaced UIGs for modern load-balancing architectures. [1, 5] 

[1] [https://mikerodionov.medium.com](https://mikerodionov.medium.com/gcp-mig-uig-and-neg-e4221ae26725)
[2] [https://medium.com](https://medium.com/@stephythomaspss/becoming-a-google-cloud-engineer-my-daily-learning-journal-day-4-d4cd49677fb7)
[3] [https://cloud.google.com](https://cloud.google.com/instance-groups)
[4] [https://www.ibm.com](https://www.ibm.com/docs/en/tarm/8.19.6?topic=vms-virtual-machine-spec-google-cloud-managed-instance-group-mig)
[5] [https://docs.cloud.google.com](https://docs.cloud.google.com/compute/docs/instance-groups)
[6] [https://www.youtube.com](https://www.youtube.com/watch?v=COC143ckAFM&t=5)
[7] [https://dev.to](https://dev.to/latchudevops/part-37-google-compute-engine-managed-instance-groups-stateful-in-google-cloud-platform-gcp-2ib4)
