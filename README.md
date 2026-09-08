# University Resource Register (URMS)

A desktop application for managing college lab and campus resources (venues, equipment, instruments, consumables, and allocations).

---

## 📁 Project Structure & Package Layout

All Java code is structured under the root `urms` package:

```text
University-Resource-Management/
├── pom.xml                                   # Maven configuration (Java 17, SQLite JDBC, jBCrypt)
├── README.md                                 # Project documentation & architecture guide
├── resourceregister.db                       # SQLite runtime database (auto-created on launch, git-ignored)
│
└── src/
    └── main/
        ├── java/
        │   └── urms/
        │       ├── Main.java                 # Entry point: initializes theme, bootstraps DB, launches UI
        │       │
        │       ├── dao/
        │       │   └── DatabaseConnection.java # SQLite connection provider & schema/seed bootstrapper
        │       │
        │       ├── ui/
        │       │   └── DefaultWindow.java    # Swing application window frame
        │       │
        │       └── util/
        │           └── AppConfig.java        # Cross-machine SQLite database path resolution
        │
        └── resources/
            └── db/
                ├── schema.sql                # DDL: Users, Categories, Resources, Allocations
                └── seed.sql                  # Initial sample data for testing & demo
```

---

## 🏛️ Architectural Call-Flow Rule

The system enforces a strict layered call flow:

$$\text{UI} \longrightarrow \text{Service} \longrightarrow \text{DAO} \longrightarrow \text{Model}$$

* **Rule:** The **UI never calls the DAO layer directly**. All user actions in the UI flow through domain Services, which enforce validation rules, quantity checks, and state transitions before delegating persistence to DAOs.
* **Separation:** No UI imports appear in DAO or Service layers, and no raw SQL appears outside the DAO layer.

---

## 🗄️ Database Schema

The SQLite schema (`src/main/resources/db/schema.sql`) defines four core tables:

1. **`Users`**: Operator credentials (`username`, BCrypt `password_hash`, `full_name`, `is_active`).
2. **`Categories`**: Resource classifications (`category_id`, `category_name`, `description`, `is_active`).
3. **`Resources`**: Inventory tracking (`resource_name`, `category_id`, `resource_type`, `location`, `total_quantity`, `available_quantity`, `is_active`).
4. **`Allocations`**: Checkouts and returns (`resource_id`, `borrower_id`, `borrower_name`, `borrower_type`, `quantity`, `issue_date`, `due_date`, `return_date`, `status`, `issued_by`, `remarks`).

---

## 🛠️ Tech Stack & Dependencies

* **Language:** Java 17 LTS
* **Database:** SQLite (`org.xerial:sqlite-jdbc`)
* **UI & Theming:** Java Swing (System Look and Feel)
* **Security:** jBCrypt (`org.mindrot:jbcrypt`)
* **Testing:** JUnit 5 (`org.junit.jupiter:junit-jupiter`)
* **Build Tool:** Maven

---

## 🚀 How to Run

### Run directly with Maven:
```powershell
mvn compile exec:java
```

### Build a standalone runnable JAR:
```powershell
mvn clean package
java -jar target/resourceregister-1.0-SNAPSHOT.jar
```
