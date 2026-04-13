# 🏦 Karate Banking Tests
### Enterprise QA Automation Framework — API Testing + Kafka + MongoDB + CI/CD

---

## 📌 What is this project?

This project is a **complete QA automation framework** for testing a banking application.
Instead of testing manually (click here, check that), we write automated tests that run
in seconds and tell us exactly what passed or failed.

**Think of it like this:**
- A real bank has APIs — endpoints like `/accounts`, `/transactions`, `/loans`
- We don't have a real bank API, so we use **WireMock** to fake it
- We write tests in **Karate** (a special testing language) to call those APIs
- We run everything in **Docker** containers so setup takes one command
- We connect to **GitHub Actions** so tests run automatically every time code is pushed

---

## 🎯 What problems does this solve?

| Problem | Our Solution |
|---------|-------------|
| Manual testing is slow | Automated tests run in seconds |
| No real bank API available | WireMock simulates the real API |
| Tests work on my laptop but not others | Docker ensures same environment everywhere |
| Nobody knows if tests passed after a code push | GitHub Actions runs tests automatically |
| Hard to read test results | Karate HTML report shows clear pass/fail |

---

## 🛠️ Tech Stack — What each tool does

| Tool | Version | Why we use it |
|------|---------|--------------|
| **Java 17** | 17 LTS | Karate runs on Java — it is the engine |
| **Maven 3.9** | 3.9+ | Downloads all dependencies and runs tests |
| **Karate** | 1.4.1 | Our test framework — writes API tests in plain English |
| **JUnit 5** | 5.x | Runs Karate tests and reports results |
| **Docker** | 24+ | Runs all services (Kafka, MongoDB, WireMock) in containers |
| **Kafka** | 7.4.0 | Message streaming — tests event-driven systems |
| **MongoDB** | 6.0 | Database — validates data was saved correctly |
| **WireMock** | latest | Fake API server — simulates the real bank API |
| **Schema Registry** | 7.4.0 | Validates Kafka message formats (Avro schemas) |
| **GitHub Actions** | - | Runs our tests automatically in the cloud |

---

## 📋 Prerequisites — Install these first

### Why do we need these?

- **Java** — Karate is built on Java. Without it, nothing runs.
- **Maven** — Downloads Karate, JUnit, Kafka libraries automatically. Without it, you would manually download 50+ JAR files.
- **Docker** — Runs Kafka, MongoDB, WireMock in isolated containers. Without it, you would install and configure each service manually (hours of work).
- **Git** — Version controls your code and connects to GitHub.

### 1. Install Java 17

```bash
# Windows (PowerShell as Administrator)
winget install Microsoft.OpenJDK.17

# Then add JAVA_HOME to System Environment Variables:
# System -> Environment Variables -> New
# Variable name:  JAVA_HOME
# Variable value: C:\Program Files\Microsoft\jdk-17
```

**Verify Java is installed correctly:**
```bash
java -version
javac -version
# Both should show: 17.x.x
```

### 2. Install Apache Maven 3.9+

Maven is a build tool. It reads your `pom.xml` file and automatically downloads all
the libraries your project needs (Karate, JUnit, Kafka client etc).

```bash
# Download from: https://maven.apache.org/download.cgi
# Extract to: C:\apache-maven-3.9.x
# Add to PATH: C:\apache-maven-3.9.x\bin
```

**Verify Maven is installed correctly:**
```bash
mvn -version
# Expected: Apache Maven 3.9.x ... Java version: 17
```

### 3. Install Docker Desktop

Docker runs our services (Kafka, MongoDB, WireMock) in isolated "containers".
Think of containers as lightweight virtual machines — each one runs a service
without affecting your laptop.

```bash
# Download from: https://www.docker.com/products/docker-desktop/
# During install: make sure "Use WSL 2" is checked (Windows)
```

**Verify Docker is installed and running:**
```bash
docker --version
# Expected: Docker version 24.x.x

docker ps
# Expected: empty table (means Docker engine is running)
```

