For a healthcare insurance broker dashboard, $facet is perfect for the  use case. It lets us calculate the "big number" metrics and all the drill-down breakdowns in one efficient query.

However, the critical success factor is role-based access control (RBAC). We must enforce data visibility before the $facet stage. Here is exactly how to structure it.

The Aggregation Pipeline Structure

```javascript
db.applications.aggregate([
  // 1. ENFORCE RBAC - Filter based on broker's access level
  {
    $match: {
      $or: [
        { brokerId: currentUserId }, // Personal applications
        { teamId: { $in: user.teamIds } }, // Team access
        { region: { $in: user.accessibleRegions } } // Regional access
      ],
      // Apply user-selected filters (state, marketCategory, status)
      ...(filters.state && { state: filters.state }),
      ...(filters.marketCategory && { marketCategory: filters.marketCategory }),
      ...(filters.status && { status: filters.status })
    }
  },

  // 2. RUN ALL METRICS & DRILL-DOWNS IN PARALLEL
  {
    $facet: {
      // KPI: Total applications count
      "totalApplications": [
        { $count: "value" }
      ],
      
      // Drill-down: Count by State
      "breakdownByState": [
        { $group: { _id: "$state", count: { $sum: 1 } } },
        { $sort: { count: -1 } }
      ],
      
      // Drill-down: Count by County
      "breakdownByCounty": [
        { $group: { _id: "$county", count: { $sum: 1 } } },
        { $sort: { count: -1 } },
        { $limit: 20 } // Limit for UI performance
      ],
      
      // Drill-down: Count by Market Category
      "breakdownByMarket": [
        { $group: { _id: "$marketCategory", count: { $sum: 1 } } },
        { $sort: { count: -1 } }
      ],
      
      // Drill-down: Count by Application Status
      "breakdownByStatus": [
        { $group: { _id: "$status", count: { $sum: 1 } } },
        { $sort: { count: -1 } }
      ],
      
      // Optional: Recent applications for the activity feed
      "recentApplications": [
        { $sort: { createdAt: -1 } },
        { $limit: 10 },
        { $project: { applicantName: 1, state: 1, status: 1, createdAt: 1 } }
      ]
    }
  },

  // 3. CLEAN UP THE RESPONSE
  {
    $project: {
      totalApplications: { $ifNull: [{ $arrayElemAt: ["$totalApplications.value", 0] }, 0] },
      breakdownByState: 1,
      breakdownByCounty: 1,
      breakdownByMarket: 1,
      breakdownByStatus: 1,
      recentApplications: 1
    }
  }
])
```

Critical Implementation Details for Your Broker Dashboard

1. Access Control Strategy

· Region-based: If brokers own specific territories, filter by state or region.
· Team-based: Use teamId for agency hierarchies.
· Broker-specific: Use brokerId for personal ownership.
· Hint: Store access rules on the broker's profile and use $lookup if necessary.

2. Dynamic User Filters
Pass user-selected filters (state, market, status) into the initial $match. This reduces the dataset before $facet, dramatically improving performance—the most important optimization for this pattern.

3. Indexing for Speed
Create a compound index covering the RBAC and filter fields:

```javascript
db.applications.createIndex({ 
  brokerId: 1, state: 1, marketCategory: 1, status: 1 
})
```

4. Handling the 16MB Limit
If we have thousands of counties/states, the response could exceed 16MB. Use $limit on breakdowns (e.g., top 50) or allow clients to request detailed drill-downs via separate queries.

## When to Avoid $facet for This Dashboard

· If we have millions of applications and need sub-second responses, $facet may be too heavy. Instead, pre-aggregate using materialized views with $merge (updated daily/hourly).
· If drill-downs require drastically different time ranges (e.g., "last 7 days" vs. "all-time"), process them separately to avoid $facet scanning unnecessary documents.

## Quick Alternative for Simpler Needs

If performance becomes an issue, you can run these as parallel Promise.all queries in your Node.js/Java backend—though $facet is almost always more efficient since it scans the data once.

