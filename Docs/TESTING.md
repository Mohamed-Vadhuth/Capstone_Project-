# 🧪 Testing Strategy & Execution

This document details the automated testing architecture, execution commands, and test suites implemented in the **Freelancer Platform**.

---

## 📋 Overview

The testing suite validates backend business logic, REST API contracts, input validation, role-based authorization, entity lifecycles, and security controls using **JUnit 5**, **Mockito** (with Spring Boot 3.5 `@MockitoBean`), and **Spring MockMvc**.

---

## 🚀 Running Tests

Execute the full suite using the Maven wrapper:

```powershell
# From the freelancer-platform subdirectory:
.\mvnw.cmd clean test
```

For running an individual test class:

```powershell
.\mvnw.cmd test -Dtest=SecurityTests
```

---

## 📊 Current Test Metrics

- **Total Tests Run**: 77
- **Passed**: 77
- **Failures**: 0
- **Errors**: 0
- **Skipped**: 0
- **Result**: `BUILD SUCCESS`

---

## 🧩 Test Suites Breakdown

### 1. Security & Authentication (`SecurityTests.java`)
- **Tests**: 10
- **Coverage**:
  - Validates public endpoints are reachable without tokens (`/api/users/register`, `/api/users/login`, `/api/projects`, `/health`).
  - Verifies that protected endpoints return `401 Unauthorized` when called without authentication.
  - Asserts role-based authorization: `FREELANCER` is rejected with `403 Forbidden` when attempting to create a project (`POST /api/projects`).
  - Asserts role-based authorization: `CLIENT` is rejected with `403 Forbidden` when attempting to submit a proposal (`POST /api/proposals`).
  - Verifies JWT bearer token validation and security filter execution.

### 2. User & Profile Management
- **`UserControllerTest.java`** (7 tests): Controller-level validation and response contract testing for user registration, login, and profile endpoints.
- **`UserServiceTest.java`** (7 tests): Service-layer tests covering registration, duplicate email handling, BCrypt hashing, and credentials verification.
- **`UserProfileServiceTest.java`** (2 tests): Tests profile retrieval and profile field updates (bio, skills, experience, portfolio URL).

### 3. Project Management & Lifecycle
- **`ProjectControllerTest.java`** (9 tests): Validates HTTP responses, request payload validation, search/filtering query parameters, and completion endpoints.
- **`ProjectServiceTest.java`** (3 tests): Service-layer tests for creating projects and searching projects by category/keyword.
- **`ProjectLifecycleAndDashboardTest.java`** (7 tests):
  - State transitions: `OPEN` ➔ `IN_PROGRESS` ➔ `COMPLETED`.
  - Enforces client ownership on completion (`CLIENT` can only complete their own project).
  - Verifies invalid lifecycle operations are blocked.
  - Client dashboard metrics aggregation.

### 4. Proposal Management & Lifecycle
- **`ProposalControllerTest.java`** (7 tests): Controller tests for proposal submission, fetching project proposals, and accepting/rejecting bids.
- **`ProposalServiceTest.java`** (7 tests): Service logic for bidding, validation of delivery timeline and proposed bid amount.
- **`ProposalLifecycleAndDashboardTest.java`** (6 tests):
  - Accepting a proposal sets the project status to `IN_PROGRESS`.
  - Accepting a proposal marks other pending bids for that project as `REJECTED`.
  - Prevents submissions to non-open projects.
  - Freelancer dashboard metrics aggregation.

### 5. Review & Rating System
- **`ReviewControllerTest.java`** (4 tests): Verifies HTTP contract and validation for review submissions and query endpoints.
- **`ReviewServiceTest.java`** (6 tests):
  - Rating range enforcement (1 to 5).
  - Restricts reviews exclusively to `COMPLETED` projects.
  - Verifies client can review hired freelancer and freelancer can review client.
  - Prevents duplicate reviews for the same project/participant pair.
  - Prevents self-reviews.
  - Aggregates average user rating and total count correctly.

### 6. Application Context & Home
- **`FreelancerPlatformApplicationTests.java`** (1 test): Verifies Spring application context loads cleanly without bean wiring errors.
- **`HomeControllerTest.java`** (8 tests): Verifies system health endpoints and root status mappings.