> **Common Issue on Windows:** If docker ps gives an error,
> open Docker Desktop and wait until it shows "Docker is running" before trying again.

### 4. Install Git

Git tracks changes to your code and lets you push to GitHub.

```bash
# Download from: https://git-scm.com/download/win

# After install, configure your identity:
git config --global user.name "Your Name"
git config --global user.email "your-email@gmail.com"

# Verify
git --version
# Expected: git version 2.x.x
```

---

## 📁 Project Structure — What each file does

Understanding what each file/folder does is critical before running anything.

```
karate-banking-tests/
|
|-- pom.xml                     <- Maven config — lists ALL dependencies
|                                  (Karate, JUnit, Kafka, MongoDB driver)
|                                  Maven reads this to download libraries
|
|-- Jenkinsfile                 <- Jenkins CI pipeline definition
|                                  Used when running tests via Jenkins server
|
|-- .github/
|   `-- workflows/
|       `-- karate-tests.yml   <- GitHub Actions pipeline
|                                  Runs tests automatically when you push code
|
`-- src/test/
    |-- java/com/qalab/
    |   |
    |   |-- AppTest.java        <- Simple test to verify project compiles
    |   |
    |   |-- FirstTest.java      <- Tests WireMock is reachable (health check)
    |   |
    |   |-- runner/
    |   |   |-- TestRunner.java <- Runs ALL @regression tests
    |   |   |                     Points Karate to the banking/ folder
    |   |   |-- SmokeRunner.java<- Runs ONLY @smoke tests (fast subset)
    |   |   `-- E2ERunner.java  <- Runs ONLY @e2e tests (end-to-end)
    |   |
    |   `-- helpers/
    |       |-- KafkaTestHelper.java  <- Java code to send/receive Kafka messages
    |       |                           Karate cannot talk to Kafka natively,
    |       |                           so Java acts as the bridge
    |       `-- MongoDBHelper.java    <- Java code to query MongoDB
    |                                    Same reason — Karate needs Java bridge
    |
    `-- resources/
        |
        |-- karate-config.js    <- GLOBAL configuration file
        |                          Runs before EVERY test
        |                          Defines baseUrl, Kafka server, MongoDB URL etc.
        |                          Variables here are available in ALL feature files
        |
        |-- banking/            <- All banking API test scenarios
        |   |-- accounts/
        |   |   |-- get-account.feature      <- Tests GET /accounts/{id}
        |   |   |-- create-account.feature   <- Tests POST /accounts
        |   |   `-- update-account.feature   <- Tests PATCH /accounts/{id}
        |   |
        |   |-- transactions/
        |   |   |-- transfer-funds.feature        <- Tests POST /transactions/transfer
        |   |   |-- data-driven-transfer.feature  <- Same test with multiple CSV data rows
        |   |   |-- dynamic-payload.feature       <- Test with runtime-generated data
        |   |   `-- testdata/
        |   |       |-- transfers.csv    <- CSV data for data-driven tests
        |   |       `-- accounts.json   <- JSON data for data-driven tests
        |   |
        |   `-- loans/
        |       |-- apply-loan.feature    <- Full 5-step loan application flow
        |       `-- loan-status.feature   <- Helper — checks loan status
        |                                    Has @ignore tag so it does not run standalone
        |
        |-- shared/
        |   |-- auth.feature    <- Reusable authentication scenario
        |   |                      Called by other features to get JWT token
        |   `-- schema/
        |       |-- account-schema.json      <- JSON schema for account response
        |       |-- transaction-schema.json  <- JSON schema for transaction response
        |       `-- error-schema.json        <- JSON schema for error responses
        |                                       These files define what fields an API
        |                                       response MUST have and their data types
        |
        |-- stubs/              <- WireMock configuration files
        |   |                      Each file tells WireMock: When you receive THIS
        |   |                      request, return THAT response
        |   |-- get-account-stub.json           <- GET /accounts/* -> 200 + account data
        |   |-- post-transfer-stub.json         <- POST /transactions/transfer -> 201
        |   |-- create-account-stub.json        <- POST /accounts -> 201 + new account
        |   |-- update-account-stub.json        <- PATCH /accounts/* -> 200 + updated
        |   |-- get-loan-eligibility-stub.json  <- GET /loans/eligibility -> 200
        |   |-- post-loan-apply-stub.json       <- POST /loans/apply -> 201
        |   |-- get-loan-status-stub.json       <- GET /loans/* -> 200
        |   |-- error-400-*.json                <- Various 400 error responses
        |   |-- error-404-*.json                <- 404 not found responses
        |   `-- error-422-*.json                <- 422 business rule errors
        |
        |-- com/qalab/
        |   `-- first-test.feature  <- First test — verifies WireMock is alive
        |
        `-- e2e/
            `-- order-flow-e2e.feature  <- End-to-end test (API + Kafka + DB)
```

