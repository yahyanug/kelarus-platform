# KELORA

> A modular, multi-tenant business management platform focused on warehouse, inventory, operational management, and future IoT-enabled business intelligence.

KELORA is an evolving SaaS platform designed to help businesses manage operational processes from a single ecosystem.

The project is being developed incrementally, starting from a strong platform foundation and Warehouse Management System (WMS) before expanding into POS, CRM, reporting, analytics, forecasting, AI-assisted insights, and future IoT integrations.

The project is developed under the **Flare Digital Indonesia** team.

---

## 🏗️ System Overview

Business operations such as inventory, warehouse management, sales, customer management, reporting, and operational monitoring are often handled by separate systems or even manually.

KELORA is designed to bring those processes into one platform while keeping each business capability modular, maintainable, and independently evolvable.

The platform follows a **microservices architecture** with clear service boundaries, API-based communication, and event-driven processing where asynchronous workflows are more appropriate.

```text
                                  KELORA
                                     │
                        ┌────────────┴────────────┐
                        │                         │
                   API Gateway              Service Discovery
                        │                         │
                        └────────────┬────────────┘
                                     │
             ┌───────────────────────┼────────────────────────┐
             │                       │                        │
             ▼                       ▼                        ▼
      Identity & Access        Tenant / Business         Notification
             │                    Management                  │
             │                       │                        │
             └───────────────────────┼────────────────────────┘
                                     │
                                     ▼
                         ┌───────────────────────┐
                         │   Business Platform   │
                         └───────────┬───────────┘
                                     │
                  ┌──────────────────┼──────────────────┐
                  │                  │                  │
                  ▼                  ▼                  ▼
                 WMS                POS                CRM
          Warehouse Management   Point of Sale   Customer Management
                  │                  │                  │
                  └──────────────────┼──────────────────┘
                                     │
                                     ▼
                             Inventory Core
                                     │
                      ┌──────────────┼──────────────┐
                      │              │              │
                      ▼              ▼              ▼
                    Stock          Batch          Expiry
                  Management     Tracking       Tracking
                      │              │              │
                      └──────────────┼──────────────┘
                                     │
                                     ▼
                               Stock Ledger
                                     │
                         ┌───────────┼───────────┐
                         │           │           │
                         ▼           ▼           ▼
                     Reporting   Analytics      IoT
                                     │           │
                                     └─────┬─────┘
                                           ▼
                                      Forecasting
                                           │
                                           ▼
                                      AI / Insights
```

> **Note:** The diagram represents the intended platform direction. Not every domain shown above is implemented yet.

---

## 🎯 Project Goals

KELORA is designed around several long-term goals:

- Provide a strong foundation for multi-tenant business applications.
- Prioritize warehouse and inventory traceability before expanding into POS.
- Keep business domains separated instead of building one large monolithic application.
- Make inventory movements auditable through a stock ledger.
- Support businesses with multiple warehouses, locations, users, and roles.
- Build reporting and analytics on top of reliable operational data.
- Introduce forecasting and AI only after the underlying business data is mature enough to support them.
- Support future IoT integrations for real-world warehouse and operational monitoring.
- Keep infrastructure complexity proportional to actual product requirements.

---

## 🧩 Core Domains

### Identity & Access Management

Responsible for user identity and account security.

Planned responsibilities include:

- user accounts
- credentials
- authentication
- authorization
- session / token management
- email verification
- password reset
- account security

### Tenant & Business Management

KELORA is designed as a **multi-tenant SaaS platform**.

This domain manages the relationship between users and businesses, including:

- tenants / businesses
- tenant members
- invitations
- roles
- permissions
- subscriptions
- subscription plans
- usage limits
- business-level access control

A user account is not permanently coupled to a single business.

```text
KELORA
│
├── Business / Tenant A
│   ├── Owner
│   ├── Employees
│   ├── Warehouses
│   ├── Products
│   └── Inventory
│
├── Business / Tenant B
│   ├── Owner
│   ├── Employees
│   ├── Warehouses
│   ├── Products
│   └── Inventory
│
└── Business / Tenant C
    └── ...
```

This allows one platform to serve multiple independent businesses while maintaining clear tenant boundaries.

### Warehouse Management System

WMS is one of the primary development priorities of KELORA.

The domain is intended to cover:

- warehouses
- warehouse locations
- product categories
- products
- product units
- inventory stock
- inventory batches
- stock adjustments
- stock transfers
- receiving
- batch tracking
- expiry tracking
- stock movement history

### Point of Sale

POS is planned as a later extension of the platform after the WMS and inventory foundations are stable.

Expected capabilities include:

- cashier operations
- carts
- transactions
- payments
- receipts
- order details
- product sales
- barcode-assisted workflows

### Customer Relationship Management

The CRM domain is intended to provide:

- customer profiles
- customer history
- customer transaction relationships
- future engagement capabilities

### Notification

A centralized notification service is planned to support multiple communication channels such as:

