import React, { useState, useCallback } from 'react';
import './Dashboard.css';

// Types
interface FilterContext {
  dateRange: string;
  state: string;
  agency: string;
  broker: string;
  product: string;
}

interface KPIData {
  label: string;
  value: string;
  change: string;
  isNegative: boolean;
}

interface TableRow {
  agency: string;
  applications: string;
  premium: string;
  commission: string;
  conversionRate: string;
}

interface HierarchyNode {
  id: string;
  label: string;
  level: number;
}

// Sample data
const HIERARCHY_DATA: HierarchyNode[] = [
  { id: 'a1', label: '🏢 Agency ABC', level: 1 },
  { id: 'a1s1', label: '└─ Sub Agency ABC-East', level: 2 },
  { id: 'a1s1b1', label: '├─ Broker John', level: 3 },
  { id: 'a1s1b1p1', label: '├─ Producer Smith', level: 4 },
  { id: 'a1s1b1p2', label: '└─ Producer Johnson', level: 4 },
  { id: 'a1s1b2', label: '└─ Broker Mary', level: 3 },
  { id: 'a1s2', label: '└─ Sub Agency ABC-West', level: 2 },
  { id: 'a1s2b1', label: '└─ Broker James', level: 3 },
  { id: 'a2', label: '🏢 Agency XYZ', level: 1 },
  { id: 'a2s1', label: '└─ Sub Agency XYZ-North', level: 2 },
  { id: 'a2s1b1', label: '├─ Broker Michael', level: 3 },
  { id: 'a2s1b2', label: '└─ Broker Sarah', level: 3 },
  { id: 'a3', label: '🏢 Agency DEF', level: 1 },
  { id: 'a3s1', label: '└─ Sub Agency DEF-Central', level: 2 },
  { id: 'a3s1b1', label: '└─ Broker Robert', level: 3 },
];

const TABLE_DATA: TableRow[] = [
  {
    agency: 'ABC',
    applications: '5,200',
    premium: '$12.0M',
    commission: '$1.4M',
    conversionRate: '72%',
  },
  {
    agency: 'XYZ',
    applications: '8,100',
    premium: '$18.5M',
    commission: '$2.1M',
    conversionRate: '69%',
  },
  {
    agency: 'DEF',
    applications: '3,500',
    premium: '$8.2M',
    commission: '$0.95M',
    conversionRate: '65%',
  },
  {
    agency: 'GHI',
    applications: '4,800',
    premium: '$11.1M',
    commission: '$1.3M',
    conversionRate: '70%',
  },
  {
    agency: 'JKL',
    applications: '3,932',
    premium: '$9.6M',
    commission: '$1.1M',
    conversionRate: '68%',
  },
];

// Header Component
const Header: React.FC = () => (
  <header className="dashboard-header">
    <h1>📊 Insurance Dashboard</h1>
  </header>
);

// Filter Panel Component
interface FilterPanelProps {
  filters: FilterContext;
  onFilterChange: (key: keyof FilterContext, value: string) => void;
}

const FilterPanel: React.FC<FilterPanelProps> = ({ filters, onFilterChange }) => (
  <section className="filter-panel">
    <h2>Filters</h2>
    <div className="filters">
      <div className="filter-group">
        <label htmlFor="dateRange">Date Range</label>
        <select
          id="dateRange"
          value={filters.dateRange}
          onChange={(e) => onFilterChange('dateRange', e.target.value)}
        >
          <option>Today</option>
          <option>Yesterday</option>
          <option>Last 7 Days</option>
          <option>Last 30 Days</option>
          <option>Month to Date</option>
          <option>Year to Date</option>
          <option>Custom</option>
        </select>
      </div>
      <div className="filter-group">
        <label htmlFor="state">State</label>
        <select
          id="state"
          value={filters.state}
          onChange={(e) => onFilterChange('state', e.target.value)}
        >
          <option>All States</option>
          <option>NJ</option>
          <option>NY</option>
          <option>PA</option>
          <option>CT</option>
        </select>
      </div>
      <div className="filter-group">
        <label htmlFor="agency">Agency</label>
        <input
          type="text"
          id="agency"
          value={filters.agency}
          onChange={(e) => onFilterChange('agency', e.target.value)}
          placeholder="All Agencies"
        />
      </div>
      <div className="filter-group">
        <label htmlFor="broker">Broker</label>
        <input
          type="text"
          id="broker"
          value={filters.broker}
          onChange={(e) => onFilterChange('broker', e.target.value)}
          placeholder="All Brokers"
        />
      </div>
      <div className="filter-group">
        <label htmlFor="product">Product</label>
        <select
          id="product"
          value={filters.product}
          onChange={(e) => onFilterChange('product', e.target.value)}
        >
          <option>All Products</option>
          <option>Senior</option>
          <option>Life</option>
          <option>Health</option>
          <option>Commercial</option>
        </select>
      </div>
    </div>
  </section>
);

