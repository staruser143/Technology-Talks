## Using Websockets for this usecase
Incorporating WebSockets into this use case can provide real-time, bi-directional communication, ensuring that the recommended plans and filtering updates happen instantaneously.

---
## Solution Design
### **Frontend (ReactJS)**
1. **WebSocket Client Setup**:
   - Use the `WebSocket` API or a library like `socket.io-client` to establish a persistent connection with the backend.
   - Establish the connection when the plan listing page is mounted.

2. **Event-Driven Interaction**:
   - Listen for `recommendationUpdates` events from the server.
   - Whenever a user answers a question, send the responses to the server via the WebSocket connection.
   - Handle the server's response by dynamically updating the plan listing in the UI.

3. **State Management**:
   - Store the list of recommended plans and user responses in a global state using a tool like Redux or React Context, which will help maintain a consistent view across components.

4. **Fallback Mechanism**:
   - Ensure the application can gracefully fall back to HTTP-based communication if WebSockets are unavailable (e.g., older browsers or network restrictions).

---

### **Backend (NestJS)**
1. **WebSocket Gateway**:
   - Use NestJS’s `@WebSocketGateway()` to set up a WebSocket server.
   - Create namespaces or rooms if we need to segregate connections (e.g., one room per user session).

2. **Handling Events**:
   - Define an event for receiving user responses (e.g., `userResponse`) and process it.
   - Implement an event for sending filtered plans back to the client (e.g., `recommendationUpdates`).

3. **Real-Time Filtering Logic**:
   - When the backend receives user responses via the WebSocket, process the filtering in real time.
   - Optionally, push enriched recommendations asynchronously using a queue system (like Kafka) and notify the frontend.

4. **Connection Management**:
   - Track and manage active WebSocket connections (e.g., store user sessions in memory or a database).
   - Handle reconnections gracefully to maintain the real-time experience.

5. **Security**:
   - Use WebSocket authentication (e.g., JWT) to secure the connection.
   - Validate user input to avoid injection attacks or malicious payloads.

---

### **High-Level Workflow**

#### **Initial Setup**
1. Frontend establishes a WebSocket connection to the backend when the plan listing page is opened.

#### **User Interaction**
2. User answers a question in the sidebar.
3. Frontend emits a `userResponse` event through WebSocket with the user’s answers.
4. Backend listens to this event, filters the plans in real time, and emits a `recommendationUpdates` event back to the client.

#### **Real-Time Plan Update**
5. Frontend listens for `recommendationUpdates` and updates the central plan listing dynamically.
6. If there are significant backend-side computations or updates from external services (e.g., further enriching recommendations), these can be pushed to the client as additional `recommendationUpdates` events.

#### **Plan Selection**
7. Once the user finalizes their plan selection, the frontend sends the data to the backend over HTTP or WebSocket for persisting to the data store.

---

### **Code Examples**

#### **Frontend WebSocket Client**
```javascript
import { useEffect, useState } from "react";
import { io } from "socket.io-client";

const useWebSocket = (userId) => {
  const [plans, setPlans] = useState([]);
  const socket = io("http://localhost:3000", { query: { userId } });

  useEffect(() => {
    socket.on("recommendationUpdates", (updatedPlans) => {
      setPlans(updatedPlans);
    });

    return () => {
      socket.disconnect();
    };
  }, [socket]);

  const sendUserResponse = (userResponse) => {
    socket.emit("userResponse", userResponse);
  };

  return { plans, sendUserResponse };
};
```

#### **Backend WebSocket Gateway (NestJS)**
```typescript
import { WebSocketGateway, SubscribeMessage, MessageBody, WebSocketServer } from '@nestjs/websockets';
import { Server, Socket } from 'socket.io';

@WebSocketGateway({ cors: true })
export class PlanGateway {
  @WebSocketServer()
  server: Server;

  @SubscribeMessage('userResponse')
  async handleUserResponse(@MessageBody() userResponse: any, client: Socket): Promise<void> {
    const filteredPlans = await this.planService.filterPlans(userResponse);
    client.emit('recommendationUpdates', filteredPlans);
  }

  async pushEnrichedRecommendations(userId: string, enrichedPlans: any): Promise<void> {
    const userSocket = this.server.sockets.sockets.get(userId);
    if (userSocket) {
      userSocket.emit('recommendationUpdates', enrichedPlans);
    }
  }
}
```

