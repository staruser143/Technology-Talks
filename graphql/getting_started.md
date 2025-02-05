Getting started with GraphQL in a NestJS project is a great choice! It offers a lot of flexibility and robust performance for building APIs. Here's a step-by-step guide to help you kick things off:

### Step 1: Setting Up Your NestJS Project
First, if you haven't already, install the NestJS CLI and create a new NestJS project:

```bash
npm i -g @nestjs/cli
nest new my-nest-graphql-project
cd my-nest-graphql-project
```

### Step 2: Adding GraphQL Dependencies
Next, you'll need to add the necessary GraphQL dependencies:

```bash
npm install @nestjs/graphql @nestjs/apollo graphql apollo-server-express
```

### Step 3: Configuring GraphQL Module
Create a new file called `graphql.module.ts` and set up the GraphQLModule:

```typescript
import { Module } from '@nestjs/common';
import { GraphQLModule } from '@nestjs/graphql';
import { ApolloDriver, ApolloDriverConfig } from '@nestjs/apollo';
import { join } from 'path';

@Module({
  imports: [
    GraphQLModule.forRoot<ApolloDriverConfig>({
      driver: ApolloDriver,
      autoSchemaFile: join(process.cwd(), 'src/schema.gql'),
    }),
  ],
})
export class GraphqlModule {}
```

### Step 4: Creating Your First Resolver and Schema
Create a new directory `graphql` and within it create `hello.resolver.ts` and `hello.schema.ts` files:

`hello.resolver.ts`:
```typescript
import { Resolver, Query } from '@nestjs/graphql';

@Resolver()
export class HelloResolver {
  @Query(() => String)
  sayHello(): string {
    return 'Hello, world!';
  }
}
```

`hello.schema.ts`:
```typescript
import { Module } from '@nestjs/common';
import { HelloResolver } from './hello.resolver';

@Module({
  providers: [HelloResolver],
})
export class HelloModule {}
```

### Step 5: Integrating the Resolver
Update your `app.module.ts` to import the newly created GraphQL module and resolver module:

```typescript
import { Module } from '@nestjs/common';
import { GraphqlModule } from './graphql/graphql.module';
import { HelloModule } from './graphql/hello.module';

@Module({
  imports: [GraphqlModule, HelloModule],
})
export class AppModule {}
```

### Step 6: Running Your Application
Run your application using:

```bash
npm run start
```

Navigate to `http://localhost:3000/graphql` in your browser, where you can use the GraphQL Playground to test your query:

```graphql
query {
  sayHello
}
```

You should see the response: `"Hello, world!"`


**What are feature flags in NestJS**
We can use feature flags, guards, interceptors, and middleware with GraphQL in NestJS. Each of these concepts is supported by NestJS and can be integrated seamlessly into your GraphQL application. Here's a brief overview of how you can use each of them:

### Feature Flags
Feature flags allow you to enable or disable features in your application without deploying new code. You can create a custom decorator to implement feature flags.

```typescript
import { SetMetadata, applyDecorators } from '@nestjs/common';

export const FeatureFlag = (flag: string) => applyDecorators(SetMetadata('feature-flag', flag));

// Usage in a resolver
import { UseGuards } from '@nestjs/common';
import { FeatureFlagGuard } from './feature-flag.guard';

@Resolver()
export class HelloResolver {
  @Query(() => String)
  @FeatureFlag('hello-feature')
  @UseGuards(FeatureFlagGuard)
  sayHello(): string {
    return 'Hello, world!';
  }
}
```

### Guards
Guards are used to control access to parts of your application.

```typescript
import { Injectable, CanActivate, ExecutionContext } from '@nestjs/common';
import { Reflector } from '@nestjs/core';

@Injectable()
export class FeatureFlagGuard implements CanActivate {
  constructor(private reflector: Reflector) {}

  canActivate(context: ExecutionContext): boolean {
    const flag = this.reflector.get<string>('feature-flag', context.getHandler());
    // Your logic to check if the feature flag is enabled
    return isFeatureFlagEnabled(flag);
  }
}
```

### Interceptors
Interceptors can transform the result returned by a resolver or modify the behavior before the request is completed.

