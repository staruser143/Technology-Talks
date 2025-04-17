# Message Ordering and Sessions in Azure Service Bus with Logic Apps

When building integrations that process business transactions like your quote-to-lead flow, message ordering can be critical. Service Bus sessions provide a powerful mechanism to maintain order when it matters, while still allowing parallel processing when possible. Let's explore how sessions work with Service Bus and Logic Apps, with NestJS code examples for the publishing side.

## Understanding Service Bus Sessions

Sessions in Azure Service Bus create a first-in-first-out (FIFO) guarantee for groups of related messages. This is particularly valuable when processing quotes that might have dependencies or relationships.

### How Sessions Work

When you send a message to Service Bus with a session ID, you're essentially telling Service Bus: "This message belongs to a logical group, and all messages in this group must be processed in order by a single consumer."

Think of sessions like dedicated lanes on a highway. Messages with the same session ID travel in the same lane and arrive in order, while messages in different sessions can move independently.

### Common Use Cases for Sessions in Your Scenario

Sessions are particularly valuable when:

1. Processing multiple quotes from the same customer that must be handled in chronological order
2. Managing updates to existing quotes that must be applied sequentially
3. Ensuring related transactions (like a quote and its line items) stay together
4. Implementing workflows where multiple steps must occur in a specific sequence

## Publishing Session Messages from NestJS

When implementing your MongoDB change stream microservice in NestJS, you'll need to:

1. Identify what constitutes a session in your business context
2. Set the appropriate session ID when publishing messages
3. Ensure your message structure contains all necessary data

Here's a detailed implementation example in NestJS:

```typescript
import { Injectable } from '@nestjs/common';
import { ServiceBusClient, ServiceBusMessage } from '@azure/service-bus';

@Injectable()
export class QuotePublisherService {
  private sbClient: ServiceBusClient;
  private sender: any;
  
  constructor() {
    // Initialize Service Bus client - use environment variables or config service
    this.sbClient = new ServiceBusClient(process.env.SERVICE_BUS_CONNECTION_STRING);
    this.sender = this.sbClient.createSender(process.env.SERVICE_BUS_TOPIC_NAME);
  }

  /**
   * Publishes a quote event to Service Bus with appropriate session handling
   * @param quoteData The quote data from MongoDB
   */
  async publishQuoteEvent(quoteData: any): Promise<void> {
    try {
      // Determine the appropriate session ID based on your business rules
      // This ensures related messages are processed in order
      const sessionId = this.determineSessionId(quoteData);
      
      // Create the message with the session ID
      const message: ServiceBusMessage = {
        body: {
          eventType: 'QuoteCreated',
          timestamp: new Date().toISOString(),
          data: quoteData
        },
        sessionId: sessionId,
        contentType: 'application/json',
        // Optional: Set time-to-live if needed
        timeToLive: 7 * 24 * 60 * 60 * 1000 // 7 days in milliseconds
      };

      // Send the message to the Service Bus topic
      await this.sender.sendMessages(message);
      console.log(`Published quote event with session ID: ${sessionId}`);
    } catch (error) {
      console.error('Error publishing quote event:', error);
      throw error;
    }
  }

  /**
   * Determines the appropriate session ID based on business rules
   * @param quoteData The quote data from MongoDB
   * @returns A session ID string
   */
  private determineSessionId(quoteData: any): string {
    // Implement your session ID strategy here
    // Some common approaches:
    
    // 1. Customer-based sessions (all quotes from same customer processed in order)
    if (quoteData.customerId) {
      return `customer_${quoteData.customerId}`;
    }
    
    // 2. Quote-family sessions (main quote and updates kept together)
    if (quoteData.parentQuoteId) {
      return `quote_family_${quoteData.parentQuoteId}`;
    }
    
    // 3. Location or context-based sessions
    if (quoteData.locationId) {
      return `location_${quoteData.locationId}`;
    }
    
    // Fallback: Use the quote ID itself if no logical grouping needed
    return `quote_${quoteData._id}`;
  }

  async onModuleDestroy() {
    // Clean up resources when the application shuts down
    await this.sender.close();
    await this.sbClient.close();
  }
}
```

### Setting Up MongoDB Change Stream Monitoring in NestJS

Here's how you might implement the MongoDB change stream monitoring service that calls the publisher:

