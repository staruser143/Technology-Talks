# GCP Projects
- Fundamental administrative and organizational unit in Google Cloud that groups together your cloud resources, enabled APIs, billing settings, and access control policies. [1, 2] 

## Key Characteristics of a GCP Project

* **Resource Container**: All GCP resources—such as virtual machines in Compute Engine, databases in Cloud SQL, or storage buckets in Cloud Storage—must belong to a project. [3, 4, 5] 
* **Three Identifiers**: Every project is defined by three core properties:
    * **Project Name**: A human-readable display name (which can be changed later).
    * **Project ID**: A unique, custom string of 6 to 30 lowercase letters, digits, or hyphens (fixed permanently upon creation and cannot be reused or changed).
    * **Project Number**: An automatically generated unique numeric ID assigned by Google Cloud. [1, 6, 7] 
* **Security & Permissions**: Identity and Access Management (IAM) roles and permissions are scoped at the project level, meaning policies applied to a project inherit down to all the resources inside it. [2, 8] 
* **Billing Boundary**: A project serves as the primary tracking and billing boundary where a Cloud Billing account is attached to pay for the resource usage incurred within that project. [1, 7] 
* **API Enablement**: Services do not run by default; you must explicitly enable specific Google Cloud APIs (such as the Compute Engine API or Kubernetes API) within each individual project. [5, 9] 

## Where Projects Fit in the Hierarchy
Google Cloud structures its infrastructure in a top-down hierarchy: [1, 8] 

   1. **Organization**: The root node representing a company or enterprise account.
   2. **Folders**: Optional groupings used to organize projects by department, team, or environment (e.g., Development vs. Production).
   3. **Projects**: The logical workspace living under an organization or folder.
   4. **Resources**: The actual VMs, databases, and buckets residing inside a project. [1, 4, 8] 

[1] [https://www.geeksforgeeks.org](https://www.geeksforgeeks.org/devops/how-to-create-a-gcp-project/)
[2] [https://nhimg.org](https://nhimg.org/glossary/google-cloud-project/)
[3] [https://docs.cloud.google.com](https://docs.cloud.google.com/storage/docs/projects)
[4] [https://khatzie.medium.com](https://khatzie.medium.com/my-first-gcp-project-understanding-projects-regions-and-resources-dfd21bf72cc7)
[5] [https://www.youtube.com](https://www.youtube.com/watch?v=wHQFiFhaJq4)
[6] [https://www.youtube.com](https://www.youtube.com/watch?v=wpZ7RLSGfQQ&t=40)
[7] [https://www.geeksforgeeks.org](https://www.geeksforgeeks.org/devops/google-cloud-platform-gcp/)
[8] [https://brains4cloud.com](https://brains4cloud.com/en/glossary/gcp-project)
[9] [https://developers.google.com](https://developers.google.com/workspace/guides/create-project)
