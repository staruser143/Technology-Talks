# CQRS with NestJS
To get started with using CQRS (Command Query Responsibility Segregation) in NestJS, follow these steps:

1. **Install Required Packages**:
   ```sh
   npm install @nestjs/cqrs
   ```

2. **Create Command and Query Classes**:
   - Define command classes for write operations.
   - Define query classes for read operations.

   **Example**:
   ```typescript
   export class CreateItemCommand {
     constructor(public readonly name: string, public readonly description: string) {}
   }

   export class GetItemQuery {
     constructor(public readonly id: string) {}
   }
   ```

3. **Create Command and Query Handlers**:
   - Implement handlers for the commands and queries.

   **Example**:
   ```typescript
   import { CommandHandler, ICommandHandler } from '@nestjs/cqrs';
   import { CreateItemCommand } from './commands/create-item.command';

   @CommandHandler(CreateItemCommand)
   export class CreateItemHandler implements ICommandHandler<CreateItemCommand> {
     async execute(command: CreateItemCommand) {
       // Implement the logic to create an item
       console.log(`Creating item: ${command.name}`);
     }
   }

   import { QueryHandler, IQueryHandler } from '@nestjs/cqrs';
   import { GetItemQuery } from './queries/get-item.query';

   @QueryHandler(GetItemQuery)
   export class GetItemHandler implements IQueryHandler<GetItemQuery> {
     async execute(query: GetItemQuery) {
       // Implement the logic to get an item
       console.log(`Getting item with ID: ${query.id}`);
     }
   }
   ```

4. **Register the Handlers in a Module**:
   - Register command and query handlers in your module.

   **Example**:
   ```typescript
   import { Module } from '@nestjs/common';
   import { CqrsModule } from '@nestjs/cqrs';
   import { CreateItemHandler } from './commands/handlers/create-item.handler';
   import { GetItemHandler } from './queries/handlers/get-item.handler';

   @Module({
     imports: [CqrsModule],
     providers: [CreateItemHandler, GetItemHandler],
   })
   export class ItemsModule {}
   ```

5. **Use the Command and Query Buses**:
   - Inject and use the command and query buses in your services or controllers.

   **Example**:
   ```typescript
   import { Controller, Post, Body, Get, Param } from '@nestjs/common';
   import { CommandBus, QueryBus } from '@nestjs/cqrs';
   import { CreateItemCommand } from './commands/create-item.command';
   import { GetItemQuery } from './queries/get-item.query';

   @Controller('items')
   export class ItemsController {
     constructor(private readonly commandBus: CommandBus, private readonly queryBus: QueryBus) {}

     @Post()
     async createItem(@Body() body: { name: string, description: string }) {
       await this.commandBus.execute(new CreateItemCommand(body.name, body.description));
     }

     @Get(':id')
     async getItem(@Param('id') id: string) {
       return await this.queryBus.execute(new GetItemQuery(id));
     }
   }
   ```

These steps will help us get started with using CQRS in the NestJS application.
