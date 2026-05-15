# Enterprise Backend Design

An Enterprise Order and Billing Service built with Spring Boot 4, implementing Domain-Driven Design (DDD) principles.

## Tech Stack

- **Framework**: Spring Boot 4.0.2
- **Language**: Java 17
- **Database**: PostgreSQL with Flyway migrations
- **Messaging**: Apache Kafka
- **Security**: OAuth2 Resource Server with JWT
- **Testing**: JUnit 5 with Testcontainers

## Architecture

The project follows Domain-Driven Design with a Hexagonal (Ports & Adapters) architecture:

```
src/main/java/com/application/enterprisebackenddesign/
├── domain/           # Core business logic (entities, value objects, domain events, repository ports)
├── application/      # Use cases and application services (orchestrates domain logic)
├── infrastructure/   # Adapters: persistence (JPA), messaging (Kafka), security (JWT), external integrations
├── api/              # Inbound adapters: REST controllers with OpenAPI documentation
└── config/           # Spring configuration and bean wiring
```

## Domain Features

### Customer Management
- Create, update, delete customers
- Paginated listing

### Product Management
- Create, update, delete products
- SKU-based lookup

### Order Management
- Create orders with multiple order lines
- Add, remove, and update order lines
- Confirm and cancel orders
- Automatic total amount calculation using Money value object

### Invoice Management
- Issue invoices from confirmed orders
- Track invoice status (DRAFT, ISSUED, PAID, CANCELLED)
- Mark invoices as paid

### Payment Processing
- Process payments via external payment gateway
- Validate invoice status and amount matching
- Automatic invoice status update on payment completion

### Event-Driven Design
- Domain events for all entity lifecycle changes
- Spring `ApplicationEventPublisher` for synchronous in-process handling
- Async event handlers for side effects (email, CRM sync, analytics, inventory)
- Kafka integration for external event streaming

## Getting Started

### Prerequisites
- Java 17+
- Docker and Docker Compose (for local development with PostgreSQL and Kafka)
- Maven 3.8+

### Running the Application

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod
```

With the `prod` profile, ensure these environment variables are set:
- `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`
- `KAFKA_BOOTSTRAP_SERVERS`
- `JWT_SECRET`

For development, default credentials are in `application.yml`.

The application will start on `http://localhost:8080`

### Database

PostgreSQL with Flyway migrations. Tables and migrations:

| Migration | Description |
|---|---|
| V1 | Create customers, products |
| V2 | Create orders, order_lines |
| V3 | Create invoices |
| V4 | Create payments |
| V5 | Add audit fields (version, created_date, last_modified_date) |
| V6 | Seed sample data |
| V7 | Rollback script (reverse V1-V6) |

### Seed Data

Migration V6 inserts sample data for development:
- **Customers**: Alice Johnson, Bob Williams
- **Products**: Office Chair, Desk Lamp, Notebook Set
- **Orders**: CREATED, CONFIRMED, CANCELLED statuses
- **Invoices**: ISSUED and PAID
- **Payments**: One completed payment

### Rollback Procedure

Since Flyway Community Edition does not support `undo` migrations, manual rollback is done via the V7 migration:

1. Apply V7 to reverse all migrations: Flyway will execute `V7__rollback.sql`
2. Remove V7 from the migrations directory after applying
3. Use `flyway repair` if the schema history table is out of sync

```bash
# Apply rollback
./mvnw flyway:migrate -Dflyway.locations=classpath:db/migration
```

**Note**: V7 drops all tables. Export any needed data before rolling back.

## Authentication

All endpoints except `GET /api/products` and `POST /api/auth/login` require a Bearer JWT token.

### Auth (`/api/auth`)
- `POST /api/auth/login` - Authenticate and receive JWT token

## API Endpoints

### Customers (`/api/customers`)
- `POST /api/customers` - Create customer
- `GET /api/customers` - List customers (paginated)
- `GET /api/customers/{id}` - Get customer by ID
- `PUT /api/customers/{id}` - Update customer
- `DELETE /api/customers/{id}` - Delete customer

### Products (`/api/products`)
- `POST /api/products` - Create product
- `GET /api/products` - List products (paginated, no auth required)
- `GET /api/products/{id}` - Get product by ID
- `PUT /api/products/{id}` - Update product
- `DELETE /api/products/{id}` - Delete product

### Orders (`/api/orders`)
- `POST /api/orders` - Create order
- `GET /api/orders` - List orders (paginated, filterable by `customerId`, `status`)
- `GET /api/orders/{id}` - Get order by ID
- `POST /api/orders/{id}/confirm` - Confirm order
- `POST /api/orders/{id}/cancel` - Cancel order
- `POST /api/orders/{id}/lines` - Add order line
- `PUT /api/orders/{id}/lines/{lineId}` - Update order line quantity
- `DELETE /api/orders/{id}/lines/{lineId}` - Remove order line

### Invoices (`/api/invoices`)
- `POST /api/invoices/issue/{orderId}` - Issue invoice for order
- `GET /api/invoices` - List invoices (paginated)
- `GET /api/invoices/{id}` - Get invoice by ID

### Payments (`/api/payments`)
- `POST /api/payments` - Process payment against an invoice
- `GET /api/payments` - List payments (paginated, filterable by `invoiceId`, `customerId`, `status`)
- `GET /api/payments/{id}` - Get payment by ID

## Testing

```bash
./mvnw test
```

Uses Testcontainers for PostgreSQL and Kafka.

### Test Structure
- **Controller integration tests**: `api/*/...IntegrationTest.java` (35 tests)
- **Repository integration tests**: `infrastructure/persistence/*/...IntegrationTest.java` (40 tests)
- **Unit tests**: Domain model, event handlers, ID generator

### Static Analysis

SpotBugs is integrated via Maven plugin:
```bash
./mvnw spotbugs:check
```

## Building

```bash
./mvnw clean package
```
