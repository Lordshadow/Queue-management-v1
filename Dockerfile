# ─── Build Stage ───────────────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /build

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# ─── Runtime Stage ──────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-noble

WORKDIR /app

# Copy the built JAR from the builder stage
COPY --from=builder /build/target/queue-management-0.0.1-SNAPSHOT.jar app.jar

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
    CMD java -cp app.jar org.springframework.boot.loader.launch.JarLauncher \
    || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
