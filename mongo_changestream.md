# We want to use Mongo change streams in a NestJS service to watch event collection to build READ models in domain collection based on the stream of changes from change stream. We are running multiple instances of service for high availability and also to share the changes across service instances in a balanced way so as to avoid same changes processed by more than one instance of service. But, we see changes are being sent to all service instances which results in duplicate processing . Is there a way to address this scenario .

You're encountering a common challenge when using MongoDB Change Streams in a distributed system: **ensuring that only one instance processes each change event** to avoid duplicate processing. MongoDB Change Streams are designed to be **broadcast** to all listeners, so without coordination, every instance will receive and process the same events.

Here are several strategies to address this:

---

### ✅ **1. Use a Distributed Message Queue (Recommended)**
Instead of having each service instance directly consume the change stream, use a **single consumer** to watch the change stream and **publish events to a message queue** like:

- **Kafka**
- **RabbitMQ**
- **Redis Streams**
- **AWS SQS**

Then, multiple service instances can **consume from the queue**, which supports **load balancing** and **at-least-once delivery** semantics.

---

### ✅ **2. Use a Leader Election Mechanism**
Implement a **leader election** strategy (e.g., using Redis, Zookeeper, or etcd) so that **only one instance** watches the change stream at a time. If the leader fails, another instance takes over.

Libraries:
- `node-zookeeper-client`
- `redis-lock`

---

### ✅ **3. Use MongoDB Change Stream with a Resume Token and Deduplication**
If you must allow multiple instances to listen to the change stream, you can:
- Store the **resume token** in a shared store (e.g., Redis or MongoDB).
- Implement **deduplication logic** using a unique event ID or `_id` of the change event.
- Use a **distributed lock** to ensure only one instance processes a given event.

---

### ✅ **4. Use MongoDB Change Stream with a Capped Collection**
You can write change events to a **capped collection** and have your services consume from it using a **tailable cursor**, which can be load-balanced more easily.

---

### Summary Table

| Strategy | Pros | Cons |
|---------|------|------|
| Message Queue | Scalable, reliable, built-in load balancing | Adds infrastructure |
| Leader Election | Simple, avoids duplicates | Single point of failure unless handled |
| Resume Token + Deduplication | No extra infra | Complex logic, risk of race conditions |
| Capped Collection | Native MongoDB | Less flexible, more setup |

---

# Why is the 2nd strategy a single point of failure and how could we handle that.

Great question! The **leader election strategy** can be very effective, but it does introduce a **potential single point of failure** if not implemented with proper failover mechanisms. Here's a breakdown:

---

### 🔍 Why It's a Single Point of Failure

In a basic leader election setup:
- Only **one instance** is elected as the leader.
- This leader is responsible for consuming the MongoDB change stream.
- If the leader **crashes**, **hangs**, or **loses connectivity**, no other instance will process the change stream until a new leader is elected.

Without automatic failover, this can lead to:
- **Missed events**
- **Delayed processing**
- **System downtime**

---

### ✅ How to Handle It Properly

To make leader election **resilient**, you can implement the following:

#### 1. **Heartbeat Monitoring**
Each instance periodically checks if the leader is alive (e.g., via a heartbeat key in Redis or etcd). If the heartbeat stops, a new election is triggered.

#### 2. **Automatic Re-election**
Use a distributed coordination system that supports automatic re-election:
- **Redis with Redlock**
- **etcd**
- **Zookeeper**

These systems can:
- Detect leader failure
- Elect a new leader quickly
- Ensure only one leader is active at a time

#### 3. **Short TTL on Leadership Lock**
When acquiring leadership, use a **short TTL (time-to-live)** on the lock. If the leader fails to renew the lock, it expires and another instance can take over.

#### 4. **Graceful Shutdown Hooks**
Ensure the leader releases its lock or updates its status on shutdown so others can take over immediately.

