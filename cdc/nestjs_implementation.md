Yes, you can implement a common abstraction framework for Change Data Capture (CDC) using **NestJS** that works across **MongoDB Atlas** and **Azure Cosmos DB for NoSQL API**. NestJS is a powerful Node.js framework that supports dependency injection, modular architecture, and TypeScript, making it an excellent choice for building such a framework.

Below is a step-by-step guide to achieve this in NestJS:

---

### **1. Set Up a NestJS Project**
1. Install NestJS CLI:
   ```bash
   npm install -g @nestjs/cli
   ```
2. Create a new NestJS project:
   ```bash
   nest new cdc-framework
   ```
3. Install required dependencies:
   ```bash
   npm install @nestjs/mongoose mongoose @nestjs/config @azure/cosmos
   ```

---

### **2. Define Configuration**
Use the `@nestjs/config` package to manage configuration. Create a `.env` file:
```env
DATABASE_TYPE=mongodb # or cosmosdb
MONGODB_URI=mongodb+srv://<username>:<password>@cluster0.mongodb.net/<dbname>?retryWrites=true&w=majority
COSMOSDB_URI=<cosmosdb_connection_string>
DATABASE_NAME=testdb
COLLECTION_NAME=testcollection
```

Load the configuration in `app.module.ts`:
```typescript
import { Module } from '@nestjs/common';
import { ConfigModule } from '@nestjs/config';

@Module({
  imports: [
    ConfigModule.forRoot({
      isGlobal: true,
    }),
  ],
})
export class AppModule {}
```

---

### **3. Create a Common CDC Interface**
Define a common interface for CDC operations in `src/cdc/cdc.interface.ts`:
```typescript
export interface CDCInterface {
  connect(): Promise<void>;
  listenForChanges(callback: (change: any) => void): Promise<void>;
  close(): Promise<void>;
}
```

---

### **4. Implement Database-Specific Classes**
#### **MongoDB Implementation**
Create a MongoDB service in `src/cdc/mongodb.service.ts`:
```typescript
import { Injectable } from '@nestjs/common';
import { InjectModel } from '@nestjs/mongoose';
import { Model } from 'mongoose';
import { CDCInterface } from './cdc.interface';
import { ConfigService } from '@nestjs/config';

@Injectable()
export class MongoDBService implements CDCInterface {
  private changeStream: any;

  constructor(
    @InjectModel('Change') private readonly changeModel: Model<any>,
    private readonly configService: ConfigService,
  ) {}

  async connect(): Promise<void> {
    // Connection is handled by MongooseModule in the module
  }

  async listenForChanges(callback: (change: any) => void): Promise<void> {
    this.changeStream = this.changeModel.watch();
    this.changeStream.on('change', (change) => callback(change));
  }

  async close(): Promise<void> {
    if (this.changeStream) {
      this.changeStream.close();
    }
  }
}
```

#### **Cosmos DB Implementation**
Create a Cosmos DB service in `src/cdc/cosmosdb.service.ts`:
```typescript
import { Injectable } from '@nestjs/common';
import { CosmosClient } from '@azure/cosmos';
import { CDCInterface } from './cdc.interface';
import { ConfigService } from '@nestjs/config';

@Injectable()
export class CosmosDBService implements CDCInterface {
  private client: CosmosClient;
  private container: any;

  constructor(private readonly configService: ConfigService) {}

  async connect(): Promise<void> {
    this.client = new CosmosClient(this.configService.get('COSMOSDB_URI'));
    const database = this.client.database(this.configService.get('DATABASE_NAME'));
    this.container = database.container(this.configService.get('COLLECTION_NAME'));
  }

  async listenForChanges(callback: (change: any) => void): Promise<void> {
    const iterator = this.container.items.readChangeFeed();
    for await (const change of iterator) {
      callback(change);
    }
  }

  async close(): Promise<void> {
    // Cosmos DB client does not require explicit closing
  }
}
```

---

### **5. Use a Factory Pattern**
Create a factory to instantiate the appropriate service based on the configuration in `src/cdc/cdc.factory.ts`:
```typescript
import { Injectable } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import { MongoDBService } from './mongodb.service';
import { CosmosDBService } from './cosmosdb.service';
import { CDCInterface } from './cdc.interface';

@Injectable()
export class CDCFactory {
  constructor(
    private readonly configService: ConfigService,
    private readonly mongoDBService: MongoDBService,
    private readonly cosmosDBService: CosmosDBService,
  ) {}

  getCDCInstance(): CDCInterface {
    const databaseType = this.configService.get('DATABASE_TYPE');
    if (databaseType === 'mongodb') {
      return this.mongoDBService;
    } else if (databaseType === 'cosmosdb') {
      return this.cosmosDBService;
    } else {
      throw new Error('Unsupported database type');
    }
  }
}
```

---

### **6. Create a CDC Module**
Define a module for CDC in `src/cdc/cdc.module.ts`:
```typescript
import { Module } from '@nestjs/common';
import { MongooseModule } from '@nestjs/mongoose';
import { ConfigService } from '@nestjs/config';
import { MongoDBService } from './mongodb.service';
import { CosmosDBService } from './cosmosdb.service';
import { CDCFactory } from './cdc.factory';

@Module({
  imports: [
    MongooseModule.forRootAsync({
      useFactory: (configService: ConfigService) => ({
        uri: configService.get('MONGODB_URI'),
      }),
      inject: [ConfigService],
    }),
  ],
  providers: [MongoDBService, CosmosDBService, CDCFactory],
  exports: [CDCFactory],
})
export class CDCModule {}
```

---

### **7. Use the CDC Service**
Inject the CDC service into your application logic. For example, in `src/app.service.ts`:
```typescript
import { Injectable } from '@nestjs/common';
import { CDCFactory } from './cdc/cdc.factory';

@Injectable()
export class AppService {
  constructor(private readonly cdcFactory: CDCFactory) {}

  async startCDC() {
    const cdcInstance = this.cdcFactory.getCDCInstance();
    await cdcInstance.connect();
    await cdcInstance.listenForChanges((change) => {
      console.log('Change detected:', change);
    });
  }
}
```

---

### **8. Start the Application**
In `src/main.ts`, start the CDC process:
```typescript
import { NestFactory } from '@nestjs/core';
import { AppModule } from './app.module';
import { AppService } from './app.service';

async function bootstrap() {
  const app = await NestFactory.create(AppModule);
  const appService = app.get(AppService);
  await appService.startCDC();
  await app.listen(3000);
}
bootstrap();
```

---

### **9. Test and Extend**
- Test the framework with both MongoDB Atlas and Azure Cosmos DB.
- Extend the framework to handle advanced features like filtering, batching, or integrating with message brokers.

---

By following this approach, you can build a configuration-driven, modular CDC framework in NestJS that works seamlessly across MongoDB Atlas and Azure Cosmos DB for NoSQL API.