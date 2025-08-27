# itsec_technical_test

## API Documentation
Spring Boot REST API that demonstrates user registration, JWT based authentication, article
management and audit logging.

## Prerequisites

- Java 24 or newer
- Docker (for optional PostgreSQL, pgAdmin and Mailpit services)
- Maven (the wrapper `mvnw` is included)

## Running the app

Start the backing services:

```bash
docker compose -f docker/docker-compose.yml up -d
```

Run the application:

```bash
./mvnw spring-boot:run
```

The API will be available at `http://localhost:8080`. The root endpoint returns a simple greeting
and the OpenAPI UI can be accessed at `http://localhost:8080/swagger-ui/index.html`.

## Sample requests

The [`collection-api`](collection-api) directory contains `.http` files that can be
imported into IDEs such as IntelliJ for quick testing of the endpoints.

## Running tests

```bash
./mvnw test
```

Swagger UI is available at `http://localhost:8080/swagger-ui/index.html` once the application is running.