```typescript
import { Injectable, OnModuleInit, OnModuleDestroy } from '@nestjs/common';
import { InjectModel } from '@nestjs/mongoose';
import { Model, ChangeStream } from 'mongoose';
import { QuotePublisherService } from './quote-publisher.service';

@Injectable()
export class MongoChangeStreamService implements OnModuleInit, OnModuleDestroy {
  private changeStream: ChangeStream;
  
  constructor(
    @InjectModel('Quote') private quoteModel: Model<any>,
    private quotePublisherService: QuotePublisherService
  ) {}
  
  async onModuleInit() {
    // Set up the change stream pipeline to filter for relevant changes
    const pipeline = [
      {
        $match: {
          $or: [
            { 'operationType': 'insert' },
            { 'operationType': 'update' },
            {
              'operationType': 'replace'
            }
          ],
          'ns.coll': 'quotes' // Assuming your collection is named 'quotes'
        }
      }
    ];
    
    // Create the change stream
    this.changeStream = this.quoteModel.watch(pipeline);
    
    // Set up event handlers
    this.changeStream.on('change', async (change) => {
      try {
        // Extract the relevant document data
        const quoteData = change.fullDocument || await this.fetchDocument(change.documentKey._id);
        
        // Publish the event to Service Bus with appropriate session handling
        await this.quotePublisherService.publishQuoteEvent(quoteData);
      } catch (error) {
        console.error('Error processing change event:', error);
        // Implement appropriate error handling/retry logic
      }
    });
    
    console.log('MongoDB change stream monitoring initialized');
  }
  
  /**
   * For update operations that don't include the full document,
   * fetch the current state of the document
   */
  private async fetchDocument(documentId: string): Promise<any> {
    return this.quoteModel.findById(documentId).lean().exec();
  }
  
  async onModuleDestroy() {
    // Clean up resources when the application shuts down
    if (this.changeStream) {
      await this.changeStream.close();
      console.log('MongoDB change stream closed');
    }
  }
}
```

## Configuring Logic Apps to Process Session Messages

On the Logic Apps side, you need to configure your trigger to work with Service Bus sessions. Here's how to set it up:

1. In your Logic App, add a "When a message is received in a Service Bus topic (peek-lock)" trigger
2. Select your Service Bus namespace and topic
3. Enter your subscription name
4. **Important**: In the advanced options, set "Session enabled" to "Yes"

When configured this way, the Logic App will:
- Lock an entire session at once (not just individual messages)
- Process all messages in the session in order
- Maintain the session lock until processing is complete

This ensures that related quotes are processed sequentially while still allowing unrelated quotes (in different sessions) to be processed in parallel.

### Logic App Trigger Configuration (JSON)

For reference, here's how the trigger configuration appears in the Logic App JSON definition:

```json
"triggers": {
  "When_a_message_is_received_in_a_Service_Bus_topic_(peek-lock)": {
    "type": "ApiConnection",
    "inputs": {
      "host": {
        "connection": {
          "name": "@parameters('$connections')['servicebus']['connectionId']"
        }
      },
      "method": "get",
      "path": "/@{encodeURIComponent(encodeURIComponent('your-topic-name'))}/subscriptions/@{encodeURIComponent('your-subscription-name')}/messages/head/peek",
      "queries": {
        "sessionId": "",
        "sessionEnabled": true,
        "subscriptionType": "Main"
      }
    },
    "recurrence": {
      "frequency": "Minute",
      "interval": 1
    },
    "runtimeConfiguration": {
      "concurrency": {
        "runs": 1
      }
    }
  }
}
```

## Advanced Session Handling Patterns

### 1. Dynamic Session Processing with Node.js Worker Pool

For high-throughput scenarios, you might implement a more sophisticated worker pool in your NestJS application:

