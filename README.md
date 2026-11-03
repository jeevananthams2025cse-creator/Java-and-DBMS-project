# 🩸 Blood Bank Management System (Uthiram)

A secure, transactional **Blood Bank Management System** developed in **Java 21, Spring Boot 3, Pure JDBC, and MySQL**, featuring both a modern **server-rendered web application (Thymeleaf + Tailwind CSS)** and a standalone **interactive CLI console application**.

The project demonstrates Object-Oriented Programming (OOP) concepts in Java, relational database management principles in MySQL, pessimistic concurrency control, transaction management, and clinical audit trail enforcement.

---

## 🎓 Academic Details

| Course Code | Course Name                            |
| ----------- | -------------------------------------- |
| **CS23333** | Object Oriented Programming Using Java |
| **CS23332** | Database Management Systems            |

- **Project Type:** Academic / College Project  
- **Architecture:** Hybrid Multi-Page Web Application + CLI Console Application  
- **Team Size:** 4 Members  

---

## 👥 Project Team & Responsibilities

| S. No. | Student Name       | Register Number | Role & Focus Area                                                    |
| :---:  | :----------------- | :-------------: | :------------------------------------------------------------------- |
| **1**  | **Jeevapriyan V**  | `250701279`     | Java OOP, Domain Models & Spring MVC Controllers                     |
| **2**  | **Kamlesh VG**     | `250701315`     | Database Schema, SQL Optimization & Relational Integrity Constraints |
| **3**  | **Jensen Tony T**  | `250701286`     | Pure JDBC, Connection Scoping & Concurrency Control                  |
| **4**  | **Jeevanantham S** | `250701278`     | Thymeleaf UI/UX, Spring Security, System Integration & Testing        |

---

## 📌 Project Overview

Managing blood collections, laboratory inventory, and clinical hospital orders demands high precision: a transfusion cross-match error can be fatal, and blood units spoil if not issued in a strict **First-Expiry-First-Out (FEFO)** sequence.

This application provides a centralized platform that enforces donor biological cooldowns, calculates live stock dynamically, prioritizes emergency trauma orders, and fulfills compatible units inside **ACID database transactions**.

The project features a **dual-interface architecture**:
1. **Multi-Page Web UI**: A clean, Linear/Stripe-inspired interface rendered server-side with Thymeleaf, styled with Tailwind CSS and Lucide icons.
2. **Interactive CLI Console Application**: The original 14-option terminal interface (`com.bloodbank.Main`) running seamlessly against the exact same database.

---

## ✨ Key Features

### 🎛️ 1. Central Admin Dashboard (`/dashboard`)
- **Real-Time KPIs**: Live counts for non-expired available units, pending orders (with visual emergency badges), registered donors, and hospital patients.
- **Dynamic 8-Blood-Group Stock Monitor**: Computed dynamically from available units for `A+`, `A-`, `B+`, `B-`, `AB+`, `AB-`, `O+`, `O-` tagged with `Good`, `Low`, or `Critical` thresholds.
- **Direct ID Record Management**: Quick lookup cards on the dashboard to view, edit, or delete any Donor or Patient directly by ID (`#D-1` / `#P-1`) with interactive cascade confirmations.
- **Quick Operations Hub**: Fast launchpad to register donors, record donations, admit patients, or allocate blood units.

### 👥 2. Donor Registry & Safety Policies (`/donors`)
- Register voluntary donors with input validation (name, contact phone, age, blood group).
- **Biological Safety Margin**: Enforces donor age between **18 and 65 years**, and enforces a **90-day minimum rest interval** between donations (calibrated for whole blood safety).
- Search donors by legal name, phone number, or numeric ID (`#D-1`) with blood-group filtering and pagination.
- **Relational Cascade on Edit**: Modifying a donor's blood group automatically cascades to all unissued, non-expired `AVAILABLE` blood units in inventory.
- **Audit-Safe Donor Deletion**: Purges unissued inventory while unlinking historical issued units (`bu.donation_id = NULL`) so past hospital transfusion logs are never corrupted.

### 🏥 3. Patient & Hospital Registry (`/patients`)
- Register admitted patients linked to their respective medical centers or hospitals.
- Search patients by name, hospital, phone, or numeric ID (`#P-1`).
- **Relational Cascade on Edit**: Correcting a patient's blood group automatically cascades to all their active `PENDING` blood requests.
- **Strict Clinical Audit Preservation**: The system **strictly forbids deleting any patient who has transfusion records on file**. Patients with transfusion history cannot be hard-deleted; only patients with zero issue history can be removed (along with their unfulfilled pending requests).

### 🩸 4. Donation Management & Unit Serialization (`/donations`)
- Record donations for voluntary donors across product types: **Whole Blood**, **Plasma**, and **Platelets**.
- Automatically updates donor's `last_donation_date`.
- Serializes each donation into discrete `blood_units` in inventory with calculated collection dates and product-specific shelf-life expiry dates.

