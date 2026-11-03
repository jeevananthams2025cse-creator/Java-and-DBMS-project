# 🩸 Blood Bank Management System (Uthiram)

An enterprise-grade, secure **Blood Bank Management System** developed in **Java 21, Spring Boot 3, Pure JDBC, and MySQL**, featuring both a modern **server-rendered web application (Thymeleaf + Tailwind CSS)** and a standalone **interactive console application**.

The project demonstrates advanced Object-Oriented Programming (OOP) concepts in Java, relational database management principles in MySQL, robust transaction handling, role-based security, and relational cascade management.

---

## 🎓 Academic Details

| Course Code | Course Name                            |
| ----------- | -------------------------------------- |
| **CS23333** | Object Oriented Programming Using Java |
| **CS23332** | Database Management Systems            |

- College Project : 
- **Architecture:** Hybrid Multi-Page Web Application + CLI Console Application  
- **Team Size:** 4 Members  

---

## 👥 Project Team & Updated Responsibilities

| S. No. | Student Name       | Register Number | Role & Focus Area                                               |
| :---:  | :-------------     | :-------------: | :-------------------------------------------------------------- |
| **1**  | **Jeevapriyan V**  | `250701279`     | Java OOP, Domain Models & Spring MVC Controllers                |
| **2**  | **Kamlesh VG**     | `250701315`     | Database Schema, SQL Optimization & Relational Cascades         |
| **3**  | **Jensen Tony T**  | `250701286`     | Pure JDBC, HikariCP Connection Pooling & Transaction Engine     |
| **4**  | **Jeevanantham S** | `250701278`     | Thymeleaf UI/UX, Spring Security, System Integration & Testing   |

---

## 📌 Project Overview

Managing blood collections, laboratory inventory, and hospital transfusion orders requires extreme precision: a single blood-type mismatch can be fatal, and blood units expire if not utilized in a **First-Expiry-First-Out (FEFO)** order.

This project delivers an end-to-end management platform that automates donor eligibility checks, tracks inventory shelf-life in real time, prioritizes emergency requests, and executes compatible transfusion fulfillment inside **atomic database transactions**.

The system features a **dual-interface architecture**:
1. **Modern Multi-Page Web UI**: A clean, Linear/Stripe-inspired interface rendered server-side with Thymeleaf, styled with Tailwind CSS and Lucide icons.
2. **Interactive CLI Console Application**: The original 14-option terminal interface (`com.bloodbank.Main`) running seamlessly off the exact same business logic and database.

---

## ✨ Key Features

### 🎛️ 1. Central Admin Dashboard (`/dashboard`)
- **Real-Time KPIs**: Live counts for non-expired available units, pending requests (with emergency alert badges), registered donors, and hospital patients.
- **8-Blood-Group Stock Monitor**: Stock gauge for `A+`, `A-`, `B+`, `B-`, `AB+`, `AB-`, `O+`, `O-` tagged with `Good`, `Low`, or `Critical` status indicators.
- **Direct ID Record Management**: Dedicated lookup cards to search, edit, or delete any Donor or Patient directly by ID (`#D-1` / `#P-1`) with automated cascade handling.
- **Quick Operations Hub**: Rapid launchpad to register donors, record donations, admit patients, or allocate blood units.

### 👥 2. Donor Registry & Biological Safety Checks (`/donors`)
- Register voluntary donors with input validation (name, contact phone, age, blood group).
- **Biological Eligibility Constraints**: Enforces donor age between **18 and 65 years**, and guarantees a **mandatory 90-day minimum gap** between donations.
- Search donors by legal name, phone number, or numeric ID (`#D-1`) with blood-group filtering and pagination.
- **Relational Cascade on Edit**: Modifying a donor's blood group automatically cascades to all unissued, non-expired `AVAILABLE` blood units in inventory.
- **Safe Cascade on Deletion**: Purges available units in inventory while cleanly unlinking historical `ISSUED` units (`bu.donation_id = NULL`) so past hospital transfusion records remain intact.