// KPI Card Component
interface KPICardProps {
  data: KPIData;
}

const KPICard: React.FC<KPICardProps> = ({ data }) => (
  <div className="kpi-card">
    <div className="kpi-label">{data.label}</div>
    <div className="kpi-value">{data.value}</div>
    <div className={`kpi-change ${data.isNegative ? 'negative' : ''}`}>
      {data.change}
    </div>
  </div>
);

// KPI Section Component
interface KPISectionProps {
  kpiData: KPIData[];
}

const KPISection: React.FC<KPISectionProps> = ({ kpiData }) => (
  <section className="kpi-section">
    {kpiData.map((kpi, index) => (
      <KPICard key={index} data={kpi} />
    ))}
  </section>
);

// Hierarchy Panel Component
interface HierarchyPanelProps {
  data: HierarchyNode[];
  activeItemId: string | null;
  onItemClick: (itemId: string) => void;
}

const HierarchyPanel: React.FC<HierarchyPanelProps> = ({
  data,
  activeItemId,
  onItemClick,
}) => (
  <aside className="hierarchy-panel">
    <h3>Drill-Down Hierarchy</h3>
    <div className="hierarchy-tree">
      {data.map((item) => (
        <div
          key={item.id}
          className={`tree-item tree-item-level-${item.level} ${
            activeItemId === item.id ? 'active' : ''
          }`}
          onClick={() => onItemClick(item.id)}
        >
          {item.label}
        </div>
      ))}
    </div>
  </aside>
);

// Chart Container Component
interface ChartContainerProps {
  title: string;
  variant?: 'default' | 'alt' | 'alt2' | 'alt3';
}

const ChartContainer: React.FC<ChartContainerProps> = ({
  title,
  variant = 'default',
}) => (
  <div className="chart-container">
    <div className="chart-title">{title}</div>
    <div className={`chart-placeholder ${variant !== 'default' ? variant : ''}`}>
      {variant === 'default' && '📈 Trend Chart Placeholder'}
      {variant === 'alt' && '📊 Bar Chart Placeholder'}
      {variant === 'alt2' && '🍰 Pie Chart Placeholder'}
      {variant === 'alt3' && '📊 Stacked Bar Placeholder'}
    </div>
  </div>
);

// Analytics Panel Component
const AnalyticsPanel: React.FC = () => (
  <div className="analytics-panel">
    <ChartContainer title="Applications Trend (Monthly)" />
    <ChartContainer title="Commission by Agency" variant="alt" />
    <ChartContainer title="New vs Renewal" variant="alt2" />
    <ChartContainer title="Product Mix" variant="alt3" />
  </div>
);

// Detail Table Component
interface DetailTableProps {
  data: TableRow[];
  onDrillDown: (agency: string) => void;
}