### 📦 5. Blood Inventory & Dynamic Stock Calculation (`/inventory`)
- **No Denormalized Counters**: Available stock is computed dynamically via aggregation queries (`status = 'AVAILABLE' AND expiry_date >= CURRENT_DATE`), eliminating state desynchronization.
- **Available Units Ledger (`/inventory/available`)**: Lists all non-expired units ready for clinical allocation, detailing Unit ID, blood group, collection date, expiry date, days remaining, and status.
- Automatically excludes expired units (`expiry_date < CURRENT_DATE`).

### ⚡ 6. Clinical Requests & Emergency Triage (`/requests`)
- Register blood orders from hospitals specifying required units, product type, and priority (**NORMAL**, **URGENT**, **EMERGENCY**).
- **Emergency Priority Sorting**: Emergency orders automatically float to the top across all views with visual alert badges.

### 🧬 7. Whole Blood Compatibility & FEFO Fulfillment Engine (`/requests/{id}/fulfil`)
- **Red Cell / Whole Blood Compatibility Matrix**: Enforces the 27 standard erythrocyte compatibility rules (universal donor `O-`, universal recipient `AB+`, Rh-positive blood strictly blocked from Rh-negative recipients).
- **Product-Specific Scope**: The automated cross-matching engine is specifically calibrated for **Whole Blood** transfusions. Requests for other components are restricted in this workflow because plasma compatibility follows the reverse rule set (where AB is the universal plasma donor).
- **FEFO (First-Expiry-First-Out)**: Eligible compatible units nearing expiration are prioritized first to eliminate blood wastage.
- **Pessimistic Concurrency & Transactional Fulfillment**: Runs inside an isolated database transaction with row-level locks (`SELECT ... FOR UPDATE`) on both the request and candidate units, followed by atomic conditional status updates to prevent race conditions and double-issuance.

### 📜 8. Immutable Transfusion Issue Audit Trail (`/issues`)
- Permanent, append-only clinical audit log recording Issue ID, Request ID, Patient Name, Admitting Hospital, Blood Unit ID, and exact Issue Timestamp.

### 📊 9. Analytics & One-Click CSV Export (`/reports`)
- **Inventory Report (`/reports/inventory`)**: Stock metrics, available vs. expired units, and downloadable CSV export.
- **Emergency Report (`/reports/emergency`)**: Clinical turnaround time, fulfillment ratios, and downloadable CSV export.

---

## 🛠️ Technologies Used

| Technology | Purpose |
| :--- | :--- |
| **Java 21 (LTS)** | Application development utilizing modern language features (records, text blocks, switch expressions) |
| **Spring Boot 3.3.5** | Web layer, MVC controller routing, dependency injection, and HTTP request handling |
| **Spring Security 6** | Admin authentication, BCrypt password hashing, session timeout (30 mins), and CSRF token defense |
| **Pure JDBC (`java.sql`)** | Direct SQL execution via PreparedStatements without ORM/Hibernate overhead |
| **HikariCP** | Enterprise connection pooling with connection leak detection |
| **MySQL 8.x** | Relational database management system |
| **Thymeleaf 3.x** | Server-side HTML template engine with modular layout components |
| **Tailwind CSS (CDN)** | Utility-first responsive styling following a modern SaaS design system |
| **Lucide Icons** | Clean SVG icons for navigation and operational badges |
| **Apache Maven** | Build automation, dependency management, and reproducible packaging |
| **JUnit 5** | Automated unit testing for compatibility matrices, FEFO sorting, and cascade rules |

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
| `blood_issues` | Immutable audit log linking fulfilled requests to specific blood units and issue timestamps |
| `compatibility_rules` | Lookup table seeded with the 27 standard ABO/Rh erythrocyte compatibility pairs |

---

## 👨‍💻 Team Contributions & Roles

### 1. Jeevapriyan V – Java OOP, Domain Models & Spring MVC Controllers
- Designed and maintained the Java model class hierarchy (`Person`, `Donor`, `Patient`, `BloodProduct`).
- Implemented the Spring MVC controller endpoints (`DonorController`, `PatientController`, `DashboardController`).
- Developed Form DTOs (`DonorForm`, `PatientForm`) and enforced server-side validation (`@Valid`, age checks, phone regex).
- Built the Direct ID Record Management endpoints allowing direct lookups and modal triggers from the dashboard.

### 2. Kamlesh VG – Database Schema, SQL Optimization & Relational Integrity Constraints
- Architected the normalized MySQL database schema, primary/foreign keys, and integrity constraints.
- Formulated atomic multi-table cascade update and delete queries for both donors and patients.
- Designed the donor blood-group cascade updating unissued inventory while unlinking historical issued units (`bu.donation_id = NULL`).
- Enforced the patient audit constraint in SQL to reject deletions of patients who have linked `blood_issues`.
- Seeded and optimized the 27 standard ABO/Rh erythrocyte compatibility rules.

