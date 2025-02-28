## Store the Complete data as Event Payload
It is not recommended to store the entire quote data (all sections captured so far) in the PersonalDetailsSaved event. 
Instead, store only the complete Personal Details section (not just the changed field) for the following reasons:

## Why Not Store the Entire Quote Data in Each Event?

1. Event Segmentation & Modularity
* In an event-sourced system, events should represent meaningful domain actions.
* A PersonalDetailsSaved event should only encapsulate personal details, not dependent or employment details.
* Storing the entire quote with every event breaks modularity and introduces unnecessary coupling between sections.

2. Avoiding Unnecessary Data Duplication
* If each section's event contains the whole quote data, the event store becomes bloated with redundant information.
* This makes querying, storing, and processing events inefficient, especially for large quotes with multiple dependents.

3. Optimizing Event Consumption & Read Model Updates
* If every event contains the whole quote, consumers processing PersonalDetailsSaved will have irrelevant data (e.g., employment details).
* If a consumer is responsible for updating only personal details in the read model, having unrelated data forces it to filter out unnecessary fields.

4. Maintaining Domain Model Independence
* Each event should be self-contained for its domain context.
If the PersonalDetailsSaved event carries the entire quote, the consumer must know all quote sections to process it, making the system less flexible for future changes.

6. Parallel Processing & Performance

* In a distributed system, different consumers process different events.
* If a PersonalDetailsSaved event includes employment details, a consumer responsible for employment data will unnecessarily receive and process data it doesn’t need.
* This reduces efficiency and increases event payload sizes unnecessarily.

Recommended Approach:

** Fire section-specific events (e.g., PersonalDetailsSaved, DependentDetailsSaved, EmploymentDetailsSaved), each containing the full state of only that section.

** Maintain a separate event like QuoteSubmitted when the entire quote is finalized, which captures the entire quote for completeness.


Example Event Flow:

1. PersonalDetailsSaved: { name, email, phone }
2. DependentDetailsSaved: { dependents: [{ name, age }, { name, age }] }
3. EmploymentDetailsSaved: { company, role, salary }
4. QuoteSubmitted: { personalDetails, dependents, employmentDetails, ... } (Final snapshot)

This keeps events modular, avoiding excessive duplication, while still ensuring event consumers only handle relevant data.


