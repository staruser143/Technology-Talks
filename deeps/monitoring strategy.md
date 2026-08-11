Excellent! Here's a complete monitoring strategy to help you make a data-driven decision about when to switch to pre-computation.

---

Monitoring Framework Overview

```javascript
// Monitoring Dashboard Structure
{
  metrics: {
    queryPerformance: {},    // Response times
    resourceUsage: {},       // MongoDB CPU, memory
    userBehavior: {},        // Filter patterns, usage frequency
    dataGrowth: {}           // Collection size trends
  },
  thresholds: {},            // Alert triggers
  recommendations: {}        // When to switch
}
```

---

1. Query Performance Monitoring

MongoDB Aggregation Profiler

Enable slow query logging and analyze patterns:

```javascript
// Enable profiling (run once)
db.setProfilingLevel(1, { slowms: 1000 });

// Create monitoring collection
db.dashboardPerformance.createIndex({ userId: 1, timestamp: -1 });

// Wrap your dashboard query with performance tracking
class DashboardMonitor {
  async getDashboard(userId, filters) {
    const start = process.hrtime();
    const memoryBefore = process.memoryUsage().heapUsed;
    
    try {
      const result = await this.executeQuery(userId, filters);
      
      const end = process.hrtime(start);
      const responseTime = end[0] * 1000 + end[1] / 1000000; // ms
      const memoryUsed = (process.memoryUsage().heapUsed - memoryBefore) / 1024 / 1024;
      
      // Log performance metrics
      await this.logPerformance({
        userId,
        responseTime,
        memoryUsed,
        documentCount: result.totalApplications || 0,
        filterString: JSON.stringify(filters),
        timestamp: new Date()
      });
      
      return result;
    } catch (error) {
      await this.logError(userId, error);
      throw error;
    }
  }
  
  async logPerformance(data) {
    await db.dashboardPerformance.insertOne({
      ...data,
      slow: data.responseTime > 2000, // Flag slow queries
      timestamp: new Date()
    });
  }
}
```

Performance Analysis Query

```javascript
// Run daily to check performance trends
db.dashboardPerformance.aggregate([
  {
    $match: {
      timestamp: { $gte: moment().subtract(7, 'days').toDate() }
    }
  },
  {
    $group: {
      _id: { 
        date: { $dateToString: { format: "%Y-%m-%d", date: "$timestamp" } },
        slow: "$slow"
      },
      avgResponseTime: { $avg: "$responseTime" },
      maxResponseTime: { $max: "$responseTime" },
      count: { $sum: 1 }
    }
  },
  {
    $project: {
      date: "$_id.date",
      slow: "$_id.slow",
      avgResponseTime: 1,
      maxResponseTime: 1,
      count: 1,
      percentage: {
        $multiply: [
          { $divide: ["$count", { $sum: "$count" }] },
          100
        ]
      }
    }
  },
  { $sort: { date: 1 } }
])
```

---

2. MongoDB Resource Monitoring

Collection Stats Tracking

```javascript
// Run this every hour
async function trackCollectionStats() {
  const stats = await db.applications.stats();
  const indexStats = await db.applications.aggregate([
    { $indexStats: {} }
  ]).toArray();
  
  await db.resourceMetrics.insertOne({
    collectionSize: stats.size,
    documentCount: stats.count,
    avgDocumentSize: stats.avgObjSize,
    indexesSize: stats.totalIndexSize,
    indexUsage: indexStats.map(idx => ({
      name: idx.name,
      accesses: idx.accesses,
      hits: idx.accesses.ops
    })),
    timestamp: new Date()
  });
}
```

Query Execution Stats

```javascript
// Monitor $facet performance
async function analyzeFacetPerformance() {
  const explain = await db.applications.aggregate([
    { $match: { /* typical filters */ } },
    { $facet: { /* your facets */ } }
  ], { explain: true }).next();
  
  await db.queryExplainCache.insertOne({
    stages: explain.stages,
    totalDocsExamined: explain.stages[0]?.totalDocsExamined || 0,
    totalKeysExamined: explain.stages[0]?.totalKeysExamined || 0,
    executionTimeMillis: explain.stages[0]?.executionTimeMillisEstimate || 0,
    timestamp: new Date()
  });
}
```

---