### 🏥 3. Patient & Hospital Registry (`/patients`)
- Register admitted patients linked to their respective medical centers or hospitals.
- Search patients by name, hospital, phone, or numeric ID (`#P-1`).
- **Relational Cascade on Edit**: Correcting a patient's blood group automatically cascades to all their active `PENDING` blood requests.
- **Safe Cascade on Deletion**: Purges pending blood requests and clears linked transfusion issue logs in a single atomic transaction.

### 🩸 4. Donation Management & Automatic Serialization (`/donations`)
- Record donations for voluntary donors across product types: **Whole Blood**, **Plasma**, and **Platelets**.
- Automatically updates donor's `last_donation_date`.
- Serializes each donation into discrete `blood_units` in inventory with calculated collection dates and product-specific shelf-life expiry dates.

### 📦 5. Blood Inventory & Shelf-Life Tracking (`/inventory`)
- Real-time stock overview categorized across all 8 blood groups.
- **Available Units Ledger (`/inventory/available`)**: Lists all non-expired units ready for clinical allocation, detailing Unit ID, blood group, collection date, expiry date, days remaining, and status.
- Automatically excludes expired units (`expiry_date < CURDATE()`).

### ⚡ 6. Clinical Requests & Emergency Triage (`/requests`)
- Register blood orders from hospitals specifying required units, product type, and priority (**NORMAL**, **URGENT**, **EMERGENCY**).
- **Emergency Priority Sorting**: Emergency orders automatically jump to the top across all views with visual pulsing alerts.

### 🧬 7. Compatibility & FEFO Fulfillment Engine (`/requests/{id}/fulfil`)
- **27 Standard ABO/Rh Compatibility Rules**: Algorithmic cross-matching ensures clinical safety (e.g., `O-` universal donor, `AB+` universal recipient, Rh-positive blood strictly blocked from Rh-negative recipients).
- **FEFO (First-Expiry-First-Out)**: Units expiring soonest are prioritized first to eliminate blood wastage.
- **ACID Transactional Fulfillment**: Runs inside an isolated database transaction—marks units `ISSUED`, writes to `blood_issues`, updates request status, and decrements stock. Automatically rolls back completely if any step fails.

### 📜 8. Transfusion Issue Audit Trail (`/issues`)
- Immutable clinical log recording Issue ID, Request ID, Patient Name, Admitting Hospital, Blood Unit ID, and exact Issue Timestamp.

### 📊 9. Analytics & One-Click CSV Export (`/reports`)
- **Inventory Report (`/reports/inventory`)**: Stock metrics, available vs. expired units, and downloadable CSV export.
- **Emergency Report (`/reports/emergency`)**: Clinical turnaround time, fulfillment ratios, and downloadable CSV export.

---

## 🛠️ Technologies Used

| Technology | Purpose |
| :--- | :--- |
| **Java 21 (LTS)** | Application development utilizing modern language features (records, text blocks, switch expressions) |
| **Spring Boot 3.3.5** | Modern web layer, MVC controller routing, dependency injection, and REST/HTTP handling |
| **Spring Security 6** | User authentication, BCrypt password hashing, session timeout (30 mins), and brute-force rate-limiting |
| **Pure JDBC (`java.sql`)** | High-performance direct SQL execution via PreparedStatements without ORM/Hibernate bloat |
| **HikariCP** | Enterprise-grade connection pooling with automatic leak detection |
| **MySQL 8.x** | Relational database management system |
| **Thymeleaf 3.x** | Server-side HTML template engine with modular layout components |
| **Tailwind CSS (CDN)** | Utility-first responsive styling following a modern SaaS design system |
| **Lucide Icons** | Clean, minimalist SVG icons for navigation and operational badges |
| **Apache Maven** | Build automation, dependency management, and reproducible packaging |
| **JUnit 5** | Automated unit and integration testing for compatibility matrices, FEFO sorting, and cascade rules |

---

## 🧩 Object-Oriented Programming Concepts

