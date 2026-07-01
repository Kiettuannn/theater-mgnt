<div align="center">
  <!-- TODO: Add Project Logo here -->
  <!-- <img src="URL_TO_LOGO" alt="Project Logo" width="200"/> -->

# 🎬 Cinemax - Enterprise Theater Management System

_An enterprise-grade Cinema Management Platform built with Java 21, Spring Boot, Next.js, Vite, and an internal AI-powered Assistant._

  <!-- Tech Badges -->
  <p align="center">
    <img src="https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=java&logoColor=white" alt="Java 21" />
    <img src="https://img.shields.io/badge/Spring_Boot-3.5-brightgreen?style=for-the-badge&logo=springboot&logoColor=white" alt="Spring Boot" />
    <img src="https://img.shields.io/badge/Next.js-16-black?style=for-the-badge&logo=next.js&logoColor=white" alt="Next.js" />
    <img src="https://img.shields.io/badge/React-19-blue?style=for-the-badge&logo=react&logoColor=white" alt="React" />
    <img src="https://img.shields.io/badge/PostgreSQL-14%2B-blue?style=for-the-badge&logo=postgresql&logoColor=white" alt="PostgreSQL" />
    <img src="https://img.shields.io/badge/Tailwind_CSS-38B2AC?style=for-the-badge&logo=tailwind-css&logoColor=white" alt="Tailwind CSS" />
  </p>
</div>

---

## 🌐 Live Demo & Credentials

The system is deployed and available for testing:

