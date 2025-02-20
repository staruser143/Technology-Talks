What is Dynamic Rule Evaluation?

Dynamic Rule Evaluation is the ability to determine logic at runtime based on configurable rules, rather than hardcoded conditions. In your case, it means the framework should dynamically evaluate which handler(s) to invoke based on rules stored externally (e.g., JSON, database, or a rules engine) instead of being fixed in code.


---

How JSON-Based Rule Evaluation Works

Instead of hardcoding conditions in TypeScript (or Java in Spring Boot), we define a JSON-based rules configuration that determines which handlers match a request.

Example JSON Rules Configuration

{
  "rules": [
    {
      "handler": "CreatePolicyHandler",
      "conditions": {
        "requestType": "policyCreation",
        "user.role": "broker"
      }
    },
    {
      "handler": "NotifyUnderwriterHandler",
      "conditions": {
        "requestType": "policyCreation",
        "policyDetails.coverageType": "auto"
      }
    },
    {
      "handler": "FraudCheckHandler",
      "conditions": {
        "user.region": "US",
        "policyDetails.premiumAmount": { "gt": 10000 }
      }
    }
  ]
}

This JSON defines:

Each rule has a handler (e.g., CreatePolicyHandler)

Each rule has conditions (e.g., requestType == "policyCreation")

Supports nested attributes (e.g., user.role, policyDetails.coverageType)

Supports operators (e.g., "gt" for greater than)



---

How to Evaluate JSON Rules in NestJS

1. Load JSON rules dynamically (from a file, database, or API)


2. Parse JSON conditions and match against incoming request attributes


3. Support complex evaluations like numeric comparisons (>, <, !=)


4. Select all matching handlers and invoke them



Step 1: Load JSON Rules

You can store rules in a JSON file or fetch them dynamically from a database.

import * as fs from 'fs';

@Injectable()
export class RuleLoader {
  private rules: any;

  constructor() {
    this.loadRules();
  }

  private loadRules() {
    const fileContent = fs.readFileSync('rules.json', 'utf-8');
    this.rules = JSON.parse(fileContent);
  }

  getRules() {
    return this.rules.rules;
  }
}


---

Step 2: Evaluate Rules Dynamically

Create a RuleEvaluator service that matches incoming requests against the JSON rules.

import { Injectable } from '@nestjs/common';

@Injectable()
export class RuleEvaluator {
  constructor(private readonly ruleLoader: RuleLoader) {}

  findMatchingHandlers(requestAttributes: Record<string, any>): string[] {
    const rules = this.ruleLoader.getRules();
    const matchingHandlers: string[] = [];

    for (const rule of rules) {
      if (this.doesRuleMatch(rule.conditions, requestAttributes)) {
        matchingHandlers.push(rule.handler);
      }
    }
    return matchingHandlers;
  }

  private doesRuleMatch(conditions: Record<string, any>, requestAttributes: Record<string, any>): boolean {
    return Object.entries(conditions).every(([key, value]) => {
      const actualValue = this.getNestedValue(requestAttributes, key);

      if (typeof value === 'object' && 'gt' in value) {
        return actualValue > value.gt; // Numeric comparison
      }

      return actualValue === value;
    });
  }

  private getNestedValue(obj: any, path: string): any {
    return path.split('.').reduce((o, key) => (o ? o[key] : undefined), obj);
  }
}


---

Step 3: Use Rule Evaluation in Command Dispatcher

Now integrate this dynamic evaluation in the CommandDispatcher.

@Injectable()
export class CommandDispatcher {
  constructor(private readonly ruleEvaluator: RuleEvaluator) {}

  async dispatch(requestDto: any) {
    const requestAttributes = {
      requestType: requestDto.requestType,
      requestSource: requestDto.requestSource,
      userRole: requestDto.user?.role,
      userRegion: requestDto.user?.region,
      policyType: requestDto.policyDetails?.coverageType,
      premiumAmount: requestDto.policyDetails?.premiumAmount
    };

    const handlerNames = this.ruleEvaluator.findMatchingHandlers(requestAttributes);

    if (handlerNames.length === 0) {
      throw new Error('No matching handler found');
    }

    console.log(`Executing handlers: ${handlerNames.join(', ')}`);

    for (const handlerName of handlerNames) {
      const handler = this.getHandlerInstance(handlerName);
      await handler.handle(requestDto);
    }

    return { status: 'success', executedHandlers: handlerNames };
  }

  private getHandlerInstance(handlerName: string) {
    // Instantiate handlers dynamically or use NestJS DI
  }
}


---

Final Outcome


---

Why Use JSON-Based Dynamic Rules?

✅ Configuration-driven – No need to change code to add/remove handlers
✅ Supports complex conditions – Can handle multiple attributes, operators, and nested fields
✅ Decouples logic from code – Handlers are selected at runtime based on external configuration
✅ Easier to maintain & update – Change JSON file or database without modifying the app