#### 5. **Fallback to Message Queue (Hybrid Model)**
Combine leader election with a message queue:
- Leader watches the change stream and publishes to a queue.
- Other instances consume from the queue.
- If the leader fails, another instance takes over and continues publishing.

---

### 🛠 Example with Redis (Simplified)

```ts
// Pseudo-code for acquiring leadership
const isLeader = await redis.set('change-stream-leader', instanceId, 'NX', 'EX', 10);
if (isLeader) {
  startWatchingChangeStream();
  setInterval(() => {
    redis.expire('change-stream-leader', 10); // Renew lock
  }, 5000);
}
```

If the leader fails to renew the lock, another instance can acquire it.

---


# Seems using a distributed message queue is better. How do I implement it using Kafka.

Using **Kafka** to decouple MongoDB Change Streams from your NestJS service instances is a robust and scalable solution. Here's how you can implement it step-by-step:

---

## ✅ Architecture Overview

1. **Change Stream Listener Service**:
   - A single service (or leader-elected instance) listens to MongoDB change streams.
   - It publishes change events to a Kafka topic.

2. **Worker Services**:
   - Multiple NestJS service instances consume from the Kafka topic.
   - Kafka ensures **load-balanced** delivery (each message is processed by only one consumer in a group).

---

## 🧱 Step-by-Step Implementation

### 1. **Set Up Kafka**

You can use:
- **Apache Kafka** (self-hosted or on Kubernetes)
- **Confluent Cloud** (managed Kafka)
- **Redpanda** (Kafka-compatible, lightweight)

---

### 2. **Install Kafka Packages in NestJS**

```bash
npm install @nestjs/microservices kafkajs
```

---

### 3. **Change Stream Listener Service**

This service watches MongoDB and publishes to Kafka:

```ts
// mongo-change-stream.service.ts
import { Kafka } from 'kafkajs';
import { Injectable, OnModuleInit } from '@nestjs/common';
import { MongoClient } from 'mongodb';

@Injectable()
export class MongoChangeStreamService implements OnModuleInit {
  private kafka = new Kafka({ brokers: ['localhost:9092'] });
  private producer = this.kafka.producer();

  async onModuleInit() {
    await this.producer.connect();

    const client = new MongoClient('mongodb://localhost:27017');
    await client.connect();
    const db = client.db('your-db');
    const collection = db.collection('events');

    const changeStream = collection.watch();

    changeStream.on('change', async (change) => {
      await this.producer.send({
        topic: 'mongo-events',
        messages: [{ value: JSON.stringify(change) }],
      });
    });
  }
}
```

---

### 4. **Kafka Consumer in Worker Services**

Each instance consumes from Kafka and processes the event:

```ts
// kafka.consumer.ts
import { Controller } from '@nestjs/common';
import { MessagePattern, Payload } from '@nestjs/microservices';

@Controller()
export class KafkaConsumerController {
  @MessagePattern('mongo-events')
  async handleMongoEvent(@Payload() message: any) {
    const change = JSON.parse(message.value);
    // Process the change event and update your READ model
  }
}
```

---

### 5. **Configure Kafka Microservice in NestJS**

```ts
// main.ts
import { NestFactory } from '@nestjs/core';
import { AppModule } from './app.module';
import { MicroserviceOptions, Transport } from '@nestjs/microservices';

async function bootstrap() {
  const app = await NestFactory.create(AppModule);

  app.connectMicroservice<MicroserviceOptions>({
    transport: Transport.KAFKA,
    options: {
      client: {
        brokers: ['localhost:9092'],
      },
      consumer: {
        groupId: 'read-model-updaters', // All instances share this group
      },
    },
  });

  await app.startAllMicroservices();
  await app.listen(3000);
}
bootstrap();
```

---

## ✅ Benefits of This Setup

- **Scalable**: Add more consumers to scale processing.
- **Reliable**: Kafka ensures message durability and delivery guarantees.
- **Decoupled**: MongoDB change stream logic is separated from processing logic.

---

# With this strategy, should we run multiple instances of listener service for HA and if so which instance would be tasked with publishing to Kafka.