### 3. Jensen Tony T – Pure JDBC, Connection Scoping & Concurrency Control
- Engineered the data access layer using pure JDBC PreparedStatements, eliminating SQL injection risks.
- Integrated HikariCP connection pooling via `DatabaseConfig` with a static fallback for standalone CLI execution.
- Implemented transaction connection scoping: passed the active transaction `Connection` across subordinate DAO calls, eliminating InnoDB cross-connection lock wait timeouts.
- Implemented pessimistic concurrency control (`SELECT ... FOR UPDATE`) and atomic conditional status updates in `RequestService` to prevent double-issuance under concurrent requests.
- Developed backend business services (`DonorService`, `PatientService`, `DonationService`, `RequestService`, `CompatibilityService`).

### 4. Jeevanantham S – Thymeleaf UI/UX, Spring Security, Integration & Testing
- Designed and implemented the complete multi-page web UI using Thymeleaf layout fragments, Tailwind CSS, and Lucide icons.
- Configured Spring Security 6 with BCrypt password hashing, session expiration (30 mins), CSRF protection, and in-memory rate-limiting (`LoginAttemptService`).
- Implemented interactive cascade confirmation modals for record deletions and real-time stock status badge chips.
- Developed automated CSV export utilities for both the Inventory Report and Emergency Request Report.
- Authored automated JUnit 5 test suites (`CompatibilityAndExpiryTest`, `CascadeManagementTest`) and verified dual CLI/Web compatibility.

---

## 🔒 Security, Concurrency & Data Integrity Architecture

### 1. Concurrency Control & Double-Issue Prevention
When two hospital orders request the same blood group simultaneously, concurrency anomalies are prevented using a two-tier defense:
1. **Pessimistic Row-Level Locking (`SELECT ... FOR UPDATE`)**: In `RequestService`, candidate available units are locked within the transaction, preventing other transactions from reading or reserving the same rows until completion.
2. **Atomic Conditional State Transition**: Units are marked issued using:
   ```sql
   UPDATE blood_units
   SET status = 'ISSUED'
   WHERE unit_id = ? AND status = 'AVAILABLE' AND expiry_date >= CURRENT_DATE
   ```
   If any unit fails this condition, the transaction rolls back completely.

### 2. Transaction Connection Scoping
Multi-table transactional operations (such as recording a donation or fulfilling an order) execute entirely on a single shared `Connection` instance passed between service and DAO methods. This eliminates the risk of acquiring row locks on one pooled connection while waiting on another (which previously caused InnoDB lock wait timeouts).

### 3. Dynamic Inventory Calculation vs. Denormalization
Stock counts are not stored as separate counters. Total and per-group stock counts are calculated dynamically:
```sql
SELECT COUNT(*) FROM blood_units
WHERE blood_group = ? AND status = 'AVAILABLE' AND expiry_date >= CURRENT_DATE
```
This guarantees that stock availability is always mathematically synchronized with the unit ledger.

### 4. Unit Expiry Lifecycle
Blood units past their expiration date are dynamically filtered out of all fulfillment routines and stock summaries at query time (`expiry_date >= CURRENT_DATE`). The `EXPIRED` status serves as a lifecycle marker for historical reporting and audit queries.

### 5. Single-Tier Admin Authentication & Rate-Limiting
- **Authentication**: Admin credentials are encrypted using BCrypt (`admins` table). All operational routes require an active authenticated session.
- **Brute-Force Protection**: `LoginAttemptService` implements an in-memory sliding-window IP rate limiter that temporarily locks an IP after 5 consecutive failed login attempts (suitable for single-instance academic deployments).
- **CSRF Defense**: Synchronizer Token Pattern is enforced on all state-changing POST requests.

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

## 🚧 Current Scope & Limitations

1. **Compatibility Scope**: The automated cross-matching engine evaluates standard red blood cell (Whole Blood) compatibility. Component-specific rules (such as reversed plasma compatibility where AB is the universal donor) are reserved for future multi-component workflows.
2. **Donor Rest Interval**: The 90-day donation interval is applied uniformly across donors to ensure conservative clinical safety; product-specific rest periods (e.g. 14 days for apheresis platelets) are not yet differentiated.
3. **In-Memory Rate Limiting**: The brute-force protection cache is maintained in application memory and resets on server restart; production scaling would integrate Redis or account-level lockouts.
4. **Automated Expiry Job**: Expiry is currently enforced dynamically at query time; a scheduled background cron job (`@Scheduled`) to physically update database status columns to `EXPIRED` is planned for future iterations.

---

## 🎯 Key Learning Outcomes

Through this project, the team gained practical hands-on experience in:
- Architecting enterprise Java applications with clean separation of layers (Controller $\to$ Service $\to$ DAO $\to$ Model).
- Combining pure JDBC with modern connection pooling (HikariCP) for fine-grained transaction control.
- Securing web applications using Spring Security, BCrypt, and CSRF tokens without relying on heavy ORM frameworks.
- Designing multi-table relational cascade policies that maintain historical medical audit trails.
- Enforcing pessimistic row-level locking (`SELECT ... FOR UPDATE`) to prevent race conditions during inventory allocation.
- Implementing mission-critical healthcare algorithms (FEFO expiry queueing and biological compatibility matrices).

---

## 📜 Academic Purpose

This project was developed in partial fulfillment of the academic requirements for:
- **CS23333 – Object Oriented Programming Using Java**
- **CS23332 – Database Management Systems**
