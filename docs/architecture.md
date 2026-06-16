# Architecture

This project is a learning simulation for a small insurance core. It is not a clone of any proprietary insurance platform.

## Layers

### Controller layer

Package: `com.codercollie.insurance_lab_core.controller`

Controllers expose HTTP endpoints under `/api/v1`. They receive request DTOs, run Bean Validation through `@Valid`, and delegate work to services.

Controllers should stay thin. They should not contain insurance business rules, database queries, or persistence entity construction.

### DTO layer

Package: `com.codercollie.insurance_lab_core.dto`

DTOs define the API contract. Request DTOs model incoming JSON. Response DTOs model outgoing JSON.

JPA entities are not returned directly from controllers. This keeps the REST API separate from the database model.

### Service layer

Package: `com.codercollie.insurance_lab_core.service`

Services coordinate application use cases, such as creating a quote, adding coverage to a product, or issuing a policy from a quote.

Services own transaction boundaries with `@Transactional`. Read-only methods use `@Transactional(readOnly = true)` when they only query data.

### Mapper layer

Package: `com.codercollie.insurance_lab_core.mapper`

Mappers convert between DTOs and persistence entities. Shared mapping rules, such as converting coverages to sorted coverage IDs, live in small mapper helpers.

### Repository layer

Package: `com.codercollie.insurance_lab_core.repository`

Repositories use Spring Data JPA to load and save persistence entities. They should not contain HTTP or DTO logic.

### Persistence entity layer

Package: `com.codercollie.insurance_lab_core.persistence.entity`

Persistence entities map Java objects to database tables. They are database-facing objects, not REST API objects.

### Domain layer

Package: `com.codercollie.insurance_lab_core.domain`

Domain classes and enums represent insurance concepts and business rules that do not need Spring MVC or database APIs.

Quote premium calculation is represented by the `PremiumCalculator` domain interface and implemented by `SimplePremiumCalculator`.

### Validation layer

Package: `com.codercollie.insurance_lab_core.validation`

Validation classes contain reusable request validation rules, such as checking that a quote end date is after its start date.

### Exception layer

Package: `com.codercollie.insurance_lab_core.exception`

Exception classes represent clear failure cases. `GlobalExceptionHandler` translates those failures into consistent HTTP error responses.

## Current Package Rule

Controllers may depend on DTOs and services.

Services may depend on repositories, mappers, domain services, exceptions, and persistence entities.

Mappers may depend on DTOs and persistence entities.

DTOs should not depend on persistence entities.

Repositories should only work with persistence entities.
