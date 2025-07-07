To create a **Kafka consumer using the NestJS framework**, you'll need to use the `@nestjs/microservices` module, which provides built-in support for Kafka.

Here’s a step-by-step guide and code example:

---

### 📦 **Install Required Packages**

```bash
npm install @nestjs/microservices kafkajs
```

---

### 🧱 **Step 1: Create a Kafka Consumer Service**

```ts
// kafka-consumer.service.ts
import { Injectable, OnModuleInit } from '@nestjs/common';
import { Kafka, Consumer } from 'kafkajs';
import { MongoClient } from 'mongodb';

@Injectable()
export class KafkaConsumerService implements OnModuleInit {
  private kafka: Kafka;
  private consumer: Consumer;
  private mongoClient: MongoClient;

  async onModuleInit() {
    this.kafka = new Kafka({
      clientId: 'nestjs-consumer',
      brokers: ['localhost:9092'],
    });

    this.consumer = this.kafka.consumer({ groupId: 'nestjs-group' });
    await this.consumer.connect();
    await this.consumer.subscribe({ topic: 'events_data', fromBeginning: true });

    this.mongoClient = new MongoClient('mongodb://localhost:27017');
    await this.mongoClient.connect();
    const db = this.mongoClient.db('your_database');
    const collection = db.collection('domain_data');

    await this.consumer.run({
      eachMessage: async ({ topic, partition, message }) => {
        const value = message.value?.toString();
        if (!value) return;

        const data = JSON.parse(value);
        console.log(`Received message:`, data);

        // Upsert into MongoDB
        await collection.updateOne(
          { id: data.id },
          { $set: data },
          { upsert: true }
        );
      },
    });
  }
}
```

---

### 🧱 **Step 2: Register the Service in a Module**

```ts
// app.module.ts
import { Module } from '@nestjs/common';
import { KafkaConsumerService } from './kafka-consumer.service';

@Module({
  providers: [KafkaConsumerService],
})
export class AppModule {}
```

---

### 🧠 Notes

- This consumer runs when the NestJS app starts.
- It connects to Kafka and MongoDB, listens to the `events_data` topic, and upserts into the `domain_data` collection.
- Make sure MongoDB is running and accessible.

---