- **Admin Dashboard (Vite + React)**
  - URL: [https://uitcifastar-admin-ver-2.vercel.app/](https://uitcifastar-admin-ver-2.vercel.app/)
  - Admin Account:
    - Username: `admin`
    - Password: `admin`
  - *Note: Use this account to test and manage system functionalities.*

- **Customer Portal (Next.js)**
  - URL: [https://cifastaruit.vercel.app/](https://cifastaruit.vercel.app/)

---

## 📖 Project Overview

**Theater Management System (Cinemax)** is a comprehensive cinema management software designed with a **Monolithic** architecture, aiming to handle high traffic and concurrent transactions.

The core of the project focuses on the **Admin Dashboard** and the **Core Backend**, utilizing advanced Spring Boot features to automate and optimize theater operations.

The system consists of 3 main components:

1. **Admin/Staff Dashboard (Vite & React)**: The management interface for cinema administrators and staff to schedule screenings, manage rooms, personnel, AI documents, and view detailed revenue reports.
2. **Customer Portal (Next.js)**: The end-user platform for booking tickets, purchasing combos, and tracking showtimes.
3. **Core Backend (Spring Boot)**: The central server handling business logic, security, AI integration, and real-time communication.

---

## ✨ Key Features

### 💼 Admin & Staff Features (Core Focus)

- **AI Document Management (RAG) & Internal Chatbot**: Integrates an AI Chatbot exclusively for internal staff to quickly query operational procedures and policies. Administrators can upload and manage documents (PDF, Word) to vectorize (PGvector) and populate the knowledge base (RAG) for the AI.
- **File Management**: Utilizes Cloudinary to store and manage image files (Posters, Avatars, Banners) and internal documents, optimizing storage and bandwidth.
- **Cinema & Room Management**: Manages multiple cinema branches, screening rooms, different seat types (Standard, VIP, Sweetbox), and equipment.
- **Dynamic Pricing**: Adjusts ticket prices based on time slots, weekends, seat types, and movie age ratings.
- **Scheduling**: Provides a visual interface to schedule movie screenings and prevent time or room conflicts.
- **Staff Shift Management**: Allocates and tracks working shifts for cinema staff.
- **Revenue Dashboard**: Generates detailed statistical reports and revenue charts using Recharts.
- **Notification System**: Automates email sending (via Sendinblue) for events like successful ticket bookings or cancellations.

### 🧑‍💻 Customer Features

- **Ticket & Combo Booking**: Provides a seamless ticket booking flow, including the option to purchase popcorn and drinks.
- **Ticket Transfer (Pass vé)**: Allows customers to securely transfer their purchased tickets to others through the system.
- **E-Ticket (QR Code)**: Enables easy check-in via automatically generated QR codes (using the ZXing library).
- **Authentication**: Supports login via Email/Password and Google OAuth2.
- **Online Payment (VNPay Sandbox)**: Integrates the VNPay payment gateway (Sandbox environment) for secure and fast ticket transactions.

---

## 🏗 Architecture & Technologies

### Backend

- **Core Framework**: Java 21, Spring Boot 3.5.x, Spring Cloud OpenFeign
- **Database**: PostgreSQL 14+, PGvector
- **ORM & Data Access**: Spring Data JPA, MapStruct, Lombok
- **Security**: Spring Security, OAuth2, JWT
- **AI & Integrations**: Spring AI, Socket.IO, ZXing, VNPay (Sandbox), Sendinblue, Jsoup, Cloudinary
- **Build & Quality Tool**: Maven, Spotless, Jacoco
- **Testing**: JUnit 5, Testcontainers

### Frontend - Admin Dashboard (Admin)

- **Framework**: React 19, Vite
- **UI/Styling**: Tailwind CSS, Radix UI
- **State Management & Fetching**: Zustand, Axios
- **Data Visualization & Tools**: Recharts, TinyMCE, html2canvas, jspdf, Socket.IO Client

### Frontend - Customer Portal (Client)

- **Framework**: Next.js 16 (App Router), React 19
- **UI/Styling**: Tailwind CSS, Radix UI, Framer Motion (tw-animate)
- **State Management & Fetching**: Zustand, Axios

---

## 🚀 Technical Highlights (Backend Mastery)

The project uses **Java 21** and **Spring Boot 3.5.x** with a **Modular Monolith** architecture, applying multiple advanced techniques:

- **Security (Spring Security)**:
  - Implements **OAuth2 Resource Server** combined with **JWT** (JSON Web Token) and a **Custom JWT Decoder**.
  - Applies **Method Security** (`@EnableMethodSecurity`, `@PreAuthorize`) for fine-grained action-level authorization.
  - Enforces Role-Based Access Control (RBAC) across Customer, Staff, and Admin roles.
- **Event-Driven Architecture (EDA)**: Uses **Spring Events** (`ApplicationEventPublisher`) to build a loosely coupled system. For example, automatically sending emails (via Sendinblue) upon account creation or password reset using `EventListener`.
- **Spring AI Integration**: Uses Spring AI (OpenAI API), **PGvector** (Vector Database), and **Tika Document Reader** to implement RAG (Retrieval-Augmented Generation) and manage Chat Memory via JDBC, serving as an internal smart assistant.
- **Real-time Processing**: Uses **Netty Socket.IO** to establish 2-way Websocket communication for immediate system state updates.
- **Code & Data Optimization**:
  - Uses **MapStruct** for automated DTO mapping, improving performance over reflection-based mappers.
  - Implements **Global Exception Handling** (with `@ControllerAdvice`) to standardize API responses.
  - Communicates with External APIs (VNPay Sandbox for payments, Sendinblue for emails, Cloudinary for media) seamlessly via **Spring Cloud OpenFeign** with standard security configurations.
  - Includes automated Unit/Integration Tests using **Testcontainers** and JUnit 5. Maintains code formatting using the **Spotless** plugin.

---

## 📸 Screenshots & Demo

<!-- TODO: Add Image URLs below the img tags -->

### Admin Dashboard & Management

<div style="display: flex; justify-content: space-between;">
  <img src="URL_DASHBOARD_REVENUE" alt="Revenue Chart" width="48%">
  <img src="URL_QUAN_LY_LICH_CHIEU" alt="Screening Management" width="48%">
</div>

### Internal AI Chatbot & Document RAG

<div style="display: flex; justify-content: space-between;">
  <img src="URL_AI_CHATBOT_INTERNAL" alt="Internal AI Chatbot" width="48%">
  <img src="URL_RAG_DOCUMENT_MANAGEMENT" alt="RAG Document Management" width="48%">
</div>

### Customer Portal

<div style="display: flex; justify-content: space-between;">
  <img src="URL_TRANG_CHU_CLIENT" alt="Client Homepage" width="48%">
  <img src="URL_PASS_TICKET" alt="Ticket Transfer" width="48%">
</div>

---

## 🚀 Getting Started

### System Requirements

- JDK 21+
- Node.js 20+ (or newer)
- Maven 3.8+
- Docker & Docker Compose (for running the database)

### 1. Start the Database (PostgreSQL + PGvector)

In the `backend/theatermgnt` directory, run the following command to set up the database via Testcontainers or Docker Compose:

```bash
docker-compose up -d
```

### 2. Run Backend (Spring Boot)

Navigate to the backend directory and use Maven to run the project:

```bash
cd backend/theatermgnt
./mvnw spring-boot:run
```

_Note: You need to configure the environment variables for the Database, Cloudinary, Sendinblue, and OpenAI in `application.properties` (or `application.yml`)._

### 3. Run Frontend - Admin Dashboard

Open a new terminal and navigate to the admin directory:

```bash
cd frontend/admin
npm install
npm run dev
```

The Admin page will be available at `http://localhost:5173`

### 4. Run Frontend - Customer Portal (Next.js)

Open a new terminal and navigate to the client directory:

```bash
cd frontend/client
npm install
npm run dev
```

The Customer page will be available at `http://localhost:3000`

---

<div align="center">
  <p><b>Developed by Information Systems Students</b></p>
</div>
