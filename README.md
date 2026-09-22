# Navrachana University - University Resource Management System (URMS)

A modern Java desktop application for managing university campus assets, lab equipment, audiovisual inventory, venues, and resource allocations for students and faculty.

---

## 🌟 Key Features

* **Modern Flat UI:** Powered by **FlatLaf** with crisp vector HiDPI scaling, smooth hover states, and curated university crimson branding (`#B32025`).
* **Material Vector Icons:** Offline, scalable Google Material Design icons rendered via **Ikonli**.
* **Modular Multi-Page Navigation:** Centralized `CardLayout` shell supporting independent, isolated view panels under `urms.ui.pages`.
* **Zero-Setup Database:** SQLite with automated schema bootstrap and seed data initialization on first launch.
* **Security & Auth:** Operator authentication secured with salted **BCrypt** password hashing.
* **Standalone Deployment:** Bundles into a self-contained Fat JAR with all dependencies included.

---

## 📁 Project Structure & Package Layout

All code follows a modular, package-by-layer structure under `urms`:

```text
University-Resource-Management/
├── pom.xml                                   # Maven dependencies, build plugins & Fat JAR shading
├── README.md                                 # Documentation & architecture guide
├── resourceregister.db                       # Local SQLite runtime database (auto-created on startup)
│
└── src/
    └── main/
        ├── java/
        │   └── urms/
        │       ├── Main.java                 # Entry point: sets FlatLaf theme, bootstraps DB, launches window
        │       │
        │       ├── model/                    # Domain POJOs & data transfer records
        │       │   ├── Category.java
        │       │   └── CategorySummary.java
        │       │
        │       ├── dao/                      # Data Access Objects (pure SQL queries)
        │       │   ├── DatabaseConnection.java # SQLite connection pool & SQL runner
        │       │   └── CategoryDAO.java        # Category CRUD and resource count queries
        │       │
        │       ├── service/                  # Business logic & validation layer
        │       │
        │       ├── ui/                       # Desktop Swing UI components
        │       │   ├── DefaultWindow.java    # Main application shell with CardLayout
        │       │   ├── LoginWindow.java      # Operator authentication dialog
        │       │   ├── SidebarPanel.java     # Branded navigation drawer with vector icons
        │       │   │
        │       │   └── pages/                # Independent page view panels
        │       │       └── CategoryPanel.java # Category management screen
        │       │
        │       └── util/
        │           └── AppConfig.java        # Cross-platform environment and database path resolution
        │
        └── resources/
            ├── db/
            │   ├── schema.sql                # DDL: Users, Categories, Resources, Allocations
            │   └── seed.sql                  # Initial university demo data
            └── images/
                └── navrachana_logo.png       # University branding logo
```

---

## 🏛️ Architectural Call-Flow Rule

The system enforces a strict layered call flow:

$$\text{UI (Pages)} \longrightarrow \text{Service} \longrightarrow \text{DAO} \longrightarrow \text{Database}$$

* **Rule:** The **UI never executes raw SQL directly**. Actions in the UI flow through domain Services for business rule enforcement and quantity validation before delegating persistence to DAOs.
* **Separation of Concerns:** No UI/Swing imports appear in DAO or Model layers, keeping domain logic clean and unit-testable.

---

## 🗄️ Database Schema

The SQLite schema (`src/main/resources/db/schema.sql`) manages four core domain entities:

1. **`Users`**: Operator credentials (`user_id`, `username`, `password_hash`, `full_name`, `is_active`).
2. **`Categories`**: Resource taxonomy groupings (`category_id`, `category_name`, `description`, `is_active`).
3. **`Resources`**: Equipment, labs, and asset inventory (`resource_id`, `resource_name`, `category_id`, `resource_type`, `location`, `total_quantity`, `available_quantity`, `is_active`).
4. **`Allocations`**: Checkouts, loans, and returns (`allocation_id`, `resource_id`, `borrower_id`, `borrower_name`, `borrower_type`, `quantity`, `issue_date`, `due_date`, `return_date`, `status`, `issued_by`, `remarks`).

---

## 🛠️ Tech Stack & Dependencies

* **Language:** Java 17 LTS
* **UI Framework:** Java Swing
* **Theme & Styling:** [FlatLaf](https://www.formdev.com/flatlaf/) (`3.5.4`)
* **Vector Icons:** [Ikonli Material Pack](https://kordamp.org/ikonli/) (`12.4.0`)
* **Database:** SQLite via `sqlite-jdbc` (`3.53.4.0`)
* **Security:** jBCrypt (`0.4`)
* **Testing:** JUnit 5 (`5.10.2`)
* **Build System:** Apache Maven (with `maven-shade-plugin` for Fat JAR distribution)

---

## 🚀 How to Run

### Option 1: Run directly with Maven

```powershell
mvn clean compile exec:java
```

### Option 2: Build and run the standalone Fat JAR

```powershell
# 1. Package all dependencies into a standalone JAR
mvn clean package

# 2. Run the generated JAR
java -jar target/urms-1.0-SNAPSHOT.jar
```

---

## 🔐 Default Demo Credentials

On first run, the database is seeded with a default administrator account:

| Field | Value |
| :--- | :--- |
| **Username** | `admin` |
| **Password** | `admin123` |
| **Role** | System Administrator |

*(Passwords are salted and verified at login using BCrypt).*
