# todo-service

A RESTful service for managing a simple to-do list.

## Service Description

The service allows creating and managing tasks with a description and a deadline. Tasks have three possible statuses: `NOT_DONE`, `DONE`, and `PAST_DUE`. A scheduled job automatically transitions overdue tasks to `PAST_DUE`. Tasks in `PAST_DUE` state are immutable via the REST API.

## Assumptions

- A task with status `DONE` will not be transitioned to `PAST_DUE` by the scheduler, even if its deadline has passed. Completed work stays completed. In production this would be a business decision worth clarifying.
- `DONE -> NOT_DONE` transition is allowed by the API, though in production it would be preferable to create a new task instead. Kept as-is per the task requirements.
- Это assumption — добавь в секцию Assumptions:
- Modifying the description of a `PAST_DUE` task is forbidden, same as status changes. The spec only explicitly forbids status changes via REST, but allowing description edits on an immutable task felt inconsistent.
- The deadline field is required on task creation.
- The scheduler runs every minute. In production, the frequency would depend on business requirements for status accuracy.
- The `GET /tasks` endpoint without a `status` filter returns all tasks. The task description says "get all items that are not done (with an option to retrieve all items)" – this was interpreted as a `?status=` filter, which is more flexible and extensible than a boolean `?all=true` flag.

## Architecture Decisions & Tradeoffs

- **Kotlin over Java**. The position lists Kotlin as "nice to have". Kotlin was chosen as the primary language to leverage data classes for DTOs, sealed-class-style enum modeling, null safety, and extension functions.

- **Layered architecture** (`controller - service - repository`) was chosen over hexagonal architecture. The service has a single REST adapter and a single data source – the added abstraction of ports and adapters would introduce boilerplate without meaningful benefit at this scale.

- **Code-first with springdoc** was chosen over OpenAPI-first code generation. The OpenAPI generator produces Java-flavored code that is awkward in Kotlin – mutable classes, incorrect nullability, non-idiomatic types. Writing controllers and DTOs by hand keeps the code idiomatic, and springdoc exports a live spec automatically.

- **Spring Data JPA over JOOQ.** JOOQ would offer more explicit query control, but given H2 in-memory and a single entity, performance is not a concern here. JPA is the appropriate choice for this scope.

- **Status transitions are modelled as a state machine inside the `TaskStatus` enum.** Each status knows which transitions are valid via a `when` expression. This keeps the domain rule co-located with the type it describes, and Kotlin's exhaustive `when` ensures no transition is accidentally left undefined when new statuses are added.

- **`@Scheduled` with a single-threaded pool** is appropriate here because there is no distributed deployment concern. In a multi-instance production environment, distributed locking (e.g. ShedLock) would be required to prevent duplicate executions.

- **Optimistic locking** (`@Version`). The race condition between the scheduler and a concurrent REST update (e.g. user marks `DONE` at the same moment the scheduler sets `PAST_DUE`) handled with optimistic locking (`@Version`).

## Tech Stack

- **Runtime:** JVM 21
- **Language:** Kotlin 2.x
- **Framework:** Spring Boot 4
- **Persistence:** Spring Data JPA + H2 (in-memory)
- **Migrations:** Liquibase
- **API docs:** springdoc-openapi (Swagger UI)
- **Validation:** Jakarta Validation
- **Build:** Gradle (Kotlin DSL)
- **Containerisation:** Docker

## How To

### Build

```bash
./gradlew build
```

### Run Automatic Tests

```bash
./gradlew test
```

### Run Locally

```bash
./gradlew bootRun
```

Or with Docker:

```bash
docker build -t todo-service .
docker run -p 8080:8080 todo-service
```

The service will be available at `http://localhost:8080`.

Swagger UI: `http://localhost:8080/swagger-ui.html`

H2 console: `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:todo_service_db`)

## API Overview

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/v1/tasks` | Create a new task |
| `GET` | `/api/v1/tasks` | Get tasks (optional `?status=NOT_DONE\|DONE\|PAST_DUE`) |
| `GET` | `/api/v1/tasks/{id}` | Get task by ID |
| `PATCH` | `/api/v1/tasks/{id}/description` | Update task description |
| `PATCH` | `/api/v1/tasks/{id}/status` | Update task status |