```typescript
import { CallHandler, ExecutionContext, Injectable, NestInterceptor } from '@nestjs/common';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

@Injectable()
export class LoggingInterceptor implements NestInterceptor {
  intercept(context: ExecutionContext, next: CallHandler): Observable<any> {
    console.log('Before...');
    const now = Date.now();
    return next
      .handle()
      .pipe(
        map(data => {
          console.log(`After... ${Date.now() - now}ms`);
          return data;
        }),
      );
  }
}
```

### Middleware
Middleware can be used to perform actions before the GraphQL request reaches the resolver.

```typescript
import { Injectable, NestMiddleware } from '@nestjs/common';
import { Request, Response, NextFunction } from 'express';

@Injectable()
export class LoggerMiddleware implements NestMiddleware {
  use(req: Request, res: Response, next: NextFunction) {
    console.log('Request...');
    next();
  }
}

// Apply middleware in the main.ts or any module
import { NestFactory } from '@nestjs/core';
import { AppModule } from './app.module';
import { LoggerMiddleware } from './logger.middleware';

async function bootstrap() {
  const app = await NestFactory.create(AppModule);
  app.use(LoggerMiddleware);
  await app.listen(3000);
}
bootstrap();
```

By combining these powerful features, you can create a robust and flexible GraphQL API with NestJS. 

**Can we have a unified schema for both Comos DB and Graph/ql**

Yes, you can have a unified schema for both Azure Cosmos DB (MongoDB API) and GraphQL. Here's how you can achieve this:

### Step 1: Define Your Unified Schema
Create a GraphQL schema that represents your data model. This schema will be used by both your GraphQL API and your document-based data store.

```graphql
type TodoItem {
  id: ID!
  title: String!
  completed: Boolean!
}

input CreateTodoItemInput {
  id: ID!
  title: String!
  completed: Boolean!
}

input ReplaceTodoItemInput {
  id: ID!
  title: String!
  completed: Boolean!
}

type Query {
  todoItems: [TodoItem]
  todoItem(id: ID!): TodoItem
}

type Mutation {
  createTodoItem(input: CreateTodoItemInput!): TodoItem!
  replaceTodoItem(input: ReplaceTodoItemInput!): TodoItem!
  deleteTodoItem(id: ID!): Boolean
}
```

### Step 2: Configure Resolvers for Cosmos DB
Use Azure Cosmos DB's GraphQL resolvers to map your GraphQL queries to the data stored in Cosmos DB. You can configure these resolvers in Azure API Management.

### Step 3: Integrate with Azure API Management
Use Azure API Management to expose your GraphQL API. You can define resolvers for Cosmos DB and other data sources within the API Management service.

### Step 4: Connect Your Application
Your application can now interact with the unified GraphQL API, which in turn communicates with Azure Cosmos DB using the MongoDB API. This setup allows you to manage your data using a single GraphQL schema while leveraging the flexibility of Cosmos DB.

By following these steps, you can create a unified schema that works seamlessly with both your GraphQL API and Azure Cosmos DB. This approach simplifies data management and ensures consistency across your application.



### Collections in Azure Cosmos DB (MongoDB API)
When working with Azure Cosmos DB for MongoDB API, you typically define collections that store documents. Each document in a collection can have a different structure, but it's a good practice to follow a consistent schema.

### Creating Collections
You can create collections either manually through the Azure portal or programmatically using the MongoDB driver. For example, using the MongoDB driver in your Node.js application, you can create a collection and insert documents:

```javascript
const { MongoClient } = require('mongodb');
const uri = "your_cosmos_db_connection_string";

async function run() {
  const client = new MongoClient(uri);
  try {
    await client.connect();
    const database = client.db('your_database_name');
    const collection = database.collection('todoItems');

    // Insert a document
    const doc = { id: "1", title: "First Todo", completed: false };
    await collection.insertOne(doc);
  } finally {
    await client.close();
  }
}

run().catch(console.dir);
```

### Unified Schema with GraphQL
The GraphQL schema you define is used to validate and structure the data when interacting with the API. This schema does not automatically enforce the same structure in your Cosmos DB collections, but you can use it to guide your data model.

### Connecting GraphQL Resolvers to Cosmos DB
In your GraphQL resolvers, you can interact with Cosmos DB using the same schema. Here's an example of how you might do this:

