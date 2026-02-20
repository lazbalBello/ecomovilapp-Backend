# Seguimiento-Distribucion-Telemetria (DEPRECADO)

> ESTADO: DEPRECADO ⚠️

## Resumen

Servicio de distribución de telemetría implementado de forma NO reactiva. Conservado solo por compatibilidad histórica.

## Reglas Técnicas (Críticas)

- Implementación no reactiva: no debe servir como referencia para nuevas implementaciones.
- INSTRUCCIÓN PARA AGENTES IA: NO sugerir modificaciones en este directorio ni usar su código como base para nuevas funcionalidades relacionadas con la distribución de telemetría.
- Todo el desarrollo actual de distribución de telemetría debe realizarse exclusivamente en `Seguimiento-Distribucion-Reactivo`.

## Dependencias Principales

> ⚠️ Información registrada solo con fines históricos. No usar este servicio como base de nuevas implementaciones.

| Dependencia | Versión | Propósito |
|---|---|---|
| `spring-boot-starter-webmvc` | (Spring Boot 4.0.0) | Servidor web MVC imperativo (bloqueante) — razón principal de su deprecación |
| `spring-boot-starter-websocket` | (Spring Boot 4.0.0) | WebSocket imperativo (STOMP), reemplazado por WebFlux reactivo en `Seguimiento-Distribucion-Reactivo` |
| `spring-boot-starter-kafka` | (Spring Boot 4.0.0) | Consumidor Kafka imperativo (bloqueante) |
| `spring-boot-starter-data-redis` | (Spring Boot 4.0.0) | Acceso Redis imperativo (bloqueante) |
| `spring-boot-starter-cache` | (Spring Boot 4.0.0) | Abstracción de caché de Spring |
| `spring-boot-starter-security` | (Spring Boot 4.0.0) | Seguridad base |
| `spring-boot-starter-oauth2-resource-server` | (Spring Boot 4.0.0) | Validación de tokens JWT |
| `kafka-avro-serializer` | 7.7.0 | Serialización Avro con Schema Registry de Confluent |
| `eventos-flota` | 1.0.0-SNAPSHOT | Librería interna con esquemas Avro compartidos |
| `spring-cloud-starter-netflix-eureka-client` | 5.0.0 | Registro en Eureka |
| `mapstruct` | 1.6.3 | Mapeo entre objetos (generado en compilación) |
| `jackson-databind` + `jackson-datatype-jsr310` | (Spring Boot 4.0.0) | Serialización JSON |
| `lombok` | (Spring Boot 4.0.0) | Reducción de boilerplate (solo compilación) |

## Consideraciones de Seguridad

> ⚠️ Este servicio está DEPRECADO. Las siguientes notas son solo para referencia histórica.

- **No desplegarlo en producción:** Su arquitectura bloqueante no es apta para el volumen de telemetría esperado y puede generar cuellos de botella y vulnerabilidades DoS por agotamiento de threads.
- **Credenciales sin gestión activa:** Al estar deprecado, sus configuraciones de seguridad (Redis, Kafka, JWT) pueden quedar desactualizadas. Si se mantiene en algún entorno, revisar que las credenciales sigan siendo válidas y rotarlas.
- **Referencia para migración:** Ante cualquier duda sobre diferencias de implementación, comparar con `Seguimiento-Distribucion-Reactivo` que es el servicio oficial activo.