- email
- SMS
- WhatsApp
- internal system notifications

### Reporting & Analytics

Operational data generated by KELORA will form the foundation for:

- inventory reports
- warehouse reports
- sales reports
- stock movement analysis
- operational dashboards
- business performance analytics

### Forecasting & AI

Forecasting and AI are planned as higher-level capabilities built on top of reliable operational data.

Potential future use cases include:

- demand forecasting
- inventory recommendations
- low-stock prediction
- anomaly detection
- expiry-risk insights
- replenishment recommendations
- operational decision support

AI is intentionally treated as a later capability rather than the foundation of the platform.

---

## 🌐 Future IoT Integration

KELORA is also designed with future **Internet of Things (IoT)** integration in mind, especially for warehouse, logistics, and cold-chain operations.

IoT devices can act as real-world data sources for KELORA.

Potential device types include:

- temperature sensors
- humidity sensors
- door sensors
- RFID readers
- barcode scanners
- digital scales
- GPS trackers
- energy monitoring devices
- other warehouse sensors

A possible future flow:

```text
Physical Device / Sensor
          │
          ▼
   IoT Gateway / Edge Device
          │
          ▼
       MQTT Broker
          │
          ▼
   IoT Ingestion Service
          │
   ┌──────┼───────────┬──────────────┐
   │      │           │              │
   ▼      ▼           ▼              ▼
  WMS  Inventory   Notification   Analytics
                                   │
                                   ▼
                              Forecasting
                                   │
                                   ▼
                              AI Insights
```

Possible warehouse use cases:

```text
Temperature Sensor
        │
        ▼
Warehouse / Cold Storage
        │
        ▼
Threshold Monitoring
        │
        ├── Normal
        │
        └── Violation
               │
               ├── Save telemetry event
               ├── Notify warehouse staff
               ├── Flag affected inventory
               └── Feed analytics
```

For example, a cold-storage sensor might send telemetry such as:

```json
{
  "deviceId": "TEMP-WH01-001",
  "warehouseId": "WH-001",
  "temperature": -16.2,
  "timestamp": "2026-09-19T19:30:00+07:00"
}
```

KELORA could later evaluate the telemetry against configured business rules and generate alerts or inventory-related events.

Potential future IoT capabilities include:

- device registry
- device authentication
- telemetry ingestion
- device status monitoring
- threshold rules
- sensor history
- warehouse environmental monitoring
- cold-chain monitoring
- anomaly detection
- real-time operational alerts

IoT should be introduced incrementally and only when a concrete operational requirement exists.

---

## 📦 Inventory Philosophy

KELORA does not treat inventory as only a number representing the current quantity.

Every meaningful stock movement should be traceable.

```text
Purchase / Receiving
        │
        ▼
 Warehouse Stock
        │
        ├──── Transfer ────► Another Warehouse
        │
        ├──── Adjustment
        │
        ├──── Sale
        │
        ├──── Return
        │
        └──── Damaged / Expired
                    │
                    ▼
               Stock Ledger
```

The **Stock Ledger** is intended to act as the historical record of inventory movements.

Instead of relying only on:

```text
product.stock = 120
```

the platform should be able to explain how the stock became `120`.

This provides a foundation for:

- stock movement history
- inventory auditing
- warehouse-level stock
- batch tracking
- expiry tracking
- discrepancy investigation
- reporting
- analytics
- forecasting
- future IoT-driven operational insights

This design is especially useful for inventory-sensitive businesses such as retail, distribution, food supply, and warehouse operations including frozen-food inventory.

---

## ⚙️ Architecture Principles

### Domain Separation

Business capabilities are separated according to responsibility rather than being placed into one large application.

### Independent Services

Each service owns its application logic and can evolve independently.

### API-First Communication

Services communicate through explicit contracts rather than depending directly on another service's internal implementation.

### Event-Driven Where Appropriate

Asynchronous messaging may be used for operations that do not require an immediate synchronous response.

Examples may include:

- notifications
- audit events
- stock events
- reporting events
- IoT telemetry events
- background processing

### Database Ownership

A service should own the data that belongs to its domain.

Direct cross-service database access should be avoided.

### Traceable Inventory

Stock mutations should produce explainable business records rather than silently replacing quantity values.

### Incremental Complexity

KELORA is intentionally developed step by step.

Infrastructure, messaging, caching, observability, IoT, and other complexity should be introduced when a concrete requirement justifies them.

---

## 🧱 Current Repository Structure

KELORA currently uses a **Gradle multi-project monorepo**.

```text
kelora-platform/
│
├── buildSrc/
│   ├── build.gradle
│   └── src/main/groovy/
│       ├── kelora.java-conventions.gradle
│       ├── kelora.spring-service-conventions.gradle
│       └── kelora.jpa-conventions.gradle
│
├── component/
│   ├── account/
│   ├── api-gateway/
│   └── eureka-server/
│
├── docker/
│
├── gradle/
│
├── build.gradle
├── gradle.properties
├── settings.gradle
├── gradlew
├── gradlew.bat
├── LICENSE
└── README.md
```

