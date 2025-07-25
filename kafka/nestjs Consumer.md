Sure! Here's a NestJS Kafka consumer example that demonstrates:

How to consume messages from a Kafka topic

How to manually commit offsets

How to resume from the last committed offset on restart



---

✅ Step-by-Step NestJS Kafka Consumer Example

1. Install Required Packages

npm install @nestjs/microservices kafkajs


---

2. Kafka Consumer Module

// kafka-consumer.service.ts

import { Injectable, OnModuleInit, OnModuleDestroy } from '@nestjs/common';
import { Kafka, Consumer } from 'kafkajs';

@Injectable()
export class KafkaConsumerService implements OnModuleInit, OnModuleDestroy {
  private kafka: Kafka;
  private consumer: Consumer;

  async onModuleInit() {
    this.kafka = new Kafka({
      clientId: 'my-nestjs-consumer',
      brokers: ['localhost:9092'],
    });

    this.consumer = this.kafka.consumer({ groupId: 'my-consumer-group' });

    await this.consumer.connect();
    await this.consumer.subscribe({ topic: 'my-topic', fromBeginning: false });

    await this.consumer.run({
      autoCommit: false, // We will manually commit
      eachMessage: async ({ topic, partition, message, heartbeat, pause }) => {
        const key = message.key?.toString();
        const value = message.value?.toString();
        const offset = message.offset;

        console.log(`Received message: key=${key}, value=${value}, offset=${offset}`);

        try {
          // 👉 Your business logic here

          // ✅ Manually commit offset AFTER processing
          await this.consumer.commitOffsets([
            { topic, partition, offset: (Number(offset) + 1).toString() },
          ]);
        } catch (error) {
          console.error('Error processing message:', error);
          // ❌ Don't commit offset on error — message will be reprocessed
        }
      },
    });
  }

  async onModuleDestroy() {
    await this.consumer.disconnect();
  }
}


---

3. Register in App Module

// app.module.ts

import { Module } from '@nestjs/common';
import { KafkaConsumerService } from './kafka-consumer.service';

@Module({
  providers: [KafkaConsumerService],
})
export class AppModule {}


---

4. Kafka Configuration Notes

groupId: 'my-consumer-group': ensures Kafka tracks and stores offsets in __consumer_offsets.

fromBeginning: false: start from last committed offset (default behavior).

autoCommit: false: disables auto-commit; gives us control.

commitOffsets(...): manual commit after successful processing.



---

🔁 On Restart Behavior

The consumer joins my-consumer-group

Kafka looks up last committed offset in __consumer_offsets

Resumes reading from that offset



---

Let me know if you'd also like:

An example of auto-commit

Multiple topic/partition handling

Integration with a NestJS controller or event emitter


