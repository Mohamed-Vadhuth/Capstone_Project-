# 💼 Freelancer Platform

A full-stack freelancing marketplace connecting clients and freelancers, engineered with **Spring Boot 3.5**, **Java 21**, **Spring Security**, **JWT**, **Hibernate / Spring Data JPA**, **MySQL**, and a modern responsive frontend.

---

## 📌 1. Project Overview

Freelancer Platform provides an end-to-end marketplace for job posting, proposal bidding, project lifecycle orchestration, and mutual peer reviews. It features role-based access control (RBAC), stateless JWT authentication, password hashing with BCrypt, automated schema evolution, and REST API standards.

---

## 🎯 2. Problem Statement

Modern freelancing demands a transparent, reliable, and secure environment where clients can post requirements, review proposals, track project status, and exchange peer ratings upon completion, while freelancers can showcase skills, browse listings with search/filters, track their proposals, and view client evaluations.

---

## 🏆 3. Objectives

- Deliver secure authentication via JWT tokens and BCrypt password hashing.
- Enforce role-based authorizations distinguishing `CLIENT` and `FREELANCER` capabilities.
- Provide comprehensive project management: posting, filtering by category, search by keywords, budget sorting, and state management (`OPEN`, `IN_PROGRESS`, `COMPLETED`).
- Implement end-to-end proposal lifecycle: bidding, client review, acceptance, rejection, and automated project transition.
- Implement two-way reviews and ratings (1–5 stars) strictly after project completion.
- Provide real-time personalized dashboards with dynamic analytical metrics.
- Ensure thorough test coverage and automated validation.

---

## 🚀 4. Features

### 👤 User Management & Profiles
- Registration with role selection (`CLIENT` or `FREELANCER`).
- Secure login generating signed JWT bearer tokens.
- Profile view and in-place updates: bio, skills, experience, and portfolio URL.
- Two-way rating aggregates and total reviews count calculated dynamically.

### 📋 Project Management
- Client project posting with title, description, budget, and category.
- Search projects by title and description keywords.
- Category filtering (`Development`, `Design`, `Writing`, `Marketing`, etc.).
- Sort projects by Budget (High to Low, Low to High) or Date.
- Project status progression: `OPEN` ➔ `IN_PROGRESS` ➔ `COMPLETED`.

### 📨 Proposal Management
- Freelancers can submit proposals with cover letters, proposed bids, and estimated delivery timeline.
- Proposal status progression: `PENDING`, `ACCEPTED`, `REJECTED`.
- Accepting a proposal automatically sets all other proposals for that project to `REJECTED` and moves the project to `IN_PROGRESS`.

### ⭐ Reviews and Ratings
- Mutual reviews allowed exclusively on `COMPLETED` projects.
- Client reviews the accepted freelancer; freelancer reviews the project client.
- Prevents duplicate reviews and self-reviews.
- Validates rating range strictly between 1 and 5 stars with written feedback.

### 📊 Role-Based Dashboards
- **Client Dashboard**: Total projects posted, open projects, active projects, completed projects, received proposals, and pending review alerts.
- **Freelancer Dashboard**: Total proposals submitted, pending bids, accepted proposals, active contracts, and completed projects.

---

## 👥 5. User Roles

| Role | Permissions & Capabilities |
| :--- | :--- |
| `CLIENT` | Create projects, view received proposals, accept/reject proposals, mark projects as completed, review hired freelancers, access client dashboard. |
| `FREELANCER` | Browse open projects, filter/search jobs, submit proposals, track bid status, review clients on completed projects, manage profile and skills. |

---

## 🛠️ 6. Technology Stack

- **Backend Framework**: Spring Boot 3.5.5, Java 21
- **Security**: Spring Security 6, JWT (`jjwt 0.11.5`), BCrypt password hashing
- **Persistence**: Spring Data JPA, Hibernate ORM
- **Database**: MySQL 8.0
- **Build Tool**: Apache Maven (`mvnw`)
- **Testing**: JUnit 5, Mockito (`@MockitoBean`), Spring Boot Test (`MockMvc`)
- **Frontend**: HTML5, CSS3 (Modern responsive design with glassmorphism), Vanilla JavaScript (ES6+ async/fetch)