```typescript
import { Resolver, Query, Mutation, Args } from '@nestjs/graphql';
import { MongoClient } from 'mongodb';

const uri = "your_cosmos_db_connection_string";
const client = new MongoClient(uri);

@Resolver()
export class TodoResolver {
  constructor() {
    client.connect();
  }

  @Query(() => [TodoItem])
  async todoItems() {
    const database = client.db('your_database_name');
    const collection = database.collection('todoItems');
    return await collection.find().toArray();
  }

  @Mutation(() => TodoItem)
  async createTodoItem(@Args('input') input: CreateTodoItemInput) {
    const database = client.db('your_database_name');
    const collection = database.collection('todoItems');
    await collection.insertOne(input);
    return input;
  }
}
```

In this example:
- The `todoItems` query fetches all documents from the `todoItems` collection.
- The `createTodoItem` mutation inserts a new document into the `todoItems` collection based on the GraphQL input schema.

### Summary
While the GraphQL schema defines the structure of the API, you still need to create and manage collections in Cosmos DB manually. The GraphQL resolvers handle the interaction between the API and the database, ensuring that the data follows the defined schema.



**How to keep GraphQL Schema in sync with database Schema?**

Ensuring that your GraphQL schema and your database schema are in sync is crucial to maintaining consistency across your application. Here’s how you can manage this:

### Keeping Schemas in Sync
1. **Schema Definition**: Define your schema centrally. You can use a schema definition language (SDL) or code-first approach to ensure that the schema is consistent across both GraphQL and the database.

2. **Schema Validation**: Implement schema validation to check that the GraphQL schema and the database schema match. This can help you catch any discrepancies early.

3. **Automated Migrations**: Use tools or scripts to automate the process of updating the database schema whenever the GraphQL schema changes. This ensures that both schemas are always in sync.

### Practical Example
Let’s consider a practical example using NestJS with TypeORM and GraphQL:

1. **Entity Definition**:
Define your database entity using TypeORM:

```typescript
import { Entity, Column, PrimaryGeneratedColumn } from 'typeorm';

@Entity()
export class TodoItem {
  @PrimaryGeneratedColumn()
  id: number;

  @Column()
  title: string;

  @Column()
  completed: boolean;
}
```

2. **GraphQL Schema**:
Define your GraphQL schema using the same structure:

```typescript
import { ObjectType, Field, Int } from '@nestjs/graphql';

@ObjectType()
export class TodoItem {
  @Field(() => Int)
  id: number;

  @Field()
  title: string;

  @Field()
  completed: boolean;
}
```

3. **Resolver**:
Create a resolver that uses the defined entity and schema:

```typescript
import { Resolver, Query, Mutation, Args } from '@nestjs/graphql';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { TodoItem } from './todo-item.entity';

@Resolver(() => TodoItem)
export class TodoResolver {
  constructor(
    @InjectRepository(TodoItem)
    private readonly todoRepository: Repository<TodoItem>,
  ) {}

  @Query(() => [TodoItem])
  async getTodoItems(): Promise<TodoItem[]> {
    return await this.todoRepository.find();
  }

  @Mutation(() => TodoItem)
  async createTodoItem(
    @Args('title') title: string,
    @Args('completed') completed: boolean,
  ): Promise<TodoItem> {
    const newTodo = this.todoRepository.create({ title, completed });
    return await this.todoRepository.save(newTodo);
  }
}
```

### Benefits
By following this approach, you ensure that:
- The structure of your data is consistent across both the GraphQL API and the database.
- Changes to the schema in one place are reflected in the other, reducing the risk of discrepancies.
- Maintenance is easier since you have a single source of truth for your schema.


**There are 2 different files. One for GraphQL schema and the other for the DB Entity. We would end up changing both whenever there is a change isn't it or am I missing something here?**

You're correct, the approach I described does involve maintaining two separate files—one for the GraphQL schema and one for the database entity. This can lead to extra work whenever you need to make changes. However, there are ways to streamline this process.

### Using a Code-First Approach
You can use a code-first approach with NestJS and GraphQL, where you define your schema using TypeScript classes and decorators. This allows you to generate both the GraphQL schema and the database schema from the same set of definitions.

Here’s how you can do it:

1. **Define Your Entities and Schema Together**:
   Use decorators to define both your GraphQL schema and database schema in the same class.

```typescript
import { Entity, Column, PrimaryGeneratedColumn } from 'typeorm';
import { ObjectType, Field, Int } from '@nestjs/graphql';

@Entity()
@ObjectType()
export class TodoItem {
  @PrimaryGeneratedColumn()
  @Field(() => Int)
  id: number;

  @Column()
  @Field()
  title: string;

  @Column()
  @Field()
  completed: boolean;
}
```

