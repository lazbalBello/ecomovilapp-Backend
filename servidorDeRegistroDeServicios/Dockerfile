# syntax=docker/dockerfile:1.4
# Etapa 1: Build y dependencias
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder

WORKDIR /app

# ── Capa 1: POM padre (solo cambia si cambias versiones globales) ──────────────
COPY pom.xml ./pom.xml
RUN --mount=type=cache,target=/root/.m2 \
    mvn -N clean install

# ── Capa 2: Librería compartida eventos-flota ──────────────────────────────────
COPY eventos-flota/pom.xml ./eventos-flota/pom.xml
RUN --mount=type=cache,target=/root/.m2 \
    cd eventos-flota && mvn dependency:go-offline -q

COPY eventos-flota/src ./eventos-flota/src
RUN --mount=type=cache,target=/root/.m2 \
    cd eventos-flota && mvn clean install -DskipTests -q

# ── Capa 3: Microservicio ServidorDeRegistroDeServicios ───────────────────────
WORKDIR /app/service
COPY servidorDeRegistroDeServicios/pom.xml .
RUN --mount=type=cache,target=/root/.m2 \
    mvn dependency:go-offline -q

COPY servidorDeRegistroDeServicios/src ./src
RUN --mount=type=cache,target=/root/.m2 \
    mvn clean package -DskipTests -q

# ── Etapa 2: Runtime ligero ────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine

# Hardening: Crear usuario no-root
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

WORKDIR /app
COPY --from=builder /app/service/target/*.jar app.jar

# Variables de entorno JVM (sobreescribibles vía Compose)
ENV JAVA_OPTS="-Xms64m -Xmx128m"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