Yes, you're absolutely right to think about **high availability (HA)** for the **MongoDB Change Stream listener service**. Here's how to approach it:

---

## ✅ Should You Run Multiple Listener Instances?

Yes — **for high availability**, you should run **multiple instances** of the listener service. However, **only one instance should actively publish to Kafka** at any given time to avoid duplicate events.

---

## 🧠 How to Ensure Only One Instance Publishes?

You need a **leader election mechanism** among the listener instances. Here's how it works:

### 🔁 Leader Election + Failover

1. **All listener instances** start up and attempt to acquire a **distributed lock** (e.g., using Redis, Zookeeper, or etcd).
2. The instance that acquires the lock becomes the **active leader** and starts:
   - Watching the MongoDB change stream
   - Publishing events to Kafka
3. Other instances stay in **standby mode**, periodically checking if the leader is still alive.
4. If the leader fails (e.g., crashes or loses the lock), another instance takes over.

---

## 🛠 Example with Redis (Redlock)

```ts
import Redlock from 'redlock';
import Redis from 'ioredis';

const redis = new Redis();
const redlock = new Redlock([redis], {
  retryCount: -1, // retry forever
  retryDelay: 2000,
});

async function startLeaderElection() {
  try {
    const lock = await redlock.acquire(['locks:change-stream-leader'], 10000); // 10s TTL

    console.log('This instance is the leader');
    startWatchingMongoAndPublishingToKafka();

    // Renew lock periodically
    setInterval(async () => {
      try {
        await lock.extend(10000);
      } catch {
        console.log('Lost leadership');
        process.exit(1); // or restart election
      }
    }, 5000);
  } catch (err) {
    console.log('Not the leader, retrying...');
    setTimeout(startLeaderElection, 5000);
  }
}
```

---

## ✅ Summary

| Component | Role |
|----------|------|
| **Multiple Listener Instances** | Ensure HA |
| **Leader Election (e.g., Redis Redlock)** | Ensures only one instance publishes to Kafka |
| **Kafka Topic** | Receives events from the leader |
| **Worker Services (Consumers)** | Load-balanced processing of events |

---

# Yes. provide me a working NestJS example.but, I am concerned we now have more things to manage like Redis and Kafka in addition to mongodb
Here's a working **NestJS example** that demonstrates:

- **MongoDB Change Stream listening**
- **Redis-based leader election using Redlock**
- **Kafka integration for event publishing and consumption**

📦 You can download the complete project here:  
[nestjs-kafka-redis.zip](blob:https://m365.cloud.microsoft/26c492b2-e8f2-4c65-8269-fa025d8ed977)

---

## 📁 Project Structure

```
nestjs-kafka-redis/
└── src/
    ├── main.ts                  # Bootstraps the NestJS app and Kafka microservice
    ├── app.module.ts           # Registers services and controllers
    ├── mongo-change-stream.service.ts  # Leader election + MongoDB change stream + Kafka producer
    └── kafka.consumer.ts       # Kafka consumer to process events
```

---

## 🧩 What You'll Need to Run It

- **MongoDB** (with replica set enabled for change streams)
- **Kafka** (e.g., Apache Kafka or Confluent)
- **Redis** (for distributed locking)
- Node.js and NestJS CLI

---

## 🧠 About Your Concern: More Infrastructure

You're absolutely right — this setup introduces **Kafka and Redis** in addition to MongoDB. Here's how to think about it:

| Component | Purpose | Alternatives |
|----------|---------|--------------|
| **Kafka** | Reliable, scalable event distribution | RabbitMQ, Redis Streams, SQS |
| **Redis** | Lightweight leader election and locking | Zookeeper, etcd, Consul |
| **MongoDB** | Source of truth for change events | — |

If you're looking for a **simpler setup**, you could:
- Use **Redis Streams** instead of Kafka (less infra, but less scalable).
- Use **a single listener with retry logic** (no HA, but simpler).