In this example, the `TodoItem` class defines both the GraphQL schema (using `@ObjectType` and `@Field`) and the TypeORM entity (using `@Entity`, `@Column`, and `@PrimaryGeneratedColumn`).

2. **Create Your Resolvers**:
   Define the GraphQL resolvers that use the entity.

```typescript
import { Resolver, Query, Mutation, Args } from '@nestjs/graphql';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { TodoItem } from './todo-item.entity';

@Resolver(() => TodoItem)
export class TodoResolver {
  constructor(
    @InjectRepository(TodoItem)
    private readonly todoRepository: Repository<TodoItem>,
  ) {}

  @Query(() => [TodoItem])
  async getTodoItems(): Promise<TodoItem[]> {
    return await this.todoRepository.find();
  }

  @Mutation(() => TodoItem)
  async createTodoItem(
    @Args('title') title: string,
    @Args('completed') completed: boolean,
  ): Promise<TodoItem> {
    const newTodo = this.todoRepository.create({ title, completed });
    return await this.todoRepository.save(newTodo);
  }
}
```

### Benefits of Code-First Approach
- **Single Source of Truth**: Define your schema once and use it for both GraphQL and database interactions.
- **Reduced Maintenance**: You only need to update one file when making schema changes, reducing the risk of inconsistencies.
- **Type Safety**: Leverage TypeScript’s type system to ensure your schema is consistent across your application.

By using the code-first approach, you can efficiently manage your schema and ensure consistency across your GraphQL API and database.

**I still don't get it. I see we now have only one file for both graphql schema and also the db schema. But, when are the graphQL and db schemas actuallly crated**

Let's break it down a bit more, focusing on when and how the schemas are created for both GraphQL and the database in the code-first approach.

### 1. Defining the Schema
When you define your entity using TypeScript decorators, you're essentially creating a blueprint for both GraphQL and your database. 

### 2. GraphQL Schema Creation
The GraphQL schema is generated automatically when you run your NestJS application. The `@ObjectType` and `@Field` decorators in your TypeScript class are used by NestJS to build the GraphQL schema.

For example, when you start your NestJS server, it generates a schema based on your decorated TypeScript classes and creates a schema file (like `schema.gql`). This schema defines the structure of your GraphQL API.

### 3. Database Schema Creation
The database schema is managed by TypeORM, which uses the `@Entity`, `@PrimaryGeneratedColumn`, and `@Column` decorators to define the structure of the database tables and columns.

When you run your application, TypeORM will synchronize the defined entities with your database. This means it will create or update tables to match the entity definitions.

### Putting it All Together
Here’s a step-by-step sequence:

1. **Define Your Entity**: Create a TypeScript class and use decorators to define both the GraphQL and database schemas.
   ```typescript
   import { Entity, Column, PrimaryGeneratedColumn } from 'typeorm';
   import { ObjectType, Field, Int } from '@nestjs/graphql';

   @Entity()
   @ObjectType()
   export class TodoItem {
     @PrimaryGeneratedColumn()
     @Field(() => Int)
     id: number;

     @Column()
     @Field()
     title: string;

     @Column()
     @Field()
     completed: boolean;
   }
   ```

2. **Start Your Application**: When you start your NestJS application using `npm run start`, the following happens:
   - **GraphQL Schema**: NestJS reads the decorated TypeScript classes and generates the GraphQL schema file.
   - **Database Schema**: TypeORM synchronizes the defined entities with the database, creating or updating the tables as needed.

3. **Schema Synchronization**: Any changes you make to the TypeScript class (adding or removing fields, changing types) will be automatically reflected in both the GraphQL schema and the database schema the next time you start or refresh your application.

### Example Application Start
When you start your application, you will see logs indicating that the schemas are being created and synchronized. Here’s what you might see in the console:

```bash
[Nest] 12345   - 2025-02-03 15:55:12   [GraphQLModule] Mapped {/graphql, POST} route
[Nest] 12345   - 2025-02-03 15:55:12   [TypeOrmModule] Migrations are finished
[Nest] 12345   - 2025-02-03 15:55:12   [NestApplication] Nest application successfully started
```

