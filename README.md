# Telecom Billing System

A small backend system that simulates how a telecom company manages customers, plans, usage, and monthly billing. Built with Java, JDBC, and a SQL database.

## Tech Stack

- **Java** (JDK 11+)
- **OOP** (models, DAOs, service layer)
- **SQL database**: MySQL (or PostgreSQL with minor config change)
- **JDBC** for all database operations
- **Console** menu for interaction

## Project Structure

```
telecom-billing-system
├── src
│   ├── model      # Customer, Plan, UsageRecord, Bill
│   ├── dao        # DatabaseConnection, CustomerDAO, PlanDAO, UsageBillDAO
│   ├── service    # TelecomService (business logic & billing calculation)
│   └── main       # Main (console menu)
├── database
│   └── schema.sql # Table definitions
├── README.md
└── .gitignore
```

## Database Setup

### MySQL

1. Install MySQL and ensure the server is running.
2. Create the database and user (optional):

   ```sql
   CREATE DATABASE telecom_billing;
   CREATE USER 'telecom'@'localhost' IDENTIFIED BY 'your_password';
   GRANT ALL ON telecom_billing.* TO 'telecom'@'localhost';
   ```

3. Run the schema to create tables:

   ```bash
   mysql -u root -p telecom_billing < database/schema.sql
   ```

   Or from MySQL client:

   ```sql
   USE telecom_billing;
   SOURCE /path/to/telecom-billing-system/database/schema.sql;
   ```

4. Update `src/dao/DatabaseConnection.java` with your URL, username, and password:

   ```java
   private static final String URL = "jdbc:mysql://localhost:3306/telecom_billing?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
   private static final String USER = "root";      // or your user
   private static final String PASSWORD = "your_password";
   ```

### PostgreSQL

- Use PostgreSQL JDBC driver and change the URL in `DatabaseConnection.java` to `jdbc:postgresql://localhost:5432/telecom_billing`.
- In `schema.sql`, replace `AUTO_INCREMENT` with `SERIAL` and remove MySQL-specific `COMMENT` clauses if needed, then run the script in `psql` or your GUI.

## How to Run

### Option A: Maven (recommended)

1. Install [Maven](https://maven.apache.org/).
2. From the project root:

   ```bash
   mvn compile exec:java
   ```

   The `pom.xml` already includes the MySQL JDBC driver.

### Option B: Manual compile and run

1. Download [MySQL Connector/J](https://dev.mysql.com/downloads/connector/j/) and note the JAR path.
2. Compile (from project root; use `;` instead of `:` on Windows):

   ```bash
   mkdir -p out
   javac -cp ".:path/to/mysql-connector-j-8.0.33.jar" src/model/*.java src/dao/*.java src/service/*.java src/main/*.java -d out
   ```

3. Run:

   ```bash
   java -cp "out:path/to/mysql-connector-j-8.0.33.jar" main.Main
   ```

## Features

- **Customer management**: Add, view (list + details), update, delete customers.
- **Plans**: Create plans (name, monthly price, data limit, call rate); assign plan to customer.
- **Usage**: Record call minutes, SMS, and data usage per customer per month.
- **Billing**: Generate monthly bill from plan + usage (base + overage); store in DB; view billing history and monthly summary.
- **Reports**: List all customers; top data usage customers; monthly billing summary.

## Example Output

```
=== Telecom Billing System ===

1. Add Customer
2. View Customers
3. Create Plan
4. Assign Plan
5. Record Usage
6. Generate Bill
7. View Billing History
8. Exit
Choice: 1
Name: Alice
Phone: +1-555-001
Email: alice@example.com
Customer added with ID: 1

Choice: 3
Plan name: Basic
Monthly price: 29.99
Data limit (GB): 5
Call rate per minute: 0.10
Plan created with ID: 1

Choice: 4
Customer ID: 1
Plan ID: 1
Plan assigned.

Choice: 5
Customer ID: 1
Month (1-12): 3
Year: 2025
Call minutes to add: 120
SMS count to add: 50
Data usage (GB) to add: 4
Usage recorded.

Choice: 6
Customer ID: 1
Month (1-12): 3
Year: 2025
Bill generated: 41.99
Breakdown: Base: 29.99 | Calls: 120 min x 0.10 = 12.00 | Data overage: 0 GB x 2.00 = 0.00

Choice: 7
1) By customer  2) By month (summary + list)
1
Customer ID: 1
3/2025 -> 41.99 | Base: 29.99 | Calls: 120 min x 0.10 = 12.00 | Data overage: 0 GB x 2.00 = 0.00

Choice: 8
Goodbye.
```

## Database Tables

| Table           | Description                                  |
|----------------|----------------------------------------------|
| `plans`        | Plan name, monthly price, data limit, call rate |
| `customers`    | Name, phone, email, optional `plan_id` (FK)  |
| `usage_records`| Per customer per month: call minutes, SMS, data (GB) |
| `bills`        | Generated bill per customer/month, total and breakdown |

Primary and foreign keys are defined in `database/schema.sql`.

## License

This project is for learning purposes. Use and modify as you like.