const DetailTable: React.FC<DetailTableProps> = ({ data, onDrillDown }) => {
  const [searchTerm, setSearchTerm] = useState('');
  const [currentPage, setCurrentPage] = useState(1);

  const filteredData = data.filter((row) =>
    row.agency.toLowerCase().includes(searchTerm.toLowerCase())
  );

  const itemsPerPage = 5;
  const totalPages = Math.ceil(filteredData.length / itemsPerPage);
  const startIdx = (currentPage - 1) * itemsPerPage;
  const paginatedData = filteredData.slice(startIdx, startIdx + itemsPerPage);

  const handlePrevious = () => {
    setCurrentPage((prev) => Math.max(1, prev - 1));
  };

  const handleNext = () => {
    setCurrentPage((prev) => Math.min(totalPages, prev + 1));
  };

  return (
    <section className="detail-section" style={{ margin: '0 24px 32px 24px' }}>
      <div className="detail-header">
        <h3>Detail Grid — Agency Breakdown</h3>
        <div className="detail-controls">
          <input
            type="text"
            className="search-input"
            placeholder="Search table..."
            value={searchTerm}
            onChange={(e) => {
              setSearchTerm(e.target.value);
              setCurrentPage(1);
            }}
          />
          <button className="btn btn-secondary">📥 Export CSV</button>
          <button className="btn">⚙️ Settings</button>
        </div>
      </div>

      <div className="table-wrapper">
        <table>
          <thead>
            <tr>
              <th>Agency Name</th>
              <th>Applications</th>
              <th>Premium</th>
              <th>Commission</th>
              <th>Conversion Rate</th>
              <th>Action</th>
            </tr>
          </thead>
          <tbody>
            {paginatedData.map((row, index) => (
              <tr key={index}>
                <td>
                  <strong>{row.agency}</strong>
                </td>
                <td>{row.applications}</td>
                <td>{row.premium}</td>
                <td>{row.commission}</td>
                <td>{row.conversionRate}</td>
                <td>
                  <button
                    className="btn"
                    style={{ padding: '4px 8px', fontSize: '12px' }}
                    onClick={() => onDrillDown(row.agency)}
                  >
                    Drill-down
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <div className="pagination">
        <button onClick={handlePrevious} disabled={currentPage === 1}>
          ← Previous
        </button>
        <span className="page-info">
          Page {currentPage} of {totalPages} ({filteredData.length} total records)
        </span>
        <button onClick={handleNext} disabled={currentPage === totalPages}>
          Next →
        </button>
      </div>
    </section>
  );
};

// Main Dashboard Component
const Dashboard: React.FC = () => {
  const [filters, setFilters] = useState<FilterContext>({
    dateRange: 'Last 7 Days',
    state: 'NJ',
    agency: '',
    broker: '',
    product: 'Senior',
  });

  const [activeHierarchyId, setActiveHierarchyId] = useState<string | null>(
    'a1'
  );

  const kpiData: KPIData[] = [
    {
      label: 'Applications',
      value: '25,432',
      change: '↑ 12.5% vs last period',
      isNegative: false,
    },
    {
      label: 'Renewals',
      value: '9,842',
      change: '↑ 8.3% vs last period',
      isNegative: false,
    },
    {
      label: 'Premium',
      value: '$125.4M',
      change: '↑ 15.2% vs last period',
      isNegative: false,
    },
    {
      label: 'Commission',
      value: '$11.2M',
      change: '↑ 10.1% vs last period',
      isNegative: false,
    },
    {
      label: 'Conversion Rate',
      value: '68%',
      change: '↓ 2.1% vs last period',
      isNegative: true,
    },
  ];

  const handleFilterChange = useCallback(
    (key: keyof FilterContext, value: string) => {
      setFilters((prev) => ({
        ...prev,
        [key]: value,
      }));
      console.log(`Filter changed: ${key} = ${value}`);
    },
    []
  );

  const handleHierarchyClick = (itemId: string) => {
    setActiveHierarchyId(itemId);
    const item = HIERARCHY_DATA.find((h) => h.id === itemId);
    console.log('Selected:', item?.label);
  };

  const handleDrillDown = (agency: string) => {
    console.log('Drilling down into agency:', agency);
    handleFilterChange('agency', agency);
  };

  return (
    <div className="dashboard">
      <Header />
      <FilterPanel filters={filters} onFilterChange={handleFilterChange} />
      <KPISection kpiData={kpiData} />

      <section className="main-content">
        <HierarchyPanel
          data={HIERARCHY_DATA}
          activeItemId={activeHierarchyId}
          onItemClick={handleHierarchyClick}
        />
        <AnalyticsPanel />
      </section>

      <DetailTable data={TABLE_DATA} onDrillDown={handleDrillDown} />
    </div>
  );
};

export default Dashboard;
