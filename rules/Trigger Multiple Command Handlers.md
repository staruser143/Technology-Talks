Yes, it is logically possible and quite common in real-world scenarios. A request might require multiple command handlers if:

1. Chained Execution: One command handler processes the request, and another follows based on the modified state.


2. Parallel Execution: Two or more handlers are needed simultaneously.


3. Event-Driven Flow: A request triggers multiple business processes that run independently.



How to Handle Multiple Handlers in the Framework?

1. Modify the Configuration to Support Multiple Handlers

Instead of returning a single handler, return a list of matching handlers.

2. Adjust CommandRouter to Select Multiple Handlers

Modify CommandRouter to return all matching handlers instead of just one.

3. Modify CommandDispatcher to Execute All Matching Handlers

Update CommandDispatcher to loop through the list of handlers and execute them.


---

Implementation Steps

1. Modify config.yml to Support Multiple Handlers

handlers:
  - name: CreatePolicyHandler
    priority: 2
    conditions:
      requestType: "CreatePolicy"
      source: ["Portal", "Mobile"]

  - name: NotifyUnderwriterHandler
    priority: 2
    conditions:
      requestType: "CreatePolicy"
      productType: "HighRisk"

  - name: DefaultCreatePolicyHandler
    priority: 1
    conditions:
      requestType: "CreatePolicy"

CreatePolicyHandler handles CreatePolicy requests from Portal/Mobile.

NotifyUnderwriterHandler is needed for high-risk products.

DefaultCreatePolicyHandler is a fallback.



---

2. Modify CommandRouter to Return Multiple Handlers

import { Injectable } from '@nestjs/common';
import * as fs from 'fs';
import * as yaml from 'yaml';

interface HandlerConfig {
  name: string;
  priority: number;
  conditions: Record<string, any>;
}

@Injectable()
export class CommandRouter {
  private handlerConfigs: HandlerConfig[];

  constructor() {
    this.handlerConfigs = this.loadConfig();
  }

  private loadConfig(): HandlerConfig[] {
    const file = fs.readFileSync('src/config/config.yml', 'utf8');
    return yaml.parse(file).handlers;
  }

  findMatchingHandlers(requestAttributes: Record<string, any>): string[] {
    return this.handlerConfigs
      .filter(config => this.matches(config, requestAttributes))
      .sort((a, b) => b.priority - a.priority) // Sort by priority
      .map(config => config.name);
  }

  private matches(config: HandlerConfig, requestAttributes: Record<string, any>): boolean {
    return Object.entries(config.conditions).every(([key, value]) => {
      if (Array.isArray(value)) {
        return value.includes(requestAttributes[key]);
      }
      return requestAttributes[key] === value;
    });
  }
}


---

3. Modify CommandDispatcher to Execute Multiple Handlers

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

  dispatch(requestAttributes: Record<string, any>) {
    const handlerNames = this.router.findMatchingHandlers(requestAttributes);

    if (handlerNames.length === 0) {
      throw new Error('No matching handler found');
    }

    for (const handlerName of handlerNames) {
      console.log(`Executing handler: ${handlerName}`);
      this.handlers[handlerName].handle(requestAttributes);
    }
  }
}


---

4. Example Handlers

CreatePolicyHandler.ts

import { Injectable } from '@nestjs/common';
import { CommandHandler } from '../interfaces/command-handler.interface';

@Injectable()
export class CreatePolicyHandler implements CommandHandler {
  handle(requestAttributes: Record<string, any>) {
    console.log('Processing CreatePolicy for source:', requestAttributes.source);
  }
}

NotifyUnderwriterHandler.ts

import { Injectable } from '@nestjs/common';
import { CommandHandler } from '../interfaces/command-handler.interface';

@Injectable()
export class NotifyUnderwriterHandler implements CommandHandler {
  handle(requestAttributes: Record<string, any>) {
    console.log('Notifying underwriter for HighRisk policy');
  }
}

DefaultCreatePolicyHandler.ts

import { Injectable } from '@nestjs/common';
import { CommandHandler } from '../interfaces/command-handler.interface';

@Injectable()
export class DefaultCreatePolicyHandler implements CommandHandler {
  handle(requestAttributes: Record<string, any>) {
    console.log('Processing default CreatePolicy handler');
  }
}


---

5. Test the API

Scenario 1: CreatePolicy from Portal

Request

curl -X POST "http://localhost:3000/commands/execute" \
     -H "Content-Type: application/json" \
     -d '{"requestType": "CreatePolicy", "source": "Portal"}'

Console Output

Executing handler: CreatePolicyHandler
Processing CreatePolicy for source: Portal

Scenario 2: CreatePolicy for High-Risk Product

Request

curl -X POST "http://localhost:3000/commands/execute" \
     -H "Content-Type: application/json" \
     -d '{"requestType": "CreatePolicy", "productType": "HighRisk"}'

Console Output

Executing handler: NotifyUnderwriterHandler
Notifying underwriter for HighRisk policy
Executing handler: DefaultCreatePolicyHandler
Processing default CreatePolicy handler


---

Key Takeaways

✅ Multiple Handlers can now be executed based on the request context.
✅ Handlers are executed in priority order (higher priority first).
✅ Easy to extend – just add new handlers in config.yml without changing code.


---

Next Steps

Would you like to:

1. Run handlers in parallel using async processing?


2. Integrate an event queue (e.g., RabbitMQ or Kafka) for better scalability?


3. Log executed handlers to a database for traceability?



Let me know how you’d like to enhance this!

