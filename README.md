# 🏦 Micro Loan & Credit Scoring System

A comprehensive **Spring Boot microservices architecture** integrated with **machine learning** for intelligent credit scoring and micro-loan management. Built with Java 17,
Spring Boot 3.2.5, and powered by the SMILE ML library for predictive analytics.

---

## 📋 Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Technology Stack](#technology-stack)
- [Services](#services)
- [Project Structure](#project-structure)
- [Prerequisites](#prerequisites)
- [Getting Started](#getting-started)
- [Configuration](#configuration)
- [API Endpoints](#api-endpoints)
- [ML Model Service](#ml-model-service)
- [Database Setup](#database-setup)
- [Docker Deployment](#docker-deployment)
- [Development Guide](#development-guide)
- [Troubleshooting](#troubleshooting)
- [License](#license)

---

## 🎯 Overview

The **Micro Loan & Credit Scoring System** is an enterprise-grade platform designed to:

- ✅ **Authenticate users** with JWT-based security
- ✅ **Score creditworthiness** using advanced ML algorithms
- ✅ **Manage loan applications** with complete lifecycle tracking
- ✅ **Process documents** for KYC/compliance
- ✅ **Manage user data** with role-based access control
- ✅ **Route requests** through an intelligent API Gateway
- ✅ **Scale independently** via microservices architecture

**Key Features:**
- 🤖 Dual-model ensemble (Logistic Regression + Random Forest)
- 🔐 JWT Authentication & Spring Security
- 🗄️ MySQL Database Integration
- 🐳 Docker & containerization ready
- 🚀 Cloud-native microservices design
- 📊 Credit scoring with FICO-like score ranges (300-850)

---

## 🏗️ Architecture

```
┌────────────────────────────────────────────────────────────────┐
│                        API Gateway                             │
│                  (Request Routing & Load Balancing)            │
└────────────────────────────────────────────────────────────────┘
                              │
            ┌─────────────────┼─────────────────┬────────────────┐
            │                 │                 │                │
    ┌──────────────┐  ┌───────────────┐  ┌──────────────┐  ┌────────────┐
    │ User Service │  │ Loan Service  │  │ Data Service │  │ Document   │
    │              │  │               │  │              │  │ Service    │
    │ • Auth       │  │ • Applications│  │ • Profiles   │  │ • Upload   │
    │ • Profiles   │  │ • Disbursal   │  │ • Analytics  │  │ • Validate │
    └──────────────┘  └───────────────┘  └──────────────┘  └────────────┘
            │                 │                 │
            └─────────────────┼─────────────────┘
                              │
                    ┌─────────────────────┐
                    │ Credit Scoring Svc  │
                    │ • Score Calculation │
                    │ • Risk Assessment   │
                    └──────────┬──────────┘
                               │
                    ┌──────────────────────┐
                    │ ML Model Service     │
                    │ • SMILE Integration  │
                    │ • Ensemble Prediction│
                    │ • Fallback Rules     │
                    └──────────────────────┘
```

**Microservices Overview:**

| Service | Port | Purpose |
|---------|------|---------|
| **API Gateway** | 8000 | Request routing & load balancing |
| **User Service** | 8001 | User management & authentication |
| **Loan Service** | 8002 | Loan application lifecycle |
| **Data Service** | 8003 | User data & analytics |
| **Document Service** | 8004 | Document management & KYC |
| **Credit Scoring Service** | 8005 | Credit scoring & risk assessment |
| **ML Model Service** | 8006 | Machine learning predictions |
| **Common Module** | - | Shared utilities & models |

---

## 🛠️ Technology Stack

### Backend
- **Java 17** - Modern JVM language with pattern matching & records
- **Spring Boot 3.2.5** - Production-grade framework
- **Spring Security** - Authentication & authorization
- **Spring Data JPA** - ORM & database abstraction
- **MySQL** - Relational database
- **JWT (JJWT 0.11.5)** - Stateless authentication

### Machine Learning
- **SMILE 3.0.1** - Statistical Machine Intelligence and Learning Engine
  - Logistic Regression
  - Random Forest Classification
  - Feature importance analysis

### Build & DevOps
- **Maven 3.8+** - Dependency management & build automation
- **Docker** - Containerization
- **Lombok 1.18.32** - Boilerplate reduction

### Frontend (API Gateway)
- **React** - UI framework
- **Axios** - HTTP client
- **Node.js** - Runtime

---

## 📦 Services

### 1. **User Service** 🧑‍💼
Manages user registration, profiles, and authentication.

**Responsibilities:**3
- User registration & account management
- JWT token generation & validation
- Role-based access control (RBAC)
- User profile management

**Key Dependencies:**
- Spring Web, Security, Data JPA
- JWT (JJWT)
- MySQL
- Lombok

---

### 2. **Loan Service** 💰
Manages loan applications and disbursals.

**Responsibilities:**
- Loan application creation & tracking
- Loan status management
- Disbursal processing
- Interest calculation

**Key Dependencies:**
- Spring Web, Security, Data JPA, Validation
- JWT
- MySQL
- Common Module

---

### 3. **Data Service** 📊
Handles user financial data and analytics.

**Responsibilities:**
- Financial profile management
- Income & expense tracking
- Data aggregation for scoring
- Analytics & reporting

**Key Dependencies:**
- Spring Web, Data JPA
- MySQL
- Common Module

---

### 4. **Document Service** 📄
Manages document uploads and validation.

**Responsibilities:**
- Document upload & storage
- File validation (type, size)
- KYC document management
- Document verification

**Key Dependencies:**
- Spring Web, Data JPA
- File storage (local or cloud)

---

### 5. **Credit Scoring Service** 🎯
Core scoring engine that integrates ML models.

**Responsibilities:**
- Credit score calculation
- Risk assessment
- Scoring rule management
- ML model orchestration

**Key Dependencies:**
- Spring Web, Security, Data JPA, Validation
- JWT
- MySQL
- Common Module
- Calls ML Model Service

---

### 6. **ML Model Service** 🤖
Advanced machine learning engine with ensemble models.

**Responsibilities:**
- Credit prediction using SMILE
- Model training & reloading
- Feature engineering
- Fallback scoring rules

**Key Dependencies:**
- SMILE 3.0.1 (Core & IO)
- Apache Commons CSV
- Spring Actuator (health checks)
- Lombok

**Ensemble Strategy:**
```
Final Score = (0.35 × Logistic Regression) + (0.65 × Random Forest)
```

**Feature Engineering:**
- `debtToIncomeRatio` - Monthly debt vs income
- `incomeConsistencyScore` - Income stability
- `spendingVolatilityIndex` - Expense patterns
- `savingsRate` - Savings percentage
- `paymentRegularity` - Payment history

---

### 7. **Common Module** 🔧
Shared utilities, models, and constants.

**Responsibilities:**
- Shared DTOs & entities
- Constants & enums
- Utility functions
- Common exceptions

---

### 8. **API Gateway** 🚪
Entry point for all client requests.

**Responsibilities:**
- Request routing
- Load balancing
- Authentication forwarding
- CORS handling
- Response aggregation

**Frontend Stack:**
- React components
- Axios HTTP client
- Authentication service
- Service integrations

---

## 📂 Project Structure

```
MicroLoanAndCreditScoringSystem/
├── pom.xml                          # Parent POM with dependency management
│
├── common/                          # Shared module
│   ├── pom.xml
│   └── src/
│       └── main/java/com/gla/...
│
├── user-service/                    # User management
│   ├── pom.xml
│   └── src/
│       ├── main/
│       │   ├── java/com/gla/...
│       │   └── resources/
│       └── test/
│
├── loan-service/                    # Loan management
│   ├── pom.xml
│   └── src/
│       ├── main/java/com/gla/...
│       └── test/
│
├── data-service/                    # User data & analytics
│   ├── pom.xml
│   ├── application.yaml
│   └── src/
│       └── main/java/com/gla/...
│
├── document-service/                # Document management
│   ├── pom.xml
│   └── src/main/
│
├── credit-scoring-service/          # Credit scoring engine
│   ├── pom.xml
│   ├── application.yaml
│   └── src/main/java/com/gla/...
│
├── ml-model-service/                # ML engine
│   ├── pom.xml
│   ├── Dockerfile
│   ├── README.md
│   ├── training-schema.csv
│   └── src/
│       ├── main/java/com/gla/...
│       └── test/
│
├── api-gateway/                     # Gateway & frontend
│   ├── pom.xml
│   └── src/
│       ├── api/
│       │   ├── authService.js
│       │   ├── axiosConfig.js
│       │   ├── creditService.js
│       │   ├── dataService.js
│       │   ├── documentService.js
│       │   └── loanService.js
│       ├── components/
│       │   ├── LoginPage.jsx
│       │   └── ProtectedRoute.jsx
│       ├── main/java/
│       └── resources/
│
└── README.md                        # This file
```

---

## 📋 Prerequisites

### System Requirements
- **Java 17** or higher
- **Maven 3.8+**
- **MySQL 8.0+**
- **Docker** (optional, for containerization)
- **Node.js 16+** (for frontend development)

### Installation
```bash
# Verify Java
java -version  # Should be 17+

# Verify Maven
mvn -version   # Should be 3.8+

# Verify MySQL
mysql --version

# Verify Node.js (optional)
node --version
npm --version
```

---

## 🚀 Getting Started

### 1. Clone the Repository
```bash
git clone https://github.com/yourname/MicroLoanAndCreditScoringSystem.git
cd MicroLoanAndCreditScoringSystem
```

### 2. Build All Services
```bash
# Clean and build all modules
mvn clean install -DskipTests

# Or with tests
mvn clean install
```

### 3. Start Services

#### Option A: Individual Services (Terminal Windows)
```bash
# Terminal 1: User Service
cd user-service
mvn spring-boot:run

# Terminal 2: Loan Service
cd loan-service
mvn spring-boot:run

# Terminal 3: Data Service
cd data-service
mvn spring-boot:run

# Terminal 4: Document Service
cd document-service
mvn spring-boot:run

# Terminal 5: Credit Scoring Service
cd credit-scoring-service
mvn spring-boot:run

# Terminal 6: ML Model Service
cd ml-model-service
mvn spring-boot:run

# Terminal 7: API Gateway
cd api-gateway
mvn spring-boot:run
```

#### Option B: Docker Compose (Recommended)
```bash
# Build and start all services
docker-compose up -d

# View logs
docker-compose logs -f

# Stop all services
docker-compose down
```

### 4. Verify Services
```bash
# Check if services are running
curl http://localhost:8000/api/health      # API Gateway
curl http://localhost:8001/api/health      # User Service
curl http://localhost:8005/api/health      # Credit Scoring
curl http://localhost:8006/api/v1/model/health  # ML Model
```

---

## ⚙️ Configuration

### Database Setup

#### 1. Create MySQL Database
```sql
-- Connect to MySQL
mysql -u root -p

-- Create database
CREATE DATABASE micro_loan_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Create user (optional but recommended)
CREATE USER 'loan_user'@'localhost' IDENTIFIED BY 'secure_password';
GRANT ALL PRIVILEGES ON micro_loan_system.* TO 'loan_user'@'localhost';
FLUSH PRIVILEGES;

-- Verify
SHOW DATABASES;
```

#### 2. Update Service Configurations

**user-service/src/main/resources/application.yaml:**
```yaml
spring:
  application:
    name: user-service
  datasource:
    url: jdbc:mysql://localhost:3306/micro_loan_system
    username: loan_user
    password: secure_password
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQL8Dialect
  security:
    jwt:
      secret: your-secret-key-change-in-production
      expiration: 3600000  # 1 hour in milliseconds

server:
  port: 8001
  servlet:
    context-path: /api
```

**credit-scoring-service/src/main/resources/application.yaml:**
```yaml
spring:
  application:
    name: credit-scoring-service
  datasource:
    url: jdbc:mysql://localhost:3306/micro_loan_system
    username: loan_user
    password: secure_password
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    hibernate:
      ddl-auto: update

server:
  port: 8005
  servlet:
    context-path: /api

ml-model:
  service:
    url: http://localhost:8006
```

**ml-model-service/src/main/resources/application.yaml:**
```yaml
spring:
  application:
    name: ml-model-service

server:
  port: 8006
  servlet:
    context-path: /api

ml:
  model:
    weights:
      logistic-regression: 0.35
      random-forest: 0.65
    feature-importance:
      debtToIncomeRatio: 0.25
      incomeConsistencyScore: 0.20
      spendingVolatilityIndex: 0.20
      savingsRate: 0.20
      paymentRegularity: 0.15
```

### JWT Configuration
- **Secret Key**: Change in production (use environment variables)
- **Expiration**: Configure token validity period (default: 1 hour)
- **Algorithm**: HS512 (HMAC SHA-512)

---

## 🔌 API Endpoints

### Authentication
```bash
# User Registration
POST /api/users/register
Content-Type: application/json

{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "securePassword123"
}

# User Login
POST /api/users/login
Content-Type: application/json

{
  "username": "john_doe",
  "password": "securePassword123"
}

# Response
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "expiresIn": 3600000
}
```

### Credit Scoring
```bash
# Calculate Credit Score
POST /api/credit/score
Authorization: Bearer {token}
Content-Type: application/json

{
  "userId": "12345",
  "monthlyIncome": 5000,
  "monthlyExpenses": 2000,
  "existingEMIs": 1000,
  "employmentLengthMonths": 36,
  "missedPaymentsCount": 0
}

# Response
{
  "creditScore": 750,
  "riskCategory": "LOW",
  "debtToIncomeRatio": 0.6,
  "recommendation": "APPROVED",
  "confidence": 0.92,
  "modelUsed": "ENSEMBLE"
}
```

### Loan Management
```bash
# Apply for Loan
POST /api/loans/apply
Authorization: Bearer {token}
Content-Type: application/json

{
  "userId": "12345",
  "loanAmount": 50000,
  "loanTerm": 12,
  "purpose": "Personal"
}

# Get Loan Status
GET /api/loans/{loanId}
Authorization: Bearer {token}

# Response
{
  "loanId": "LOAN-001",
  "userId": "12345",
  "amount": 50000,
  "status": "APPROVED",
  "creditScore": 750,
  "interestRate": 8.5,
  "disbursalDate": "2026-04-20"
}
```

### User Data
```bash
# Get User Profile
GET /api/data/profile/{userId}
Authorization: Bearer {token}

# Update Financial Profile
PUT /api/data/profile/{userId}
Authorization: Bearer {token}
Content-Type: application/json

{
  "monthlyIncome": 6000,
  "monthlyExpenses": 2500,
  "savingsBalance": 25000
}
```

### Document Management
```bash
# Upload Document
POST /api/documents/upload
Authorization: Bearer {token}
Content-Type: multipart/form-data

file: [document.pdf]
documentType: "INCOME_PROOF"

# Get Documents
GET /api/documents/{userId}
Authorization: Bearer {token}
```

### ML Model Service
```bash
# Get Credit Prediction
POST /api/v1/model/predict
Content-Type: application/json

{
  "monthlyIncome": 5000,
  "monthlyExpenses": 2000,
  "existingEMIs": 1000,
  "employmentLengthMonths": 36,
  "missedPaymentsCount": 0
}

# Response
{
  "predictedProbability": 0.87,
  "creditScore": 750,
  "riskCategory": "LOW",
  "featureImportance": {
    "debtToIncomeRatio": 0.25,
    "incomeConsistencyScore": 0.20,
    "spendingVolatilityIndex": 0.20,
    "savingsRate": 0.20,
    "paymentRegularity": 0.15
  },
  "modelVersion": "1.0",
  "timestamp": "2026-04-16T10:30:00Z"
}

# Get Model Health
GET /api/v1/model/health

# Get Model Metadata
GET /api/v1/model/metadata

# Train Models
POST /api/v1/model/train
Content-Type: application/json

{
  "trainingDataPath": "/data/training-schema.csv"
}

# Reload Models
POST /api/v1/model/reload
```

---

## 🤖 ML Model Service

### Overview
The ML Model Service provides intelligent credit scoring using an **ensemble of two models**:
1. **Logistic Regression** (35% weight)
2. **Random Forest** (65% weight)

### Model Architecture
```
Input Data
    ↓
Feature Engineering
    ├─→ debtToIncomeRatio
    ├─→ incomeConsistencyScore
    ├─→ spendingVolatilityIndex
    ├─→ savingsRate
    └─→ paymentRegularity
    ↓
Ensemble Prediction
    ├─→ Logistic Regression Probability (35%)
    ├─→ Random Forest Probability (65%)
    └─→ Combined Probability
    ↓
FICO-like Score (300-850)
    ↓
Risk Category & Decision
```

### Feature Engineering
Features are automatically computed from:
- Monthly Income
- Monthly Expenses
- Existing EMIs (Equated Monthly Installments)
- Employment Length (months)
- Missed Payments Count

### Fallback Mechanism
If models fail to load, a **rule-based fallback algorithm** ensures scoring continues:
```
fallbackScore = baseScore + (adjustments based on rules)
```

### Model Training
```bash
# Prepare training data (CSV format)
csv_format: monthlyIncome, monthlyExpenses, existingEMIs, ..., defaulted

# Call training endpoint
curl -X POST http://localhost:8006/api/v1/model/train \
  -H "Content-Type: application/json" \
  -d '{"trainingDataPath": "/data/training-schema.csv"}'

# Models are saved to disk for reuse
```

### Docker Deployment
```bash
# Build Docker image
docker build -t ml-model-service:1.0 .

# Run container with volume mount for models
docker run -d \
  -p 8006:8080 \
  -v $(pwd)/models:/app/models \
  --name ml-model-service \
  ml-model-service:1.0

# Check logs
docker logs -f ml-model-service
```

---

## 💾 Database Setup

### Initial Setup
```bash
# 1. Start MySQL
mysql -u root -p

# 2. Create database
CREATE DATABASE micro_loan_system;
USE micro_loan_system;

# 3. Spring Boot will auto-create tables
# When services start with: spring.jpa.hibernate.ddl-auto=update
```

### Database Schema Overview

**Users Table**
```sql
CREATE TABLE users (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(100) UNIQUE NOT NULL,
  email VARCHAR(255) UNIQUE NOT NULL,
  password VARCHAR(255) NOT NULL,
  role ENUM('ADMIN', 'USER', 'LOAN_OFFICER'),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

**Loans Table**
```sql
CREATE TABLE loans (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT,
  amount DECIMAL(15, 2),
  term_months INT,
  status ENUM('APPLIED', 'APPROVED', 'REJECTED', 'DISBURSED'),
  credit_score INT,
  interest_rate DECIMAL(5, 2),
  created_at TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id)
);
```

**Credit Scores Table**
```sql
CREATE TABLE credit_scores (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT,
  score INT,
  risk_category VARCHAR(50),
  model_version VARCHAR(10),
  calculated_at TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id)
);
```

### Backup & Recovery
```bash
# Backup database
mysqldump -u loan_user -p micro_loan_system > backup.sql

# Restore database
mysql -u loan_user -p micro_loan_system < backup.sql
```

---

## 🐳 Docker Deployment

### Docker Compose (All Services)
```yaml
# docker-compose.yml
version: '3.8'

services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: micro_loan_system
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql

  user-service:
    build: ./user-service
    ports:
      - "8001:8080"
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/micro_loan_system
      SPRING_DATASOURCE_USERNAME: root
      SPRING_DATASOURCE_PASSWORD: root
    depends_on:
      - mysql

  credit-scoring-service:
    build: ./credit-scoring-service
    ports:
      - "8005:8080"
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/micro_loan_system
      ML_MODEL_SERVICE_URL: http://ml-model-service:8080
    depends_on:
      - mysql

  ml-model-service:
    build: ./ml-model-service
    ports:
      - "8006:8080"
    volumes:
      - models_data:/app/models

  api-gateway:
    build: ./api-gateway
    ports:
      - "8000:8080"
    depends_on:
      - user-service
      - credit-scoring-service

volumes:
  mysql_data:
  models_data:
```

### Build & Run
```bash
# Build all Docker images
docker-compose build

# Start all services
docker-compose up -d

# View logs
docker-compose logs -f

# Stop all services
docker-compose down

# Remove volumes (careful!)
docker-compose down -v
```

### Individual Service Docker
```bash
# Build image
docker build -t user-service:1.0 ./user-service

# Run container
docker run -d \
  -p 8001:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/micro_loan_system \
  --name user-service \
  user-service:1.0

# Stop container
docker stop user-service
```

---

## 👨‍💻 Development Guide

### IDE Setup
#### IntelliJ IDEA
1. Open project: File → Open → Select project root
2. Maven: Right-click `pom.xml` → Maven → Reload Projects
3. Run Configuration: Edit Configurations → New → Maven
4. Command line: `spring-boot:run`
5. Working directory: Select service directory

#### VS Code
```bash
# Install extensions
- Extension Pack for Java
- Spring Boot Extension Pack
- Maven for Java

# Open workspace
code .
```

### Development Workflow
```bash
# 1. Make code changes
# 2. Rebuild module
mvn clean compile -pl credit-scoring-service

# 3. Run tests
mvn test -pl credit-scoring-service

# 4. Build JAR
mvn clean package -pl credit-scoring-service

# 5. Run service
cd credit-scoring-service
mvn spring-boot:run
```

### Testing
```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=CreditScoringServiceTest

# Run with coverage
mvn clean test jacoco:report

# View coverage report
open target/site/jacoco/index.html
```

### Logging
**Configure logging level in application.yaml:**
```yaml
logging:
  level:
    root: INFO
    com.gla: DEBUG
    org.springframework.security: DEBUG
    org.springframework.web: DEBUG
  pattern:
    console: "%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
```

### Debugging
```bash
# Run service in debug mode
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005"

# In IDE: Run → Debug → Attach to localhost:5005
```

### Code Style
```xml
<!-- checkstyle.xml configuration in parent pom -->
<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-checkstyle-plugin</artifactId>
  <version>3.2.0</version>
</plugin>
```

---

## 🔐 Security Best Practices

### JWT Token Management
```java
// Generate token
String token = jwtProvider.generateToken(username);

// Validate token
boolean isValid = jwtProvider.validateToken(token);

// Extract claims
String username = jwtProvider.getUsername(token);
```

### Password Encryption
```java
// Use BCryptPasswordEncoder
passwordEncoder.encode("plainPassword");

// During login, compare
passwordEncoder.matches(rawPassword, hashedPassword);
```

### Authorization
```java
// Secure endpoint with role
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<?> deleteUser(@PathVariable Long id) { }

// Secure with custom expression
@PreAuthorize("@userService.isOwner(#userId)")
public ResponseEntity<?> getProfile(@PathVariable Long userId) { }
```

### CORS Configuration
```java
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
            .allowedOrigins("http://localhost:3000")
            .allowedMethods("GET", "POST", "PUT", "DELETE")
            .allowCredentials(true);
    }
}
```

---

## 🐛 Troubleshooting

### Issue: Services Won't Start

**Problem:** Port already in use
```bash
# Find process using port 8001
lsof -i :8001  # macOS/Linux
netstat -ano | findstr :8001  # Windows

# Kill process
kill -9 <PID>  # macOS/Linux
taskkill /PID <PID> /F  # Windows
```

**Problem:** Database connection failed
```bash
# Verify MySQL is running
mysql -u root -p -e "SELECT 1"

# Check application.yaml configuration
# Verify username, password, and database name

# Test connection
telnet localhost 3306
```

### Issue: ML Model Service Not Responding

**Problem:** Model files missing
```bash
# Check if models directory exists
ls -la ml-model-service/models/

# If missing, train models
curl -X POST http://localhost:8006/api/v1/model/train \
  -H "Content-Type: application/json" \
  -d '{"trainingDataPath": "training-schema.csv"}'
```

**Problem:** Memory insufficient
```bash
# Increase JVM heap size
export JAVA_OPTS="-Xmx2g -Xms1g"
mvn spring-boot:run
```

### Issue: JWT Token Validation Failures

**Problem:** Token expired or invalid
```bash
# Check token expiration in application.yaml
# Default: 1 hour (3600000 ms)

# Refresh token by logging in again
POST /api/users/login

# Update JWT secret in all services (consistency required)
```

### Issue: API Gateway Routing Issues

**Problem:** Service not found
```bash
# Verify all services are running
curl http://localhost:8000/actuator/health

# Check service registration in gateway configuration
# Verify service URLs and ports in routing config
```

### Common Maven Issues
```bash
# Clear cache
mvn clean

# Update dependencies
mvn dependency:resolve

# Skip tests during build
mvn clean install -DskipTests

# Show dependency tree
mvn dependency:tree
```

---

## 📚 API Documentation

### Swagger/OpenAPI Integration
```xml
<!-- Add to pom.xml -->
<dependency>
  <groupId>org.springdoc</groupId>
  <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
  <version>2.0.0</version>
</dependency>
```

Access Swagger UI:
- User Service: `http://localhost:8001/swagger-ui.html`
- Credit Scoring: `http://localhost:8005/swagger-ui.html`
- ML Model: `http://localhost:8006/swagger-ui.html`

---

## 📊 Monitoring & Health Checks

### Actuator Endpoints
```bash
# Health status
curl http://localhost:8001/actuator/health

# Metrics
curl http://localhost:8001/actuator/metrics

# Environment
curl http://localhost:8001/actuator/env

# Beans
curl http://localhost:8001/actuator/beans
```

### Application Metrics
```yaml
# Configure in application.yaml
management:
  endpoints:
    web:
      exposure:
        include: health,metrics,prometheus,env
  metrics:
    export:
      prometheus:
        enabled: true
```

---

## 🚢 Deployment Checklist

- [ ] Update database credentials (use environment variables)
- [ ] Update JWT secret key
- [ ] Change default passwords
- [ ] Enable HTTPS/SSL
- [ ] Configure CORS properly
- [ ] Set appropriate JVM heap sizes
- [ ] Enable monitoring & logging
- [ ] Run security scans (OWASP dependency check)
- [ ] Test all critical flows
- [ ] Setup backup strategy
- [ ] Configure load balancing
- [ ] Document API endpoints

---

## 📄 License

This project is licensed under the MIT License. See LICENSE file for details.

---

## 👥 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit changes (`git commit -m 'Add AmazingFeature'`)
4. Push to branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

## 📞 Support

For issues, questions, or suggestions:
- 📧 Email: support@example.com
- 💬 GitHub Issues: Create an issue
- 📖 Wiki: Check project wiki for FAQs

---

## 🎯 Roadmap

- [ ] Integration with payment gateways
- [ ] Advanced analytics dashboard
- [ ] Mobile app support
- [ ] GraphQL API
- [ ] Real-time notifications
- [ ] Advanced fraud detection
- [ ] Multi-currency support
- [ ] KYC integration APIs

---

**Last Updated:** April 16, 2026  
**Version:** 1.0.0  
**Maintainers:** Development Team

