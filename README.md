# Examination Result Management System (JDBC + SQL)

## Files
- `src/main/java/com/examresults/Main.java` — Java app using JDBC.
- `database/schema.sql` — creates the MySQL database and table.
- `pom.xml` — Maven dependency for MySQL Connector/J.

## Requirements
- JDK 17+
- MySQL Server
- Maven 3.8+
- MySQL Workbench (optional)

## Setup
1. Open `database/schema.sql` in MySQL Workbench and execute it.
2. Configure your MySQL credentials in PowerShell (change the password to yours):
   ```powershell
   $env:EXAM_DB_USER = "root"
   $env:EXAM_DB_PASSWORD = "YOUR_MYSQL_PASSWORD"
   ```
3. Open a terminal in the folder containing `pom.xml`.
4. Run:
   ```powershell
   mvn clean compile
   mvn exec:java
   ```

The default connection URL is `jdbc:mysql://localhost:3306/exam_results?serverTimezone=UTC`.

## Pass rule
PASS only if all three subject marks are 40 or higher. Otherwise FAIL.
