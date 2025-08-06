Absolutely! Below is a **complete working example** of a **NestJS Kafka consumer** that consumes **Avro-encoded messages** from a Kafka topic using **Confluent Schema Registry** for schema resolution and deserialization.

We’ll use:
- `@nestjs/microservices` with `kafkajs`
- `kafkajs` + `kafka-avro` (or manual Avro decoding via `avsc`)
- Confluent Schema Registry
- Docker setup (optional)

---

## 🧰 Prerequisites

Ensure you have:
- Node.js (16+)
- Docker & Docker Compose (for local Kafka + Schema Registry)

---

## 📦 1. Install Dependencies

```bash
npm install @nestjs/microservices kafkajs kafka-avro
npm install --save-dev @types/kafkajs
```

> ⚠️ Note: `kafka-avro` is a lightweight wrapper for Schema Registry + Avro. Alternatively, use `avsc` directly with manual schema fetching.

---

## 🐳 2. Docker Setup (Kafka + Schema Registry)

Create `docker-compose.yml`:

```yaml
version: '3.8'
services:
  zookeeper:
    image: confluentinc/cp-zookeeper:latest
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181

  kafka:
    image: confluentinc/cp-kafka:latest
    depends_on:
      - zookeeper
    ports:
      - "9092:9092"
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: PLAINTEXT:PLAINTEXT,PLAINTEXT_INTERNAL:PLAINTEXT
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092,PLAINTEXT_INTERNAL://kafka:9093
      KAFKA_LISTENERS: PLAINTEXT://0.0.0.0:9092,PLAINTEXT_INTERNAL://0.0.0.0:9093
      KAFKA_INTER_BROKER_LISTENER_NAME: PLAINTEXT_INTERNAL
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1

  schema-registry:
    image: confluentinc/cp-schema-registry:latest
    depends_on:
      - kafka
    ports:
      - "8081:8081"
    environment:
      SCHEMA_REGISTRY_KAFKASTORE_CONNECTION_URL: zookeeper:2181
      SCHEMA_REGISTRY_HOST_NAME: schema-registry
      SCHEMA_REGISTRY_LISTENERS: http://0.0.0.0:8081
```

Start it:

```bash
docker-compose up -d
```

---

## 📁 3. Project Structure

```
src/
├── main.ts
├── app.controller.ts
├── avro.service.ts          <-- Handles Avro decoding
├── kafka.consumer.service.ts <-- Kafka consumer logic
└── app.module.ts
```

---

## 🧩 4. Avro Service (Decode Messages)

`src/avro.service.ts`

```ts
import { Injectable } from '@nestjs/common';
import { SchemaRegistry, readAVSC } from 'kafka-avro';

@Injectable()
export class AvroService {
  private schemaRegistry: SchemaRegistry;

  constructor() {
    this.schemaRegistry = new SchemaRegistry({
      host: 'http://localhost:8081', // Schema Registry URL
    });
  }

  async decode(buffer: Buffer): Promise<any> {
    try {
      const decoded = await this.schemaRegistry.decode(buffer);
      return decoded;
    } catch (err) {
      console.error('Avro decode error:', err.message);
      throw err;
    }
  }
}
```

> ✅ This automatically fetches the schema ID from the buffer and resolves the schema.

---

## 📥 5. Kafka Consumer Service

`src/kafka.consumer.service.ts`

```ts
import { Injectable, Logger } from '@nestjs/common';
import { ClientKafka, Ctx, EventPattern, Payload, KafkaContext } from '@nestjs/microservices';
import { AvroService } from './avro.service';

@Injectable()
export class KafkaConsumerService {
  private readonly logger = new Logger(KafkaConsumerService.name);

  constructor(private readonly avroService: AvroService) {}

  @EventPattern('user.created') // Topic name
  async consumeUserCreated(@Payload() data: any, @Ctx() context: KafkaContext) {
    const message = context.getMessage();
    const { topic, partition, offset } = message;

    try {
      // Decode Avro message
      const event = await this.avroService.decode(message.value);

      this.logger.log(`Consumed event:
        Topic: ${topic}
        Partition: ${partition}
        Offset: ${offset}
        Event: ${JSON.stringify(event)}
      `);

      // Process event (e.g., send email, update DB)
      await this.handleUserCreated(event);
    } catch (err) {
      this.logger.error(`Failed to process message at offset ${offset}:`, err);
      // You can requeue, DLQ, or crash & restart
    }
  }

  private async handleUserCreated(event: any) {
    console.log('Handling user creation:', event.email);
    // Your business logic here
  }
}
```