---

## 🚀 Local Setup — Step by Step

Follow these steps in order. Each step depends on the previous one.

---

### Step 1 — Clone the repository

Cloning downloads the project code to your laptop.

```bash
git clone https://github.com/Reddy062023/karate-banking-tests.git
cd karate-banking-tests
```

**Why?** You need the project files locally to run tests.

---

### Step 2 — Understand docker-compose.yml

The `docker-compose.yml` file in the project root tells Docker what services to start.
It is like a recipe — one command starts everything.

```yaml
# What docker-compose.yml does:
services:
  zookeeper:       # Required by Kafka to manage brokers
  kafka:           # Message streaming service (port 9092, 29092)
  schema-registry: # Validates Kafka message formats (port 8081)
  mongodb:         # Database for storing test data (port 27017)
  wiremock:        # Fake API server (port 8090)
```

**Why separate ports for Kafka (9092 and 29092)?**
- Port 9092 — used by containers talking to each other INSIDE Docker
- Port 29092 — used by your Java tests running OUTSIDE Docker (on your laptop)

---

### Step 3 — Start Docker Desktop

Before running any Docker commands, Docker Desktop must be running.

```bash
# Start Docker Desktop:
# Windows: Start Menu -> Docker Desktop -> Wait for "Docker is running"

# Verify Docker engine is running:
docker ps
# If you see an empty table (no error) -> Docker is running
# If you see an error -> Docker is NOT running yet
```

---

### Step 4 — Start all services

```bash
# Navigate to project root (where docker-compose.yml is)
cd C:\Users\Geetu\karate-banking-tests

# Start all services in background (-d means detached/background)
docker compose up -d
```

**What this command does:**
1. Downloads Docker images for Kafka, MongoDB, WireMock (first time only — takes 5-10 min)
2. Creates containers from those images
3. Starts all containers in background
4. Sets up networking between containers

**Verify all 5 containers are running:**
```bash
docker ps
```

Expected output — you should see 5 containers:
```
NAMES             IMAGE                                   PORTS
schema-registry   confluentinc/cp-schema-registry:7.4.0  0.0.0.0:8081->8081
kafka             confluentinc/cp-kafka:7.4.0             0.0.0.0:9092->9092
zookeeper         confluentinc/cp-zookeeper:7.4.0         0.0.0.0:2181->2181
mongodb           mongo:6.0                               0.0.0.0:27017->27017
wiremock          wiremock/wiremock:latest                 0.0.0.0:8090->8080
```

---

### Step 5 — Understand WireMock

WireMock is a **fake API server**. Since we do not have a real bank API, WireMock pretends to be one.

**How it works:**
```
Your Test              WireMock (fake bank)         Real Bank API
     |                       |                            |
     |-- GET /accounts/001 ->|                            |
     |                       | (reads stub config)        |
     |<-- 200 + JSON --------|                            |
     |                       |                     (never called)
```

**Stub files** tell WireMock how to respond:
```json
{
  "request": {
    "method": "GET",
    "urlPattern": "/accounts/.*"
  },
  "response": {
    "status": 200,
    "jsonBody": {
      "accountId": "ACC-001",
      "balance": 5000.00,
      "status": "ACTIVE"
    }
  }
}
```