| OOP Concept | Implementation in Codebase |
| :--- | :--- |
| **Encapsulation** | Private entity fields exposed via validated getters and setters; Form DTOs (`DonorForm`, `PatientForm`) validating incoming web data via Bean Validation (`@NotBlank`, `@Min`, `@Pattern`). |
| **Inheritance** | Base abstract class `Person` containing shared attributes (`id`, `name`, `age`, `phone`, `bloodGroup`) inherited by `Donor` and `Patient`. |
| **Abstraction** | Abstract `BloodProduct` class defining abstract shelf-life calculations and storage protocols. |
| **Polymorphism** | `WholeBlood` (35 days), `Platelets` (5 days), and `Plasma` (365 days) implement custom polymorphic expiry calculations. |

---

## 🗄️ Database Design

The system is normalized across the following **eight relational tables**:

| Table | Description |
| :--- | :--- |
| `admins` | Stores admin credentials with BCrypt password hashes and timestamps |
| `donors` | Stores voluntary donor details, contact info, and last donation date |
| `patients` | Stores patient records and admitting hospital information |
| `donations` | Records blood donation events linked to donors (`ON DELETE CASCADE`) |
| `blood_units` | Stores discrete serialized units, collection/expiry dates, and status (`AVAILABLE`, `ISSUED`, `EXPIRED`) |
| `blood_requests` | Maintains patient blood orders, units required, priority, and fulfillment status |
| `blood_issues` | Audit log linking fulfilled requests to specific blood units and issue timestamps |
| `compatibility_rules` | Lookup table seeded with the 27 standard ABO/Rh compatibility pairs |

---

## 👨‍💻 Team Contributions & Roles

### 1. Jeevapriyan V – Java OOP, Domain Models & Spring MVC Controllers
- Designed and maintained the Java model class hierarchy (`Person`, `Donor`, `Patient`, `BloodProduct`).
- Implemented the Spring MVC controller endpoints (`DonorController`, `PatientController`, `DashboardController`).
- Developed Form DTOs (`DonorForm`, `PatientForm`) and enforced server-side validation (`@Valid`, age checks, phone regex).
- Built the Direct ID Record Management endpoints allowing direct lookups and modal triggers from the dashboard.

### 2. Kamlesh VG – Database Schema, SQL Optimization & Relational Cascades
- Architected the normalized MySQL database schema, primary/foreign keys, and integrity constraints.
- Formulated atomic multi-table cascade update and delete queries for both donors and patients.
- Designed the donor blood-group cascade updating unissued inventory while safely unlinking historical issued units.
- Seeded and optimized the 27 standard ABO/Rh biological compatibility rules and query execution paths.
- Authored the schema for the `admins` table supporting BCrypt credential storage.

### 3. Jensen Tony T – Pure JDBC, HikariCP Connection Pooling & Transaction Engine
- Engineered the data access layer using pure JDBC PreparedStatements, eliminating SQL injection risks.
- Integrated HikariCP connection pooling via `DatabaseConfig` with a static fallback for standalone CLI execution.
- Implemented atomic multi-step database transactions (`commit` and `rollback`) for the fulfillment engine.
- Diagnosed and resolved InnoDB cross-connection lock wait timeouts in multi-table donation workflows.
- Developed backend business services (`DonorService`, `PatientService`, `DonationService`, `RequestService`, `CompatibilityService`).

### 4. Jeevanantham S – Thymeleaf UI/UX, Spring Security, Integration & Testing
- Designed and implemented the complete multi-page web UI using Thymeleaf layout fragments, Tailwind CSS, and Lucide icons.
- Configured Spring Security 6 with BCrypt password hashing, CSRF protection, and brute-force rate-limiting (`LoginAttemptService`).
- Implemented interactive cascade confirmation modals for record deletions and real-time stock status badge chips.
- Developed automated CSV export utilities for both the Inventory Report and Emergency Request Report.
- Authored automated JUnit 5 test suites (`CompatibilityAndExpiryTest`, `CascadeManagementTest`) and verified dual CLI/Web compatibility.