---

## 🏗 6. App Module

`src/app.module.ts`

```ts
import { Module } from '@nestjs/common';
import { ClientsModule, Transport } from '@nestjs/microservices';
import { KafkaConsumerService } from './kafka.consumer.service';
import { AvroService } from './avro.service';

@Module({
  imports: [
    ClientsModule.register([
      {
        name: 'KAFKA_CLIENT',
        transport: Transport.KAFKA,
        options: {
          client: {
            clientId: 'nestjs-consumer',
            brokers: ['localhost:9092'],
          },
          consumer: {
            groupId: 'notification-service-group', // Must be unique per consumer group
          },
        },
      },
    ]),
  ],
  providers: [KafkaConsumerService, AvroService],
})
export class AppModule {}
```

---

## ▶️ 7. Main App

`src/main.ts`

```ts
import { NestFactory } from '@nestjs/core';
import { MicroserviceOptions, Transport } from '@nestjs/microservices';
import { AppModule } from './app.module';

async function bootstrap() {
  const app = await NestFactory.create(AppModule);

  // Start as a microservice (Kafka consumer)
  await app.connectMicroservice<MicroserviceOptions>({
    transport: Transport.KAFKA,
    options: {
      client: {
        clientId: 'nestjs-consumer',
        brokers: ['localhost:9092'],
      },
      consumer: {
        groupId: 'notification-service-group',
      },
    },
  });

  app.startAllMicroservices();
  console.log('NestJS Kafka consumer is running...');
}
bootstrap();
```

---

## 🧪 8. Test It (Send an Avro Message)

Use `kafka-avro` or `confluent-kafka` to produce a test message.

### Example Producer (Node.js)

```ts
// producer.test.ts
import { Kafka } from 'kafkajs';
import { SchemaRegistry } from 'kafka-avro';

const kafka = new Kafka({ brokers: ['localhost:9092'] });
const producer = kafka.producer();

const schemaRegistry = new SchemaRegistry({ host: 'http://localhost:8081' });

const run = async () => {
  await producer.connect();

  const schema = await schemaRegistry.getLatestSchema('user.created-value');
  const encoded = await schemaRegistry.encode(schema.id, {
    userId: '123',
    email: 'alice@example.com',
    timestamp: Date.now(),
  });

  await producer.send({
    topic: 'user.created',
    messages: [{ value: encoded }],
  });

  console.log('Message sent');
  await producer.disconnect();
};

run().catch(console.error);
```

> Make sure the schema is registered under `user.created-value` in Schema Registry.

---

## 📝 Schema Example (Avro)

Register this schema in Schema Registry under subject `user.created-value`:

```json
{
  "type": "record",
  "name": "UserCreated",
  "fields": [
    { "name": "userId", "type": "string" },
    { "name": "email", "type": "string" },
    { "name": "timestamp", "type": "long" }
  ]
}
```

You can register it via:

```bash
curl -X POST http://localhost:8081/subjects/user.created-value/versions \
  -H "Content-Type: application/vnd.schemaregistry.v1+json" \
  -d '{
    "schema": "{\"type\":\"record\",\"name\":\"UserCreated\",\"fields\":[{\"name\":\"userId\",\"type\":\"string\"},{\"name\":\"email\",\"type\":\"string\"},{\"name\":\"timestamp\",\"type\":\"long\"}]}"
  }'
```

---

## ✅ Final Notes

- This setup works with **Debezium CDC** too — just change the topic name and schema.
- For production, add:
  - Logging to ELK/Splunk
  - OpenTelemetry tracing
  - DLQ handling
  - Consumer health checks
- Use environment variables for config (e.g., schema registry URL, brokers)

---

## 🚀 Run It

```bash
# Start Kafka stack
docker-compose up -d

# Start NestJS app
npm run start

# (Optional) Run producer test
ts-node producer.test.ts
```

You should see:
```
NestJS Kafka consumer is running...
[Nest] INFO ... Consumed event: Topic: user.created ...
Handling user creation: alice@example.com
```

---

Let me know if you want:
- A GitHub-ready repo structure
- Integration with Debezium PostgreSQL
- Error handling + DLQ with another Kafka topic
- NestJS + gRPC + Kafka combo

Happy coding! 🚀