```javascript
db.domainCollection.updateOne(
  {
    _id: domainId,
    sequence: event.sequence - 1 // Ensure strict ordering
  },
  {
    $addToSet: { dependents: event.dependent },
    $set: {
      sequence: event.sequence,
      lastEventTimestamp: event.timestamp
    }
  }
);
```
