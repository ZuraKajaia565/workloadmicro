#!/bin/bash

# Function to check logs for a specific service
check_logs() {
  service=$1
  echo "=== Checking logs for $service ==="
  docker exec -it $service /bin/bash -c "tail -f /app/logs/application.log 2>/dev/null || tail -f /var/log/application.log 2>/dev/null || echo 'No log file found, showing container logs:' && exit"
  
  # If log files aren't found inside container, show docker logs
  if [ $? -ne 0 ]; then
    docker logs $service
  fi
  echo ""
}

# Check logs for each service
check_logs workload-service
check_logs report-service

# Show how to access a shell in the containers
echo "To access a shell in the workload-service container:"
echo "docker exec -it workload-service /bin/bash"
echo ""
echo "To access a shell in the report-service container:"
echo "docker exec -it report-service /bin/bash"