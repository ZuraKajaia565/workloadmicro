#!/bin/bash

# Stop and remove existing containers if they exist
docker rm -f workload-service 2>/dev/null || true
docker rm -f report-service 2>/dev/null || true

# Build the Docker images
docker build -t workload-service:latest -f Dockerfile .
docker build -t report-service:latest -f Dockerfile.report .

# Run the containers with disabled integrations
docker run -d --name workload-service -p 8083:8083 workload-service:latest
docker run -d --name report-service -p 8084:8084 report-service:latest

echo "Services started with disabled integrations"
echo "Workload service: http://localhost:8083"
echo "Report service: http://localhost:8084"
