# Changelog

All notable changes to the Freelancer Platform project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.1.0] - 2026-09-17

### Added
- **Login Security Email Notification**:
  - Implemented dedicated `EmailService` using Spring Boot Mail (`JavaMailSender`).
  - Automatically dispatches security alerts to registered user email addresses upon successful login.
  - Multi-part email message structure featuring responsive HTML layout and plain-text fallback.
  - Included user's name greeting, login confirmation, registered account email, formatted UTC timestamp, and account recovery advisory.
  - Environment-based SMTP configuration (`MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`, `MAIL_SMTP_AUTH`, `MAIL_SMTP_STARTTLS_ENABLE`, `MAIL_FROM`).
  - Resilient error handling: email failures or SMTP outages are safely caught and logged without disrupting user authentication or JWT issuance.
  - Comprehensive unit and integration test coverage (`EmailServiceTest`, `UserServiceTest`, `SecurityTests`) bringing the test suite to 82 passing tests.

---

## [1.0.0] - 2026-09-16

### Added
- **User Authentication & Authorization**:
  - Stateless JWT token-based authentication with `JwtAuthenticationFilter` and `JwtAuthenticationEntryPoint`.
  - BCrypt password hashing (`PasswordEncoder`).
  - Distinct role-based access control for `CLIENT` and `FREELANCER` roles.
  - User registration and login endpoints with input validation.
- **User Profile Management**:
  - Comprehensive profile management allowing updates to user bio, skills list, experience, and portfolio URL.
  - Endpoints to retrieve authenticated user profile and public profiles with rating aggregates.
- **Project Management & Lifecycle**:
  - Project creation restricted to authenticated `CLIENT` users.
  - Keyword search across project titles and descriptions.
  - Category filtering (`Development`, `Design`, `Writing`, `Marketing`, etc.).
  - Multi-criteria budget sorting (High to Low, Low to High) and date ordering.
  - Robust project lifecycle state transitions (`OPEN` ➔ `IN_PROGRESS` ➔ `COMPLETED`).
- **Proposal Lifecycle & Bidding**:
  - Proposal submissions restricted to authenticated `FREELANCER` users.
  - Detailed proposal fields: cover letter, proposed bid amount, and estimated delivery timeline.
  - Acceptance and rejection workflows by client project owners.
  - Automatic transition of accepted project to `IN_PROGRESS` and automatic rejection of competing proposals.
- **Reviews & Rating System**:
  - Mutual peer reviews enabled strictly upon project completion.
  - Dual review workflows: clients review hired freelancers, freelancers review hiring clients.
  - Rating range enforcement (1 to 5 stars) with written feedback.
  - Duplicate review prevention and self-review validation guards.
- **Personalized Dashboards**:
  - Role-specific dashboard endpoints (`/api/dashboard/client` and `/api/dashboard/freelancer`).
  - Aggregated analytics: total postings, active jobs, completed contracts, received bids, and pending reviews.
- **Modern Responsive Frontend**:
  - Clean HTML5, modern CSS3 layout with glassmorphic accents, and vanilla JavaScript fetch client.
  - Dynamic tab navigation between Projects, Dashboards, Proposals, and User Profiles.
  - Interactive review modal, filter/search toolbars, and responsive UI components.
  - Resilient client-side error handling displaying actionable user alerts for HTTP 400, 401, 403, 404, and 500 responses.
- **Automated Test Suite**:
  - 77 comprehensive automated unit, service, controller, and security test cases.
  - Modernized test suite using JUnit 5 and Spring Boot 3.5 `@MockitoBean` annotations.

### Changed
- Refactored REST controller responses to standardized DTO payloads (`ProjectResponseDto`, `ProposalResponseDto`, `UserResponse`, `ReviewResponse`).
- Upgraded global exception handling with timestamped, structured `ErrorResponse` records.
- Standardized database column mapping and entity constraints with non-destructive schema evolution.

---

## [0.2.0] - Earlier Milestone

### Added
- Basic project and proposal persistence entities with Spring Data JPA.
- Initial Spring Security configuration and UserController.
- Core Maven build configuration and MySQL driver setup.

---

## [0.1.0] - Initial Milestone

### Added
- Project initialization with Spring Boot and starter web dependencies.
- Initial HTML and CSS mockups.
