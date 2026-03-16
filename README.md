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

The project follows Domain-Driven Design with a layered architecture:

```
src/main/java/com/application/enterprisebackenddesign/
├── domain/           # Core business logic
│   ├── order/        # Order aggregate
│   ├── invoice/     # Invoice aggregate
│   └── shared/      # Shared domain (Money, DomainEvent, exceptions)
├── application/      # Use cases
│   ├── order/       # Order use cases
│   └── invoice/     # Invoice use cases
├── infrastructure/   # External concerns
│   ├── persistence/ # JPA repositories
│   ├── messaging/   # Kafka event publishing
│   ├── security/   # JWT authentication
│   └── external/   # Payment gateway client
├── api/             # REST controllers (TODO)
└── config/          # Application configuration (TODO)
```

## Domain Features

### Order Management
- Create orders with multiple order lines
- Add, remove, and update order lines
- Confirm and cancel orders
- Automatic total amount calculation using Money value object

### Invoice Management
- Issue invoices from orders
- Track invoice status (DRAFT, ISSUED, PAID, CANCELLED)
- Mark invoices as paid

### Event-Driven Design
- Domain events for order and invoice lifecycle changes
- Kafka integration for event publishing
- Event types: OrderCreated, OrderConfirmed, OrderCancelled, InvoiceIssued, etc.

## Getting Started

### Prerequisites
- Java 17+
- Docker and Docker Compose (for local development)
- Maven 3.8+

### Running the Application

```bash
./mvnw spring-boot:run
```

The application will start on `http://localhost:8080`

### Configuration

Environment variables required:
- `DATABASE_URL` - PostgreSQL connection
- `KAFKA_BOOTSTRAP_SERVERS` - Kafka server
- `JWT_ISSUER_URI` - OAuth2 issuer for JWT validation

### Database

PostgreSQL with Flyway migrations. Tables:
- orders
- order_lines
- invoices
- payments

## Testing

```bash
./mvnw test
```

Uses Testcontainers for PostgreSQL and Kafka.

## Building

```bash
./mvnw clean package
```

## What's Left

- REST API controllers
- Security configuration
- API documentation (OpenAPI/Swagger)
- Full end-to-end integration
