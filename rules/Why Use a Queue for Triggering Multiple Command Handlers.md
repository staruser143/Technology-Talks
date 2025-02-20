Using a queue is not always necessary, but it becomes useful in certain scenarios where we need asynchronous processing beyond the scope of a single request-response cycle.

Why Consider a Queue?

If your handlers involve:
✅ Long-running tasks (e.g., calling multiple external APIs, running ML models)
✅ High-throughput processing (e.g., handling thousands of concurrent requests)
✅ Retries & failure handling (e.g., retrying failed API calls, database transactions)
✅ Decoupling execution from request lifecycle (e.g., avoid making the client wait)

Then offloading work to a background queue (like BullMQ) can help.


---

Comparison: Promise.all() vs Queue-based Execution


---

How a Queue-based Approach Works

1️⃣ Command Dispatcher pushes tasks to a Redis-based queue instead of executing them directly.
2️⃣ Workers running in background process commands asynchronously without blocking the request-response cycle.
3️⃣ If a worker fails, the queue automatically retries the command.


---

Queue-based Command Execution in NestJS using BullMQ

Step 1: Install BullMQ

npm install bullmq


---

Step 2: Create a Queue for Commands

Define a NestJS service to push commands to the queue.

import { Injectable } from '@nestjs/common';
import { Queue } from 'bullmq';

@Injectable()
export class CommandQueueService {
  private commandQueue: Queue;

  constructor() {
    this.commandQueue = new Queue('commandQueue', {
      connection: { host: 'localhost', port: 6379 }, // Redis connection
    });
  }

  async enqueueCommand(commandName: string, payload: any) {
    await this.commandQueue.add(commandName, payload);
    console.log(`Queued command: ${commandName}`);
  }
}


---

Step 3: Modify Command Dispatcher to Use the Queue

Instead of executing handlers directly, we enqueue them.

import { Injectable } from '@nestjs/common';
import { CommandRouter } from './command-router.service';
import { CommandQueueService } from './command-queue.service';

@Injectable()
export class CommandDispatcher {
  constructor(
    private readonly router: CommandRouter,
    private readonly queueService: CommandQueueService,
  ) {}

  async dispatch(requestAttributes: Record<string, any>) {
    const handlerNames = this.router.findMatchingHandlers(requestAttributes);

    if (handlerNames.length === 0) {
      throw new Error('No matching handler found');
    }

    console.log(`Queuing handlers: ${handlerNames.join(', ')}`);

    // Instead of running Promise.all(), we enqueue each handler
    for (const handlerName of handlerNames) {
      await this.queueService.enqueueCommand(handlerName, requestAttributes);
    }
  }
}


---

Step 4: Create a Worker to Process Commands

Workers consume the queue and execute the actual handlers in background.

import { Worker } from 'bullmq';
import { Injectable } from '@nestjs/common';
import { CreatePolicyHandler } from '../handlers/create-policy.handler';
import { NotifyUnderwriterHandler } from '../handlers/notify-underwriter.handler';

@Injectable()
export class CommandWorkerService {
  constructor(
    private readonly createPolicyHandler: CreatePolicyHandler,
    private readonly notifyUnderwriterHandler: NotifyUnderwriterHandler,
  ) {
    new Worker('commandQueue', async (job) => {
      console.log(`Processing job: ${job.name}`);

      if (job.name === 'CreatePolicyHandler') {
        await this.createPolicyHandler.handle(job.data);
      } else if (job.name === 'NotifyUnderwriterHandler') {
        await this.notifyUnderwriterHandler.handle(job.data);
      }

      console.log(`Completed job: ${job.name}`);
    }, {
      connection: { host: 'localhost', port: 6379 },
    });
  }
}


---

Step 5: Start Redis Server

Since BullMQ requires Redis, start Redis before running the application:

docker run -d --name redis -p 6379:6379 redis


---

How This Works

1️⃣ When a request comes in, CommandDispatcher queues the handlers instead of executing them.
2️⃣ The API immediately responds, without waiting for long-running tasks to finish.
3️⃣ Workers process the queued commands in parallel.
4️⃣ If a worker fails, BullMQ automatically retries the command.


---

Final Thoughts

If the commands execute quickly (few seconds) → Use Promise.all().

If the commands take longer or need retries → Use BullMQ.


Would this approach work for your use case?