---

### Step 6 — Load WireMock stubs

WireMock stubs must be loaded before tests run.

```bash
curl -X POST http://localhost:8090/__admin/mappings -H "Content-Type: application/json" -d @src\test\resources\stubs\get-account-stub.json
curl -X POST http://localhost:8090/__admin/mappings -H "Content-Type: application/json" -d @src\test\resources\stubs\post-transfer-stub.json
curl -X POST http://localhost:8090/__admin/mappings -H "Content-Type: application/json" -d @src\test\resources\stubs\create-account-stub.json
curl -X POST http://localhost:8090/__admin/mappings -H "Content-Type: application/json" -d @src\test\resources\stubs\update-account-stub.json
curl -X POST http://localhost:8090/__admin/mappings -H "Content-Type: application/json" -d @src\test\resources\stubs\get-loan-eligibility-stub.json
curl -X POST http://localhost:8090/__admin/mappings -H "Content-Type: application/json" -d @src\test\resources\stubs\post-loan-apply-stub.json
curl -X POST http://localhost:8090/__admin/mappings -H "Content-Type: application/json" -d @src\test\resources\stubs\get-loan-status-stub.json
curl -X POST http://localhost:8090/__admin/mappings -H "Content-Type: application/json" -d @src\test\resources\stubs\error-400-missing-fields-stub.json
curl -X POST http://localhost:8090/__admin/mappings -H "Content-Type: application/json" -d @src\test\resources\stubs\error-404-account-not-found-stub.json
curl -X POST http://localhost:8090/__admin/mappings -H "Content-Type: application/json" -d @src\test\resources\stubs\error-422-insufficient-funds-stub.json
curl -X POST http://localhost:8090/__admin/mappings -H "Content-Type: application/json" -d @src\test\resources\stubs\error-400-same-account-stub.json
curl -X POST http://localhost:8090/__admin/mappings -H "Content-Type: application/json" -d @src\test\resources\stubs\error-400-invalid-currency-stub.json
curl -X POST http://localhost:8090/__admin/mappings -H "Content-Type: application/json" -d @src\test\resources\stubs\error-400-no-owner-stub.json
curl -X POST http://localhost:8090/__admin/mappings -H "Content-Type: application/json" -d @src\test\resources\stubs\error-400-transfer-missing-stub.json

# Verify all stubs loaded
curl http://localhost:8090/__admin/mappings
```

**Why do we need to reload stubs every time?**
WireMock stores stubs in memory. When the container restarts, stubs are lost.
That is why we reload them before every test session.

---

### Step 7 — Download dependencies (first time only)

```bash
mvn clean install -DskipTests
```

**What this command does:**
- `clean` — Deletes old compiled files in target/ folder
- `install` — Downloads dependencies, compiles code, packages everything
- `-DskipTests` — Skips running tests (we just want to download dependencies)

**Expected output:**
```
[INFO] BUILD SUCCESS
```

---

### Step 8 — Run your first test

```bash
mvn test
```