---

## 🏛️ 7. Architecture

The application adopts a layered architecture:

```
[ Frontend: HTML5 / CSS3 / Vanilla JS (Port 3000) ]
                      │  HTTP REST (JSON + JWT Bearer)
                      ▼
[ Controllers / REST Endpoints / Security Filter (Port 8080) ]
                      │
                      ▼
[ Service Layer (Business Logic, Validations, State Transitions) ]
                      │
                      ▼
[ Repository Layer (Spring Data JPA) ]
                      │
                      ▼
[ Database: MySQL (Relational Entities: User, Project, Proposal, Review) ]
```

---

## 📂 8. Project Structure

```
Capstone_Project-/
├── Docs/
│   ├── Architecture.md
│   ├── Database_Schema.md
│   ├── Milestone_Report.md
│   └── diagrams/
├── docs/
│   ├── TESTING.md
│   └── ARCHITECTURE.md
├── freelancer-platform/
│   ├── frontend/
│   │   ├── index.html
│   │   ├── style.css
│   │   └── script.js
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/freelancer_platform/
│   │   │   │   ├── config/
│   │   │   │   ├── controller/
│   │   │   │   ├── dto/
│   │   │   │   ├── entity/
│   │   │   │   ├── exception/
│   │   │   │   ├── repository/
│   │   │   │   ├── security/
│   │   │   │   └── service/
│   │   │   └── resources/
│   │   │       └── application.properties
│   │   └── test/
│   │       └── java/freelancer_platform/
│   │           ├── controller/
│   │           ├── service/
│   │           └── SecurityTests.java
│   ├── .env.example
│   ├── pom.xml
│   └── mvnw.cmd
├── CHANGELOG.md
├── README.md
└── .gitignore
```

---

## 🗄️ 9. Database Overview

Relational MySQL schema with non-destructive evolution:
- **`user`**: `id`, `name`, `email` (UNIQUE), `password` (BCrypt hash), `role`, `bio`, `skills`, `experience`, `portfolio_url`.
- **`project`**: `id`, `title`, `description`, `budget`, `category`, `client_email`, `status`, `created_at`.
- **`proposal`**: `id`, `project_id`, `freelancer_email`, `cover_letter`, `proposed_amount`, `estimated_delivery`, `status`, `created_at`.
- **`review`**: `id`, `project_id`, `reviewer_email`, `reviewee_email`, `rating` (1–5), `comment`, `created_at`.

---

## 🔌 10. API Overview

### Authentication & Profiles
- `POST /api/users/register` - Register a new client or freelancer
- `POST /api/users/login` - Authenticate and obtain JWT token
- `GET /api/users/profile` - Get authenticated user's profile and stats
- `PUT /api/users/profile` - Update user bio, skills, experience, and portfolio
- `GET /api/users/profile/{email}` - Get public profile and rating history

### Projects
- `GET /api/projects` - Retrieve projects with optional `search`, `category`, and `sortBy`
- `POST /api/projects` - Create a project (`CLIENT` role required)
- `GET /api/projects/{id}` - Get project details
- `PUT /api/projects/{id}/complete` - Mark project as `COMPLETED` (`CLIENT` role required)

### Proposals
- `POST /api/proposals` - Submit a proposal (`FREELANCER` role required)
- `GET /api/proposals/project/{projectId}` - Get proposals for a project (Owner client or submitting freelancer)
- `PUT /api/proposals/{id}/accept` - Accept proposal (`CLIENT` role required)
- `PUT /api/proposals/{id}/reject` - Reject proposal (`CLIENT` role required)

### Reviews
- `POST /api/reviews` - Submit review for completed project
- `GET /api/reviews/project/{projectId}` - Get reviews for a project
- `GET /api/reviews/user/{email}` - Get reviews received by a user

### Dashboards & Health
- `GET /api/dashboard/client` - Get client statistics and project breakdowns
- `GET /api/dashboard/freelancer` - Get freelancer bid and contract statistics
- `GET /health` - System health check endpoint (`{"status":"UP"}`)

---