The `component` directory groups independently runnable services.

Current Gradle project hierarchy:

```text
kelora-platform
└── component
    ├── account
    ├── api-gateway
    └── eureka-server
```

Gradle project paths:

```text
:component:account
:component:api-gateway
:component:eureka-server
```

Future components may be added as the platform evolves, for example:

```text
component/
├── account
├── api-gateway
├── eureka-server
├── tenant
├── wms
├── notification
├── pos
├── crm
└── iot
```

The exact service boundaries may change as requirements become clearer.

---

## 🛠️ Build Conventions

Shared Gradle build logic is maintained in `buildSrc`.

```text
kelora.java-conventions
        │
        └── Common Java configuration

kelora.spring-service-conventions
        │
        └── Common runnable Spring Boot service configuration

kelora.jpa-conventions
        │
        └── Common relational persistence configuration
```

This prevents duplicated build configuration across services while keeping service-specific dependencies inside each service.

---

## 💻 Technology Stack

### Backend

- Java 21
- Spring Boot
- Spring Cloud
- Gradle
- REST APIs
- Microservices

### Platform & Data

The platform is designed to support infrastructure such as:

- PostgreSQL
- Redis
- Elasticsearch
- Docker

### Future Messaging & IoT

Possible future infrastructure includes:

- Kafka or another message broker for asynchronous business events
- MQTT for IoT device communication
- telemetry storage where required
- event-driven processing for device and operational events

Additional infrastructure will be introduced only when required by the relevant domain.

### Architecture

- Microservices
- Multi-tenant SaaS
- API Gateway
- Service Discovery
- Role-Based Access Control
- Event-driven processing where appropriate
- Domain-oriented service boundaries
- Future IoT integration

---

## 🗺️ Development Roadmap

The current development direction is:

```text
Platform Foundation
        │
        ▼
Identity & Authentication
        │
        ▼
Tenant / Business Management
        │
        ▼
Warehouse Management System
        │
        ▼
Inventory & Stock Ledger
        │
        ▼
Reporting & Analytics
        │
        ▼
Point of Sale
        │
        ▼
CRM
        │
        ▼
IoT Integration
        │
        ▼
Forecasting & AI
```

### Phase 1 — Platform Foundation

Focus:

- Gradle multi-project structure
- shared build conventions
- account service
- API Gateway
- Eureka service discovery
- Docker-based local development environment
- foundational authentication design

- businesses / tenants
- memberships
- invitations
- roles
- permissions
- subscription foundation
- tenant-level authorization

### Phase 2 — WMS

Focus:

- warehouses
- warehouse locations
- products
- units
- inventory
- stock movement
- batches
- expiry
- stock ledger

### Phase 3 — Reporting & Operational Insights

Focus:

- inventory reporting
- warehouse reporting
- dashboards
- operational analytics

### Phase 4 — POS & CRM

Focus:

- cashier workflows
- sales transactions
- payments
- receipts
- customer profiles
- customer transaction history

### Phase 5 — Forecasting & AI

Potential focus:

- demand forecasting
- replenishment recommendations
- inventory anomaly detection
- expiry-risk analysis
- telemetry-assisted insights
- business decision support

### Phase 6 — IoT Integration

Potential focus:

- device registry
- device authentication
- MQTT integration
- telemetry ingestion
- warehouse sensor monitoring
- threshold rules
- cold-chain monitoring
- operational alerts

The roadmap may change as KELORA is validated against real-world requirements.

---

## 🚀 Running the Project

Clone the repository and run Gradle from the repository root.

### Windows

```bash
gradlew.bat clean build
```

### Linux / macOS

```bash
./gradlew clean build
```

View the Gradle project structure:

### Windows

```bash
gradlew.bat projects
```

### Linux / macOS

```bash
./gradlew projects
```

Individual services can be executed through their corresponding Gradle tasks or directly from the IDE.

> Environment-specific configuration may be required before all infrastructure-dependent services can run locally.

---

## 🧪 Development Status

KELORA is currently under active development.

The repository should be viewed as both:

- an evolving software product, and
- a practical exploration of scalable backend architecture and real-world business system design.

Architecture, service boundaries, business rules, infrastructure, and implementation details may continue to evolve as the project grows.

---

## 🔒 License

KELORA is **proprietary software**.

The source code may be publicly visible during the development and portfolio phase, but public availability does not grant permission to use, copy, modify, distribute, or commercialize the software.

See the [`LICENSE`](./LICENSE) file for details.

---

## 🌱 Where KELORA Is Heading

KELORA is not intended to become a collection of disconnected features.

The long-term goal is to build a platform where operational data flows naturally between business domains and, eventually, between physical operations and software systems.

The project will continue to evolve around real-world problems, business requirements, architectural learning, and lessons from existing products.

As the project becomes production-ready and commercially sensitive, parts or all of the repository may become private.
