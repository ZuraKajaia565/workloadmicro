FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Copy the pre-built fat JAR
COPY target/*-exec.jar app.jar

# Expose the application port
EXPOSE 8083

# Run with disabled integrations
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=test"]
