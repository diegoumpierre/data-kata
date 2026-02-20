# MySQL Observability Demo 🔍

A complete demonstration of MySQL query observability using Spring Boot, Prometheus, Loki, Tempo, and Grafana.

## 📋 Overview

This project demonstrates how to implement comprehensive database query observability in a Spring Boot application. Every SQL query is automatically tracked, providing insights into:

- **Where** queries are executed (class, method, line number)
- **How often** each query runs
- **How long** queries take (min, max, average, percentiles)
- **What type** of queries are being executed (SELECT, INSERT, UPDATE, DELETE)

## 🏗️ Architecture

```
┌─────────────────┐
│  Spring Boot    │
│  Application    │
│                 │
│  ┌───────────┐  │
│  │ JdbcTemplate│ │
│  └─────┬─────┘  │
│        │        │
│  ┌─────▼─────┐  │
│  │DataSource │  │
│  │  Proxy    │◄─┼── Intercepts all queries
│  └─────┬─────┘  │
│        │        │
│  ┌─────▼─────┐  │
│  │Observability│ │
│  │  Service  │  │
│  └─────┬─────┘  │
└────────┼────────┘
         │
    ┌────┴────┬──────────┬──────────┐
    │         │          │          │
    ▼         ▼          ▼          ▼
┌────────┐ ┌──────┐ ┌───────┐ ┌─────────┐
│Prometheus│Loki  │ │Tempo  │ │ MySQL   │
│(Metrics)││(Logs)│ │(Traces)││(Database)│
└────┬────┘└───┬──┘ └───┬───┘ └─────────┘
     │         │        │
     └─────────┴────────┴───────┐
                                │
                          ┌─────▼─────┐
                          │  Grafana  │
                          │(Dashboards)│
                          └───────────┘
```

## 🚀 Technologies

- **Java 21** - Latest LTS version (Note: Java 25 and Spring Boot 4 are not yet released)
- **Spring Boot 3.4.0** - Latest stable version
- **MySQL 8.0** - Database
- **DataSource Proxy** - Query interception
- **Prometheus** - Metrics collection
- **Loki** - Log aggregation
- **Tempo** - Distributed tracing
- **Grafana** - Visualization

## 📦 Prerequisites

- Java 21 or higher
- Maven 3.6+
- Docker & Docker Compose
- 8GB RAM recommended

## 🎯 Quick Start

### 1. Clone and Setup

```bash
cd mysql-observability-demo
```

### 2. Start Observability Stack

```bash
# Start MySQL, Prometheus, Loki, Tempo, and Grafana
./scripts/start-stack.sh
```

This will start all required services and wait for them to be ready.

### 3. Build Application

```bash
./mvnw clean package
```

### 4. Run Application

```bash
./mvnw spring-boot:run
```