### Summary
- **GraphQL Schema**: Created automatically when NestJS starts, based on your TypeScript classes with `@ObjectType` and `@Field`.
- **Database Schema**: Created or synchronized by TypeORM when the application starts, based on your TypeScript classes with `@Entity`, `@PrimaryGeneratedColumn`, and `@Column`.



**Does that mean in GraphQL resolver methods, we would be referring to the unified 'TodoItem' class for both accessing GraphQL features as well as for DB persistence too and there is no need to write a GraphQL schema file , with types and inputs defined in it.\**

With the code-first approach in NestJS, you define a single `TodoItem` class, which serves as the unified schema for both GraphQL and your database.

### Key Points:
1. **Unified Class**: The `TodoItem` class with TypeScript decorators handles both GraphQL schema and database entity definitions.
   - `@ObjectType` and `@Field` decorators define the GraphQL schema.
   - `@Entity`, `@PrimaryGeneratedColumn`, and `@Column` decorators define the database schema.

2. **No Separate GraphQL Schema File**: There's no need to maintain a separate GraphQL schema file (`.graphql` or `.gql`). The GraphQL schema is generated automatically from your TypeScript classes when the application starts.

### Example
Here’s how the unified class definition works in practice:

#### Entity and GraphQL Type Definition:
```typescript
import { Entity, Column, PrimaryGeneratedColumn } from 'typeorm';
import { ObjectType, Field, Int } from '@nestjs/graphql';

@Entity()
@ObjectType()
export class TodoItem {
  @PrimaryGeneratedColumn()
  @Field(() => Int)
  id: number;

  @Column()
  @Field()
  title: string;

  @Column()
  @Field()
  completed: boolean;
}
```

#### Resolver:
In your GraphQL resolver methods, you refer to this unified `TodoItem` class for both accessing GraphQL features and database persistence.

```typescript
import { Resolver, Query, Mutation, Args } from '@nestjs/graphql';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { TodoItem } from './todo-item.entity';

@Resolver(() => TodoItem)
export class TodoResolver {
  constructor(
    @InjectRepository(TodoItem)
    private readonly todoRepository: Repository<TodoItem>,
  ) {}

  @Query(() => [TodoItem])
  async getTodoItems(): Promise<TodoItem[]> {
    return await this.todoRepository.find();
  }

  @Mutation(() => TodoItem)
  async createTodoItem(
    @Args('title') title: string,
    @Args('completed') completed: boolean,
  ): Promise<TodoItem> {
    const newTodo = this.todoRepository.create({ title, completed });
    return await this.todoRepository.save(newTodo);
  }
}
```

### Benefits:
- **Consistency**: Ensures that both GraphQL and the database schema are always in sync.
- **Reduced Maintenance**: Changes only need to be made in one place, reducing the risk of discrepancies.
- **Simplified Development**: Faster and more straightforward development process since you're working with a single source of truth.

By using this unified approach, you streamline your development process and maintain a consistent schema across your application.


**What about the Queries and Input Types in GraphQL. Don't we need it at all?**

We still need to define queries and input types to fully utilize GraphQL features. In the code-first approach, we can define these directly in our TypeScript code using decorators. Let me show you how this works.

### Defining Input Types
Input types are used to define the structure of the data that can be passed to your GraphQL mutations and queries. Here's how you can define input types:

```typescript
import { InputType, Field } from '@nestjs/graphql';

@InputType()
export class CreateTodoItemInput {
  @Field()
  title: string;

  @Field()
  completed: boolean;
}

@InputType()
export class UpdateTodoItemInput {
  @Field()
  id: number;

  @Field()
  title: string;

  @Field()
  completed: boolean;
}
```

### Defining Queries and Mutations
Queries and mutations are defined in your resolver classes. You can use decorators to specify the input types and the return types for your resolver methods.

