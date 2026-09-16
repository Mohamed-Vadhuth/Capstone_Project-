# 🏛️ Freelancer Platform System Architecture

This document describes the high-level system architecture, entity relationships, security workflows, and lifecycle state machines for the Freelancer Platform.

---

## 1. High-Level Architecture Diagram

```mermaid
graph TD
    subgraph Client Layer
        UI["Modern Web Interface<br/>(HTML5 / CSS3 / Vanilla JS)<br/>Port 3000"]
    end

    subgraph Security Layer
        Filter["JwtAuthenticationFilter<br/>(Bearer Token Validation)"]
        EntryPoint["JwtAuthenticationEntryPoint<br/>(401 Unauthorized Handler)"]
    end

    subgraph REST Controllers
        UC["UserController<br/>(/api/users)"]
        PC["ProjectController<br/>(/api/projects)"]
        PropC["ProposalController<br/>(/api/proposals)"]
        RC["ReviewController<br/>(/api/reviews)"]
        DC["DashboardController<br/>(/api/dashboard)"]
        HC["HomeController<br/>(/health)"]
    end

    subgraph Service Layer
        US["UserService & UserProfileService"]
        PS["ProjectService"]
        PropS["ProposalService"]
        RS["ReviewService"]
    end

    subgraph Persistence Layer
        UR["UserRepository"]
        PR["ProjectRepository"]
        PropR["ProposalRepository"]
        RR["ReviewRepository"]
    end

    subgraph Database
        DB[("MySQL Database<br/>(freelancer_db)")]
    end

    UI -->|HTTP Requests + Bearer Token| Filter
    Filter -->|Unauthorized| EntryPoint
    Filter -->|Authorized Principal| UC & PC & PropC & RC & DC & HC
    UC --> US
    PC --> PS
    PropC --> PropS
    RC --> RS
    DC --> PS & PropS
    US --> UR
    PS --> PR
    PropS --> PropR
    RS --> RR
    UR & PR & PropR & RR --> DB
```

---

## 2. Entity-Relationship (ER) Model

```mermaid
erDiagram
    USER {
        bigint id PK
        varchar name
        varchar email UK
        varchar password
        varchar role "CLIENT or FREELANCER"
        text bio
        text skills
        varchar experience
        varchar portfolio_url
    }

    PROJECT {
        bigint id PK
        varchar title
        text description
        double budget
        varchar category
        varchar client_email
        varchar status "OPEN, IN_PROGRESS, COMPLETED"
        datetime created_at
    }

    PROPOSAL {
        bigint id PK
        bigint project_id FK
        varchar freelancer_email
        text cover_letter
        double proposed_amount
        varchar estimated_delivery
        varchar status "PENDING, ACCEPTED, REJECTED"
        datetime created_at
    }

    REVIEW {
        bigint id PK
        bigint project_id FK
        varchar reviewer_email
        varchar reviewee_email
        int rating "1 to 5"
        text comment
        datetime created_at
    }

    USER ||--o{ PROJECT : "posts (as CLIENT)"
    PROJECT ||--o{ PROPOSAL : "receives"
    USER ||--o{ PROPOSAL : "submits (as FREELANCER)"
    PROJECT ||--o{ REVIEW : "evaluated under"
```

---

## 3. Project & Proposal Lifecycle State Machine

```mermaid
stateDiagram-v2
    [*] --> OPEN : Client Posts Project

    state Project_Open {
        OPEN --> OPEN : Freelancers Submit Proposals (PENDING)
    }

    OPEN --> IN_PROGRESS : Client Accepts a Proposal
    note right of IN_PROGRESS
        Accepted proposal becomes ACCEPTED.
        All other proposals for this project become REJECTED.
    end note

    IN_PROGRESS --> COMPLETED : Client Marks Project Complete

    state Project_Completed {
        COMPLETED --> MutualReviews : Unlock Reviews
        MutualReviews --> COMPLETED : Client Reviews Freelancer (1-5★)
        MutualReviews --> COMPLETED : Freelancer Reviews Client (1-5★)
    }

    COMPLETED --> [*]
```

---

## 4. Authentication & JWT Authorization Flow

```mermaid
sequenceDiagram
    autonumber
    actor User as User / Browser
    participant API as Spring Boot API
    participant DB as MySQL DB

    User->>API: POST /api/users/login (email, password)
    API->>DB: Fetch user by email
    DB-->>API: User record (with BCrypt hash)
    API->>API: Verify password with BCrypt
    API-->>User: 200 OK (JWT Token, role, name, email)

    User->>API: GET /api/dashboard/client (Header: Authorization Bearer <token>)
    API->>API: JwtAuthenticationFilter validates signature & expiry
    API->>API: Enforce SecurityConfig role checks (ROLE_CLIENT)
    API->>DB: Query client project & proposal metrics
    DB-->>API: Aggregated counts
    API-->>User: 200 OK (ClientDashboardDto JSON)
```