3. User Behavior Monitoring

Track Query Patterns

```javascript
// Monitor what filters users actually apply
db.filterUsageLog.insertOne({
  userId: currentUserId,
  filters: {
    state: filters.state || 'none',
    marketCategory: filters.marketCategory || 'none',
    status: filters.status || 'none',
    customDateRange: filters.dateRange || false
  },
  hasDrillDown: !!filters.drillDown,
  timestamp: new Date()
});

// Analyze filter patterns (weekly)
db.filterUsageLog.aggregate([
  {
    $match: {
      timestamp: { $gte: moment().subtract(30, 'days').toDate() }
    }
  },
  {
    $group: {
      _id: null,
      totalQueries: { $sum: 1 },
      uniqueUsers: { $addToSet: "$userId" },
      mostCommonFilters: {
        $push: {
          state: "$filters.state",
          marketCategory: "$filters.marketCategory"
        }
      },
      drillDownRate: {
        $avg: { $cond: ["$hasDrillDown", 1, 0] }
      }
    }
  }
])
```

---

4. Real-time Alert System

```javascript
// Monitor and alert when thresholds are breached
class PerformanceAlertSystem {
  async checkThresholds() {
    const recentPerformance = await this.getRecentPerformance();
    
    const alerts = [];
    
    // Alert 1: Slow response times
    if (recentPerformance.avgResponseTime > 3000) {
      alerts.push({
        level: 'CRITICAL',
        metric: 'Response Time',
        message: `Average response time ${recentPerformance.avgResponseTime}ms exceeds 3000ms threshold`,
        recommendation: 'Consider switching to pre-computation'
      });
    }
    
    // Alert 2: High document scans
    if (recentPerformance.avgDocsExamined > 500000) {
      alerts.push({
        level: 'WARNING',
        metric: 'Documents Scanned',
        message: `Scanning ${recentPerformance.avgDocsExamined} documents per query`,
        recommendation: 'Add more specific filters or implement pre-computation'
      });
    }
    
    // Alert 3: Collection growth
    const growthRate = await this.getCollectionGrowthRate();
    if (growthRate > 10) { // >10% monthly growth
      alerts.push({
        level: 'INFO',
        metric: 'Data Growth',
        message: `Collection growing at ${growthRate}% per month`,
        recommendation: 'Plan for pre-computation in next quarter'
      });
    }
    
    // Alert 4: Peak hour performance
    if (this.isPeakHour() && recentPerformance.maxResponseTime > 5000) {
      alerts.push({
        level: 'CRITICAL',
        metric: 'Peak Performance',
        message: 'Peak hour queries exceeding 5 seconds',
        recommendation: 'Immediate pre-computation needed for peak hours'
      });
    }
    
    // Store and optionally send alerts
    await db.performanceAlerts.insertMany(alerts);
    if (alerts.some(a => a.level === 'CRITICAL')) {
      await this.sendSlackAlert(alerts);
    }
  }
}
```

---

5. Monitoring Dashboard Query

Weekly Performance Report

```javascript
// Generate weekly report for decision making
async function generateWeeklyReport() {
  const sevenDays = moment().subtract(7, 'days').toDate();
  
  const report = await db.dashboardPerformance.aggregate([
    { $match: { timestamp: { $gte: sevenDays } } },
    {
      $facet: {
        "performance": [
          {
            $group: {
              _id: null,
              avgResponseTime: { $avg: "$responseTime" },
              p95ResponseTime: { 
                $percentile: { 
                  p: 95, 
                  input: "$responseTime" 
                } 
              },
              slowQueryPercentage: {
                $avg: { $cond: ["$slow", 1, 0] }
              }
            }
          }
        ],
        "usage": [
          {
            $group: {
              _id: "$userId",
              count: { $sum: 1 }
            }
          },
          {
            $group: {
              _id: null,
              dailyAvgUsers: { 
                $avg: { $size: { $setUnion: ["$userId"] } } 
              },
              totalQueries: { $sum: "$count" }
            }
          }
        ],
        "resourceTrend": [
          {
            $group: {
              _id: { 
                day: { $dateToString: { format: "%Y-%m-%d", date: "$timestamp" } }
              },
              avgDocsExamined: { $avg: "$documentCount" }
            }
          },
          { $sort: { "_id.day": 1 } }
        ]
      }
    }
  ]).next();
  
  // Decision logic
  const decisions = {
    switchToPrecomputation: false,
    reason: '',
    urgency: 'low'
  };
  
  const perf = report.performance[0];
  if (perf.avgResponseTime > 3000 || perf.slowQueryPercentage > 0.2) {
    decisions.switchToPrecomputation = true;
    decisions.reason = `Performance degradation: ${perf.avgResponseTime}ms avg, ${perf.slowQueryPercentage*100}% slow queries`;
    decisions.urgency = perf.avgResponseTime > 5000 ? 'high' : 'medium';
  }
  
  if (report.resourceTrend.length > 5) {
    const growth = this.calculateGrowth(report.resourceTrend);
    if (growth > 15) {
      decisions.reason += ` Data growing ${growth}% weekly. `;
      decisions.urgency = 'medium';
    }
  }
  
  return { report, decisions };
}
```