#### Resolver with Queries and Mutations:
```typescript
import { Resolver, Query, Mutation, Args } from '@nestjs/graphql';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { TodoItem } from './todo-item.entity';
import { CreateTodoItemInput, UpdateTodoItemInput } from './create-todo-item.input';

@Resolver(() => TodoItem)
export class TodoResolver {
  constructor(
    @InjectRepository(TodoItem)
    private readonly todoRepository: Repository<TodoItem>,
  ) {}

  @Query(() => [TodoItem])
  async getTodoItems(): Promise<TodoItem[]> {
    return await this.todoRepository.find();
  }

  @Mutation(() => TodoItem)
  async createTodoItem(@Args('input') input: CreateTodoItemInput): Promise<TodoItem> {
    const newTodo = this.todoRepository.create(input);
    return await this.todoRepository.save(newTodo);
  }

  @Mutation(() => TodoItem)
  async updateTodoItem(@Args('input') input: UpdateTodoItemInput): Promise<TodoItem> {
    await this.todoRepository.update(input.id, input);
    return await this.todoRepository.findOne(input.id);
  }

  @Mutation(() => Boolean)
  async deleteTodoItem(@Args('id') id: number): Promise<boolean> {
    const result = await this.todoRepository.delete(id);
    return result.affected > 0;
  }
}
```

### Summary
- **Input Types**: Defined using `@InputType` and `@Field` decorators to specify the structure of the data for mutations and queries.
- **Queries and Mutations**: Defined in resolver classes using `@Query`, `@Mutation`, and `@Args` decorators to handle data fetching and modifications.

By defining input types and resolver methods in your TypeScript code, you maintain a consistent schema and ensure that your GraphQL API is fully functional.

This approach allows you to take advantage of all GraphQL features while keeping your schema definitions in sync with your database entities. 


**Does that mean we can only have a unified schema for both GraphQL and data store schema. But, we still need GraphQL queries and input types to be defined. how about mutations and subscriptions , would we have any need for them here.**

The unified schema approach primarily allows you to define a single class for both the GraphQL and database schemas. However, you still need to define GraphQL queries, input types, mutations, and subscriptions to fully utilize GraphQL's capabilities.

### Summary of Key Points:
1. **Unified Schema**: A single TypeScript class can serve as both the GraphQL schema and the database entity.
2. **Queries and Input Types**: Need to be defined using decorators to handle the structure and arguments of your GraphQL operations.

### Mutations and Subscriptions:
Mutations and subscriptions are essential parts of a complete GraphQL API:

#### Mutations:
Mutations allow you to modify data (create, update, delete) and are similar to POST, PUT, DELETE operations in REST APIs.

**Example Mutation Definition:**
```typescript
import { Mutation, Args, Resolver } from '@nestjs/graphql';
import { TodoItem } from './todo-item.entity';
import { CreateTodoItemInput, UpdateTodoItemInput } from './create-todo-item.input';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';

@Resolver(() => TodoItem)
export class TodoResolver {
  constructor(
    @InjectRepository(TodoItem)
    private readonly todoRepository: Repository<TodoItem>,
  ) {}

  @Mutation(() => TodoItem)
  async createTodoItem(@Args('input') input: CreateTodoItemInput): Promise<TodoItem> {
    const newTodo = this.todoRepository.create(input);
    return await this.todoRepository.save(newTodo);
  }

  @Mutation(() => TodoItem)
  async updateTodoItem(@Args('input') input: UpdateTodoItemInput): Promise<TodoItem> {
    await this.todoRepository.update(input.id, input);
    return await this.todoRepository.findOne(input.id);
  }

  @Mutation(() => Boolean)
  async deleteTodoItem(@Args('id') id: number): Promise<boolean> {
    const result = await this.todoRepository.delete(id);
    return result.affected > 0;
  }
}
```

#### Subscriptions:
Subscriptions allow clients to subscribe to real-time updates and are similar to WebSockets or Server-Sent Events.

**Example Subscription Definition:**
```typescript
import { Resolver, Query, Mutation, Args, Subscription } from '@nestjs/graphql';
import { PubSub } from 'graphql-subscriptions';
import { TodoItem } from './todo-item.entity';
import { CreateTodoItemInput, UpdateTodoItemInput } from './create-todo-item.input';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';

const pubSub = new PubSub();

@Resolver(() => TodoItem)
export class TodoResolver {
  constructor(
    @InjectRepository(TodoItem)
    private readonly todoRepository: Repository<TodoItem>,
  ) {}

  @Query(() => [TodoItem])
  async getTodoItems(): Promise<TodoItem[]> {
    return await this.todoRepository.find();
  }

  @Mutation(() => TodoItem)
  async createTodoItem(@Args('input') input: CreateTodoItemInput): Promise<TodoItem> {
    const newTodo = this.todoRepository.create(input);
    const savedTodo = await this.todoRepository.save(newTodo);
    pubSub.publish('todoAdded', { todoAdded: savedTodo });
    return savedTodo;
  }

  @Subscription(() => TodoItem, {
    filter: (payload, variables) => payload.todoAdded.id === variables.id,
  })
  todoAdded(@Args('id') id: number) {
    return pubSub.asyncIterator('todoAdded');
  }
}
```

