# Gestion

## Responsabilidad Principal

API REST que actúa como servidor de recursos para operaciones CRUD sobre vehículos, conductores, paradas y rutas, y para gestionar asignaciones.

## Reglas Técnicas y Arquitectura

- Seguridad: funciona como Resource Server, validando tokens emitidos por Keycloak.
- Integración Kafka (orquestación tipo Saga): escucha eventos de creación de usuarios desde `identidad`, registra el conductor en PostgreSQL y publica eventos de éxito/fallo para coordinar rollback si corresponde.
- Contratos: usar las clases generadas por `eventos-flota` para publicar/consumir mensajes en Kafka.
- Manejo de errores: centralizado mediante `GlobalExceptionHandler`.

## Dependencias Principales

| Dependencia | Versión | Propósito |
|---|---|---|
| `spring-boot-starter-web` | (BOM padre) | Exposición de API REST CRUD para vehículos, conductores, paradas y rutas |
| `spring-boot-starter-data-jpa` | (BOM padre) | ORM con Hibernate para persistencia de entidades en PostgreSQL |
| `postgresql` | (BOM padre) | Driver JDBC del motor de base de datos PostgreSQL (scope runtime) |
| `spring-boot-starter-security` | 3.4.2 | Framework de seguridad base; habilita protección de endpoints |
| `spring-boot-starter-oauth2-resource-server` | 3.5.0 | Valida tokens JWT de Keycloak; este servicio actúa como Resource Server protegido |
| `spring-security-oauth2-jose` | 6.5.0 | Procesamiento de JWT (validación de firma y claims mediante JWKS) |
| `spring-kafka` | (BOM padre) | Consumo de eventos de creación de conductor desde `identidad` y publicación de respuesta Saga |
| `kafka-avro-serializer` | 7.7.0 | Serialización/deserialización Avro con Schema Registry de Confluent |
| `eventos-flota` | 1.0.0-SNAPSHOT | Librería interna con esquemas Avro compartidos (contratos de mensajes Kafka) |
| `spring-cloud-starter-netflix-eureka-client` | 2025.0.0 (BOM) | Registro del servicio en Eureka para descubrimiento |
| `spring-boot-starter-websocket` | 3.4.3 | Soporte de WebSocket (STOMP) para notificaciones en tiempo real |
| `spring-boot-starter-validation` | (BOM padre) | Validación de DTOs de entrada (Bean Validation / JSR-380) |
| `spring-boot-starter-actuator` | (BOM padre) | Endpoints de salud y métricas |
| `mapstruct` | 1.6.3 | Mapeo entre entidades JPA y DTOs (generado en compilación) |
| `jackson-datatype-jsr310` | 2.18.3 | Soporte de tipos de fecha/hora Java 8+ en serialización JSON |
| `lombok` | (BOM padre) | Reducción de boilerplate (solo compilación) |

> **Nota build:** Usa `--enable-preview` en el compilador (Java 21 preview features activas).

## Consideraciones de Seguridad

- **Resource Server (IMPLEMENTADO):** Toda petición debe portar un JWT válido emitido por Keycloak. Configurar `spring.security.oauth2.resourceserver.jwt.jwk-set-uri` apuntando a Keycloak. Nunca deshabilitar la verificación de tokens.
- **Autorización por roles (IMPLEMENTADO):** Implementar control de acceso basado en roles (RBAC) con los claims del JWT (ej. `ROLE_ADMIN`, `ROLE_CONDUCTOR`); no confiar en cabeceras de identidad que no vengan del token.
- **Inyección SQL (IMPLEMENTADO):** Usar siempre consultas parametrizadas o JPQL de Spring Data JPA; nunca construir queries con concatenación de strings.
- **Credenciales de PostgreSQL (IMPLEMENTADO):** Las credenciales de la base de datos (`spring.datasource.username`, `spring.datasource.password`) deben inyectarse como variables de entorno o secretos; no deben estar en `application.yml` en texto plano.
- **Kafka consumidor (IMPLEMENTADO):** Validar los mensajes entrantes de Kafka antes de persistirlos en BD; un mensaje malformado no debe romper el flujo de la Saga ni dejar datos inconsistentes.
- **WebSocket:** Si los WebSockets exponen datos sensibles, asegurar que la configuración STOMP requiere autenticación; verificar el token en el handshake inicial.
- **Actuator:** Restringir endpoints de `actuator` a la red interna (no exponerlos públicamente) y protegerlos con autenticación si es necesario.
- **Preview features:** El uso de `--enable-preview` implica dependencia de features no estables de Java que pueden cambiar; evaluar su necesidad antes de pasar a producción.
