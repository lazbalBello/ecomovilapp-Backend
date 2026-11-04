# identidad

## Responsabilidad Principal

Intermediario entre el frontend y Keycloak para autenticación y gestión de usuarios.

## Reglas Técnicas

- Flujo de login: recibir credenciales desde el frontend, delegar en Keycloak y devolver tokens al cliente.
- Registro y Saga: al crear un conductor, registrar el usuario en Keycloak y publicar un evento en Kafka; escuchar la respuesta de `Gestion` y, si falla el registro en BD, ejecutar rollback borrando el usuario en Keycloak.
- Contratos: publicar eventos de creación de usuarios en Kafka usando los esquemas de `eventos-flota`.

## Endpoints (estándar /api/v1)

- `/api/v1/auth/login` (POST, público), `/api/v1/auth/refrescar` (POST, público), `/api/v1/auth/guest-token` (GET, público), `/api/v1/auth/recuperar-password` (POST, público, anti-enumeración; dispara `executeActionsEmail(["UPDATE_PASSWORD"])` en Keycloak).
- `/api/v1/usuarios` (POST, público + API key de usuario en gateway), `/api/v1/usuarios/admin` (POST, rol admin + API key admin), `/api/v1/usuarios/conductores` (POST, rol admin + API key admin → saga).
- `/api/v1/usuarios/me` (GET: obtiene perfil con roles; PATCH: edita nombre/apellido; DELETE: auto-eliminación permitida solo para rol `user`; bloqueada para `admin` y `driver`).
- `/api/v1/usuarios/me/password` (PUT: cambio de contraseña validando primero la clave actual vía `TokenClient.login`).
- `/api/v1/admin/usuarios` (GET, rol admin; devuelve proyección `{id, nombreUsuario, email, habilitado}`), `/api/v1/admin/usuarios/{id}` (PUT: edición de nombre, apellido, email, enabled; DELETE: rol admin).
- Seguridad en `SecurityConfig`: permitAll en login/refrescar/guest-token/recuperar-password y el POST de `/api/v1/usuarios`; `/api/v1/usuarios/me/**` autenticado con Bearer JWT; resto del árbol administrativo (`/api/v1/admin/usuarios/**`, creación admin/conductor) requiere rol `admin`.
- Las rutas antiguas (`/auth/registrar-*`, `/usuarios/listartodo`, `/usuarios/eliminar/{id}`, `/telemetria/...`) **ya no existen**: el gateway no las enruta. Documentación completa en `docs/NewRestDoc.md` sección 6.

## Arquitectura interna (post-refactor)

- **Login unificado vía token endpoint:** `ServicioUsuarios.login` delega en `TokenClient.login(username, password)` (`WebClient`, `grant_type=password` contra `/realms/{realm}/protocol/openid-connect/token`). NO usar Direct Access Grants del admin-client. Mensajes de error de `AuthenticationFailedException`: 401 → "Credenciales inválidas: usuario o contraseña incorrectos"; conexión caída → "Imposible conectar con el servidor de identidad". `TokenClient.mapearToken(tr, mensajePorDefecto)` centraliza el null-check (no usar `assert`; son no-ops en producción).
- **Sin pool de ResteasyClients:** `PooledKeycloak`, `RestClientPoolConfig` y `ResteasyClientFactory` fueron eliminados. `KeycloakProvider` construye un único `Keycloak` admin-client en `@PostConstruct` reutilizando un ResteasyClient compartido.
- **Caché de roles de Keycloak:** `KeycloakProvider.obtenerRol(nombreRol)` usa `ConcurrentHashMap` + `computeIfAbsent`; elimina llamadas redundantes a la API admin en cada registro.
- **Sin interfaz de servicio:** `ServicioUsuarios` es clase concreta (se eliminó `Utils/IKeycloakService.java`). Helpers privados: `registrarUsuario` (crea usuario, valida 201/409/otro → `UserAlreadyExistsException` / `UserCreationException`), `construirUserRepresentation` (credenciales embebidas no-temporales: elimina la llamada `resetPassword`, 1 solo HTTP call), `extraerUserId`, `asignarRolRealm`, `publicarRegistroConductor` (publish-after-commit del evento `RegistroConductorIniciado` al topic `inicio-registro-conductor` con `adminId` de `SecurityContextHolder`).
- **DTOs como records:** `ConductorUsuarioDto` es record; compact constructor con default `Boolean disponibilidad` null → false. Sin `Serializable`.
- **Errores REST normalizados:** `GlobalExceptionHandler` responde solo con campo `error` (+ `fieldErrors` en validación 400), igual que `Gestion`. Ya no se exponen `mensage`/`message`/`status`. Las dos excepciones de autenticación están mapeadas: `AuthenticationException` (Spring Security) → 401 texto fijo `"Autenticación fallida"`; `AuthenticationFailedException` (custom, de `TokenClient`) → 401 con el mensaje específico (ej. `"Credenciales inválidas: usuario o contraseña incorrectos"`). Si se añaden excepciones custom nuevas, mapearlas SIEMPRE en `GlobalExceptionHandler` — no caer al handler genérico de 500.
- **Tests:** `ServicioUsuariosTest`, `KeycloakProviderTest`, `TokenClientTest` en `src/test/java` (Mockito puro / JDK HttpServer para el token endpoint). Ejecutar: `.\mvnw.cmd -q "-Dtest=ServicioUsuariosTest,KeycloakProviderTest,TokenClientTest" test`.

