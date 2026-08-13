Here's a **complete OpenAPI 3.0.1 specification** for all the broker dashboard endpoints. I've included detailed schemas, examples, and security definitions.

---

## 📄 OpenAPI Specification

```yaml
openapi: 3.0.1
info:
  title: Broker Insurance Dashboard API
  description: |
    API for healthcare insurance broker dashboard providing hierarchy management,
    real-time metrics, and drill-down analytics.
  version: 1.0.0
  contact:
    name: API Support
    email: api-support@insurance.com
  license:
    name: Proprietary
    url: https://insurance.com/terms

servers:
  - url: https://api.insurance.com/v1
    description: Production API
  - url: https://staging-api.insurance.com/v1
    description: Staging environment

tags:
  - name: Hierarchy
    description: Organization tree and team management
  - name: Dashboard
    description: Main dashboard metrics and KPIs
  - name: Drilldown
    description: Drill-down analytics and detailed breakdowns
  - name: Transactions
    description: Transaction listing and search
  - name: Filters
    description: Available filters and configuration

security:
  - bearerAuth: []

paths:
  # ==================== HIERARCHY ENDPOINTS ====================
  /hierarchy/tree:
    get:
      tags:
        - Hierarchy
      summary: Get user's organization tree
      description: Returns the full hierarchy tree for the authenticated user including ancestors and descendants
      operationId: getHierarchyTree
      responses:
        '200':
          description: Organization tree retrieved successfully
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/HierarchyTreeResponse'
              example:
                user:
                  id: 4
                  name: "Jane Smith"
                  role: "broker"
                  license: "TX-78901"
                ancestors:
                  - id: 3
                    name: "Dallas Downtown Office"
                    type: "agency"
                    depth: 1
                  - id: 2
                    name: "Texas Regional Agency"
                    type: "agency"
                    depth: 2
                descendants:
                  immediate:
                    - id: 6
                      name: "John Doe"
                      role: "broker"
                      type: "individual"
                  full_tree:
                    - id: 6
                      name: "John Doe"
                      depth: 2
                      parent_id: 4
                    - id: 7
                      name: "Houston Satellite"
                      depth: 3
                      parent_id: 6
                permissions:
                  can_view_downstream: true
                  can_manage: false
                  max_depth: 3
        '401':
          $ref: '#/components/responses/Unauthorized'
        '403':
          $ref: '#/components/responses/Forbidden'

  /api/team:
    get:
      tags:
        - Hierarchy
      summary: Get team members
      description: Returns team members within the user's hierarchy for dropdowns and filters
      operationId: getTeamMembers
      parameters:
        - name: depth
          in: query
          schema:
            type: integer
            minimum: 1
            maximum: 10
            default: 1
          description: Maximum hierarchy depth to include (1 = immediate team)
        - name: role
          in: query
          schema:
            type: string
            enum: [broker, agency, all]
            default: all
          description: Filter by role type
        - name: active_only
          in: query
          schema:
            type: boolean
            default: true
          description: Include only active team members
      responses:
        '200':
          description: Team members retrieved successfully
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/TeamResponse'
              example:
                team_members:
                  - id: 4
                    name: "Jane Smith"
                    role: "broker"
                    level: 0
                    license: "TX-78901"
                  - id: 6
                    name: "John Doe"
                    role: "broker"
                    level: 1
                    license: "TX-45678"
                total: 2
                max_depth_reached: 1
        '401':
          $ref: '#/components/responses/Unauthorized'

  # ==================== DASHBOARD ENDPOINTS ====================
  /dashboard/summary:
    get:
      tags:
        - Dashboard
      summary: Get main dashboard summary
      description: Returns KPI cards with totals, trends, and applied filters
      operationId: getDashboardSummary
      parameters:
        - $ref: '#/components/parameters/Period'
        - $ref: '#/components/parameters/State'
        - $ref: '#/components/parameters/MarketCategory'
        - $ref: '#/components/parameters/Status'
      responses:
        '200':
          description: Dashboard summary retrieved successfully
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/DashboardSummaryResponse'
              example:
                period: "2026-08"
                summary:
                  total_applications: 845
                  total_renewals: 234
                  total_commissions: 45678.90
                  total_premium: 1234567.89
                  conversion_rate: 68.4
                  avg_commission_per_app: 54.06
                trends:
                  applications:
                    current: 845
                    previous: 723
                    change: 16.8
                  commissions:
                    current: 45678.90
                    previous: 38900.00
                    change: 17.4
                filters_applied:
                  state: "TX"
                  market_category: null
                  status: null
                last_updated: "2026-08-13T14:30:00Z"
        '401':
          $ref: '#/components/responses/Unauthorized'
        '403':
          $ref: '#/components/responses/Forbidden'

  /dashboard/breakdowns:
    get:
      tags:
        - Dashboard
      summary: Get breakdown data for charts
      description: Returns dimension-based breakdowns (state, market category, status, etc.) for visualization
      operationId: getDashboardBreakdowns
      parameters:
        - name: dimension
          in: query
          required: true
          schema:
            type: string
            enum: [state, market_category, status, broker, product]
          description: Dimension to break down by
        - $ref: '#/components/parameters/Period'
        - name: sort
          in: query
          schema:
            type: string
            enum: [count, commission, premium]
            default: count
          description: Sort field
        - name: limit
          in: query
          schema:
            type: integer
            minimum: 1
            maximum: 100
            default: 10
          description: Maximum number of breakdown items
        - $ref: '#/components/parameters/State'
        - $ref: '#/components/parameters/MarketCategory'
        - $ref: '#/components/parameters/Status'
      responses:
        '200':
          description: Breakdown data retrieved successfully
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/BreakdownResponse'
              example:
                dimension: "state"
                period: "2026-08"
                breakdowns:
                  - key: "TX"
                    count: 345
                    commissions: 23456.78
                    premium: 567890.12
                    percentage: 40.8
                    trend: 12.3
                  - key: "CA"
                    count: 256
                    commissions: 17890.45
                    premium: 456789.34
                    percentage: 30.3
                    trend: -3.2
                metadata:
                  total_count: 845
                  total_commissions: 45678.90
                  total_premium: 1234567.89
        '401':
          $ref: '#/components/responses/Unauthorized'

  /dashboard/trends:
    get:
      tags:
        - Dashboard
      summary: Get time-series trend data
      description: Returns historical performance data for line charts
      operationId: getDashboardTrends
      parameters:
        - name: metric
          in: query
          required: true
          schema:
            type: string
            enum: [applications, commissions, premium, renewals]
          description: Metric to chart
        - name: granularity
          in: query
          schema:
            type: string
            enum: [daily, weekly, monthly]
            default: daily
          description: Time granularity
        - name: period
          in: query
          schema:
            type: string
            enum:
              - current_month
              - previous_month
              - last_90_days
              - last_180_days
              - year_to_date
            default: current_month
          description: Date range
        - $ref: '#/components/parameters/State'
        - $ref: '#/components/parameters/MarketCategory'
      responses:
        '200':
          description: Trend data retrieved successfully
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/TrendResponse'
              example:
                metric: "applications"
                granularity: "daily"
                data_points:
                  - date: "2026-08-01"
                    value: 28
                    cumulative: 28
                  - date: "2026-08-02"
                    value: 32
                    cumulative: 60
                summary:
                  total: 845
                  average: 28.2
                  max: 45
                  min: 18
                  trend: 8.5
        '401':
          $ref: '#/components/responses/Unauthorized'

  # ==================== DRILLDOWN ENDPOINTS ====================
  /dashboard/drilldown:
    get:
      tags:
        - Drilldown
      summary: Drill into specific segment
      description: Get detailed breakdown when user clicks a specific segment (e.g., clicking "TX" state)
      operationId: drillDown
      parameters:
        - name: dimension
          in: query
          required: true
          schema:
            type: string
            enum: [state, market_category, status, broker, product]
          description: Dimension of the selected segment
        - name: key
          in: query
          required: true
          schema:
            type: string
          description: Key value of the selected segment (e.g., "TX" for state)
        - name: sub_dimension
          in: query
          required: true
          schema:
            type: string
            enum: [market_category, broker, product, status, state]
          description: Sub-dimension to break down within the selected segment
        - $ref: '#/components/parameters/Period'
        - $ref: '#/components/parameters/State'
        - $ref: '#/components/parameters/MarketCategory'
        - $ref: '#/components/parameters/Status'
      responses:
        '200':
          description: Drill-down data retrieved successfully
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/DrilldownResponse'
              example:
                segment:
                  dimension: "state"
                  key: "TX"
                  total_count: 345
                  total_commissions: 23456.78
                sub_breakdown:
                  by_market_category:
                    - key: "PPO"
                      count: 210
                      commissions: 14567.89
                    - key: "HMO"
                      count: 135
                      commissions: 8888.89
                  by_broker:
                    - id: 6
                      name: "John Doe"
                      count: 89
                      commissions: 6234.56
                    - id: 4
                      name: "Jane Smith"
                      count: 67
                      commissions: 4567.89
                filters_applied:
                  state: "TX"
                  period: "current_month"
        '400':
          description: Invalid parameters
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'
        '401':
          $ref: '#/components/responses/Unauthorized'

  # ==================== TRANSACTIONS ENDPOINTS ====================
  /transactions:
    get:
      tags:
        - Transactions
      summary: Get transaction list
      description: Returns paginated transaction list with sorting and filtering
      operationId: getTransactions
      parameters:
        - name: page
          in: query
          schema:
            type: integer
            minimum: 1
            default: 1
          description: Page number
        - name: limit
          in: query
          schema:
            type: integer
            minimum: 1
            maximum: 100
            default: 25
          description: Items per page
        - name: sort_by
          in: query
          schema:
            type: string
            enum: [application_date, premium, commission, status]
            default: application_date
          description: Sort field
        - name: sort_order
          in: query
          schema:
            type: string
            enum: [asc, desc]
            default: desc
          description: Sort order
        - name: broker_id
          in: query
          schema:
            type: integer
          description: Filter by specific broker ID
        - name: date_from
          in: query
          schema:
            type: string
            format: date
          description: Filter from date (YYYY-MM-DD)
        - name: date_to
          in: query
          schema:
            type: string
            format: date
          description: Filter to date (YYYY-MM-DD)
        - $ref: '#/components/parameters/State'
        - $ref: '#/components/parameters/MarketCategory'
        - $ref: '#/components/parameters/Status'
      responses:
        '200':
          description: Transactions retrieved successfully
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/TransactionListResponse'
              example:
                pagination:
                  page: 1
                  limit: 25
                  total: 345
                  pages: 14
                transactions:
                  - policy_number: "POL-2026-001"
                    broker:
                      id: 6
                      name: "John Doe"
                      license: "TX-45678"
                    applicant:
                      name: "Mary Johnson"
                      dob: "1985-03-15"
                    product:
                      name: "PPO Silver"
                      category: "PPO"
                    state: "TX"
                    application_date: "2026-08-10"
                    status: "approved"
                    premium: 1250.00
                    commission: 106.25
                summary:
                  total_premium: 431250.00
                  total_commission: 36656.25
                  avg_commission: 106.25
        '401':
          $ref: '#/components/responses/Unauthorized'

  # ==================== FILTERS ENDPOINTS ====================
  /filters:
    get:
      tags:
        - Filters
      summary: Get available filters
      description: Returns all available filter values for dropdowns based on user's hierarchy
      operationId: getAvailableFilters
      responses:
        '200':
          description: Available filters retrieved successfully
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/FiltersResponse'
              example:
                states: ["TX", "CA", "FL", "NY"]
                market_categories: ["PPO", "HMO", "DENTAL", "VISION"]
                statuses: ["approved", "pending", "rejected", "in_review"]
                brokers:
                  - id: 4
                    name: "Jane Smith"
                  - id: 6
                    name: "John Doe"
                products:
                  - name: "PPO Silver"
                    category: "PPO"
                  - name: "HMO Gold"
                    category: "HMO"
                periods:
                  current_month: "2026-08"
                  previous_month: "2026-07"
                  last_90_days: "2026-05-15 to 2026-08-13"
        '401':
          $ref: '#/components/responses/Unauthorized'

  /permissions:
    get:
      tags:
        - Filters
      summary: Get user permissions
      description: Returns user permissions to control UI feature visibility
      operationId: getUserPermissions
      responses:
        '200':
          description: User permissions retrieved successfully
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/PermissionsResponse'
              example:
                can_view_dashboard: true
                can_see_commissions: true
                can_see_downstream_data: true
                can_see_sensitive_metrics: false
                visible_filters: ["state", "market_category", "status"]
                visible_charts: ["applications_trend", "commission_breakdown", "state_heatmap"]
                max_hierarchy_depth: 3
        '401':
          $ref: '#/components/responses/Unauthorized'

# ==================== COMPONENTS ====================
components:
  securitySchemes:
    bearerAuth:
      type: http
      scheme: bearer
      bearerFormat: JWT
      description: JWT token with user permissions

  parameters:
    Period:
      name: period
      in: query
      schema:
        type: string
        enum: [current_month, previous_month, last_90_days, last_180_days, year_to_date]
        default: current_month
      description: Time period for data aggregation
    
    State:
      name: state
      in: query
      schema:
        type: string
        minLength: 2
        maxLength: 2
      description: US state code (e.g., TX, CA)
    
    MarketCategory:
      name: market_category
      in: query
      schema:
        type: string
        enum: [PPO, HMO, DENTAL, VISION, ALL]
      description: Product market category
    
    Status:
      name: status
      in: query
      schema:
        type: string
        enum: [approved, pending, rejected, in_review, all]
        default: all
      description: Application status filter

  responses:
    Unauthorized:
      description: Unauthorized - Invalid or missing JWT token
      content:
        application/json:
          schema:
            $ref: '#/components/schemas/ErrorResponse'
          example:
            error: "Unauthorized"
            message: "Missing or invalid JWT token"
            status: 401
    
    Forbidden:
      description: Forbidden - Insufficient permissions
      content:
        application/json:
          schema:
            $ref: '#/components/schemas/ErrorResponse'
          example:
            error: "Forbidden"
            message: "Access denied for the requested resource"
            status: 403

  schemas:
    # ====== ERROR SCHEMA ======
    ErrorResponse:
      type: object
      properties:
        error:
          type: string
          example: "Bad Request"
        message:
          type: string
          example: "Invalid parameter value"
        status:
          type: integer
          example: 400
        timestamp:
          type: string
          format: date-time
          example: "2026-08-13T14:30:00Z"
        path:
          type: string
          example: "/dashboard/breakdowns"

    # ====== HIERARCHY SCHEMAS ======
    HierarchyNode:
      type: object
      properties:
        id:
          type: integer
          description: Party ID
        name:
          type: string
          description: Display name
        type:
          type: string
          enum: [agency, individual]
        role:
          type: string
          enum: [broker, agency_manager, admin]
        license:
          type: string
        depth:
          type: integer
          description: Depth in hierarchy
        parent_id:
          type: integer
          description: Parent party ID

    HierarchyTreeResponse:
      type: object
      properties:
        user:
          $ref: '#/components/schemas/HierarchyNode'
        ancestors:
          type: array
          items:
            $ref: '#/components/schemas/HierarchyNode'
        descendants:
          type: object
          properties:
            immediate:
              type: array
              items:
                $ref: '#/components/schemas/HierarchyNode'
            full_tree:
              type: array
              items:
                $ref: '#/components/schemas/HierarchyNode'
        permissions:
          type: object
          properties:
            can_view_downstream:
              type: boolean
            can_manage:
              type: boolean
            max_depth:
              type: integer

    TeamResponse:
      type: object
      properties:
        team_members:
          type: array
          items:
            allOf:
              - $ref: '#/components/schemas/HierarchyNode'
              - type: object
                properties:
                  level:
                    type: integer
        total:
          type: integer
        max_depth_reached:
          type: integer

    # ====== DASHBOARD SCHEMAS ======
    DashboardSummaryResponse:
      type: object
      properties:
        period:
          type: string
          example: "2026-08"
        summary:
          type: object
          properties:
            total_applications:
              type: integer
            total_renewals:
              type: integer
            total_commissions:
              type: number
              format: double
            total_premium:
              type: number
              format: double
            conversion_rate:
              type: number
              format: double
            avg_commission_per_app:
              type: number
              format: double
        trends:
          type: object
          additionalProperties:
            type: object
            properties:
              current:
                type: number
              previous:
                type: number
              change:
                type: number
                format: double
        filters_applied:
          type: object
          properties:
            state:
              type: string
              nullable: true
            market_category:
              type: string
              nullable: true
            status:
              type: string
              nullable: true
        last_updated:
          type: string
          format: date-time

    BreakdownItem:
      type: object
      properties:
        key:
          type: string
          description: Dimension value (e.g., "TX" for state)
        count:
          type: integer
          description: Number of applications
        commissions:
          type: number
          format: double
        premium:
          type: number
          format: double
        percentage:
          type: number
          format: double
          description: Percentage of total
        trend:
          type: number
          format: double
          description: Percentage change from previous period

    BreakdownResponse:
      type: object
      properties:
        dimension:
          type: string
          description: Breakdown dimension
        period:
          type: string
        breakdowns:
          type: array
          items:
            $ref: '#/components/schemas/BreakdownItem'
        metadata:
          type: object
          properties:
            total_count:
              type: integer
            total_commissions:
              type: number
              format: double
            total_premium:
              type: number
              format: double

    TrendDataPoint:
      type: object
      properties:
        date:
          type: string
          format: date
        value:
          type: number
          format: double
        cumulative:
          type: number
          format: double

    TrendResponse:
      type: object
      properties:
        metric:
          type: string
        granularity:
          type: string
          enum: [daily, weekly, monthly]
        data_points:
          type: array
          items:
            $ref: '#/components/schemas/TrendDataPoint'
        summary:
          type: object
          properties:
            total:
              type: number
              format: double
            average:
              type: number
              format: double
            max:
              type: number
              format: double
            min:
              type: number
              format: double
            trend:
              type: number
              format: double
              description: Overall trend percentage

    # ====== DRILLDOWN SCHEMAS ======
    DrilldownResponse:
      type: object
      properties:
        segment:
          type: object
          properties:
            dimension:
              type: string
            key:
              type: string
            total_count:
              type: integer
            total_commissions:
              type: number
              format: double
        sub_breakdown:
          type: object
          additionalProperties:
            type: array
            items:
              oneOf:
                - $ref: '#/components/schemas/BreakdownItem'
                - type: object
                  properties:
                    id:
                      type: integer
                    name:
                      type: string
                    count:
                      type: integer
                    commissions:
                      type: number
                      format: double
        filters_applied:
          type: object
          properties:
            state:
              type: string
              nullable: true
            period:
              type: string
            market_category:
              type: string
              nullable: true
            status:
              type: string
              nullable: true

    # ====== TRANSACTION SCHEMAS ======
    Transaction:
      type: object
      properties:
        policy_number:
          type: string
          example: "POL-2026-001"
        broker:
          type: object
          properties:
            id:
              type: integer
            name:
              type: string
            license:
              type: string
        applicant:
          type: object
          properties:
            name:
              type: string
            dob:
              type: string
              format: date
        product:
          type: object
          properties:
            name:
              type: string
            category:
              type: string
        state:
          type: string
          minLength: 2
          maxLength: 2
        application_date:
          type: string
          format: date
        status:
          type: string
          enum: [approved, pending, rejected, in_review]
        premium:
          type: number
          format: double
        commission:
          type: number
          format: double

    TransactionListResponse:
      type: object
      properties:
        pagination:
          type: object
          properties:
            page:
              type: integer
            limit:
              type: integer
            total:
              type: integer
            pages:
              type: integer
        transactions:
          type: array
          items:
            $ref: '#/components/schemas/Transaction'
        summary:
          type: object
          properties:
            total_premium:
              type: number
              format: double
            total_commission:
              type: number
              format: double
            avg_commission:
              type: number
              format: double

    # ====== FILTERS SCHEMAS ======
    FiltersResponse:
      type: object
      properties:
        states:
          type: array
          items:
            type: string
            minLength: 2
            maxLength: 2
        market_categories:
          type: array
          items:
            type: string
            enum: [PPO, HMO, DENTAL, VISION]
        statuses:
          type: array
          items:
            type: string
            enum: [approved, pending, rejected, in_review]
        brokers:
          type: array
          items:
            type: object
            properties:
              id:
                type: integer
              name:
                type: string
        products:
          type: array
          items:
            type: object
            properties:
              name:
                type: string
              category:
                type: string
        periods:
          type: object
          additionalProperties:
            type: string

    PermissionsResponse:
      type: object
      properties:
        can_view_dashboard:
          type: boolean
        can_see_commissions:
          type: boolean
        can_see_downstream_data:
          type: boolean
        can_see_sensitive_metrics:
          type: boolean
        visible_filters:
          type: array
          items:
            type: string
        visible_charts:
          type: array
          items:
            type: string
        max_hierarchy_depth:
          type: integer

# ==================== EXTENSIONS ====================
externalDocs:
  description: API Documentation Portal
  url: https://docs.insurance.com/broker-api
```