## 🔒 11. Authentication & Security

- **Stateless Authentication**: All secure requests require an `Authorization: Bearer <token>` header.
- **BCrypt Encryption**: Passwords hashed with high salt rounds; plaintext passwords or hashes are never exposed in API responses.
- **Role-Based Guards**: Method-level and path-level constraints preventing unauthorized operations (e.g. freelancers cannot post projects; clients cannot submit proposals).
- **Protected Environment**: Configuration supports external environment variables (`DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`) preventing credential leakage.

---

## 🔄 12. Project & Proposal Lifecycles

### Project Status Transitions
1. `OPEN`: Created by client, visible in marketplace, open for proposals.
2. `IN_PROGRESS`: Automatically set when client accepts a freelancer proposal.
3. `COMPLETED`: Client manually marks project completed upon satisfactory delivery.

### Proposal Status Transitions
1. `PENDING`: Initial proposal submission by freelancer.
2. `ACCEPTED`: Client accepts proposal; project transitions to `IN_PROGRESS`; other project proposals become `REJECTED`.
3. `REJECTED`: Client explicitly rejects proposal or proposal is superseded by another accepted bid.

---

## 💻 13. Local Setup & Installation

### Prerequisites
- Java Development Kit (JDK 21+)
- MySQL Server 8.0+
- Node.js (v18+ for local static frontend hosting) or any static file server

### 1. Database Configuration
Create the MySQL database:
```sql
CREATE DATABASE IF NOT EXISTS freelancer_db;
```

### 2. Environment Variables Configuration

Copy `.env.example` to configure variables locally or in your cloud hosting provider:
```properties
# Server
PORT=8080

# Database
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/freelancer_platform?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=your_password

# JPA / Hibernate
SPRING_JPA_HIBERNATE_DDL_AUTO=update
SPRING_JPA_SHOW_SQL=false

# Security (Set 256-bit+ secret in production)
JWT_SECRET=your_secure_256bit_production_jwt_secret_key_here
JWT_EXPIRATION_MS=86400000

# CORS Allowed Origins
CORS_ALLOWED_ORIGINS=http://localhost:3000,http://127.0.0.1:3000,http://localhost:5500,http://127.0.0.1:5500,http://localhost:8080
```

### 3. Backend Setup
Navigate to `freelancer-platform/` and run:
```powershell
.\mvnw.cmd spring-boot:run
```
Backend will be live at `http://localhost:8080/`. Health check: `http://localhost:8080/health`.

### 4. Frontend Setup
Serve the `frontend/` directory on port 3000:
```powershell
npx serve -p 3000 frontend
```
Frontend will be accessible at `http://localhost:3000/`.
> **Frontend API URL Configuration**: The frontend defaults to `http://localhost:8080`. For cloud deployment, configure `window.API_BASE_URL = "https://your-backend-domain.com"` or store it in `localStorage.setItem("customApiUrl", "https://your-backend-domain.com")`.

---

## 🧪 14. Testing & Verification

Run the complete automated test suite:
```powershell
.\mvnw.cmd clean test
```

### Current Test Suite Result:
- **Total Tests**: 77
- **Passed**: 77
- **Failures**: 0
- **Errors**: 0
- **Skipped**: 0
- **Status**: BUILD SUCCESS

---

## ⚙️ 15. Continuous Integration (CI)

Automated testing is configured using **GitHub Actions** (`.github/workflows/ci.yml`):
- **Triggers**: Automated build runs on every `push` to `main` and on `pull_request` targeting `main`.
- **Environment**: Ubuntu Latest with Eclipse Temurin JDK 21 and a dedicated MySQL 8.0 service container.
- **Workflow Steps**:
  1. Checks out repository code.
  2. Provisions JDK 21 and configures Maven dependency caching.
  3. Executes the full automated suite via `./mvnw clean test`.
  4. Fails the workflow immediately if any test fails.

---

## 🔮 16. Future Enhancements

- Milestone payment escrow integration (Stripe / PayPal sandbox).
- Real-time WebSocket messaging and project collaboration chat.
- Secure file and asset attachment storage for project deliverables.
- Cloud staging and production deployment.