## Dependencias Principales

| Dependencia | Versión | Propósito |
|---|---|---|
| `spring-boot-starter-web` | (BOM padre) | Exposición de endpoints REST para login y registro de conductores |
| `spring-boot-starter-webflux` | (BOM padre) | Cliente HTTP reactivo para comunicación con Keycloak y otros servicios |
| `spring-boot-starter-security` | (BOM padre) | Seguridad base; protege endpoints propios del servicio |
| `spring-boot-starter-oauth2-resource-server` | (BOM padre) | Valida tokens JWT de Keycloak en peticiones entrantes |
| `spring-security-oauth2-jose` | 6.5.0 | Procesamiento de JWT (firma, verificación, parsing de claims) |
| `keycloak-admin-client` | 26.0.5 | Interacción directa con la API de administración de Keycloak (crear/eliminar usuarios, rollback) |
| `resteasy-core-spi` | 6.2.12.Final | SPI requerido por el cliente de administración de Keycloak (dependencia transitiva) |
| `spring-kafka` | (BOM padre) | Publicación y consumo de eventos de creación de usuarios en Kafka (patrón Saga) |
| `kafka-avro-serializer` | 7.7.0 | Serialización Avro de mensajes Kafka con Schema Registry de Confluent |
| `eventos-flota` | 1.0.0-SNAPSHOT | Librería interna con esquemas Avro compartidos (contratos de mensajes) |
| `spring-cloud-starter-netflix-eureka-client` | 2025.0.0 (BOM) | Registro del servicio en Eureka para descubrimiento |
| `spring-cloud-starter-openfeign` | 2025.0.0 (BOM) | Cliente HTTP declarativo para llamadas sincrónicas entre servicios |
| `spring-boot-starter-validation` | (BOM padre) | Validación de DTOs de entrada (Bean Validation / JSR-380) |
| `spring-boot-starter-actuator` | (BOM padre) | Endpoints de salud y monitoreo |
| `commons-pool2` | 2.12.1 | Sin usos actuales (quedó del pool eliminado); candidato a retirar del pom |
| `lombok` | (BOM padre) | Reducción de boilerplate (solo compilación) |

> **Spring Cloud Version:** `2025.0.0` — gestionada mediante BOM en `dependencyManagement`.

## Consideraciones de Seguridad

- **Credenciales de Keycloak Admin (IMPLEMENTADO):** El `keycloak-admin-client` requiere credenciales de administrador de Keycloak (`client-secret`, `admin user/password`). Estas **nunca** deben estar en el código fuente; usar variables de entorno o un gestor de secretos (ej. HashiCorp Vault, Docker Secrets).
- **Manejo de tokens en tránsito:** Este servicio recibe credenciales del frontend y las delega a Keycloak. Toda comunicación debe ser por HTTPS para evitar interceptación de contraseñas en texto plano.
- **Rollback seguro (IMPLEMENTADO):** El mecanismo de Saga (registro en Keycloak + evento Kafka → respuesta de Gestión → rollback si falla) debe garantizar que un rollback fallido no deje usuarios huérfanos en Keycloak. Implementar logs de auditoría para estos casos.
- **Scope de permisos Keycloak:** El cliente de administración debe configurarse con el mínimo de permisos necesarios en Keycloak (principio de mínimo privilegio); evitar usar credenciales de `admin` global.
- **Validación de payload (IMPLEMENTADO):** Usar `spring-boot-starter-validation` para rechazar peticiones malformadas antes de interactuar con Keycloak o publicar en Kafka.
- **Rate limiting en endpoints de login (IMPLEMENTADO):** Los endpoints de autenticación son objetivos de ataques de fuerza bruta; aplicar limitación de intentos (via api-gateway o localmente).
- **Tokens en logs (IMPLEMENTADO):** Asegurarse de que los tokens JWT y contraseñas nunca se registren en los logs de la aplicación.
- **Autocuidado y Cambio de Contraseña (IMPLEMENTADO):** `/api/v1/usuarios/me/password` exige validar la contraseña actual mediante autenticación OIDC antes de resetear la credencial.
- **Protección Anti-Enumeración en Recuperación (IMPLEMENTADO):** `/api/v1/auth/recuperar-password` responde siempre 200 OK con mensaje genérico neutral, impidiendo determinar si un email existe o no en Keycloak.
- **Restricción de Auto-eliminación (IMPLEMENTADO):** `/api/v1/usuarios/me` (`DELETE`) bloquea la auto-eliminación para cuentas `admin` (evitar lockout total) y `driver` (coordinación con Gestión).
