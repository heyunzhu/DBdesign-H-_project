# Library Backend

Spring Boot + MyBatis backend for the library borrowing management system.

## Requirements

- JDK 17+
- Maven 3.8+
- MySQL 8.0

## Database

Run the SQL files in this order:

```text
../SQL/schema.sql
../SQL/seed.sql
```

The default database configuration uses MySQL 8.0 on port `3307`.

Before starting the backend, set your local MySQL password through an environment variable:

```powershell
$env:DB_PASSWORD="your-mysql-password"
```

## Run

```bash
mvn spring-boot:run
```

Health check:

```text
GET http://localhost:8081/api/health
```

Book list:

```text
GET http://localhost:8081/api/books
```
