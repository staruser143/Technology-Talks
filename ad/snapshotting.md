# What is Snapshotting?
* Snapshotting is the process of periodically saving the current state of an aggregate to persistent storage.
* Instead of always replaying the entire history of events from the beginning to reconstruct the aggregate's state when processing a command, we can load the most recent snapshot and then only replay the events that occurred after that snapshot.

## Why Use Snapshotting?
As an aggregate evolves and accumulates a long history of events, replaying all those events every time a command needs to be processed can become inefficient and time-consuming. This can lead to:
 * **Increased Latency**: Command processing takes longer as more events need to be read and applied.
 * **Increased Resource Consumption**: More CPU and memory are used for replaying a large number of events.
 * **Scalability Issues**: As the number of events grows, the performance degradation can impact the overall scalability of your application.
Snapshotting helps mitigate these issues by reducing the number of events that need to be replayed.

## How Snapshotting Works:
 * **Threshold**: We define a threshold (e.g., every 100 or 500 events) for when a snapshot should be taken.
 * **Snapshot Storage**: We need a separate storage mechanism to save snapshots. This could be another collection in the MongoDB database or a different storage system altogether. Each snapshot would typically store:
   * The aggregateId of the snapshot.
   * The state of the aggregate at the time the snapshot was taken (all relevant properties).
   * The version or the sequence number of the last event included in the snapshot.
 * **Taking a Snapshot**: After a certain number of events have been applied to an aggregate, the system saves a snapshot of its current state along with the version of the last applied event.
 * **Loading the Aggregate**: When a command is received for a specific aggregate:
   * The system first tries to load the most recent snapshot for that aggregateId.
   * If a snapshot is found, it loads the aggregate's state from the snapshot.
   * Then, it only retrieves and replays the events that occurred after the version recorded in the snapshot.
   * If no snapshot is found (e.g., for a newly created aggregate), it replays all events from the beginning.


## Implementing Snapshotting in Your NestJS Application with MongoDB:
### Here's a conceptual outline of how to implement snapshotting:
 * **Snapshot Repository**: Create a service or repository (e.g., QuoteRequestSnapshotRepository) to handle saving and retrieving snapshots from MongoDB. This repository would interact with a dedicated MongoDB collection for snapshots (e.g., quote_request_snapshots).
```
   // src/quotes/repositories/quote-request-snapshot.repository.ts

import { Injectable, InjectModel } from '@nestjs/common';
import { Model } from 'mongoose';
import { QuoteRequestSnapshot } from '../schemas/quote-request-snapshot.schema'; // Define your snapshot schema

@Injectable()
export class QuoteRequestSnapshotRepository {
  constructor(
    @InjectModel(QuoteRequestSnapshot.name)
    private readonly quoteRequestSnapshotModel: Model<QuoteRequestSnapshot>,
  ) {}

  async getLatest(aggregateId: string): Promise<QuoteRequestSnapshot | null> {
    return this.quoteRequestSnapshotModel
      .findOne({ aggregateId })
      .sort({ version: -1 })
      .exec();
  }

  async save(aggregateId: string, version: number, state: any): Promise<void> {
    await this.quoteRequestSnapshotModel.create({ aggregateId, version, state });
  }
}
```

 * **Modifying the Aggregate**: The QuoteRequest aggregate needs to track its version (number of events applied). We'll also need a way to serialize its state for saving in the snapshot and to hydrate from a snapshot.

