# Money Manager API

A comprehensive personal finance management REST API built with Spring Boot that enables users to track income, expenses, and manage their financial transactions.

## Live Demo & Repository

- 🌐 **Live Demo**: [https://fza-moneymanager.netlify.app/](https://fza-moneymanager.netlify.app/)
- 💻 **Frontend Repository**: [https://github.com/fikriupm/moneymanager-react](https://github.com/fikriupm/moneymanager-react)

> **Note**: The backend API is deployed on Render's free tier and may experience inactivity suspensions. Please allow a moment for the service to spin up on first use (or **suspended** already).

## Features

- 🔐 **Authentication & Authorization** - JWT-based secure authentication
- 💰 **Income Management** - Track and manage income sources
- 💸 **Expense Tracking** - Record and categorize expenses
- 📊 **Dashboard Analytics** - View financial summaries and insights
- 🔍 **Advanced Filtering** - Filter transactions by date, amount, and keywords
- 📂 **Category Management** - Organize transactions with custom categories
- 👤 **User Profiles** - Personalized user accounts
- 📧 **Email Notifications** - Stay informed about important events
- 📤 **Excel Export** - Export financial data to Excel format

## Technologies

- **Java 17+**
- **Spring Boot 3.x**
- **Spring Security** with JWT
- **Spring Data JPA**
- **PostgreSQL**
- **Maven**
- **Lombok**
- **Docker** (optional deployment)

## Prerequisites

- Java JDK 17 or higher
- Maven 3.6+
- PostgreSQL 12+
- (Optional) Docker & Docker Compose

## Installation

### 1. Clone the Repository

```bash
git clone <repository-url>
cd moneymanager
```

### 2. Configure Database

Create a PostgreSQL/MySQL database:


### 3. Configure Application Properties

Edit `src/main/resources/application.properties`:

### 4. Build the Application

```bash
./mvnw clean install
```

## Running the Application

### Using Maven

```bash
./mvnw spring-boot:run
```

### Using Java

```bash
java -jar target/moneymanager-0.0.1-SNAPSHOT.jar
```

### Using Docker

```bash
docker build -t moneymanager .
docker run -p 8080:8080 moneymanager
```

The API will be available at `http://localhost:8080/api/v1.0`


## Security

All endpoints except authentication are secured with JWT tokens. Include the JWT token in the Authorization header:

```
Authorization: Bearer <your-jwt-token>
```

## Configuration Profiles

- **Development**: `application.properties`
- **Production**: `application-prod.properties`

Run with specific profile:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod
```
