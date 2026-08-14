# 💼 Freelancer Platform

A full-stack web application that connects clients with freelancers.

Clients can post projects, freelancers can submit proposals, and users can register and log in through a web-based interface.

The application is developed using **Spring Boot, Java, MySQL, HTML, CSS, and JavaScript**.

---

## 📌 Project Overview

The Freelancer Platform is designed to provide a simple online marketplace where:

- Clients can create projects.
- Freelancers can view available projects.
- Freelancers can submit proposals.
- Users can register accounts.
- Users can log in.
- Project and proposal data is stored in MySQL.
- The frontend communicates with the Spring Boot REST API.

---

## 🚀 Features

### 👤 User Management

- User registration
- User login
- Freelancer role
- Client role
- User information stored in MySQL

### 📋 Project Management

- Create/post projects
- View available projects
- Project title
- Project description
- Project budget
- Project category
- Project data stored in MySQL

### 📨 Proposal Management

- Submit proposals for projects
- Freelancer email
- Cover letter
- Proposed amount
- Proposal status
- Support for:
  - `PENDING`
  - `ACCEPTED`
  - `REJECTED`

### 🎨 Frontend

- Responsive web interface
- Navigation bar
- Hero section
- Project cards
- Registration form
- Login form
- Post Project form
- Submit Proposal form

---

## 🛠️ Technologies Used

| Technology | Purpose |
|---|---|
| Java 21 | Programming Language |
| Spring Boot 3.5.5 | Backend Framework |
| Spring Web | REST APIs |
| Spring Data JPA | Database Operations |
| Hibernate | ORM |
| MySQL | Database |
| HTML5 | Frontend Structure |
| CSS3 | Frontend Styling |
| JavaScript | Frontend Logic |
| Maven | Build Tool |
| VS Code | Development Environment |
| Postman | API Testing |
| MySQL Workbench | Database Management |

---

## 🏗️ Project Architecture

```text
                    Freelancer Platform
                           |
             +-------------+-------------+
             |                           |
          Frontend                    Backend
             |                           |
       HTML/CSS/JS                 Spring Boot
             |                           |
             |                    REST Controllers
             |                           |
             |                    Service / Logic
             |                           |
             |                    Spring Data JPA
             |                           |
             +-----------> MySQL <-------+
