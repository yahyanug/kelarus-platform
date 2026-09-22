# KELARUS

> A modular, multi-tenant business management platform focused on warehouse, inventory, operational management, and future IoT-enabled business intelligence.

KELARUS is an evolving SaaS platform designed to help businesses manage operational processes from a single ecosystem.

The project is being developed incrementally, starting from a strong platform foundation and Warehouse Management System (WMS) before expanding into POS, CRM, reporting, analytics, forecasting, AI-assisted insights, and future IoT integrations.

The project is developed under the **Flare Digital Indonesia** team.

---

## 🏗️ System Overview

Business operations such as inventory, warehouse management, sales, customer management, reporting, and operational monitoring are often handled by separate systems or even manually.

KELARUS is designed to bring those processes into one platform while keeping each business capability modular, maintainable, and independently evolvable.

The platform follows a **microservices architecture** with clear service boundaries, API-based communication, and event-driven processing where asynchronous workflows are more appropriate.

```text
                                  KELARUS
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

KELARUS is designed around several long-term goals:

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

KELARUS is designed as a **multi-tenant SaaS platform**.

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
KELARUS
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

WMS is one of the primary development priorities of KELARUS.

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

Operational data generated by KELARUS will form the foundation for:

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

KELARUS is also designed with future **Internet of Things (IoT)** integration in mind, especially for warehouse, logistics, and cold-chain operations.

IoT devices can act as real-world data sources for KELARUS.

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

KELARUS could later evaluate the telemetry against configured business rules and generate alerts or inventory-related events.

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

KELARUS does not treat inventory as only a number representing the current quantity.

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

KELARUS is intentionally developed step by step.

Infrastructure, messaging, caching, observability, IoT, and other complexity should be introduced when a concrete requirement justifies them.

---

## 🧱 Current Repository Structure

KELARUS currently uses a **Gradle multi-project monorepo**.

```text
kelarus-platform/
│
├── buildSrc/
│   ├── build.gradle
│   └── src/main/groovy/
│       ├── kelarus.java-conventions.gradle
│       ├── kelarus.spring-service-conventions.gradle
│       ├── kelarus.jpa-conventions.gradle
│       ├── kelarus.infrastructure-conventions.gradle
│       └── kelarus.library-conventions.gradle
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

Each runnable service uses `src/main/resources/application.yml` for Spring Boot
configuration. Keep any future profile-specific configuration in `application-<profile>.yml`.

The `account` service implements email/password registration, email verification, JWT login,
refresh rotation, logout, and password reset/change. Its implementation is organized under
`src/main/java/id/com/flare/kelarus/component/` into `controller`, `dto`, `domain`,
`repository`, `service`, `config`, `security`, `exception`, and `validation`. Business service
interfaces are in `service/`, with implementations in `service/impl/`. The account database
schema is defined in `src/main/resources/db/changelog/` (one master changelog and
five table changesets), and integration/security tests live under `src/test/java/`.
PostgreSQL provisioning is in `docker/postgres/init-account.sql`.
See [account setup and API documentation](component/account/README.md) for environment
variables, local execution, Liquibase, testing, and the deferred notification-delivery boundary.
The existing `utilities/general` directory remains reserved for shared KELARUS code.

Current Gradle project hierarchy:

```text
kelarus-platform
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

| Convention | Responsibility | Current consumers |
| --- | --- | --- |
| `kelarus.java-conventions` | Java toolchain, UTF-8 compilation, JUnit Platform | All conventions |
| `kelarus.infrastructure-conventions` | Boot packaging, Boot/Cloud BOMs, common Boot test dependencies; no web stack or persistence | Gateway, Eureka, service convention |
| `kelarus.spring-service-conventions` | Infrastructure base plus MVC, validation, Eureka client | Application services |
| `kelarus.jpa-conventions` | Service base plus Spring Data JPA | Account |
| `kelarus.library-conventions` | Java library with API/implementation separation; no Boot packaging or runtime starters | Available for future shared libraries |

Versions remain centralized in `gradle.properties`; module builds declare only their
specific dependencies. A new MVC service applies the service convention, a JPA service
applies the JPA convention, and infrastructure chooses its web stack explicitly.
Register new modules in `settings.gradle`. The unused `utilities/general/build.gradle`
stays empty until shared code needs it. Swagger annotations and Lombok remain Account-only,
supporting its existing controller annotations without adding an OpenAPI server.

```text
kelarus.java-conventions
        │
        └── Common Java configuration

