# GamerSync — Gaming Café Management System

> A Java + MySQL CLI-based management system for gaming cafés.
> Built as a college mini project using Core Java, JDBC, and MySQL.

---

## 👥 Team Members
<!-- Add your names here -->
- Member 1: NILESH SETHI
- Member 2: OM GADWE
- Member 3: NIMHAN VEDANT
- Member 4: OMAR PATEL

---

## 🗂️ Project Structure

```
GamerSync/
├── run.bat                          ← Compile and run (Windows CMD)
├── README.md                        ← This file
├── lib/
│   └── mysql-connector-j-x.x.x.jar ← MySQL JDBC Driver
└── gamersync/
    ├── Main.java                    ← CLI Entry Point
    ├── db/
    │   ├── DBConnection.java        ← Singleton JDBC Connection
    │   └── InvalidDataException.java← Custom Exception
    ├── model/
    │   ├── Customer.java            ← Customer Entity
    │   └── GamingSession.java       ← Session Entity
    ├── dao/
    │   ├── BaseDAO.java             ← Abstract parent (Inheritance)
    │   ├── ICustomerDAO.java        ← Interface (Contract)
    │   ├── ISessionDAO.java         ← Interface (Contract)
    │   ├── CustomerDAO.java         ← Customer DB Operations
    │   └── SessionDAO.java          ← Session DB Operations
    └── service/
        ├── CustomerService.java     ← Customer CLI Module
        ├── SessionService.java      ← Session CLI Module
        ├── WalkInService.java       ← Walk-In Flow
        ├── GamingProfileService.java← Gaming Accounts & Achievements
        ├── FoodPaymentService.java  ← Food Orders & Payments
        ├── TournamentService.java   ← Tournament Management
        ├── PCService.java           ← PC Management
        ├── MembershipService.java   ← Membership Management
        └── QueryService.java        ← Analytical Queries
```

---

## 🗄️ Database — GamerSync (MySQL)

12 Tables:
| Table | Description |
|---|---|
| CUSTOMER | Registered café customers |
| PC | Gaming PCs with status and hourly rate |
| GAMING_SESSION | Active gaming sessions per customer per PC |
| GAMING_ACC | Customer gaming accounts (Valorant, CSGO etc.) |
| ACHIEVEMENTS | Achievements unlocked per gaming account |
| MEMBERSHIP | Customer memberships (Hourly/Weekly/Monthly) |
| HOURLY | Hourly membership details |
| WEEKLY | Weekly membership details |
| MONTHLY | Monthly membership details |
| FOOD_ORDER | Food orders placed during a session |
| TOURNAMENTS | Tournament registrations |
| PAYMENT | Payments collected per session |

DB Features used: Triggers, Stored Procedures, Functions, Views

---

## ⚙️ Tech Stack

| Layer | Technology |
|---|---|
| Language | Core Java |
| UI | CLI (Command Line Interface) |
| DB Connectivity | JDBC (PreparedStatement, CallableStatement) |
| Database | MySQL 8+ |
| IDE | VS Code |

---

## 🧱 OOP Concepts Demonstrated

| Concept | Where Used |
|---|---|
| Inheritance | BaseDAO → CustomerDAO, SessionDAO |
| Interfaces | ICustomerDAO, ISessionDAO |
| Polymorphism | toString() override in Customer, GamingSession; overloaded getCustomer() |
| Encapsulation | All model fields private with public getters/setters |
| Packages | gamersync.db, gamersync.model, gamersync.dao, gamersync.service |
| Custom Exception | InvalidDataException for input validation |

---

## 🗃️ Database Features Used

| Feature | Detail |
|---|---|
| Trigger 1 | check_amount — rejects PAYMENT with AMOUNT <= 0 |
| Trigger 2 | update_pc_status — auto-sets PC to 'In Use' after session insert |
| Stored Proc 1 | get_customer_data(cid) — fetch customer by ID |
| Stored Proc 2 | get_sessions() — fetch all sessions |
| Function 1 | get_total_payment(cid) — total payment by customer |
| Function 2 | session_duration(sid) — duration of a session |
| View 1 | customer_sessions — customer + game + duration |
| View 2 | total_spending — total payment per customer |

---

## 🚀 How to Run

### Prerequisites
- Java JDK 17+
- MySQL 8+
- MySQL Connector/J JAR (already in `/lib/`)

### Setup Steps

1. Open MySQL Workbench and run `gamersync_database_project.sql`
2. Open `gamersync/db/DBConnection.java`
3. Set your MySQL password:
   ```java
   private static final String PASSWORD = "your_password_here";
   ```
4. From project root, double-click `run.bat`
   OR run manually in CMD from root folder:
   ```
   cd gamersync\src
   javac -cp ".;..\lib\*" gamersync\db\*.java gamersync\model\*.java gamersync\dao\*.java gamersync\service\*.java gamersync\Main.java
   java -cp ".;..\lib\*" gamersync.Main
   ```

---

## 📋 Main Menu Options

```
1.  Walk-In Flow
2.  Customer Module
3.  Session Module
4.  Gaming Profile
5.  Food & Payment
6.  Tournaments
7.  PC Management
8.  Analytical Queries
9.  Membership Module
10. View All Tables
0.  Exit
```

---

## 🧪 Sample Test Flow (Viva Demo)

1. Run option 1 (Walk-In Flow) → add new customer → assign PC → start session
   → add membership → register gaming account
2. Run option 5 (Food & Payment) → add food order → add payment → view full bill
3. Run option 8 (Analytical Queries) → run ranking query → call stored procedure
   → query views
4. Run option 10 → view all 12 tables at once

---

## 📚 Rubric Coverage

| Marks | CO | Requirement | Covered By |
|---|---|---|---|
| 2 | CO1 | Classes, variables, access specifiers | All model + DAO classes |
| 2 | CO2 | Inheritance, polymorphism, interfaces, packages | BaseDAO, ICustomerDAO, toString() |
| 3 | CO3 | Specific exception handling | InvalidDataException + SQLException + NumberFormatException |
| 2 | CO3 | Agile UML diagram | Use Case Diagram in Main.java comments |
| 5 | CO4 | DML queries | INSERT/UPDATE/DELETE across all 12 tables |
| 5 | CO4 | DRL queries | SELECT + JOIN + GROUP BY + WINDOW FUNCTIONS |
| 5 | CO4 | DBMS reports | Phase 1 + Phase 2 submitted separately |

---

## 📝 Notes

- END_TIME in sessions is auto-calculated from START_TIME + DURATION (never entered manually)
- DB triggers handle PC status update and payment validation automatically
- All SQL uses PreparedStatement — no string concatenation (SQL injection safe)
- Scanner is never closed inside service methods (shared instance from Main)