**Expected output:**
```
Tests run: 20, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

---

## 🧪 Running Tests — All Commands Explained

### Run ALL tests
```bash
mvn test
```
Runs every feature file in the project. Takes 15-20 seconds locally.

### Run only SMOKE tests (fast check)
```bash
mvn test -Dkarate.options="--tags @smoke"
```
Smoke tests are a small fast subset that verify the most critical paths work.
Run these first — if smoke fails, no point running full regression.

### Run only REGRESSION tests (full suite)
```bash
mvn test -Dkarate.options="--tags @regression"
```
Runs all tests tagged @regression. This is the complete test suite.

### Run a specific feature file
```bash
mvn test -Dkarate.options="classpath:banking/accounts/get-account.feature"
```
When debugging a specific test, you do not want to run all 20 tests every time.

### Run against a different environment
```bash
mvn test -Dkarate.env=staging
```
karate-config.js reads karate.env and switches the baseUrl.
Same tests run against staging server instead of local WireMock.

### Run tests in parallel (faster)
```bash
mvn test -Dkarate.threads=5
```
Runs 5 feature files simultaneously instead of one at a time.

### Exclude tests tagged @wip (work in progress)
```bash
mvn test -Dkarate.options="--tags ~@wip"
```
The ~ means NOT. Useful for excluding tests you are still writing.

---

## 📊 Test Reports

After every test run, Karate generates a detailed HTML report automatically.

### Open the report
```bash
# Windows
start target\karate-reports\karate-summary.html
```

### What the report shows

**Summary Dashboard (top of page):**
```
Features:   8    Passed: 8    Failed: 0
Scenarios:  20   Passed: 20   Failed: 0
Time:       6.4 seconds
```

**Feature breakdown table:**
```
Feature File              | Scenarios | Passed | Failed | Time
get-account.feature       |     3     |   3    |   0    | 0.1s
create-account.feature    |     3     |   3    |   0    | 0.1s
transfer-funds.feature    |     4     |   4    |   0    | 0.1s
apply-loan.feature        |     1     |   1    |   0    | 0.0s
```

**Click any feature to see each step:**
```
Scenario: Get existing ACTIVE account returns 200
  PASS  Given path '/accounts/ACC-001'         0ms
  PASS  When method GET                        45ms
  PASS  Then status 200                        0ms
  PASS  And match response.status == 'ACTIVE'  0ms
```

**Failed step shows with details:**
```
  FAIL  Then status 400
        status code was: 201, expected: 400
        Response body: {...full JSON...}
```

### Report file locations
```
target/
|-- karate-reports/
|   `-- karate-summary.html     <- Main HTML report (share with your manager)
`-- surefire-reports/
    `-- *.xml                   <- JUnit XML (used by Jenkins/GitHub Actions)
```

---

## ⚙️ Configuration Files Explained

### karate-config.js

This file runs BEFORE every single test. It sets up global variables
that all feature files can use.

```javascript
function fn() {
  var env = karate.env || 'local';

  var config = {
    env: env,
    wireMockUrl: 'http://localhost:8090',
    kafkaBootstrapServers: 'localhost:29092',
    schemaRegistryUrl: 'http://localhost:8081',
    mongoUri: 'mongodb://localhost:27017',
    mongoDatabase: 'bankingdb'
  };

  if (env == 'staging') {
    config.wireMockUrl = 'http://staging-wiremock:8090';
  }

  karate.configure('connectTimeout', 5000);
  karate.configure('readTimeout', 10000);

  return config;
}
```

### Schema JSON files

Schema files define what an API response MUST look like.
Instead of checking every field manually in every test, we define the schema once
and reuse it everywhere.

```json
{
  "accountId": "#string",
  "balance": "#number",
  "status": "#string",
  "owner": {
    "name": "#string",
    "email": "#string"
  },
  "createdAt": "#notnull"
}
```

Used in feature file:
```gherkin
And match response == read('../../shared/schema/account-schema.json')
```

---

## 🔄 CI/CD — GitHub Actions

### What is CI/CD?

Without CI/CD:
```
Developer pushes code -> Forgets to run tests -> Broken code goes to production
```

With CI/CD:
```
Developer pushes code -> Tests run automatically ->
If pass: code is safe | If fail: developer is notified immediately
```

### How our pipeline works

Every git push triggers this sequence automatically:

```
1. Creates fresh Ubuntu machine in the cloud
2. Checks out your code
3. Installs Java 17
4. Restores Maven dependency cache (saves time on repeat runs)
5. Starts Docker services (WireMock, Kafka, MongoDB)
6. Waits for all services to be ready
7. Loads all WireMock stub files
8. Runs @smoke tests first (fast validation)
9. If smoke passes -> Runs full regression tests
10. Uploads HTML report as downloadable artifact
11. Publishes test results summary
```

### View results on GitHub

