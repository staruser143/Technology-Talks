# Should I use NestJS with fastify or with default expressJS engine.

It depends on your project requirements and performance goals. Here's a comparison to help you decide:

Use NestJS with Fastify if:

Performance is critical: Fastify is faster and more performant than Express due to its highly optimized architecture.

Low latency requirements: It handles high-concurrency scenarios better.

JSON-heavy APIs: Fastify has efficient JSON serialization and deserialization.

You can afford some compatibility adjustments: Fastify may require some minor changes or middleware adjustments, especially if you are migrating from Express.


Use NestJS with Default Express if:

Ease of use and compatibility: Express has a larger ecosystem, and many NestJS plugins are designed for Express.

Legacy compatibility: If you're migrating an existing Express-based project.

Faster setup: You get built-in compatibility without needing additional tweaks.

Middleware compatibility: Most third-party middleware is designed for Express.


Performance Consideration

If performance is a strong concern, Fastify is often the better choice. However, for many typical applications where ease of development and ecosystem compatibility are more important, sticking with Express might be simpler.

Would you like some benchmarks or steps to configure Fastify with NestJS?

