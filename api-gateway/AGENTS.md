# api-gateway

## Responsabilidad Principal

Punto de entrada único para el sistema: enrutamiento, balanceo y validación perimetral de seguridad.

## Reglas Técnicas

- Enrutamiento: utilizar Spring Cloud Gateway para dirigir peticiones a `Gestion`, `identidad` o `Seguimiento-Distribucion-Reactivo`.
- Orden de rutas (crítico): las rutas de `identidad` se declaran **antes** que la ruta genérica de `Gestion`, para que `/api/v1/admin/usuarios/**` (identidad) no sea capturada por `/api/v1/admin/**` (Gestion) — primera coincidencia gana.
- Mapa de rutas de identidad: `/api/v1/auth/**` (login/refrescar/guest-token/recuperar-password), `/api/v1/usuarios` (POST + API key de usuario), `/api/v1/usuarios/admin` (POST + API key admin), `/api/v1/usuarios/conductores` (POST + API key admin), `/api/v1/usuarios/me/**` (perfil y cambio de contraseña propio), `/api/v1/admin/usuarios/**` (rol admin). Distribución: `/api/v1/telemetria/**`. NO usar los prefijos antiguos `/auth/**`, `/admin/**` ni `/telemetria/v1/**`.
- Manejo de Errores Global en Gateway: `GlobalErrorWebExceptionHandler` (`@Order(-2)`) estandariza todas las respuestas de error generadas por el propio Gateway (como 404 por ruta inexistente, 429 por rate limit o 503 por microservicio no disponible) con el formato unificado del sistema: `{ timestamp, estado, error, path }`.
- Seguridad perimetral: `permitAll` para `/api/v1/publico/**`, `/api/v1/auth/**` y `/api/v1/usuarios/**`; el resto exige JWT. Los roles finos se validan en cada microservicio.
- Descubrimiento: integrado con `ServidorDeRegistroDeServicios` (Eureka) para localizar instancias.
- Seguridad: validar tokens JWT en el borde antes de enrutar peticiones protegidas.

## Dependencias Principales

| Dependencia | Versión | Propósito |
|---|---|---|
| `spring-cloud-starter-gateway-server-webflux` | 2025.0.0 (BOM) | Motor de enrutamiento reactivo (WebFlux) que actúa como proxy inverso inteligente |
| `spring-cloud-starter-netflix-eureka-client` | 2025.0.0 (BOM) | Descubrimiento de servicios para enrutar dinámicamente a instancias registradas |
| `spring-boot-starter-security` | 3.4.2 | Framework de seguridad base; habilita filtros de autenticación/autorización |
| `spring-boot-starter-oauth2-client` | (BOM padre) | Permite actuar como cliente OAuth2 (flujo de login delegado a Keycloak) |
| `spring-security-oauth2-resource-server` | (BOM padre) | Validación de tokens JWT/Opaque emitidos por Keycloak en el borde de la red |
| `spring-boot-starter-data-redis-reactive` | (BOM padre) | Conexión reactiva a Redis; requerida por el `RedisRateLimiter` (rate limiting por IP con Token Bucket) |
| `spring-boot-starter-actuator` | (BOM padre) | Endpoints de salud y métricas del gateway |
| `reactor-test` | (BOM padre) | Utilidades de prueba para el stack reactivo (solo test) |
| `lombok` | (BOM padre) | Reducción de boilerplate (solo compilación) |

> **Versión Spring Cloud:** Heredada del POM padre del proyecto (`2025.0.0` BOM).

## Consideraciones de Seguridad

- **Único punto de entrada (IMPLEMENTADO):** Este servicio es la primera línea de defensa; toda validación de tokens JWT debe ocurrir aquí, **antes** de enrutar la petición a servicios internos. Nunca permitir que los microservicios internos sean accesibles directamente desde internet.
- **Validación JWT (IMPLEMENTADO):** El `spring-security-oauth2-resource-server` debe configurarse con la `jwks-uri` de Keycloak para verificar firmas de tokens. Mantener actualizada la URL del JWKS endpoint.
- **CORS:** Configurar políticas CORS restrictivas; solo permitir orígenes del frontend oficial en producción. Nunca usar `allowedOrigins("*")` en entorno productivo.
- **Rate Limiting (IMPLEMENTADO):** Se usa el filtro `RequestRateLimiter` de Spring Cloud Gateway con `RedisRateLimiter` (algoritmo Token Bucket). `RateLimiterConfig.java` define la clave por IP y dos perfiles: `authRateLimiter` (5 req/s, pico 10) para login/registro y `defaultRateLimiter` (10 req/s, pico 20) para las demás rutas. Usa la misma instancia de Redis del docker-compose (puerto 6379).
- **HTTPS obligatorio:** Configurar TLS en el gateway; redirigir HTTP a HTTPS. Los certificados deben renovarse antes de su vencimiento.
- **Filtros de cabeceras:** Eliminar o sanitizar cabeceras sensibles entrantes (ej. `X-User-Id`, `X-Internal-*`) para prevenir header injection desde clientes externos.
- **Actuator restringido:** Limitar la exposición de endpoints de `actuator` a la red interna; nunca exponer `/actuator` públicamente.

