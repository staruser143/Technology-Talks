# CQRS with GraphQL in NestJS

To use CQRS in a GraphQL NestJS application, follow these steps:

### Step 1: Integrate GraphQL with NestJS

1. **Add GraphQL Module**:
   ```sh
   npm install @nestjs/graphql graphql-tools graphql
   ```

2. **Configure GraphQL Settings**:
   ```typescript
   import { Module } from '@nestjs/common';
   import { GraphQLModule } from '@nestjs/graphql';
   import { AppController } from './app.controller';
   import { AppService } from './app.service';
   import { CqrsModule } from '@nestjs/cqrs';
   import { ItemsModule } from './items/items.module';

   @Module({
     imports: [
       GraphQLModule.forRoot({
         autoSchemaFile: true,
       }),
       CqrsModule,
       ItemsModule,
     ],
     controllers: [AppController],
     providers: [AppService],
   })
   export class AppModule {}
   ```

### Step 2: Use CQRS in the GraphQL Application

1. **Create GraphQL Resolvers**:
   ```typescript
   import { Resolver, Query, Mutation, Args } from '@nestjs/graphql';
   import { CommandBus, QueryBus } from '@nestjs/cqrs';
   import { CreateItemCommand } from './commands/create-item.command';
   import { GetItemQuery } from './queries/get-item.query';
   import { Item } from './models/item.model';

   @Resolver(() => Item)
   export class ItemsResolver {
     constructor(private readonly commandBus: CommandBus, private readonly queryBus: QueryBus) {}

     @Query(() => Item)
     async getItem(@Args('id') id: string) {
       return await this.queryBus.execute(new GetItemQuery(id));
     }

     @Mutation(() => Item)
     async createItem(@Args('name') name: string, @Args('description') description: string) {
       return await this.commandBus.execute(new CreateItemCommand(name, description));
     }
   }
   ```

2. **Set Up DTOs and Input Types**:
   ```typescript
   import { ObjectType, Field, ID } from '@nestjs/graphql';

   @ObjectType()
   export class Item {
     @Field(() => ID)
     id: string;

     @Field()
     name: string;

     @Field()
     description: string;
   }
   ```
