#!/bin/bash

# Run the application with Docker Compose
docker-compose up -d

echo "Services started with enabled integrations"
echo "Workload service: http://localhost:8083"
echo "Report service: http://localhost:8084"
echo "ActiveMQ console: http://localhost:8161"
echo "MongoDB is running on port 27017"