# GCP Global Resources
- Google Cloud Platform (GCP) global resources are assets accessible by any resource in any zone or region within the same project without needing a location scope specification. [1] 

## Key Examples of Global Resources

* **Global Static External IP Addresses**: Reserved IP addresses used specifically for global load balancers.
* **Images**: Preconfigured or custom boot images that can be used by any instance or disk resource in the same project.
* **Disk Snapshots and Machine Images**: Stored data templates that can span multi-region or regional storage while behaving globally.
* **VPC Routes**: Network traffic routing rules that apply across your Virtual Private Cloud networks within a project.
* **Firewall Rules**: Network security rules applied across a VPC network regardless of zone or region.
* **Global Operations**: Task tracking or API operations executed on a global resource. [1, 2] 

## Scope Comparison in GCP

* **Global**: Accessible from anywhere in the project regardless of region or zone (e.g., images, global static IPs).
* **Regional**: Accessible by any resource within a specific geographic region across all of its zones (e.g., regional static IPs, subnets).
* **Zonal**: Restricted to a single isolated zone within a region (e.g., Virtual Machine instances, zonal persistent disks). [1, 3, 4, 5] 

[1] [https://docs.cloud.google.com](https://docs.cloud.google.com/compute/docs/regions-zones/global-regional-zonal-resources)
[2] [https://blog.optimal.io](https://blog.optimal.io/Google-Cloud-Global-Resources/)
[3] [https://k21academy.com](https://k21academy.com/google-cloud/google-cloud-architecture-2/)
[4] [https://tutorialsdojo.com](https://tutorialsdojo.com/google-cloud-global-infrastructure/)
[5] [https://www.geeksforgeeks.org](https://www.geeksforgeeks.org/devops/google-cloud-platform-gcp/)
