# Architecture

This project is a learning simulation for a small insurance core. It is not a clone of any proprietary insurance platform.

## Layers

The project uses a simple layered architecture:

```text
HTTP request
  -> Controller
  -> Service
  -> Repository
  -> Database

Service
  -> Mapper
  -> DTO response
```

The goal is not to make many layers for their own sake. The goal is to keep each kind of code in a predictable place.

### Controller layer

Package: `com.codercollie.insurance_lab_core.controller`

Controllers expose HTTP endpoints under `/api/v1`. They receive request DTOs, run Bean Validation through `@Valid`, and delegate work to services.

Controllers should stay thin. They should not contain insurance business rules, database queries, or persistence entity construction.

Current controllers:

- `CustomerController`: customer read and create endpoints.
- `ProductController`: product read and create endpoints, plus product coverage endpoints.
- `QuoteController`: quote read and create endpoints, plus quote issuing.

### DTO layer

Package: `com.codercollie.insurance_lab_core.dto`

DTOs define the API contract. Request DTOs model incoming JSON. Response DTOs model outgoing JSON.

JPA entities are not returned directly from controllers. This keeps the REST API separate from the database model.

### Service layer

Package: `com.codercollie.insurance_lab_core.service`

Services coordinate application use cases, such as creating a quote, adding coverage to a product, or issuing a policy from a quote.

Services own transaction boundaries with `@Transactional`. Read-only methods use `@Transactional(readOnly = true)` when they only query data.

Current services:

- `CustomerService`: customer lookup and creation.
- `ProductService`: product lookup and creation.
- `CoverageService`: adding coverages to products and listing product coverages.
- `QuoteService`: quote lookup and quote creation.
- `PolicyIssueService`: issuing a policy from a quote and creating the first premium.
- `SimplePremiumCalculator`: simple learning-only premium calculation.

### Mapper layer

Package: `com.codercollie.insurance_lab_core.mapper`

Mappers convert between DTOs and persistence entities. Shared mapping rules, such as converting coverages to sorted coverage IDs, live in small mapper helpers.

Mapper rules:

- Controllers should return response DTOs, not persistence entities.
- Create request DTOs are converted into entities by mappers or services.
- Repeated mapping rules should be extracted into small mapper helpers.

### Repository layer

Package: `com.codercollie.insurance_lab_core.repository`

Repositories use Spring Data JPA to load and save persistence entities. They should not contain HTTP or DTO logic.

### Persistence entity layer

Package: `com.codercollie.insurance_lab_core.persistence.entity`

Persistence entities map Java objects to database tables. They are database-facing objects, not REST API objects.

Persistence entities may contain database relationship details such as `@ManyToOne`, `@ManyToMany`, and table/column annotations.

Those details should not leak into controller responses.

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

## Current Use Cases

### Create a quote

`QuoteController` receives a `CreateQuoteRequest`.

`QuoteService` validates the request, checks that the customer and product exist, loads selected coverages, checks that coverages belong to the selected product, asks `PremiumCalculator` to calculate money amounts, saves a `QuoteEntity`, and returns a `QuoteResponse`.

The controller does not calculate the premium.

### Issue a policy from a quote

`QuoteController` receives a request to issue a quote.

`PolicyIssueService` loads the quote, rejects quotes that were already issued, creates a `PolicyEntity`, creates a first `PremiumEntity`, and returns a `PolicyResponse`.

The controller does not create policy or premium entities directly.

### Add coverage to a product

`ProductController` receives a `CreateCoverageRequest` for a product.

`CoverageService` loads the product, maps the request to a `CoverageEntity`, connects it to the product, saves it, and returns a `CoverageResponse`.

## Testing Rules

Tests should describe behavior, not implementation details.

Good test names include the situation and the expected result:

```text
returnsQuoteWhenQuoteExists
throwsNotFoundWhenQuoteDoesNotExist
returnsCreatedQuoteWhenRequestIsValid
```

Controller tests check HTTP status codes, JSON responses, validation errors, and service delegation.

Service tests check business workflow rules, exceptions, repository interactions, and returned DTO values.

Domain tests check object validation and pure business rules without Spring MVC.

Mapper tests check reusable mapping rules when those rules are shared or important.

## Refactoring Rules

Refactoring should not intentionally change behavior.

Before and after refactoring, run:

```powershell
.\mvnw.cmd test
```

Prefer small changes:

- rename unclear tests;
- extract duplicated mapping logic;
- keep controllers thin;
- keep business rules in services or domain classes;
- update documentation when package responsibilities change.