```
   // src/quotes/domain/quote-request.aggregate.ts

import { AggregateRoot } from '@nestjs/cqrs';
import { v4 as uuidv4 } from 'uuid';
import { QuoteRequestedEvent } from '../events/quote-requested.event';
import { RequestQuoteCommand } from '../commands/request-quote.command';
import { QuoteCalculatedEvent } from '../events/quote-calculated.event';

export class QuoteRequest extends AggregateRoot {
  private id: string;
  private userId: string;
  private coverageOptions: string[];
  private age: number;
  private location: string;
  private status: string;
  private premiumAmount?: number;
  private version = 0; // Track the number of events applied

  constructor(id: string) {
    super();
    this.id = id;
  }

  static create(command: RequestQuoteCommand & { quoteId: string }): QuoteRequest {
    const quoteRequest = new QuoteRequest(command.quoteId);
    const eventId = uuidv4();
    quoteRequest.apply(
      new QuoteRequestedEvent({ eventId, aggregateId: command.quoteId, ...command, timestamp: new Date() }),
    );
    return quoteRequest;
  }

  applyQuoteRequestedEvent(event: QuoteRequestedEvent) {
    this.userId = event.userId;
    this.coverageOptions = event.coverageOptions;
    this.age = event.age;
    this.location = event.location;
    this.status = 'Requested';
    this.version++;
  }

  calculatePremium(premiumAmount: number): void {
    const eventId = uuidv4();
    this.apply(new QuoteCalculatedEvent({ eventId, aggregateId: this.id, premiumAmount, timestamp: new Date() }));
  }

  applyQuoteCalculatedEvent(event: QuoteCalculatedEvent) {
    this.premiumAmount = event.premiumAmount;
    this.status = 'Calculated';
    this.version++;
  }

  getVersion(): number {
    return this.version;
  }

  getState(): any {
    return {
      id: this.id,
      userId: this.userId,
      coverageOptions: this.coverageOptions,
      age: this.age,
      location: this.location,
      status: this.status,
      premiumAmount: this.premiumAmount,
      version: this.version,
    };
  }

  loadFromSnapshot(snapshot: any): void {
    this.id = snapshot.id;
    this.userId = snapshot.userId;
    this.coverageOptions = snapshot.coverageOptions;
    this.age = snapshot.age;
    this.location = snapshot.location;
    this.status = snapshot.status;
    this.premiumAmount = snapshot.premiumAmount;
    this.version = snapshot.version;
  }
}
```

 * **Modifying the Repository**: The QuoteRepository needs to incorporate snapshotting logic when loading and saving aggregates.
```
   // src/quotes/repositories/quote.repository.ts

import { Injectable, Inject } from '@nestjs/common';
import { EventStore } from '@nestjs/event-sourcing'; // Assuming you're using NestJS Event Sourcing module
import { QuoteRequest } from '../domain/quote-request.aggregate';
import { QuoteRequestSnapshotRepository } from './quote-request-snapshot.repository';

@Injectable()
export class QuoteRepository {
  constructor(
    private readonly eventStore: EventStore,
    private readonly snapshotRepository: QuoteRequestSnapshotRepository,
  ) {}

  async findOneById(aggregateId: string): Promise<QuoteRequest | undefined> {
    const snapshot = await this.snapshotRepository.getLatest(aggregateId);
    const quoteRequest = new QuoteRequest(aggregateId);
    let events;

    if (snapshot) {
      quoteRequest.loadFromSnapshot(snapshot.state);
      events = await this.eventStore.getEventsByAggregateId(aggregateId, snapshot.version + 1);
    } else {
      events = await this.eventStore.getEventsByAggregateId(aggregateId);
    }

    quoteRequest.loadFromHistory(events);
    return quoteRequest;
  }

  async save(aggregate: QuoteRequest): Promise<void> {
    await this.eventStore.publish(aggregate.getUncommittedEvents());
    aggregate.commit();

    // Decide if a snapshot should be taken
    if (aggregate.getVersion() % 100 === 0) { // Example threshold
      await this.snapshotRepository.save(
        aggregate.getId(),
        aggregate.getVersion(),
        aggregate.getState(),
      );
    }
  }
}
```

### Key Considerations:
 * **Snapshot Frequency**: Choosing the right snapshot frequency is crucial. Too frequent snapshots might add unnecessary overhead, while too infrequent snapshots might not provide significant performance benefits. We'll need to experiment to find the optimal threshold for your application.
 * **Snapshot Content**: Decide which parts of the aggregate's state are essential to include in the snapshot. Avoid including transient or easily recalculable data.
 * **Snapshot Consistency**: Ensure that the snapshot is taken at a consistent point in the aggregate's lifecycle (e.g., after all events for a command have been applied).
 * **Testing**: Thoroughly test the snapshotting implementation to ensure it correctly saves and restores the aggregate's state and doesn't introduce any inconsistencies.
 * **Schema Evolution**: Consider how we will handle changes to your aggregate's state over time and ensure that the snapshot schema can accommodate these changes or implement migration strategies.
   By implementing snapshotting, we can significantly improve the performance of loading and processing commands for aggregates with a long event history in your Event Sourcing System.