---

## 🧪 Testing the API with Swagger UI

### Sample cURL Commands:

```bash
# 1. Get dashboard summary
curl -X GET "https://api.insurance.com/v1/dashboard/summary?period=current_month&state=TX" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# 2. Get breakdown by state
curl -X GET "https://api.insurance.com/v1/dashboard/breakdowns?dimension=state&period=current_month&limit=5" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# 3. Drill down into Texas
curl -X GET "https://api.insurance.com/v1/dashboard/drilldown?dimension=state&key=TX&sub_dimension=market_category" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# 4. Get transactions with filters
curl -X GET "https://api.insurance.com/v1/transactions?page=1&limit=20&status=approved&state=TX" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

## 📱 Frontend Integration Example

```javascript
// OpenAPI Generator can generate TypeScript client
import { DashboardApi, Configuration } from './generated/api';

const config = new Configuration({
    basePath: 'https://api.insurance.com/v1',
    accessToken: userJWT
});

const dashboardApi = new DashboardApi(config);

// Load main dashboard
async function loadDashboard() {
    const summary = await dashboardApi.getDashboardSummary({
        period: 'current_month',
        state: 'TX'
    });
    
    const breakdowns = await dashboardApi.getDashboardBreakdowns({
        dimension: 'state',
        period: 'current_month'
    });
    
    renderKPIs(summary.data.summary);
    renderPieChart(breakdowns.data.breakdowns);
}
```

---

## ✅ Validation & Compliance

1. **All endpoints require HTTPS** (enforced via server URL)
2. **JWT tokens** contain:
   ```json
   {
     "sub": 4,
     "party_id": 4,
     "roles": ["broker"],
     "permissions": ["view_own", "view_downstream"],
     "exp": 1234567890
   }
   ```
3. **Rate limiting** – 1000 requests per 5 minutes per user
4. **Response caching headers** – `Cache-Control: max-age=300` for dashboard endpoints
5. **All date fields** in ISO 8601 format

---

