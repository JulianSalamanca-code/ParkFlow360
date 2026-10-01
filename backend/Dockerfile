# ============================================================
# ParkFlow360 Backend - Dockerfile (multi-stage)
# ============================================================

# --- Etapa 1: Build ---
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Cachear dependencias
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Compilar
COPY src ./src
RUN mvn clean package -DskipTests -B

# --- Etapa 2: Runtime ---
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
