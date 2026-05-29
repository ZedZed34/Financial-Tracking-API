# Stage 1: Build the SvelteKit Frontend
FROM node:22-alpine AS frontend-build
WORKDIR /app/frontend
COPY frontend/package*.json ./
RUN npm install
COPY frontend/ ./
# The svelte.config.js uses adapter-static to output to ../src/main/resources/static
# This will write the static HTML/JS/CSS to /app/src/main/resources/static
RUN npm run build

# Stage 2: Build the Spring Boot Backend
FROM maven:3.9.6-eclipse-temurin-21-alpine AS backend-build
WORKDIR /app
COPY pom.xml .
# Download dependencies for cache
RUN mvn dependency:go-offline || true
COPY src ./src
# Copy the compiled static frontend files into the Spring Boot resource directory
COPY --from=frontend-build /app/src/main/resources/static ./src/main/resources/static
RUN mvn clean package -DskipTests

# Stage 3: Minimal Production Image
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=backend-build /app/target/*.jar app.jar

# Explicitly set the active profile for production if needed
ENV SPRING_PROFILES_ACTIVE=prod

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
