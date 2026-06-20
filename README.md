# HireTrack AI

A secure, multi-user job-application tracker built with Spring Boot, featuring an AI-powered interview-question generator backed by Google Gemini.

HireTrack lets a user register, log in, and manage their job applications (company, role, status, applied date) through a REST API protected by JWT authentication. Every application is scoped to its owner, and for any application a user has logged, HireTrack can generate tailored interview questions by calling the Gemini API with a prompt built from that role and company.

This project was built from scratch to demonstrate a production-style backend: layered architecture, stateless JWT security, ownership-based authorization, an external AI integration with proper error handling, and a full testing suite spanning unit tests and a real-database integration test.

---

## Highlights

- **JWT-secured REST API** — stateless authentication; passwords stored as BCrypt hashes, never in plaintext.
- **Ownership-based authorization** — users can only read, update, or delete their own applications. Requests for applications that aren't yours return `404 Not Found` rather than `403`, to avoid leaking which IDs exist (anti-enumeration).
- **AI interview-question generator** — `POST /api/jobs/{id}/interview-questions` calls the Gemini API with a prompt derived from the application's role and company, and returns a tailored set of technical and behavioral questions. The external call is wrapped in error handling so a flaky third-party API fails cleanly.
- **Clean layered architecture** — controller (HTTP), service (business logic), repository (data access) separation throughout.
- **Filtering and sorting** — list endpoints support `?status=` filtering and `?sortBy=` sorting via query parameters.
- **Centralized exception handling** — a single `@RestControllerAdvice` maps domain exceptions to consistent HTTP responses.
- **Full testing pyramid** — unit tests (JUnit 5 + Mockito) for service logic, plus a Testcontainers-backed integration test that boots the whole app against a real, disposable MySQL container.

---

## Tech stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 4 (Spring Web, Spring Data JPA, Spring Security) |
| Database | MySQL 8 |
| Auth | JWT (jjwt), BCrypt password hashing |
| AI | Google Gemini API (`gemini-2.5-flash`) via Spring `RestClient` |
| Build | Maven (wrapper included) |
| Testing | JUnit 5, Mockito, AssertJ, Testcontainers |

---

## Architecture

HireTrack follows a standard three-layer Spring Boot structure:

- **Controllers** (`*Controller`) handle HTTP requests and responses. They never contain business logic — they delegate to services and shape the HTTP result.
- **Services** (`*Service`) hold the business logic: registration with duplicate-email checks, JWT issuance, ownership validation, the Gemini call, and so on.
- **Repositories** (`*Repository`) are Spring Data JPA interfaces that handle database access.

Entities are never exposed directly over the API. Requests and responses use dedicated DTOs, so internal fields (like the password hash) can never leak into a response.

The AI feature lives in `InterviewQuestionService`, which builds a prompt, calls Gemini over HTTP via `RestClient`, parses the nested JSON response, and handles failures gracefully. The parsing logic is isolated so it can be unit-tested without making real network calls.

### Package layout

```
com.hiretrack
├── config/     Security config, JWT utilities, auth filter
├── common/     Shared exceptions and the global exception handler
├── user/       User entity, auth (register / login / me), JWT issuance
└── job/        Job applications, filtering/sorting, and the AI question generator
```

---

## Getting started

### Prerequisites

- Java 21
- Docker (for the MySQL database)
- A Google Gemini API key ([Google AI Studio](https://aistudio.google.com/apikey))

### 1. Start MySQL

HireTrack expects a MySQL 8 database. The quickest way is Docker:

```bash
docker run --name hiretrack-mysql \
  -e MYSQL_ROOT_PASSWORD=rootpass \
  -e MYSQL_DATABASE=hiretrack_db \
  -p 3306:3306 \
  -d mysql:8.0
```

### 2. Configure environment variables

The application reads three values from the environment (it never hardcodes secrets):

| Variable | Description |
|---|---|
| `DB_PASSWORD` | MySQL root password (e.g. `rootpass`) |
| `JWT_SECRET` | A long secret used to sign JWTs (at least 32 characters) |
| `GEMINI_API_KEY` | Your Google Gemini API key |

Set them in your shell, your IDE run configuration, or a local `.env`-style mechanism. They are referenced in `application.properties` as `${DB_PASSWORD}`, `${JWT_SECRET}`, and `${GEMINI_API_KEY}` — no secret is committed to source control.

### 3. Run the application

```bash
./mvnw spring-boot:run
```

The app starts on `http://localhost:8080`.

### 4. Run the tests

```bash
./mvnw test
```

The integration test uses Testcontainers, which starts its own throwaway MySQL container — so Docker must be running, but you do not need the dev database up for tests.

---

## API reference

All `/api/jobs/**` endpoints require a JWT in the `Authorization: Bearer <token>` header. Obtain a token by registering and logging in.

### Authentication

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/auth/register` | Create a new user. Returns `201 Created`. |
| `POST` | `/api/auth/login` | Log in and receive a JWT. |
| `GET` | `/api/auth/me` | Return the authenticated user's details. |

**Register / login request body:**

```json
{ "email": "user@example.com", "password": "secret123", "fullName": "Jane Doe" }
```

### Job applications

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/jobs` | Create a job application. |
| `GET` | `/api/jobs` | List your applications. Supports `?status=` and `?sortBy=`. |
| `PUT` | `/api/jobs/{id}` | Update an application you own. |
| `DELETE` | `/api/jobs/{id}` | Delete an application you own. |
| `POST` | `/api/jobs/{id}/interview-questions` | Generate AI interview questions for an application you own. |

**Create / update request body:**

```json
{ "company": "Google", "role": "Software Engineer", "status": "INTERVIEWING" }
```

`status` is one of: `APPLIED`, `INTERVIEWING`, `OFFER`, `REJECTED`.

**Example — generate interview questions:**

```bash
curl -X POST http://localhost:8080/api/jobs/1/interview-questions \
  -H "Authorization: Bearer <your-token>"
```

Returns the application's company and role plus a generated set of tailored interview questions.

---

## Testing approach

HireTrack is tested at two levels:

- **Unit tests** isolate individual service logic. `UserServiceTest` mocks the repository and JWT utility to verify registration and login rules without a database. `InterviewQuestionServiceTest` tests the Gemini response-parsing logic directly against sample response data — including a malformed-response case that verifies the service fails with a clear error rather than an unhandled exception.
- **Integration test** (`HireTrackIntegrationTest`) boots the entire application with `@SpringBootTest` against a real MySQL instance started on demand by Testcontainers. It drives the live API over HTTP to verify the full register-and-login flow end to end — exercising the controller, security filter, service layer, JPA, and the database together. Using a real MySQL container (rather than an in-memory database) means the test runs against the same SQL dialect as production.

---

## Notes

This is a portfolio project built to demonstrate backend fundamentals and an external AI integration. Secrets are supplied via environment variables and are not committed to the repository.