kelarus.spring-service-conventions
        │
        └── Common runnable Spring Boot service configuration

kelarus.jpa-conventions
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

The roadmap may change as KELARUS is validated against real-world requirements.

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

### Eureka and Gateway foundation

Start these commands in separate terminals, in order:

```powershell
.\gradlew.bat :component:eureka-server:bootRun
.\gradlew.bat :component:account:bootRun
.\gradlew.bat :component:api-gateway:bootRun
```

Account requires its existing PostgreSQL database and security environment variables;
see [Account setup](component/account/README.md). No authentication behavior is changed
by the discovery/routing foundation.

| Component | Port | Discovery behavior |
| --- | --- | --- |
| Eureka Server | 18102 | Standalone registry; does not register itself or fetch peers |
| Account | 18101 | Registers as `account` and fetches the registry |
| API Gateway | 18100 | Fetches the registry and forwards to `lb://account` |

`KELARUS_EUREKA_URL` defaults to `http://localhost:18102/eureka/`.
The standalone registry's `KELARUS_EUREKA_HOSTNAME` defaults to `localhost`; its self URL
uses that hostname and its configured port so it is not mistaken for a peer.
The Gateway exposes `/v1/public/auth/**` and `/v1/auth/**` with their paths unchanged.
Authorization headers are forwarded; Account continues to enforce authentication.
Automatic discovery route exposure is disabled, so `/account/**` is not a route.
If no Account instance is discoverable, the matching route returns 503.
Registration and registry refresh are asynchronous; allow time for discovery after startup.
The Eureka dashboard is at `http://localhost:18102/`; keep the registry on a trusted
internal network when deploying.

For Docker, `docker/apps.yml` contains the same three applications on the existing
shared network. Build their images with the corresponding `bootBuildImage` tasks and
`--imageName=kelarus-platform/eureka-server`, `--imageName=kelarus-platform/account`,
and `--imageName=kelarus-platform/api-gateway`. Start the existing PostgreSQL service
and provision `kelarus_account` first. Supply Account's database credentials and JWT
secret through the environment, then run:

```powershell
docker compose --env-file docker/.env -f docker/apps.yml up -d
```

Containers use `http://kelarus-platform-eureka-server:18102/eureka/` for discovery.
Account advertises its container IP so the Gateway can reach it on the shared network.
No database volumes are recreated by this setup.

Gateway tests exercise discovery-based load balancing against an isolated HTTP fixture,
including path/body/header forwarding and downstream 401 responses. Eureka tests start
the real registry and register, look up, and remove an Account fixture.
Account's discovery test starts its real Eureka client against an isolated registry fixture
and verifies registration. A missing discovered Account is also tested to return 503.

> Environment-specific configuration may be required before all infrastructure-dependent services can run locally.

---

## 🧪 Development Status

KELARUS is currently under active development.

The repository should be viewed as both:

- an evolving software product, and
- a practical exploration of scalable backend architecture and real-world business system design.

Architecture, service boundaries, business rules, infrastructure, and implementation details may continue to evolve as the project grows.

---

## 🔒 License

KELARUS is **proprietary software**.

The source code may be publicly visible during the development and portfolio phase, but public availability does not grant permission to use, copy, modify, distribute, or commercialize the software.

See the [`LICENSE`](./LICENSE) file for details.

---

## 🌱 Where KELARUS Is Heading

KELARUS is not intended to become a collection of disconnected features.

The long-term goal is to build a platform where operational data flows naturally between business domains and, eventually, between physical operations and software systems.

The project will continue to evolve around real-world problems, business requirements, architectural learning, and lessons from existing products.

As the project becomes production-ready and commercially sensitive, parts or all of the repository may become private.
