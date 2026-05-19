# Etapa 1: Build y dependencias
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder

WORKDIR /app

# Copiamos el POM padre del multi-módulo y lo instalamos
COPY pom.xml ./pom.xml
RUN mvn -N clean install

# 1. Copiamos e instalamos la librería compartida 'eventos-flota'
COPY eventos-flota ./eventos-flota
RUN cd eventos-flota && mvn clean install -DskipTests

# 2. Copiamos y compilamos el microservicio
WORKDIR /app/service
COPY Gestion/pom.xml .
COPY Gestion/src ./src

RUN mvn clean package -DskipTests

# Etapa 2: Runtime ligero
FROM eclipse-temurin:21-jre-alpine

# Hardening: Crear usuario no-root
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

WORKDIR /app
COPY --from=builder /app/service/target/*.jar app.jar

# Variables de entorno JVM (sobreescribibles vía Compose)
ENV JAVA_OPTS="-Xms128m -Xmx256m"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS --enable-preview -jar app.jar"]
