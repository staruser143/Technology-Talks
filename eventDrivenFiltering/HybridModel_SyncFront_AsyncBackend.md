## Using Hybrid Model combing Sync and Async Patterns
In a hybrid model, we can combine synchronous and asynchronous patterns to strike a balance between fast responses for the user and scalable, decoupled operations on the backend.


---

### **1. Frontend Workflow**
- **Primary Flow (Synchronous)**:
   - When a user answers a question in the sidebar, the frontend immediately sends the responses to the backend through an HTTP request.
   - The backend processes the request and returns the filtered/recommended plans based on lightweight, synchronous filtering logic.
   - The frontend updates the UI with the results for a seamless user experience.

- **Secondary Flow (Asynchronous)**:
   - While the user interacts with the recommended plans, the backend asynchronously triggers more complex operations, such as precomputing recommendations for additional user behavior or enriching plans with external data.

---

### **2. Backend Design**
**Synchronous Operations**:
- **Step 1: Lightweight Filtering in Memory/Database**:
   - The backend performs quick filtering of plans using an optimized query in MongoDB (e.g., `$and`, `$or`, `$in` operators) or by leveraging a caching layer like Redis for precomputed data.
   - This ensures that the response time is kept low, enabling real-time updates on the frontend.

**Asynchronous Operations**:
- **Step 2: Asynchronous Event Handling**:
   - Publish an event to Kafka/Azure Event Grid after the filtering process.
   - These events can trigger additional workflows such as:
     - Enriching plans with recommendations based on machine learning models.
     - Caching the updated plan recommendations for future queries.
     - Logging user behavior for analytics and personalization.

---

### **3. Example Implementation**

**Frontend** (ReactJS):
- When the user interacts with the sidebar, an HTTP `POST /plans/filter` request is sent to fetch filtered plans.
- After the response, update the UI dynamically.
- Optionally, display a loading indicator if the recommendation logic triggers asynchronous updates.

**Backend** (NestJS):
1. **Synchronous Endpoint** (`POST /plans/filter`):
   ```typescript
   @Post('filter')
   async filterPlans(@Body() userResponses: UserResponseDto): Promise<Plan[]> {
       const filteredPlans = await this.planService.filterPlans(userResponses); // Quick DB query or in-memory filtering
       this.eventEmitter.emit('plans.filtered', userResponses); // Asynchronous event trigger
       return filteredPlans; // Return immediate response
   }
   ```

2. **Asynchronous Event Handler**:
   ```typescript
   @OnEvent('plans.filtered')
   async handleFilteredEvent(userResponses: UserResponseDto) {
       const enrichedPlans = await this.planService.enrichPlans(userResponses); // Run heavy computations
       await this.cacheService.storeRecommendations(userResponses.userId, enrichedPlans); // Cache results
   }
   ```

3. **Caching with Redis** (Optional):
   - If the user repeats the process with the same answers, fetch recommendations from Redis for ultra-fast responses.

**Asynchronous Workflow with Kafka/Azure Event Grid**:
- Publish the user's responses as a message to Kafka or Event Grid.
- Downstream services can process the data without holding up the immediate response.

---

### **4. Workflow in Action**
1. **First Interaction**:
   - User answers a question.
   - Backend filters plans synchronously and returns the results to the frontend (~low latency).
   - Simultaneously, the backend publishes an event for downstream processing.

2. **Subsequent Interaction**:
   - Backend fetches recommendations or updates from Redis for immediate response.
   - Asynchronous systems continue enhancing the user experience behind the scenes.

---

### **Benefits of Hybrid Model**
- **Low Latency**:
   - The user gets fast responses for primary interactions.
- **Scalability**:
   - Asynchronous workflows offload heavy processing to separate services.
- **Decoupling**:
   - Kafka/Event Grid ensures independent and resilient downstream services.
- **Improved User Experience**:
   - Subsequent interactions can use enriched or cached data to provide more refined recommendations.

---
