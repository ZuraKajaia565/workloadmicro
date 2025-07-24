# Trainer Workload Tracking Service

A Spring Boot microservice for managing and tracking trainer workloads in a distributed system architecture.

## Overview

This service provides functionality for tracking and managing trainer workloads, built using Spring Boot 3.2.6 with Java 17. The application integrates with MongoDB for data persistence and ActiveMQ for asynchronous messaging.

## Technology Stack

- **Java**: 17
- **Spring Boot**: 3.2.6
- **Spring Cloud**: 2023.0.0
- **Database**: MongoDB
- **Messaging**: ActiveMQ
- **Build Tool**: Maven
- **Containerization**: Docker & Docker Compose

## Architecture

The service follows a layered architecture with the following components:

- **Controllers**: REST API endpoints
- **Services**: Business logic layer
- **Repositories**: Data access layer
- **DTOs**: Data transfer objects
- **Models/Documents**: MongoDB document entities
- **Security**: Authentication and authorization
- **Messaging**: Asynchronous communication
- **Configuration**: Application configuration
- **Exception Handling**: Centralized error handling
- **Logging**: Application logging

## Prerequisites

Before running this application, make sure you have the following installed:

- Java 17 or higher
- Maven 3.6 or higher
- Docker and Docker Compose

## Getting Started

### Using Docker Compose (Recommended)

1. Clone the repository
2. Navigate to the project directory
3. Start the services:
   ```bash
   docker-compose up -d
   ```

This will start:
- MongoDB on port 27018
- ActiveMQ on ports 61616 (JMS) and 8161 (Web Console)
- The workload service

### Manual Setup

1. Start MongoDB and ActiveMQ manually
2. Build the application:
   ```bash
   ./mvnw clean package
   ```
3. Run the application:
   ```bash
   ./mvnw spring-boot:run
   ```

### Using Provided Scripts

The project includes several convenience scripts:

- `build-and-run.sh`: Builds and runs the application
- `run-integrated.sh`: Runs integrated tests
- `check-logs.sh`: Checks application logs

## Configuration

The application can be configured through:
- `application.properties` or `application.yml`
- Environment variables
- Docker environment configuration

Key configuration areas:
- Database connection settings
- ActiveMQ connection properties
- Security settings
- Logging configuration

## API Documentation

The service exposes REST APIs for workload management. Once the application is running, you can access:

- Application: http://localhost:8080
- ActiveMQ Console: http://localhost:8161 (admin/admin)

## Database

The service uses MongoDB for data persistence. The database runs on port 27018 when using Docker Compose to avoid conflicts with local MongoDB installations.

## Messaging

ActiveMQ is used for asynchronous messaging between microservices. The JMS broker is available on port 61616.

## Testing

The project includes comprehensive testing with:
- JUnit 5 for unit testing
- Cucumber for behavior-driven development testing
- Integration tests

Run tests with:
```bash
./mvnw test
```

## Development

### Project Structure

```
src/
├── main/
│   ├── java/com/example/micro/
│   │   ├── controller/     # REST controllers
│   │   ├── service/        # Business logic
│   │   ├── repository/     # Data access
│   │   ├── document/       # MongoDB documents
│   │   ├── dto/           # Data transfer objects
│   │   ├── config/        # Configuration classes
│   │   ├── security/      # Security configuration
│   │   ├── messaging/     # Message handling
│   │   ├── exception/     # Exception handling
│   │   ├── logging/       # Logging utilities
│   │   └── model/         # Domain models
│   └── resources/         # Configuration files
└── test/                  # Test files
```

### Building

```bash
# Clean and compile
./mvnw clean compile

# Package the application
./mvnw clean package

# Skip tests during build
./mvnw clean package -DskipTests
```

## Deployment

### Docker

Build the Docker image:
```bash
docker build -t workload-service .
```

### Production Deployment

The application is production-ready with:
- Proper logging configuration
- Security features
- Health checks
- Metrics and monitoring capabilities

## Monitoring and Health Checks

The application includes:
- Spring Boot Actuator endpoints for health monitoring
- Comprehensive logging
- Metrics collection capabilities

## Troubleshooting

### Common Issues

1. **Port Conflicts**: If you have MongoDB or ActiveMQ running locally, stop them or change the ports in docker-compose.yml
2. **Memory Issues**: Increase Docker memory allocation if containers fail to start
3. **Network Issues**: Ensure Docker network configuration allows container communication

### Logs

Check application logs using:
```bash
./check-logs.sh
```

Or view Docker logs:
```bash
docker-compose logs workload-service
```

## Version
Current version: 0.0.1-SNAPSHOT
