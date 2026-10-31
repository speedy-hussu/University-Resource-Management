# University Resource Management System (URMS)

**A modern Java desktop application for managing campus assets, lab equipment, audiovisual inventory, venues, borrowers, and resource allocations at Navrachana University.**

![Java](https://img.shields.io/badge/Java-17_LTS-B32025?logo=openjdk&logoColor=white)
![UI](https://img.shields.io/badge/UI-Swing_+_FlatLaf-1C4E6F)
![Database](https://img.shields.io/badge/Database-SQLite-003B57?logo=sqlite&logoColor=white)
![Build](https://img.shields.io/badge/Build-Maven-C71A36?logo=apachemaven&logoColor=white)

---

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Dashboard & Metrics](#dashboard--metrics)
- [Tech Stack](#tech-stack)
- [Getting Started](#getting-started)
- [Authentication & Sessions](#authentication--sessions)
- [Architecture](#architecture)
- [Project Structure](#project-structure)
- [Database Schema](#database-schema)
- [UI & Design System](#ui--design-system)

---

## Overview

URMS gives university operators a single place to track what the campus owns, who is borrowing it, and what is currently checked out. It runs as a standalone desktop app with a zero-setup embedded database, so it can be launched on any machine with Java 17 installed.

## Features

| Area | Highlights |
| :--- | :--- |
| **Executive Dashboard** | Real-time overview with 4 KPI stat cards (Total Users, Total Resources, Categories, Resources Allocated), interactive navigation shortcuts, and a live feed of the **Last 10 Allocations** sorted chronologically newest-first (`issue_date DESC`, `rowid DESC`) with role and status badges |
| **Campus Directory** | Student and faculty registry with enrollment ID tracking, role badges (`STUDENT`, `FACULTY`), live search, and active/inactive filters |
| **Categories** | Resource grouping with active resource count badges, status indicators (`IN USE`, `EMPTY`), deletion guards for assigned items, and empty state handling |
| **Resource Inventory** | Lifecycle tracking for equipment, labs, and consumables with category filtering, stock availability indicators, and modal editing |
| **Allocations** | Checkout, loan, and return records linking resources to borrowers with atomic inventory syncing, date element pickers with interactive calendar popups, quick due-date presets, overdue tracking, and status badges (`ACTIVE`, `RETURNED`, `OVERDUE`) |
| **Navigation** | Central `CardLayout` shell with persistent sidebar and isolated page panels under `urms.ui.pages` |
| **Security** | Operator login with salted **BCrypt** hashing and persistent sessions |
| **Modern UI** | **FlatLaf** with HiDPI vector scaling, hover states, and Navrachana crimson branding (`#B32025`); offline **Ikonli** Material icons |
| **Zero-Setup Database** | SQLite with automatic schema creation and seed data on first launch |
| **Deployment** | Self-contained Fat JAR with all dependencies bundled |

## Dashboard & Metrics

URMS provides an executive dashboard that serves as the default landing view upon operator login:

### 1. KPI Metric Cards
Four top-level overview cards display real-time aggregates across the university database:
- **Total Users:** Count of active registered student and faculty borrowers (`Users` table).
- **Total Resources:** Count of active registered catalog resources (`Resources` table).
- **Categories:** Total active campus item categories and departments (`Categories` table).
- **Resources Allocated:** Total quantity/units of resources currently on loan (`Allocations` with status `ACTIVE` or `OVERDUE`).

*Interaction:* Clicking any KPI card immediately navigates to that corresponding module and syncs the active tab in the sidebar.

### 2. Recent Allocations Feed
A live data table below the KPI cards provides visibility into university resource checkout activity:
- Displays the **10 most recent allocations**, sorted chronologically newest-first (`ORDER BY a.issue_date DESC, a.rowid DESC`).
- Formatted with role badges (`STUDENT` in blue, `FACULTY` in purple) and dynamic status badges (`ACTIVE` in amber, `OVERDUE` in red, `RETURNED` in green).
- **View All Allocations:** A quick-action button in the card header and row double-click action jump directly to the complete Allocations management table.
- **Refresh:** An action in the header bar allows manual re-synchronization with the SQLite database.

## Tech Stack

| Layer | Technology | Version |
| :--- | :--- | :--- |
| Language | Java | 17 LTS |
| UI Framework | Java Swing | - |
| Theme & Styling | [FlatLaf](https://www.formdev.com/flatlaf/) | 3.5.4 |
| Icons | [Ikonli Material Pack](https://kordamp.org/ikonli/) | 12.4.0 |
| Database | SQLite via `sqlite-jdbc` | 3.53.4.0 |
| Password Hashing | jBCrypt | 0.4 |
| Testing | JUnit 5 | 5.10.2 |
| Build | Apache Maven (`maven-shade-plugin` for Fat JAR) | - |

## Getting Started

### Prerequisites

- JDK 17 or later
- Apache Maven 3.6+

### Run with Maven

```powershell
mvn clean compile exec:java
```

### Build and run the Fat JAR

```powershell
# Package the app and all dependencies into one JAR
mvn clean package

# Run it
java -jar target/urms-1.0-SNAPSHOT.jar
```

On first launch, the SQLite database (`resourceregister.db`) is created automatically from `schema.sql` and populated from `seed.sql`.

## Authentication & Sessions

### Default demo credentials

A default administrator is seeded on first run:

| Field | Value |
| :--- | :--- |
| Username | `admin` |
| Password | `admin123` |
| Role | System Administrator |

> **Note:** These credentials are for demonstration only. Change the password before any real deployment. Passwords are salted and verified with BCrypt.

### Session persistence

Operators stay signed in across launches through a rolling 7-day session.

| Stage | Behavior |
| :--- | :--- |
| **Login** | After successful authentication in [`LoginWindow`](src/main/java/urms/ui/LoginWindow.java), [`DatabaseConnection.createSession(username)`](src/main/java/urms/dao/DatabaseConnection.java) stores a UUID token in SQLite that expires in 7 days |
| **Auto-login** | On startup, [`Main`](src/main/java/urms/Main.java) calls `DatabaseConnection.getActiveSessionUser()`; if a valid session exists, the login screen is skipped |
| **Rolling refresh** | Each successful session check extends the expiry by another 7 days from the time of use |
| **Logout** | The sidebar Logout button asks for confirmation, then `DatabaseConnection.clearSession()` resets `session_token` and `token_expiry` to `NULL` and returns to the login screen |

## Architecture

URMS follows a strict layered call flow:

```mermaid
flowchart LR
    UI["UI<br/>(Pages & Dialogs)"] --> S["Service Layer"] --> D["DAO Layer"] --> DB[("SQLite")]
```

**Rules**

- **No SQL in the UI.** Actions go through services for validation and business rules, which then delegate persistence to DAOs.
- **Separation of concerns.** The DAO and model layers contain no Swing imports, which keeps domain logic clean and unit-testable.
- **Explicit imports.** Source files avoid wildcards (`import javax.swing.JButton`, not `import javax.swing.*`).

## Project Structure

```text
University-Resource-Management/
├── pom.xml                        # Dependencies, build plugins, Fat JAR shading
├── README.md
├── resourceregister.db            # SQLite runtime database (auto-created)
└── src/
    ├── main/
    │   ├── java/urms/
    │   │   ├── Main.java              # Entry point: theme, DB bootstrap, window launch
    │   │   ├── model/                 # Domain entities and DTOs
    │   │   │   ├── Allocation.java
    │   │   │   ├── Category.java
    │   │   │   ├── CategorySummary.java
    │   │   │   ├── DashboardStats.java
    │   │   │   ├── Resource.java
    │   │   │   └── User.java
    │   │   ├── dao/                   # Data access (pure SQL)
    │   │   │   ├── DatabaseConnection.java   # Connection, schema runner, auth/session
    │   │   │   ├── AllocationDAO.java
    │   │   │   ├── CategoryDAO.java
    │   │   │   ├── DashboardDAO.java
    │   │   │   ├── ResourceDAO.java
    │   │   │   └── UserDAO.java
    │   │   ├── service/               # Business logic and validation
    │   │   │   ├── AllocationService.java
    │   │   │   ├── CategoryService.java
    │   │   │   ├── DashboardService.java
    │   │   │   ├── ResourceService.java
    │   │   │   └── UserService.java
    │   │   ├── ui/                    # Swing components
    │   │   │   ├── DefaultWindow.java        # App shell (CardLayout)
    │   │   │   ├── LoginWindow.java
    │   │   │   ├── SidebarPanel.java
    │   │   │   ├── dialogs/
    │   │   │   │   ├── AllocationModalDialog.java
    │   │   │   │   ├── CategoryModalDialog.java
    │   │   │   │   ├── ResourceModalDialog.java
    │   │   │   │   └── UserModalDialog.java
    │   │   │   └── pages/
    │   │   │       ├── AllocationPanel.java
    │   │   │       ├── CategoryPanel.java
    │   │   │       ├── DashboardPanel.java
    │   │   │       ├── ResourcePanel.java
    │   │   │       └── UserPanel.java
    │   │   └── util/
    │   │       ├── AppConfig.java            # Environment and DB path resolution
    │   │       └── ColorConstants.java       # Branding and theme palette
    │   └── resources/
    │       ├── db/
    │       │   ├── schema.sql
    │       │   └── seed.sql
    │       └── images/
    │           └── navrachana_logo.png
    └── test/
        └── java/urms/
            ├── AuthSessionTest.java          # Session persistence tests
            └── DashboardTest.java            # KPI metrics & recent activity tests
```

## Database Schema

The schema lives in [`schema.sql`](src/main/resources/db/schema.sql). All primary keys are UUIDs.

```mermaid
erDiagram
    Categories ||--o{ Resources : groups
    Resources  ||--o{ Allocations : "is allocated in"
    Users      ||--o{ Allocations : borrows
```

| Table | Purpose | Columns |
| :--- | :--- | :--- |
| **Admin** | Operator accounts | `admin_id`, `username`, `password_hash`, `full_name`, `session_token`, `token_expiry`, `is_active` |
| **Users** | Campus borrowers | `user_id`, `enrollment_no`, `full_name`, `role` (`STUDENT` \| `FACULTY`), `is_active`, `created_at` |
| **Categories** | Resource groupings | `category_id`, `category_name`, `description`, `is_active` |
| **Resources** | Equipment, labs, assets | `resource_id`, `resource_name`, `category_id` (FK), `resource_type`, `location`, `total_quantity`, `available_quantity`, `is_active` |
| **Allocations** | Checkouts, loans, returns | `allocation_id`, `resource_id` (FK), `user_id` (FK), `quantity`, `issue_date`, `due_date`, `return_date`, `status`, `issued_by`, `remarks` |

### Transaction & Inventory Integrity

- **Checkout (`AllocationDAO.insertAllocation`):** Runs inside an atomic SQLite transaction (`conn.setAutoCommit(false)`). Verifies stock sufficiency, decrements `available_quantity` in `Resources`, and logs the allocation with status `ACTIVE`.
- **Return (`AllocationDAO.returnAllocation`):** Atomic transaction marks the loan as `RETURNED`, sets the `return_date` timestamp, and increments `available_quantity` back into inventory.
- **Category Guard:** Categories with linked active resources are protected against deletion via foreign-key / count checks in `CategoryService`.

## UI & Design System

Theme values are centralized in `ColorConstants`.

| Token | Value | Use |
| :--- | :--- | :--- |
| `BRAND_CRIMSON` | `#B32025` | Primary brand color |
| `BRAND_CRIMSON_HOVER` | `#99181D` | Hover state for primary actions |
| `TEXT_PRIMARY` | `#1C4E6F` | Navy text and accents |
| `BLUE_ACCENT` | `#2563EB` | Secondary accent |
| Canvas / Cards | `#F2F6F9` / `#FFFFFF` | Page background and card/dialog surfaces |

**Button architecture:** custom buttons call `setContentAreaFilled(false)` and `setOpaque(true)` and use `MouseAdapter` listeners for hover darkening and active/disabled feedback. This gives consistent color and icon rendering and avoids native Look and Feel glitches.