```typescript
import { Injectable, OnModuleInit } from '@nestjs/common';
import { ServiceBusClient, ServiceBusReceiver } from '@azure/service-bus';
import { Worker } from 'worker_threads';
import * as os from 'os';

@Injectable()
export class SessionProcessorService implements OnModuleInit {
  private sbClient: ServiceBusClient;
  private receiver: ServiceBusReceiver;
  private workerPool: Worker[] = [];
  
  async onModuleInit() {
    // Initialize Service Bus client
    this.sbClient = new ServiceBusClient(process.env.SERVICE_BUS_CONNECTION_STRING);
    
    // Create a session-enabled receiver
    this.receiver = this.sbClient.createReceiver(
      process.env.SERVICE_BUS_TOPIC_NAME,
      process.env.SERVICE_BUS_SUBSCRIPTION_NAME,
      { receiveMode: 'peekLock', sessionIdleTimeout: 5 * 60 * 1000 }
    );
    
    // Create worker pool for parallel session processing
    const numWorkers = Math.max(1, os.cpus().length - 1);
    for (let i = 0; i < numWorkers; i++) {
      this.startSessionWorker();
    }
  }
  
  private async startSessionWorker() {
    try {
      // Accept the next available session
      const sessionReceiver = await this.receiver.acceptNextSession();
      console.log(`Started processing session: ${sessionReceiver.sessionId}`);
      
      let messageHandled = false;
      do {
        // Try to receive a batch of messages from this session
        const messages = await sessionReceiver.receiveMessages(10, { maxWaitTimeInMs: 5000 });
        
        if (messages.length === 0) {
          // No more messages in this session
          break;
        }
        
        messageHandled = true;
        
        // Process messages sequentially within this session
        for (const message of messages) {
          try {
            // Process the message
            await this.processMessage(message.body);
            
            // Complete the message to remove it from the queue
            await sessionReceiver.completeMessage(message);
          } catch (error) {
            // Handle processing error
            console.error(`Error processing message: ${error}`);
            
            // Abandon the message to make it available again
            await sessionReceiver.abandonMessage(message);
          }
        }
      } while (messageHandled);
      
      // Close the session receiver
      await sessionReceiver.close();
      console.log(`Finished processing session: ${sessionReceiver.sessionId}`);
      
      // Start processing another session
      setImmediate(() => this.startSessionWorker());
    } catch (error) {
      console.error('Error in session worker:', error);
      
      // Retry after a delay
      setTimeout(() => this.startSessionWorker(), 5000);
    }
  }
  
  private async processMessage(messageBody: any) {
    // Implement your message processing logic here
    // This could call out to Salesforce or other business systems
    
    // For demonstration purposes
    console.log(`Processing message: ${JSON.stringify(messageBody)}`);
    await new Promise(resolve => setTimeout(resolve, 500)); // Simulate processing time
  }
}
```

### 2. Handling Session Timeouts and Recovery

In production systems, you'll need to handle session timeouts and recovery scenarios:

```typescript
// Add this to your session processor service
private async recoverAbandonedSessions() {
  try {
    // Get the list of sessions with messages
    const sessionIds = await this.sbClient.listSessionIds(
      process.env.SERVICE_BUS_TOPIC_NAME,
      process.env.SERVICE_BUS_SUBSCRIPTION_NAME
    );
    
    console.log(`Found ${sessionIds.length} sessions with pending messages`);
    
    // For each abandoned session, create a dedicated receiver
    for (const sessionId of sessionIds) {
      try {
        const sessionReceiver = await this.sbClient.createReceiver(
          process.env.SERVICE_BUS_TOPIC_NAME,
          process.env.SERVICE_BUS_SUBSCRIPTION_NAME,
          { 
            receiveMode: 'peekLock',
            sessionId: sessionId 
          }
        );
        
        console.log(`Recovering session: ${sessionId}`);
        
        // Process all messages in the session
        await this.processSession(sessionReceiver);
        
        // Close the receiver
        await sessionReceiver.close();
      } catch (error) {
        console.error(`Error recovering session ${sessionId}:`, error);
      }
    }
  } catch (error) {
    console.error('Error recovering abandoned sessions:', error);
  }
}
```

## Best Practices for Service Bus Sessions with Logic Apps

1. **Choose Session IDs Wisely**: Select a session ID strategy that naturally groups related messages without creating too many sessions (hundreds is fine, millions is problematic).

2. **Set Appropriate Timeouts**: Configure session timeouts based on your expected processing time, especially in Logic Apps where complex operations may take longer.

3. **Monitor Session Metrics**: Azure Portal provides session-specific metrics. Watch for:
   - Abandoned session count
   - Active session count
   - Session duration

4. **Implement Dead-Letter Handling**: Messages that repeatedly fail processing will go to the dead-letter queue. Implement a process to monitor and handle these exceptions.

5. **Consider Session State**: Service Bus allows storing session state, which can be useful for tracking processing progress across multiple messages in complex workflows.

By implementing sessions effectively in your quote-to-lead architecture, you ensure that related business transactions are processed in the correct order while still maintaining the flexibility and scalability benefits of a message-based architecture.

Would you like me to expand on any particular aspect of sessions implementation, or would you prefer more details on another area of your Service Bus and Logic Apps integration?