1. Go to https://github.com/Reddy062023/karate-banking-tests
2. Click Actions tab
3. Click latest workflow run
4. Green = tests passed, Red = tests failed
5. Scroll down to Artifacts section
6. Download karate-report-N zip file
7. Extract and open karate-summary.html

### Why first run takes longer

```
First run:
  Download Java          -> 2-3 minutes
  Download Maven deps    -> 5-7 minutes (50+ libraries)
  Kafka startup          -> 2-3 minutes
  Run tests              -> 2-3 minutes
  Total                  -> 15-20 minutes

Second run (cached):
  Java cached            -> 30 seconds
  Maven deps cached      -> 1 minute
  Kafka startup          -> 2 minutes
  Run tests              -> 2 minutes
  Total                  -> 6 minutes
```

---

## 📝 Karate Assertion Cheat Sheet

| Matcher | Meaning | Example |
|---------|---------|---------|
| `#string` | Must be a string | `match response.name == '#string'` |
| `#number` | Must be a number | `match response.balance == '#number'` |
| `#boolean` | Must be true or false | `match response.active == '#boolean'` |
| `#array` | Must be an array | `match response.items == '#array'` |
| `#object` | Must be a JSON object | `match response.owner == '#object'` |
| `#notnull` | Must exist and not be null | `match response.id == '#notnull'` |
| `#null` | Must be null | `match response.deleted == '#null'` |
| `#ignore` | Skip this field | `match response == {id: '#ignore'}` |
| `#[_ > 0]` | Array with at least 1 item | `match response.items == '#[_ > 0]'` |
| `#[3]` | Array with exactly 3 items | `match response.items == '#[3]'` |
| `#? _ > 0` | Number greater than zero | `match response.balance == '#? _ > 0'` |

---

## 🚨 Troubleshooting

### docker ps gives error "Cannot connect to Docker daemon"
**Reason:** Docker Desktop is not running
**Fix:** Open Docker Desktop and wait for "Docker is running"

### WSL error when starting Docker Desktop
**Reason:** Windows Subsystem for Linux is outdated
**Fix:**
```bash
wsl --update
wsl --set-default-version 2
# Restart laptop, then open Docker Desktop
```

### Tests fail with "status code was: 404, expected: 200"
**Reason:** WireMock stub is not loaded
**Fix:**
```bash
# Check loaded stubs
curl http://localhost:8090/__admin/mappings
# Reload the missing stub
curl -X POST http://localhost:8090/__admin/mappings \
  -H "Content-Type: application/json" \
  -d @src\test\resources\stubs\get-account-stub.json
```

### Tests fail with "Connection refused to localhost:29092"
**Reason:** Kafka container is not running
**Fix:**
```bash
docker ps | grep kafka
docker compose up -d
docker compose logs kafka
```

### Port already in use error
**Reason:** Another application is using the same port
**Fix:**
```bash
netstat -ano | findstr "8090"
taskkill /PID <number> /F
```

---

## 🌐 Service URLs

| Service | URL | Purpose |
|---------|-----|---------|
| WireMock | http://localhost:8090 | Fake API server |
| WireMock Admin | http://localhost:8090/__admin/mappings | View stubs |
| Schema Registry | http://localhost:8081 | Kafka schema validation |
| MongoDB | mongodb://localhost:27017 | Database |
| Kafka | localhost:29092 | Used by Java tests |
| Zookeeper | localhost:2181 | Kafka coordinator |

---

## 📅 Progress

| Part | Topic | Status |
|------|-------|--------|
| Part 1 | Karate API Testing | Complete |
| Part 2 | Kafka Event-Driven Testing | In Progress |
| Part 3 | E2E Testing (API + Kafka + DB) | Upcoming |
| Part 4 | Oracle SQL | Upcoming |
| Part 5 | MongoDB | Upcoming |
| Part 6 | Agile + Jira + Xray | Upcoming |

---

## 👨‍💻 Author

**Geetu** — QA Automation Engineer
GitHub: https://github.com/Reddy062023/karate-banking-tests
