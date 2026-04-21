# QA Automation Lab — Karate + Kafka + Oracle + MongoDB

[![Karate Tests](https://github.com/Reddy062023/karate-banking-tests/actions/workflows/karate-tests.yml/badge.svg)](https://github.com/Reddy062023/karate-banking-tests/actions/workflows/karate-tests.yml)
[![Allure Report](https://img.shields.io/badge/Allure-Report-green)](https://reddy062023.github.io/karate-banking-tests/)

A comprehensive QA automation framework covering API testing,
Kafka event validation, Oracle SQL and MongoDB — built to mirror
real-world enterprise testing practices from the Instore project.

---

## Live Allure Dashboard

https://reddy062023.github.io/karate-banking-tests/

---

## Project Structure
karate-banking-tests/
├── src/
│   ├── main/
│   │   ├── avro/
│   │   │   └── order-event.avsc          ← Avro schema
│   │   ├── java/com/qalab/
│   │   │   ├── kafka/
│   │   │   │   ├── OrderEvent.java        ← Kafka message model
│   │   │   │   ├── OrderEventProducer.java← JSON Kafka producer
│   │   │   │   ├── OrderEventConsumer.java← Manual offset consumer
│   │   │   │   ├── AvroOrderProducer.java ← Avro producer
│   │   │   │   └── AvroOrderConsumer.java ← Avro consumer
│   │   │   ├── helpers/
│   │   │   │   └── KafkaTestHelper.java   ← Karate-Kafka bridge
│   │   │   ├── oracle/
│   │   │   │   └── OracleHelper.java      ← Oracle JDBC utility
│   │   │   └── mongodb/
│   │   │       └── MongoHelper.java       ← MongoDB utility
│   │   └── resources/
│   │       └── avro/order-event.avsc      ← Schema on classpath
│   └── test/
│       ├── java/com/qalab/
│       │   ├── runner/
│       │   │   └── TestRunner.java        ← Main Karate runner
│       │   ├── FirstTest.java             ← WireMock smoke test
│       │   ├── AppTest.java               ← Basic sanity test
│       │   └── kafka/
│       │       ├── KafkaProducerConsumerTest.java
│       │       └── AvroProducerConsumerTest.java
│       └── resources/
│           ├── karate-config.js           ← Global config (env aware)
│           ├── stubs/                     ← WireMock JSON stubs
│           └── banking/
│               ├── accounts/
│               │   ├── create-account.feature
│               │   ├── get-account.feature
│               │   ├── update-account.feature
│               │   └── schema-validation.feature
│               ├── loans/
│               │   └── apply-loan.feature
│               ├── transactions/
│               │   ├── transfer-funds.feature
│               │   ├── data-driven-transfer.feature
│               │   ├── dynamic-payload.feature
│               │   └── testdata/transfers.csv
│               ├── kafka/
│               │   ├── kafka-order-events.feature
│               │   ├── order-system-e2e.feature
│               │   ├── kafka-realworld-scenarios.feature
│               │   ├── ARCHITECTURE.md
│               │   ├── schema-registry-reference.md
│               │   └── TROUBLESHOOTING.md
│               ├── oracle/
│               │   ├── oracle-validation.feature
│               │   └── SQL_EXERCISES.md
│               └── mongodb/
│                   └── mongodb-validation.feature
├── docker-compose.yml                     ← All services
├── load-stubs.bat                         ← Load WireMock stubs
├── pom.xml                                ← Maven dependencies
└── .github/workflows/
├── karate-tests.yml                   ← Banking CI/CD
└── quickmart-tests.yml                ← QuickMart CI/CD

---

## Tech Stack

| Technology        | Version  | Purpose                          |
|-------------------|----------|----------------------------------|
| Karate DSL        | 1.4.1    | API + E2E test framework         |
| Java              | 17       | Test helpers and producers       |
| Apache Kafka      | 3.5.1    | Event streaming                  |
| Confluent Avro    | 7.5.0    | Schema Registry + Avro           |
| Oracle XE         | 11g      | Relational database              |
| MongoDB           | 6.0      | Event store / audit trail        |
| WireMock          | Latest   | API mocking                      |
| Allure            | 2.29.1   | Test reporting                   |
| GitHub Actions    | -        | CI/CD pipeline                   |
| Docker            | -        | Local services                   |

---

## Part 1 - Karate API Testing

### What we built
Complete API test suite for a Banking system backed by WireMock.

### Feature Files

**create-account.feature**
- POST /accounts - create new account
- Validate 201 response, accountId, status

**get-account.feature**
- GET /accounts/{id} - retrieve account
- Validate 200 response, all fields

**update-account.feature**
- PATCH /accounts/{id} - freeze/update account
- Validate status change

**apply-loan.feature**
- Full 5-step loan flow:
  Check balance → Check eligibility → Apply → Approve → Verify

**transfer-funds.feature**
- POST /transactions/transfer
- Happy path + error scenarios

**data-driven-transfer.feature**
- CSV-driven test: transfers.csv
- 4 scenarios from CSV data

**dynamic-payload.feature**
- Runtime payload generation
- UUID, timestamps, random amounts

**schema-validation.feature**
- Type matchers: #string #number #boolean #notnull
- Reusable schema definitions
- Regex validation: #regex TXN-.*
- Conditional: #? _ == "A" || _ == "B"
- Error response schema validation
- Response time: assert responseTime < 3000

### karate-config.js
Environment-aware configuration:
```javascript
local   → localhost:8090  (WireMock)
ci      → localhost:8090  (GitHub Actions)
staging → staging-api:8090
```

### Tags
@smoke      → quick sanity check
@regression → full test suite
@data-driven → CSV tests
@kafka      → Kafka tests
@oracle     → Oracle tests (local only)
@mongodb    → MongoDB tests
@e2e        → End-to-end tests
@realworld  → Real-world scenarios
@ignore     → Skip in all runs

---

## Part 2 - Kafka Event Testing

### What we built
Complete Kafka producer/consumer with Karate integration.

### Java Files

**OrderEvent.java**
- Data model for Kafka messages
- Fields: orderId, customerId, amount, items, eventType

**OrderEventProducer.java**
- JSON Kafka producer
- Reliability: acks=all, retries=3, idempotent=true
- Performance: linger.ms=5, snappy compression
- Headers: eventType, traceId, schemaVersion

**OrderEventConsumer.java**
- Manual offset commit consumer
- DLQ routing for failures
- Header-based event routing
- isolation.level=read_committed

**AvroOrderProducer.java**
- Avro producer with Schema Registry
- Auto-registers schema on first send
- Backward compatibility enforced

**AvroOrderConsumer.java**
- Avro consumer with GenericRecord
- Reads fields by name from Avro record

**KafkaTestHelper.java**
- Static utility called from Karate
- poll() - find message by orderId
- pollByType() - find message by eventType
- Used in Karate as: KafkaTestHelper.poll(BROKER, TOPIC, orderId, 30)

### Feature Files

**kafka-order-events.feature**
- HTTP call → Kafka event produced → Karate validates
- EventType routing validation
- Full ORDER_CREATED → ORDER_PAID lifecycle

**order-system-e2e.feature**
- Full 10-step event-driven architecture test
- API → Kafka → validate all layers
- DLQ flow simulation

**kafka-realworld-scenarios.feature**
- Idempotency test (enable.idempotence=true)
- Partition key routing (same customer → same partition)
- Offset replay (auto.offset.reset=earliest)

### Schema Registry
```bash
# List subjects
curl http://localhost:8081/subjects

# Check compatibility
curl -X POST http://localhost:8081/compatibility/subjects/order-events-avro-value/versions/latest \
  -H "Content-Type: application/vnd.schemaregistry.v1+json" \
  -d '{"schema": "..."}'
# → {"is_compatible":true}
```

### Compatibility Types
BACKWARD  - New schema reads old data (safe: add optional fields)
FORWARD   - Old schema reads new data
FULL      - Both BACKWARD and FORWARD
BREAKING  - Remove required field, change type, rename field

---

## Part 4 - Oracle SQL

### What we built
Oracle XE database with order system schema + JDBC integration.

### Schema
```sql
customers   - customer_id, first_name, tier, credit_score
products    - product_id, name, category, price, stock_qty
orders      - order_id, customer_id, status, total_amount,
              kafka_topic, kafka_offset, kafka_partition
order_items - item_id, order_id, product_id, quantity, unit_price
payments    - payment_id, order_id, method, amount, status
api_audit_log - audit_id, endpoint, request_amount, response_status
```

### Key Queries

INNER JOIN - Orders with customer details
LEFT JOIN - All customers including no orders
Correlated Subquery - Customers with DELIVERED orders
Aggregation - Revenue by tier
Window Functions - RANK, DENSE_RANK, ROW_NUMBER, LAG
CTEs - Multi-step aggregations with WITH clause
Kafka Validation - Find orders not published to Kafka
Payment Reconciliation - Orders vs payments cross-check

### OracleHelper.java
Static JDBC utility called from Karate:
OracleHelper.scalar() - COUNT, SUM queries
OracleHelper.query()  - returns List of Maps
OracleHelper.orderExists() - check if order in DB
OracleHelper.findUnpublishedOrders() - stuck transactions
OracleHelper.findDuplicateOrders() - idempotency check
OracleHelper.findAmountMismatches() - reconciliation

Note: Oracle tests tagged @ignore - run locally only.
Oracle XE not available in GitHub Actions CI.

---

## Part 5 - MongoDB

### What we built
MongoDB event store with Kafka metadata validation.

### Collection Structure
```javascript
db.order_events: {
  orderId, customerId, eventType, status, totalAmount,
  items: [{productId, name, qty, price}],
  customer: {name, email, tier},
  kafka: {topic, partition, offset},  ← Kafka metadata stored!
  errorDetail: {code, message},       ← DLQ events
  timestamp, schemaVersion
}
```

### MongoHelper.java
Static utility called from Karate:
MongoHelper.findByOrderId()         - get event by orderId
MongoHelper.validateKafkaMetadata() - verify topic/partition/offset
MongoHelper.findMissingKafka()      - events without Kafka metadata
MongoHelper.findDLQEvents()         - failed transactions
MongoHelper.getAuditTrail()         - full event history for order
MongoHelper.count()                 - count documents

### Feature Files

**mongodb-validation.feature**
- Validate ORDER_CREATED event stored in MongoDB
- Validate Kafka metadata correct (topic, partition, offset)
- Find events missing Kafka metadata
- Find DLQ events (failed transactions)
- Validate event audit trail
- Count total events

---

## CI/CD Pipeline

### GitHub Actions Workflow

```yaml
Trigger: push to main/develop on src/**, pom.xml changes

Services:
  - WireMock  (port 8090)
  - Zookeeper (port 2181)
  - Kafka     (port 9092)
  - MongoDB   (port 27017)

Steps:
  1. Wait for all services ready
  2. Load WireMock stubs
  3. Seed MongoDB test data
  4. Run all tests (mvn test -Dkarate.env=ci)
  5. Upload Karate HTML report
  6. Build Allure report with history
  7. Deploy to GitHub Pages
  8. Publish test results
```

### Allure Dashboard
Live at: https://reddy062023.github.io/karate-banking-tests/

Shows:
- Test case count and pass rate
- Trend graph across runs
- Feature-by-feature breakdown
- Execution timeline

---

## Local Setup

### Prerequisites
Java 17+
Maven 3.8+
Docker Desktop
Oracle XE 11g (for Oracle tests)

### Start Services
```bash
docker compose up -d
load-stubs.bat
```

### Run Tests
```bash
# All tests
mvn test

# Specific tag
mvn test -Dtest=com.qalab.runner.TestRunner -Dkarate.options="--tags @smoke"
mvn test -Dtest=com.qalab.runner.TestRunner -Dkarate.options="--tags @kafka"
mvn test -Dtest=com.qalab.runner.TestRunner -Dkarate.options="--tags @mongodb"

# Oracle tests (requires Oracle XE running)
mvn test -Dtest=com.qalab.runner.TestRunner -Dkarate.options="--tags @oracle"

# Avro tests (requires Schema Registry running)
mvn test -Dtest=AvroProducerConsumerTest -Davro.tests.enabled=true

# Kafka producer tests
mvn test -Dtest=KafkaProducerConsumerTest
```

### View Reports
```bash
# Open Karate HTML report
target/karate-reports/karate-summary.html

# Open Allure report locally
mvn allure:serve
```

---

## Test Coverage Summary

| Module              | Scenarios | Tags                        |
|---------------------|-----------|-----------------------------|
| Account API         | 9         | @smoke @regression          |
| Schema Validation   | 6         | @smoke @regression          |
| Loan Flow           | 1         | @smoke @regression          |
| Transfer API        | 4         | @regression                 |
| Data-Driven         | 4         | @regression @data-driven    |
| Dynamic Payload     | 1         | @regression                 |
| Kafka Events        | 3         | @regression @kafka          |
| Kafka E2E           | 2         | @regression @kafka @e2e     |
| Kafka Real-World    | 3         | @regression @kafka @realworld|
| Oracle Validation   | 6         | @oracle @ignore             |
| MongoDB Validation  | 6         | @mongodb                    |
| **Total CI**        | **39**    |                             |
| **Total Local**     | **45**    | (includes Oracle)           |

---

## Key Design Decisions

**Why static methods in KafkaTestHelper?**
GraalVM (Karate's JS engine) cannot call instance methods
on Java objects returned from static factories.
Static methods work reliably from Karate feature files.

**Why @ignore on Oracle tests?**
Oracle XE is not available in GitHub Actions.
Tests run locally against Oracle XE 11g.
MongoDB tests run in CI because MongoDB Docker is available.

**Why karate.sizeOf() instead of .size()?**
GraalVM cannot call Java List.size() method directly.
karate.sizeOf() is Karate's built-in helper for this.

**Why single test run in CI?**
Previously had separate smoke + regression runs.
Allure only captured last run results.
Single run ensures complete Allure report.

---

## Instore Project Mapping

This project mirrors the Instore retail automation project:

| Instore                  | This Project                    |
|--------------------------|---------------------------------|
| Store register scan      | POST /transactions/transfer     |
| KOLOG transaction        | OrderEvent Kafka message        |
| KOLOG → Kafka publish    | OrderEventProducer.java         |
| Ingress Service consume  | OrderEventConsumer.java         |
| TLOG audit trail         | MongoDB order_events collection |
| Core banking Oracle DB   | Oracle qalab schema             |
| Cash sale validation     | KafkaTestHelper + Karate        |
| Failed transaction → DLQ | DLQ flow in kafka-realworld     |

---

## Author

Japendra
QA Automation Engineer
GitHub: https://github.com/Reddy062023

## QuickMart Backend (Spring Boot REST APIs)

A companion Spring Boot project used to practice testing real REST APIs.

**Location:** `C:\Users\Geetu\quickmart-backend\quickmart-backend`
**Port:** 8085
**MongoDB:** localhost:27018 (quickmart-mongodb Docker container)

### Start QuickMart Backend

```bash
# 1. Start QuickMart containers
cd C:\Users\Geetu\quickmart\quickmart
docker compose up -d

# 2. Start Spring Boot app
cd C:\Users\Geetu\quickmart-backend\quickmart-backend
mvn spring-boot:run
```

App starts at `http://localhost:8085`. MongoDB connects to `quickmart` database on port 27018.

### APIs

| Method | Endpoint | Purpose |
|---|---|---|
| POST | /api/cash/inbound | Cash IN (sales, deposit, float) |
| POST | /api/cash/outbound | Cash OUT (refund, pickup) |
| POST | /api/cash/loan | Temporary cash loan to till |
| POST | /api/cash/pickup | Cash collected for bank |
| POST | /api/till/open | Open till with opening balance |
| POST | /api/till/{tillId}/close | Close till with closing balance |
| GET | /api/till/{tillId}/status | Current till status |
| POST | /api/business-day/create | Open store for trading |
| POST | /api/business-day/{id}/end | Close business day |
| GET | /api/business-day/store/{storeId}/current | Get current open business day |

### Test APIs (PowerShell)

```powershell
# Cash Inbound
Invoke-RestMethod -Method POST -Uri "http://localhost:8085/api/cash/inbound" `
  -ContentType "application/json" `
  -Body '{"storeId":"STORE-001","cashierId":"CASHIER-01","amount":100.00,"reason":"CASH_SALE","currency":"GBP"}'

# Cash Outbound
Invoke-RestMethod -Method POST -Uri "http://localhost:8085/api/cash/outbound" `
  -ContentType "application/json" `
  -Body '{"storeId":"STORE-001","cashierId":"CASHIER-01","amount":50.00,"reason":"REFUND","currency":"GBP"}'

# Cash Loan
Invoke-RestMethod -Method POST -Uri "http://localhost:8085/api/cash/loan" `
  -ContentType "application/json" `
  -Body '{"storeId":"STORE-001","cashierId":"CASHIER-01","amount":200.00,"currency":"GBP"}'

# Open Till
Invoke-RestMethod -Method POST -Uri "http://localhost:8085/api/till/open" `
  -ContentType "application/json" `
  -Body '{"storeId":"STORE-001","cashierId":"CASHIER-01","openingBalance":200.00}'

# Create Business Day
Invoke-RestMethod -Method POST -Uri "http://localhost:8085/api/business-day/create" `
  -ContentType "application/json" `
  -Body '{"storeId":"STORE-001","createdBy":"MANAGER-01","openingFloat":500.00}'
```

### MongoDB Collections Created

| Collection | Purpose |
|---|---|
| `cash_transactions` | All cash inbound/outbound/loan/pickup |
| `till` | Till open/close lifecycle and balances |
| `business_day` | Daily trading day open/close records |

---

## QuickMart Flow Project (Original Kafka Pipeline)

**Location:** `C:\Users\Geetu\quickmart\quickmart`
**MongoDB:** localhost:27018

A Kafka → RabbitMQ → MongoDB event-driven pipeline:

```
JUnit test
    ↓
StoreTransactionProducer → Kafka (cash-transactions topic)
    ↓
IngressService (reads Kafka by transactionId, sends to RabbitMQ)
    ↓
G4SManagerService (reads RabbitMQ, writes to MongoDB)
    ↓
MongoDB: transactions + safe_balance collections
```

Transaction types: `CASH_SALE` (status: PROCESSED) and `VOID` (status: VOIDED).

### Start and Run

```bash
cd C:\Users\Geetu\quickmart\quickmart
docker compose up -d    # starts quickmart-mongodb + rabbitmq
mvn test                # runs 3 integration tests, BUILD SUCCESS
```

---

## Instore Project Mapping

| This Project | Instore Equivalent |
|---|---|
| WireMock stub APIs | KOLOG REST endpoints |
| Cash INBOUND transaction | KOLOG CASH_SALE event |
| Cash OUTBOUND transaction | KOLOG CASH_REFUND / VOID event |
| Kafka `cash-transactions` topic | KOLOG Kafka event bus |
| MongoDB `transactions` collection | KOLOG transaction store |
| `safe_balance` collection | Safe/ledger balance tracker |
| Till Open/Close APIs | Instore till lifecycle management |
| Business Day Create/End | Instore trading day lifecycle |
| Cash Loan API | Float top-up to till |
| Cash Pickup API | End-of-day cash collection |
| Schema Registry | KOLOG Avro schema management |
| Dead Letter Queue (DLQ) | Failed transaction routing |

---

## Daily Startup Routine

```bash
# Banking tests
cd C:\Users\Geetu\karate-banking-tests
docker compose up -d
load-stubs.bat
mvn test

# QuickMart flow tests
cd C:\Users\Geetu\quickmart\quickmart
docker compose up -d
mvn test

# QuickMart backend (optional)
cd C:\Users\Geetu\quickmart-backend\quickmart-backend
mvn spring-boot:run
```

---

## Key Design Decisions and Gotchas

**GraalVM requires static Java methods.** Karate's JS engine cannot call instance methods. All helpers use only `static` methods.

**Use `karate.sizeOf()` not `.size()`.** Java `List` objects returned to Karate JS must use `karate.sizeOf(list)` — `.size()` throws in GraalVM.

**Oracle tagged `@ignore` for CI.** Oracle XE is not available in GitHub Actions. Local only.

**MongoDB runs in CI.** Available as a GitHub Actions service container.

**Allure needs a single test run.** Running tests twice overwrites results. CI uses one `mvn test` call.

**Kafka CI needs port mapping + rebalance delay.** Map `9092:9092` and set `KAFKA_GROUP_INITIAL_REBALANCE_DELAY_MS=0` in the GitHub Actions service config.

**Schema Registry uses `kafka:29092` internally.** Docker containers talk to each other on the internal hostname. Your local Java app uses `localhost:9092`. Both listeners must be configured.

**`assert responseTime < 3000`, not `match`.** In Karate 1.4.1, response time checks use `* assert responseTime < 3000`.

**WireMock stubs lost on container restart.** Run `load-stubs.bat` every time WireMock restarts.

**QuickMart IngressService reads by transactionId.** Uses `AUTO_OFFSET_RESET=earliest` and polls multiple times searching for the specific transaction ID — prevents reading stale messages from previous test runs.

---
Built by Japendra
Portfolio: https://Reddy062023.github.io
GitHub: https://github.com/Reddy062023
Contact: japendras06@gmail.com