---

## ⚙️ Project Setup & Installation

### Prerequisites
- **Java JDK 21** or higher
- **Apache Maven 3.8+**
- **MySQL Server 8.0+**
- **Git**

### 1. Clone the Repository
```bash
git clone https://github.com/yourusername/BloodBankManagement.git
cd BloodBankManagement
```

### 2. Configure MySQL Database
Open your MySQL client or MySQL Workbench and run the schema script:
```sql
CREATE DATABASE IF NOT EXISTS blood_bank;
USE blood_bank;
SOURCE src/main/resources/schema.sql;
```

### 3. Configure Database Credentials
Credentials can be configured via `src/main/resources/application.properties` or environment variables:

**PowerShell (Windows):**
```powershell
$env:DB_URL = "jdbc:mysql://localhost:3306/blood_bank"
$env:DB_USER = "root"
$env:DB_PASSWORD = "your_mysql_password"
```

**Bash (Linux / macOS):**
```bash
export DB_URL="jdbc:mysql://localhost:3306/blood_bank"
export DB_USER="root"
export DB_PASSWORD="your_mysql_password"
```

### 4. Compile the Project & Run Automated Tests
```bash
mvn clean test
```
*(All 10 unit and integration tests will run, verifying compatibility, expiry filtering, and cascade logic).*

---

## 🚀 Running the Application

### Option A: Modern Web Application (Recommended)
```bash
mvn spring-boot:run
```
- **Web URL:** `http://localhost:8080/login`
- **Default Admin Email:** `admin@bloodbank.com`
- **Default Admin Password:** `Admin@123`
*(Admin account is automatically seeded on startup if not already present).*

### Option B: Standalone CLI Console Application
```bash
mvn compile
java -cp target/classes com.bloodbank.Main
```

---

## 📋 Console Application Menu (Dual Mode)

```text
==================================
   BLOOD BANK MANAGEMENT SYSTEM
==================================
1. Register Donor
2. View All Donors
3. Register Patient
4. View All Patients
5. Record Donation
6. View Donation History
7. View Blood Inventory
8. View Available Blood Units
9. Register Blood Request
10. View All Blood Requests
11. Fulfil Blood Request
12. View Blood Issue History
13. Inventory Report
14. Emergency Request Report
0. Exit
==================================
```

---

## 🔒 Security & Data Integrity Features

- **Parameterized Queries**: 100% PreparedStatements across all DAOs to make SQL injection impossible.
- **Brute-Force Rate Limiting**: Automatic IP-based lockout after 5 consecutive failed login attempts.
- **BCrypt Password Encryption**: Strong cryptographic password hashing; no plaintext credentials stored.
- **Atomic Transfusion Fulfillment**: Multi-table operations execute in single ACID transactions with automatic rollback on error.
- **Audit Log Preservation**: Deleting a donor sets `donation_id = NULL` on issued units, preventing dangling references while preserving hospital transfusion records.
- **CSRF Defense**: Synchronizer Token Pattern enforced on all state-changing POST requests.

---

## 🎯 Key Learning Outcomes

Through this project, the team gained practical hands-on experience in:
- Architecting enterprise Java applications with clean separation of layers (Controller $\to$ Service $\to$ DAO $\to$ Model).
- Combining pure JDBC with modern connection pooling (HikariCP) for fine-grained transaction control.
- Securing web applications using Spring Security, BCrypt, and CSRF tokens.
- Designing multi-table relational cascade policies that maintain historical medical audit trails.
- Building responsive, accessible user interfaces with Thymeleaf and Tailwind CSS.
- Implementing mission-critical healthcare algorithms (FEFO expiry queueing and biological compatibility matrices).

---

## 📜 Academic Purpose

This project was developed in partial fulfillment of the academic requirements for:
- **CS23333 – Object Oriented Programming Using Java**
- **CS23332 – Database Management Systems**
