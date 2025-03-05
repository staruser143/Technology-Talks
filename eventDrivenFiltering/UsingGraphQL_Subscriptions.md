## GraphQL and WebSockets
* GraphQL does support WebSockets through **GraphQL Subscriptions**, which are designed for real-time communication.
* Subscriptions allow clients to receive updates from the server whenever specific events occur.
* WebSockets serve as the transport protocol for these subscriptions, enabling a persistent, bi-directional connection between the client and server.

Here’s how we can leverage GraphQL Subscriptions with WebSockets for the use case:

---

### **1. Why Use GraphQL Subscriptions for This Use Case?**
- **Real-Time Updates**: When users answer questions, the server can push updated recommendations to the client in real time.
- **Efficient Data Transfer**: GraphQL Subscriptions allows us to specify exactly what data the client needs, reducing bandwidth usage.
- **Unified API**: We can use GraphQL for queries, mutations, and subscriptions, keeping the API consistent and easy to manage.

---

### **2. Setting Up GraphQL Subscriptions**

#### **Backend (NestJS with Apollo Server)**
1. **Install Required Packages**:
   - Install `@nestjs/graphql`, `apollo-server-express`, and `graphql-ws` for WebSocket support.
   ```bash
   npm install @nestjs/graphql apollo-server-express graphql-ws
   ```

2. **Configure GraphQL Module**:
   - Enable subscriptions in the GraphQL module configuration.
   ```typescript
   import { GraphQLModule } from '@nestjs/graphql';
   import { ApolloDriver, ApolloDriverConfig } from '@nestjs/apollo';

   @Module({
     imports: [
       GraphQLModule.forRoot<ApolloDriverConfig>({
         driver: ApolloDriver,
         autoSchemaFile: true,
         subscriptions: {
           'graphql-ws': true, // Enable WebSocket support
         },
       }),
     ],
   })
   export class AppModule {}
   ```

3. **Define Subscription Resolvers**:
   - Create a subscription resolver for real-time updates.
   ```typescript
   import { Resolver, Subscription } from '@nestjs/graphql';
   import { PubSub } from 'graphql-subscriptions';

   const pubSub = new PubSub();

   @Resolver()
   export class PlanResolver {
     @Subscription(() => [Plan])
     recommendationUpdates() {
       return pubSub.asyncIterator('recommendationUpdates');
     }

     async triggerRecommendationUpdates(updatedPlans: Plan[]) {
       await pubSub.publish('recommendationUpdates', { recommendationUpdates: updatedPlans });
     }
   }
   ```

4. **Trigger Subscriptions**:
   - When a user answers a question, trigger the `recommendationUpdates` subscription to push updated plans to the client.
   ```typescript
   async handleUserResponse(userResponse: UserResponseDto) {
     const updatedPlans = await this.planService.filterPlans(userResponse);
     await this.planResolver.triggerRecommendationUpdates(updatedPlans);
   }
   ```

---

#### **Frontend (ReactJS with Apollo Client)**
1. **Install Required Packages**:
   - Install `@apollo/client` and `graphql-ws` for WebSocket support.
   ```bash
   npm install @apollo/client graphql-ws
   ```

2. **Set Up Apollo Client with WebSocket Link**:
   - Configure Apollo Client to use WebSockets for subscriptions.
   ```javascript
   import { ApolloClient, InMemoryCache, split } from '@apollo/client';
   import { GraphQLWsLink } from '@apollo/client/link/subscriptions';
   import { createClient } from 'graphql-ws';
   import { getMainDefinition } from '@apollo/client/utilities';

   const wsLink = new GraphQLWsLink(createClient({ url: 'ws://localhost:3000/graphql' }));

   const client = new ApolloClient({
     link: wsLink,
     cache: new InMemoryCache(),
   });
   ```

3. **Subscribe to Updates**:
   - Use the `useSubscription` hook to listen for real-time updates.
   ```javascript
   import { gql, useSubscription } from '@apollo/client';

   const RECOMMENDATION_UPDATES = gql`
     subscription {
       recommendationUpdates {
         id
         name
         description
       }
     }
   `;

   const PlanList = () => {
     const { data, loading } = useSubscription(RECOMMENDATION_UPDATES);

     if (loading) return <p>Loading...</p>;

     return (
       <ul>
         {data.recommendationUpdates.map((plan) => (
           <li key={plan.id}>{plan.name}</li>
         ))}
       </ul>
     );
   };
   ```

---

### **3. Workflow in Action**
1. **User Interaction**:
   - The user answers a question in the sidebar.
   - The frontend sends the response to the backend via a GraphQL mutation.

2. **Backend Processing**:
   - The backend processes the response and filters the plans.
   - It triggers the `recommendationUpdates` subscription to push the updated plans to all connected clients.

3. **Real-Time Updates**:
   - The frontend receives the updated plans via the WebSocket connection and updates the UI dynamically.

---

### **4. Benefits of Using GraphQL Subscriptions**
- **Real-Time Experience**: Users see updated recommendations instantly without refreshing the page.
- **Efficient Data Handling**: Only the required data is sent to the client, reducing bandwidth usage.
- **Scalable Architecture**: GraphQL Subscriptions can handle multiple clients efficiently with proper WebSocket management.