Wait for the application to start (you'll see the startup banner).

### 5. Generate Traffic

Open a new terminal and run:

```bash
./scripts/generate-traffic.sh
```

This script will make various API calls to generate query traffic.

### 6. View Dashboard

Open Grafana:
- URL: http://localhost:3000
- Username: `admin`
- Password: `admin`

Navigate to: **Dashboards → Database Monitoring → MySQL Query Observability**

## 🔗 Access URLs

| Service | URL | Credentials |
|---------|-----|-------------|
| Application | http://localhost:8080 | - |
| Grafana | http://localhost:3000 | admin/admin |
| Prometheus | http://localhost:9090 | - |
| Loki | http://localhost:3100 | - |
| Tempo | http://localhost:3200 | - |
| MySQL | localhost:3306 | demo_user/demo_password |

## 📊 Available Endpoints

### Users API

```bash
# Get all users
curl http://localhost:8080/api/users

# Get user by ID
curl http://localhost:8080/api/users/1

# Get user by email
curl http://localhost:8080/api/users/email/alice@example.com

# Get users by city
curl http://localhost:8080/api/users/city/New%20York

# Create user
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"name":"John Doe","email":"john@example.com","city":"Boston"}'

# Update user
curl -X PUT http://localhost:8080/api/users/1 \
  -H "Content-Type: application/json" \
  -d '{"name":"Jane Doe","email":"jane@example.com","city":"Seattle"}'

# Delete user
curl -X DELETE http://localhost:8080/api/users/1

# Count users
curl http://localhost:8080/api/users/count
```

### Orders API

```bash
# Get all orders
curl http://localhost:8080/api/orders

# Get order by ID
curl http://localhost:8080/api/orders/1

# Get orders by user
curl http://localhost:8080/api/orders/user/1

# Get orders by status
curl http://localhost:8080/api/orders/status/PENDING

# Create order
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{"userId":1,"product":"Laptop","amount":999.99,"status":"PENDING"}'

# Update order status
curl -X PATCH "http://localhost:8080/api/orders/1/status?status=COMPLETED"

# Delete order
curl -X DELETE http://localhost:8080/api/orders/1

# Count orders
curl http://localhost:8080/api/orders/count
```

## 📈 Grafana Dashboard

The dashboard shows:

1. **Queries per Second** - Real-time query rate
2. **Query Latency (p95)** - 95th percentile response time
3. **Query Rate by Type** - SELECT, INSERT, UPDATE, DELETE breakdown
4. **Query Latency Percentiles** - p50, p95, p99 over time
5. **Top 10 Query Locations** - Most frequently called query locations
6. **Slow Query Log** - Queries taking > 100ms

## 🔍 Metrics Available

### Prometheus Metrics

Access metrics at: http://localhost:8080/actuator/prometheus

Key metrics:
- `db_queries_total` - Total number of queries (by type and location)
- `db_query_duration_bucket` - Query duration histogram
- `db_query_execution_time_ms` - Distribution summary
- `hikaricp_connections_*` - Connection pool metrics

### Example Queries

```promql
# Queries per second
sum(rate(db_queries_total[1m]))

# Average latency
rate(db_query_duration_sum[5m]) / rate(db_query_duration_count[5m])

# p95 latency
histogram_quantile(0.95, sum(rate(db_query_duration_bucket[5m])) by (le))

# Top 10 query locations
topk(10, sum(rate(db_queries_total[5m])) by (location))
```

### Loki Logs

Query logs in Loki:

```logql
# All queries
{application="mysql-observability-demo"} | json

# Slow queries only
{application="mysql-observability-demo"} |= "Slow query"

# Queries from specific service
{application="mysql-observability-demo"} | json | location =~ "UserService.*"

# Queries by type
{application="mysql-observability-demo"} | json | query_type = "SELECT"
```

## 🏗️ Project Structure

```
mysql-observability-demo/
├── src/
│   └── main/
│       ├── java/com/demo/observability/
│       │   ├── config/
│       │   │   └── DataSourceConfig.java          # DataSource proxy setup
│       │   ├── controller/
│       │   │   ├── UserController.java            # User REST API
│       │   │   └── OrderController.java           # Order REST API
│       │   ├── model/
│       │   │   ├── User.java                      # User domain model
│       │   │   └── Order.java                     # Order domain model
│       │   ├── service/
│       │   │   ├── UserService.java               # User business logic
│       │   │   └── OrderService.java              # Order business logic
│       │   ├── observability/
│       │   │   └── QueryObservabilityService.java # Core observability
│       │   └── ObservabilityDemoApplication.java  # Main application
│       └── resources/
│           ├── application.yml                     # App configuration
│           └── logback-spring.xml                  # Logging config
├── observability/
│   ├── grafana/
│   │   ├── datasources/
│   │   │   └── datasources.yml                    # Grafana datasources
│   │   └── dashboards/
│   │       ├── dashboard-provider.yml             # Dashboard config
│   │       └── mysql-overview.json                # Main dashboard
│   ├── prometheus/
│   │   └── prometheus.yml                         # Prometheus config
│   ├── loki/
│   │   └── loki-config.yml                        # Loki config
│   └── tempo/
│       └── tempo-config.yml                       # Tempo config
├── scripts/
│   ├── init.sql                                   # Database init
│   ├── start-stack.sh                             # Start services
│   ├── stop-stack.sh                              # Stop services
│   └── generate-traffic.sh                        # Generate test traffic
├── docker-compose.yml                             # Docker services
├── pom.xml                                        # Maven dependencies
└── README.md                                      # This file
```

## 🔧 How It Works

### 1. Query Interception

The `DataSourceConfig` wraps the MySQL DataSource with a proxy that intercepts all JDBC calls:

```java
ProxyDataSourceBuilder
    .create(originalDataSource)
    .afterQuery((execInfo, queryInfoList) -> {
        // Capture stack trace to find caller
        // Record metrics, logs, and traces
    })
```

### 2. Stack Trace Analysis

For each query, the system analyzes the stack trace to find the application code that triggered it:

```java
private StackTraceElement findApplicationCaller(StackTraceElement[] stack) {
    // Skip framework classes (Spring, JDBC, etc.)
    // Return first application class
}
```

### 3. Multi-Channel Observability

Each query is recorded to:
- **Prometheus**: Metrics (counters, timers, histograms)
- **Loki**: Structured logs with query details
- **Tempo**: Distributed trace spans

### 4. Visualization

Grafana queries all three datasources and presents unified dashboards.

## 🎓 Learning Points

This demo showcases:

1. **Transparent Instrumentation** - No code changes needed in services
2. **DataSource Proxy Pattern** - Intercept all database operations
3. **Stack Trace Analysis** - Identify query origins automatically
4. **Multi-Signal Observability** - Metrics + Logs + Traces
5. **Grafana Integration** - Unified visualization
6. **Spring Boot Actuator** - Production-ready metrics

## 🐛 Troubleshooting

### Services won't start

```bash
# Check Docker
docker info

# View logs
docker-compose logs -f [service-name]

# Restart services
./scripts/stop-stack.sh
./scripts/start-stack.sh
```

### Application can't connect to MySQL

```bash
# Check MySQL is ready
docker-compose exec mysql mysqladmin ping -h localhost -u root -prootpassword

# Check connection from host
mysql -h 127.0.0.1 -P 3306 -u demo_user -pdemo_password demo_db
```

### No data in Grafana

1. Ensure application is running: `curl http://localhost:8080/actuator/health`
2. Generate traffic: `./scripts/generate-traffic.sh`
3. Check metrics endpoint: `curl http://localhost:8080/actuator/prometheus | grep db_queries`
4. Verify Prometheus is scraping: http://localhost:9090/targets

### Loki not showing logs

1. Check Loki is running: `curl http://localhost:3100/ready`
2. Check application is sending logs: `docker-compose logs app`
3. Query Loki directly: `curl -G http://localhost:3100/loki/api/v1/query --data-urlencode 'query={application="mysql-observability-demo"}'`

## 🛑 Stopping

```bash
# Stop services but keep data
./scripts/stop-stack.sh

# Stop and remove all data
docker-compose down -v
```

## 📝 Next Steps

To extend this demo:

1. **Add Alerting** - Configure Prometheus alerts for slow queries
2. **Add More Metrics** - Track query result sizes, connection pool usage
3. **Add Error Tracking** - Capture and track SQL errors
4. **Add Query Plans** - Use EXPLAIN to analyze slow queries
5. **Add Cost Tracking** - Estimate query costs and resource usage
6. **Add Custom Dashboards** - Create team-specific views
7. **Add N+1 Detection** - Identify and alert on N+1 query patterns

## 📚 References

- [Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)
- [Micrometer](https://micrometer.io/)
- [DataSource Proxy](https://github.com/ttddyy/datasource-proxy)
- [Prometheus](https://prometheus.io/docs/introduction/overview/)
- [Grafana Loki](https://grafana.com/docs/loki/latest/)
- [Grafana Tempo](https://grafana.com/docs/tempo/latest/)
- [OpenTelemetry](https://opentelemetry.io/)

## 📄 License

This is a demo project for educational purposes.

## 🤝 Contributing

Feel free to fork and experiment! This is meant to be a learning resource.

---

Made with ❤️ for database observability
