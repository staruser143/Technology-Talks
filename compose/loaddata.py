from pymongo import MongoClient
from datetime import datetime

client = MongoClient("mongodb://localhost:27017/")
db = client["db1"]

# Sample quoteEvents
quote_events = [
    {"quoteId": "Q123", "amount": 5000, "timestamp": datetime.utcnow()},
    {"quoteId": "Q124", "amount": 7000, "timestamp": datetime.utcnow()}
]
db.quoteEvents.insert_many(quote_events)
print("✅ Inserted quoteEvents")

# Sample profileEvents
profile_events = [
    {"profileId": "P001", "name": "Alice", "timestamp": datetime.utcnow()},
    {"profileId": "P002", "name": "Bob", "timestamp": datetime.utcnow()}
]
db.profileEvents.insert_many(profile_events)
print("✅ Inserted profileEvents")