### Summary:
- **Unified Schema**: Use a single class for GraphQL and database schema definitions.
- **Queries and Input Types**: Necessary to handle data fetching and input structure.
- **Mutations**: For data modifications (create, update, delete).
- **Subscriptions**: For real-time updates.

By defining your GraphQL queries, input types, mutations, and subscriptions using TypeScript decorators and classes, you can build a robust and consistent API that leverages both the unified schema and the powerful features of GraphQL.

**Do you mean we don't need to define the mutation types and subscription types which we had to add in graphQL schema files separately.**

With the code-first approach in NestJS, you don't need to manually define mutation types and subscription types in a separate GraphQL schema file. Instead, you define them directly in your TypeScript code using decorators and classes.

Here’s a breakdown of how it works:

### Defining Mutations and Subscriptions
- **Mutations**: Defined in your resolver class using the `@Mutation` decorator.
- **Subscriptions**: Defined in your resolver class using the `@Subscription` decorator.

### Example Mutation and Subscription Definition
Here’s a complete example that shows how to define mutations and subscriptions directly in your TypeScript code:

#### Input Types:
```typescript
import { InputType, Field } from '@nestjs/graphql';

@InputType()
export class CreateTodoItemInput {
  @Field()
  title: string;

  @Field()
  completed: boolean;
}

@InputType()
export class UpdateTodoItemInput {
  @Field()
  id: number;

  @Field()
  title: string;

  @Field()
  completed: boolean;
}
```

#### Entity and GraphQL Type:
```typescript
import { Entity, Column, PrimaryGeneratedColumn } from 'typeorm';
import { ObjectType, Field, Int } from '@nestjs/graphql';

@Entity()
@ObjectType()
export class TodoItem {
  @PrimaryGeneratedColumn()
  @Field(() => Int)
  id: number;

  @Column()
  @Field()
  title: string;

  @Column()
  @Field()
  completed: boolean;
}
```

#### Resolver with Mutations and Subscriptions:
```typescript
import { Resolver, Query, Mutation, Subscription, Args } from '@nestjs/graphql';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { PubSub } from 'graphql-subscriptions';
import { TodoItem } from './todo-item.entity';
import { CreateTodoItemInput, UpdateTodoItemInput } from './create-todo-item.input';

const pubSub = new PubSub();

@Resolver(() => TodoItem)
export class TodoResolver {
  constructor(
    @InjectRepository(TodoItem)
    private readonly todoRepository: Repository<TodoItem>,
  ) {}

  @Query(() => [TodoItem])
  async getTodoItems(): Promise<TodoItem[]> {
    return await this.todoRepository.find();
  }

  @Mutation(() => TodoItem)
  async createTodoItem(@Args('input') input: CreateTodoItemInput): Promise<TodoItem> {
    const newTodo = this.todoRepository.create(input);
    const savedTodo = await this.todoRepository.save(newTodo);
    pubSub.publish('todoAdded', { todoAdded: savedTodo });
    return savedTodo;
  }

  @Mutation(() => TodoItem)
  async updateTodoItem(@Args('input') input: UpdateTodoItemInput): Promise<TodoItem> {
    await this.todoRepository.update(input.id, input);
    return await this.todoRepository.findOne(input.id);
  }

  @Mutation(() => Boolean)
  async deleteTodoItem(@Args('id') id: number): Promise<boolean> {
    const result = await this.todoRepository.delete(id);
    return result.affected > 0;
  }

  @Subscription(() => TodoItem, {
    filter: (payload, variables) => payload.todoAdded.id === variables.id,
  })
  todoAdded(@Args('id') id: number) {
    return pubSub.asyncIterator('todoAdded');
  }
}
```

### Benefits of Code-First Approach:
- **Single Source of Truth**: Define your schema, queries, mutations, and subscriptions all in one place.
- **Automatic Schema Generation**: The GraphQL schema is automatically generated from your TypeScript code when you start the NestJS application.
- **Consistency**: Ensure that your API and database schemas remain consistent without manual synchronization.

This approach simplifies your development process and ensures that all parts of your GraphQL API are defined in a single, unified way. 
