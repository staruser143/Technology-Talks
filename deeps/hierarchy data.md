# Accessing Agency Hierarchial Data
 Since the hierarchy lives outside MongoDB, we have two solid approaches.

---

## Option 1: Pre-compute Access Rules (Recommended)

Before hitting MongoDB, resolve the user's entire hierarchy in the backend and generate a flat list of accessible IDs.

```javascript
// BACKEND (Node.js example)
async function getBrokerDashboard(userId, filters) {
  // 1. Resolve hierarchy from external system
  const access = await externalHierarchyAPI.getAccessTree(userId);
  // Returns: { brokerIds: ['b1','b2','b3'], teamIds: ['t1','t2'], regionIds: ['r1'] }
  
  // 2. Build MongoDB query with flat lists
  const pipeline = [
    {
      $match: {
        $or: [
          { brokerId: { $in: access.brokerIds } },
          { teamId: { $in: access.teamIds } },
          { regionId: { $in: access.regionIds } }
        ],
        ...(filters.state && { state: filters.state }),
        ...(filters.marketCategory && { marketCategory: filters.marketCategory }),
        ...(filters.status && { status: filters.status })
      }
    },
    {
      $facet: {
        "totalApplications": [{ $count: "value" }],
        "breakdownByState": [
          { $group: { _id: "$state", count: { $sum: 1 } } },
          { $sort: { count: -1 } }
        ],
        "breakdownByMarket": [
          { $group: { _id: "$marketCategory", count: { $sum: 1 } } },
          { $sort: { count: -1 } }
        ],
        "breakdownByStatus": [
          { $group: { _id: "$status", count: { $sum: 1 } } },
          { $sort: { count: -1 } }
        ]
      }
    },
    {
      $project: {
        totalApplications: { $ifNull: [{ $arrayElemAt: ["$totalApplications.value", 0] }, 0] },
        breakdownByState: 1,
        breakdownByMarket: 1,
        breakdownByStatus: 1
      }
    }
  ];
  
  return await db.collection('applications').aggregate(pipeline).next();
}
```

- Pros: Simple, fast, uses indexes efficiently, easy to cache.
- Cons: If a broker manages 10,000+ sub-brokers, the $in array gets large (but MongoDB handles this well up to ~50,000 IDs).

---

## Option 2: Hierarchy-Aware Grouping with $lookup (If Hierarchy Were in MongoDB)

If we could store hierarchy in MongoDB, we'd use $lookup to join and aggregate at each level:

```javascript
db.applications.aggregate([
  {
    $lookup: {
      from: "brokerHierarchy",
      localField: "brokerId",
      foreignField: "brokerId",
      as: "brokerInfo"
    }
  },
  { $unwind: "$brokerInfo" },
  {
    $match: {
      $or: [
        { "brokerInfo.agencyPath": /^LargeAgency/ }, // All under this agency
        { "brokerInfo.teamId": user.teamId }
      ]
    }
  },
  // ... then $facet for metrics
])
```

But since the hierarchy is external, this isn't viable.

---

## Best Practice: Hybrid Approach with Caching

For a production dashboard, implement this pattern:

```javascript
class BrokerDashboardService {
  async getDashboard(userId, filters) {
    // 1. Get hierarchy with caching (5-minute TTL)
    const access = await this.getCachedAccess(userId);
    
    // 2. Build MongoDB query
    const metrics = await this.queryMetrics(access, filters);
    
    // 3. Add hierarchy metadata for UI
    return {
      userLevel: access.level, // 'agency', 'subagency', 'broker'
      accessibleCount: access.totalBrokers,
      metrics: metrics,
      drillDowns: this.getAvailableDrillDowns(access.level)
    };
  }
  
  async getCachedAccess(userId) {
    const cacheKey = `access:${userId}`;
    let access = await redis.get(cacheKey);
    if (!access) {
      access = await externalHierarchyAPI.resolve(userId);
      await redis.setex(cacheKey, 300, JSON.stringify(access));
    }
    return access;
  }
}
```

---

## Critical Considerations for the Hierarchy

1. **Aggregate vs. Individual Metrics**

- Agency-level users see totals across all sub-agencies and brokers.
- Broker-level users see only their own applications.
· The $facet automatically handles this because the $match stage filters the dataset before aggregation.

2. **Performance Optimization**
If an agency has 50,000+ brokers and the $in array is huge, we can use this alternative:

```javascript
// Instead of $in with 50k IDs, pre-filter via a separate collection
// Create a "userAccess" collection updated via webhook when hierarchy changes
db.userAccess.createIndex({ userId: 1, brokerId: 1 });

// Then query:
db.applications.aggregate([
  {
    $lookup: {
      from: "userAccess",
      let: { brokerId: "$brokerId" },
      pipeline: [
        { $match: { 
          $expr: { $eq: ["$brokerId", "$$brokerId"] },
          userId: currentUserId 
        }},
        { $limit: 1 }
      ],
      as: "access"
    }
  },
  { $match: { access: { $ne: [] } } },
  // ... then $facet
])
```

3. Drill-Down by Hierarchy Level

- When a user clicks "View by County", should they see their agency's county breakdown or all counties? 
- The $facet already handles this—we just have to ensure the initial $match applies the correct access filter.

---

Sample Response for Agency vs. Broker

```javascript
// Agency-level user sees:
{
  userLevel: 'subagency',
  accessibleBrokers: 47,
  metrics: {
    totalApplications: 284, // All 47 brokers combined
    breakdownByState: [{ _id: 'TX', count: 142 }, { _id: 'CA', count: 89 }],
    breakdownByBroker: [{ _id: 'broker123', count: 34 }, ...] // Extra drill-down
  }
}

// Broker-level user sees:
{
  userLevel: 'broker',
  accessibleBrokers: 1,
  metrics: {
    totalApplications: 12, // Only their own
    breakdownByState: [{ _id: 'TX', count: 8 }, { _id: 'CA', count: 4 }]
    // No broker breakdown (irrelevant at this level)
  }
}
```

---

## Recommendation

- Go with Option 1—resolve hierarchy externally, pass flat ID lists to MongoDB, and use $facet for all metrics. 
- It's clean, performant, and keeps the external hierarchy system decoupled from the database.
- Add Redis caching for the access resolution (5-minute TTL) to avoid hitting the external system on every dashboard load.
