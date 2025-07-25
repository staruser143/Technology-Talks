# ✅ What is Auto-Commit?

* Auto-commit means the Kafka consumer automatically commits offsets of records it has read, at a regular interval.
* This allows Kafka to remember where the consumer left off, so it can resume from the correct position on restart.


## 🔧 Key Configurations

Property	                       | Default	| Description

enable.auto.commit	| true	| Automatically commit the offset after consuming

auto.commit.interval.ms	| 5000	 | Interval (in milliseconds) to commit latest offset

* So by default, Kafka commits offsets every 5 seconds.
---

### ⚙️ How Auto-Commit Works Internally

* 1. The consumer fetches messages from Kafka.
* 2. After messages are delivered to your eachMessage() handler, Kafka keeps track of the highest offset processed.
* 3. Every auto.commit.interval.ms, it sends the last processed offset to the __consumer_offsets topic.
* 4. On consumer restart, Kafka resumes from the last committed offset.

# ⚠️ Important Caveat: At-Least-Once Delivery

* If your app crashes after processing a message but before the next auto-commit, that message will be re-delivered when the app restarts.
* So auto-commit provides at-least-once delivery, not exactly-once.


---

# ✅ NestJS Example with Auto-Commit Enabled

// kafka-auto-commit.service.ts

import { Injectable, OnModuleInit, OnModuleDestroy } from '@nestjs/common';
import { Kafka, Consumer } from 'kafkajs';

@Injectable()
export class KafkaAutoCommitService implements OnModuleInit, OnModuleDestroy {
  private kafka: Kafka;
  private consumer: Consumer;

  async onModuleInit() {
    this.kafka = new Kafka({
      clientId: 'auto-commit-consumer',
      brokers: ['localhost:9092'],
    });

    this.consumer = this.kafka.consumer({
      groupId: 'auto-commit-group',
      allowAutoTopicCreation: true,
    });

    await this.consumer.connect();
    await this.consumer.subscribe({ topic: 'my-topic', fromBeginning: false });

    await this.consumer.run({
      // ⚠️ Default: autoCommit = true
      eachMessage: async ({ topic, partition, message }) => {
        const value = message.value?.toString();
        const offset = message.offset;

        console.log(`Auto-Commit: Received message: ${value} (offset ${offset})`);

        // No need to commit manually; offset will be committed every 5 seconds
      },
    });
  }

  async onModuleDestroy() {
    await this.consumer.disconnect();
  }
}


---

# 🆚 Auto-Commit vs Manual Commit

Feature	            | Auto-Commit            | 	Manual Commit
Ease	               ✅ Simpler	                    ❌ More control needed
Reliability	         ⚠️ Risk of reprocessing	      ✅ More control over when to commit
Delivery Guarantee	 At-least-once	                Can be tuned for at-most or exactly-once
Crash Safety	       Risk of reprocessing messages	Better guarantees if used properly

---

# 📌 Summary
* Auto-commit is convenient for simple use cases.
* For critical systems where message processing must be reliable, prefer manual commit.
* In NestJS using kafkajs, auto-commit is the default behavior unless you override it with autoCommit: false.