---

### **Pros of WebSocket Integration**
- Real-time plan updates without manual refreshes or HTTP polling.
- Improved user experience with instant feedback.
- Efficient for high-frequency interactions (e.g., frequent question answering).

### **Cons**
- Added complexity for managing WebSocket connections.
- Requires proper handling for scalability (e.g., sticky sessions or shared session storage for distributed WebSocket servers).

---

This architecture supports both real-time and asynchronous workflows, ensuring a seamless user experience.

---

### **Why Fire the Same `recommendationUpdates` Event?**
Reusing the `recommendationUpdates` event ensures consistency on the client side. Here’s why:

1. **Unified Handling on the Frontend**:
   - The frontend only needs to listen to a single event (`recommendationUpdates`) to receive updates about both initial recommendations and enriched data.
   - This simplifies frontend logic—any updates to the plans (filtered or enriched) are handled consistently.

2. **Incremental Updates**:
   - Initially, the user gets basic filtered recommendations (synchronous).
   - Later, enriched recommendations are pushed as they are ready (asynchronous). By using the same event name, the client dynamically updates the listing without differentiating between the stages of updates.

3. **Real-Time User Feedback**:
   - The `recommendationUpdates` event can be sent multiple times. For example:
     - First update: Send basic recommendations (low-latency response).
     - Second update: Send enriched or refined recommendations (when enrichment is completed asynchronously).

This approach keeps the user interface seamless and responsive, maintaining a real-time experience.

---

### **How is `pushEnrichedRecommendations` Triggered?**
The `pushEnrichedRecommendations` method is triggered asynchronously after the enrichment process is complete. Here’s how it works:

1. **Enrichment Workflow**:
   - When the backend receives user responses (via WebSocket or HTTP), it processes the initial filtering logic synchronously to generate basic recommendations.
   - Simultaneously, the backend emits an asynchronous event (e.g., to Kafka or Redis Pub/Sub) for enrichment. This could involve calling machine learning models, combining external data sources, or applying advanced filtering.

2. **Background Enrichment Process**:
   - A separate service or a background worker (e.g., using NestJS queues or a microservice) processes the enrichment task.
   - Once enriched data is ready, it triggers the backend gateway's `pushEnrichedRecommendations` method to notify the connected client via WebSockets.

3. **Example of Asynchronous Trigger**:
   ```typescript
   async handleFilteredEvent(userResponses: UserResponseDto) {
       const enrichedPlans = await this.planService.enrichPlans(userResponses); // Perform enrichment
       this.planGateway.pushEnrichedRecommendations(userResponses.userId, enrichedPlans); // Notify the client
   }
   ```

4. **Event Propagation**:
   - The enrichment service could also publish a message to Kafka or a similar message broker. A consumer in the backend (e.g., using a NestJS Kafka subscriber) listens to this message and triggers `pushEnrichedRecommendations`.

---

### **Visualizing the Workflow**
1. **User Interaction**:
   - The user answers a question. 
   - The backend filters plans and immediately sends filtered recommendations via `recommendationUpdates`.

2. **Enrichment in the Background**:
   - The backend or another service begins enriching recommendations asynchronously.

3. **Sending Enriched Recommendations**:
   - Once enrichment is complete, the backend pushes enriched recommendations to the client via the same `recommendationUpdates` event.

---

### **Benefits of This Design**
- **Real-Time Updates**: The user gets immediate results (filtered plans) without waiting for the full enrichment process.
- **Consistency**: The frontend only deals with one event type (`recommendationUpdates`), simplifying its implementation.
- **Scalability**: The asynchronous enrichment can be offloaded to a distributed system like Kafka, making the architecture resilient to load.


