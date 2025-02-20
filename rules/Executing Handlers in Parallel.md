Yes, we can run multiple handlers in parallel by leveraging asynchronous execution in NestJS. The best approach depends on whether the handlers are CPU-bound or I/O-bound:

I/O-bound tasks (e.g., database queries, API calls) → Use Promise.all() for concurrent execution.

CPU-bound tasks (e.g., complex calculations) → Use worker_threads or BullMQ for parallel processing.


Solution: Run Multiple Handlers Concurrently Using Promise.all()

Since most handlers in a CQRS system deal with I/O operations (e.g., persisting data, notifying external systems), we can execute them concurrently using JavaScript's Promise.all().


---

1. Modify CommandDispatcher to Run Handlers in Parallel

import { Injectable, Inject } from '@nestjs/common';
import { CommandRouter } from './command-router.service';
import { CreatePolicyHandler } from '../handlers/create-policy.handler';
import { NotifyUnderwriterHandler } from '../handlers/notify-underwriter.handler';
import { DefaultCreatePolicyHandler } from '../handlers/default-create-policy.handler';

@Injectable()
export class CommandDispatcher {
  private readonly handlers: Record<string, CommandHandler>;

  constructor(
    private readonly router: CommandRouter,
    private readonly createPolicyHandler: CreatePolicyHandler,
    private readonly notifyUnderwriterHandler: NotifyUnderwriterHandler,
    private readonly defaultCreatePolicyHandler: DefaultCreatePolicyHandler,
  ) {
    this.handlers = {
      CreatePolicyHandler: createPolicyHandler,
      NotifyUnderwriterHandler: notifyUnderwriterHandler,
      DefaultCreatePolicyHandler: defaultCreatePolicyHandler,
    };
  }

  async dispatch(requestAttributes: Record<string, any>) {
    const handlerNames = this.router.findMatchingHandlers(requestAttributes);

    if (handlerNames.length === 0) {
      throw new Error('No matching handler found');
    }

    console.log(`Executing handlers: ${handlerNames.join(', ')}`);

    // Execute handlers in parallel using Promise.all()
    await Promise.all(handlerNames.map(handlerName => this.handlers[handlerName].handle(requestAttributes)));
  }
}


---

2. Modify Handlers to Be Async

Now, we update our handlers to be async functions so they can be executed in parallel.

Example 1: CreatePolicyHandler.ts

import { Injectable } from '@nestjs/common';
import { CommandHandler } from '../interfaces/command-handler.interface';

@Injectable()
export class CreatePolicyHandler implements CommandHandler {
  async handle(requestAttributes: Record<string, any>) {
    console.log(`Processing CreatePolicy for source: ${requestAttributes.source}`);
    await new Promise(resolve => setTimeout(resolve, 2000)); // Simulating async task
    console.log(`CreatePolicy completed`);
  }
}

Example 2: NotifyUnderwriterHandler.ts

import { Injectable } from '@nestjs/common';
import { CommandHandler } from '../interfaces/command-handler.interface';

@Injectable()
export class NotifyUnderwriterHandler implements CommandHandler {
  async handle(requestAttributes: Record<string, any>) {
    console.log(`Notifying underwriter for HighRisk policy...`);
    await new Promise(resolve => setTimeout(resolve, 3000)); // Simulating async API call
    console.log(`Underwriter notified`);
  }
}

Example 3: DefaultCreatePolicyHandler.ts

import { Injectable } from '@nestjs/common';
import { CommandHandler } from '../interfaces/command-handler.interface';

@Injectable()
export class DefaultCreatePolicyHandler implements CommandHandler {
  async handle(requestAttributes: Record<string, any>) {
    console.log(`Processing default CreatePolicy handler...`);
    await new Promise(resolve => setTimeout(resolve, 1000)); // Simulating async DB update
    console.log(`DefaultCreatePolicyHandler completed`);
  }
}


---

3. Testing Parallel Execution

Run the API with the following request:

curl -X POST "http://localhost:3000/commands/execute" \
     -H "Content-Type: application/json" \
     -d '{"requestType": "CreatePolicy", "productType": "HighRisk"}'

Expected Output (Executed in Parallel)

Executing handlers: NotifyUnderwriterHandler, DefaultCreatePolicyHandler
Notifying underwriter for HighRisk policy...
Processing default CreatePolicy handler...
DefaultCreatePolicyHandler completed  ✅ (after 1s)
Underwriter notified  ✅ (after 3s)

Total execution time: ~3 seconds instead of 4 (1s + 3s sequentially)


---

Alternative: Offload Execution to a Background Queue

For long-running tasks, use BullMQ (Redis-based job queue) to execute handlers in parallel outside the request lifecycle.

Steps to Use BullMQ

1. Install BullMQ

npm install bullmq


2. Create a Worker for Handlers


3. Dispatch Jobs to the Queue Instead of Executing Directly



Would you like a full example using BullMQ? Let me know!