---

6. Implementation Checklist

Phase 1: Setup (Week 1)

```javascript
// 1. Create monitoring collections
db.createCollection('dashboardPerformance');
db.createCollection('resourceMetrics');
db.createCollection('filterUsageLog');
db.createCollection('performanceAlerts');

// 2. Enable MongoDB profiling
db.setProfilingLevel(1, { slowms: 1000 });

// 3. Deploy monitoring wrapper around your dashboard query
// (Add the DashboardMonitor class shown above)

// 4. Create weekly report generator
// (Add the generateWeeklyReport function)
```

Phase 2: Monitor (Week 2-4)

```javascript
// 5. Let it run for 2-4 weeks collecting data
// 6. Check weekly reports each Monday
// 7. Look for patterns:
//    - Are response times degrading?
//    - Which users/filters are slowest?
//    - When are peak hours?
```

Phase 3: Decide (Week 4+)

```javascript
// If ANY of these are true, switch to pre-computation:
if (
  avgResponseTime > 3000 ||           // Slow dashboard
  slowQueryPercentage > 20 ||         // Many slow queries  
  docsExamined > 500000 ||            // Heavy scans
  growthRate > 15 ||                  // Fast growth
  peakHourResponseTime > 5000 ||      // Peak hour issues
  userCount > 1000                    // Many concurrent users
) {
  // Implement pre-computation
}
```

---

Quick Decision Dashboard

```javascript
// Quick health check - run daily
async function getDashboardHealth() {
  const last24h = moment().subtract(24, 'hours').toDate();
  
  const stats = await db.dashboardPerformance.aggregate([
    { $match: { timestamp: { $gte: last24h } } },
    {
      $group: {
        _id: null,
        avgResponseTime: { $avg: "$responseTime" },
        maxResponseTime: { $max: "$responseTime" },
        queryCount: { $sum: 1 },
        slowCount: { $sum: { $cond: ["$slow", 1, 0] } }
      }
    }
  ]).next();
  
  const collectionSize = await db.applications.estimatedDocumentCount();
  
  return {
    status: stats.avgResponseTime < 2000 ? 'HEALTHY' : 'DEGRADED',
    metrics: {
      avgResponseTime: `${stats.avgResponseTime}ms`,
      maxResponseTime: `${stats.maxResponseTime}ms`,
      slowRate: `${(stats.slowCount / stats.queryCount * 100)}%`,
      documentCount: collectionSize
    },
    recommendation: stats.avgResponseTime > 3000 
      ? '⚠️ Consider pre-computation'
      : collectionSize > 1000000
        ? '⚠️ Plan for pre-computation soon'
        : '✅ Real-time queries working well'
  };
}
```

---

Summary

Start monitoring NOW, even if performance is fine today. This gives you historical data to:

1. See degradation trends before they become critical
2. Justify the investment in pre-computation with real numbers
3. Identify specific users/filters causing problems

The 4 Red Flags that trigger pre-computation:

· 🚨 Avg response time > 3 seconds for 3 consecutive days
· 🚨 > 20% of queries are "slow" (over 2 seconds)
· 🚨 MongoDB CPU consistently > 70% during peak hours
· 🚨 Collection grows > 50% in a month

Would you like me to help you set up the alert notifications (Slack/Email) or the pre-computation implementation plan?