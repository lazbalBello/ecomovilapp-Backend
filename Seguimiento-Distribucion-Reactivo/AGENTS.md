# Seguimiento-Distribucion-Reactivo

## Responsabilidad Principal

Consumir telemetría desde Kafka, aplicar filtros de negocio, persistir estados y distribuir ubicaciones a clientes web mediante WebSockets de forma completamente reactiva.

## Reglas Técnicas (Críticas)

- Paradigma: Totalmente reactivo usando Spring WebFlux. NO se permiten operaciones bloqueantes.
- Filtros de negocio:
  - Validar movimiento significativo.
  - Límite de actualización (throttle): máximo 1 actualización cada 2 segundos por vehículo.
- Caché: almacenar estado y última ubicación de vehículos en Redis para lecturas de baja latencia (usar Redis reactivo).
- WebSockets: emitir ubicaciones públicamente (sin autenticación) mediante configuración reactiva.
- Contratos: deserializar eventos de Kafka usando las clases generadas por `eventos-flota`.

## Dependencias Principales

| Dependencia | Versión | Propósito |
|---|---|---|
| `spring-boot-starter-webflux` | (Spring Boot 4.0.0) | Stack reactivo (Reactor/Netty) para endpoints no bloqueantes y WebSocket reactivos |
| `reactor-kafka` | 1.3.25 | Consumidor Kafka reactivo (Project Reactor); integra Kafka en el modelo reactivo sin bloqueo |
| `kafka-avro-serializer` | 7.7.0 | Deserialización Avro de mensajes Kafka con Schema Registry de Confluent |
| `eventos-flota` | 1.0.0-SNAPSHOT | Librería interna con esquemas Avro compartidos (contratos de mensajes) |
| `spring-boot-starter-data-redis-reactive` | (Spring Boot 4.0.0) | Acceso reactivo a Redis para almacenar estado y última ubicación de vehículos con baja latencia |
| `spring-boot-starter-security-oauth2-resource-server` | (Spring Boot 4.0.0) | Valida tokens JWT de Keycloak de forma reactiva (WebFlux Security) |
| `spring-cloud-starter-netflix-eureka-client` | 2025.1.0 (BOM) | Registro del servicio en Eureka para descubrimiento |
| `spring-boot-starter-validation` | (Spring Boot 4.0.0) | Validación reactiva de datos entrantes |
| `mapstruct` | 1.6.3 | Mapeo entre objetos Avro y DTOs de dominio (generado en compilación) |
| `lombok` | (Spring Boot 4.0.0) | Reducción de boilerplate (solo compilación) |

> **Framework base:** Spring Boot `4.0.0`. Spring Cloud `2025.1.0`.

## Consideraciones de Seguridad

- **WebSocket público (IMPLEMENTADO):** Según las reglas técnicas, los WebSockets emiten ubicaciones **sin autenticación** (visualización pública). Solo datos de ubicación no sensibles deben enviarse por este canal; nunca incluir IDs de conductor, datos personales o tokens en los mensajes WebSocket públicos.
- **Resource Server reactivo (IMPLEMENTADO):** Configurar correctamente `spring-security-oauth2-resource-server` para WebFlux; el paradigma reactivo tiene diferencias con el imperativo en la cadena de filtros de seguridad de Spring.
- **Redis con contraseña (IMPLEMENTADO):** Este servicio accede a Redis mediante `spring-boot-starter-data-redis-reactive`. Verificar que la conexión use la contraseña (`requirepass`) configurada en el docker-compose; nunca conectarse a Redis sin autenticación en producción.
- **Kafka consumer reactivo (IMPLEMENTADO, SSL/TLS PENDIENTES):** Asegurar que `reactor-kafka` esté configurado con SSL/TLS en producción para cifrar la comunicación con el broker. Configurar correctamente el `group-id` del consumer para evitar colisiones.
- **Sin operaciones bloqueantes (IMPLEMENTADO, Necesita revisión):** No introducir llamadas bloqueantes (`Thread.sleep`, JDBC síncrono, etc.) en el código; esto puede colapsar el event loop de Netty y crear un vector de DoS inadvertido.
- **Throttle como protección (IMPLEMENTADO):** El límite de 1 actualización cada 2 segundos por vehículo también actúa como protección ante floods de datos; mantener esta regla y evaluar reducir el límite si el tráfico crece.
- **Schema Registry (IMPLEMENTADO):** Asegurar que el Schema Registry sea accesible únicamente desde la red interna; un Schema Registry público podría revelar la estructura de datos del sistema.
