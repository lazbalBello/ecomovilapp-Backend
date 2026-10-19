# Contexto General del Proyecto: Gestión y Seguimiento de flota de vehículos eléctricos espirituanos

## Objetivo del Negocio

Backend diseñado para el seguimiento de una flota de vehículos eléctricos y la gestión de su información, incluyendo visualización pública en tiempo real a tavés de una página web.

## Aclaraciones Críticas (Reglas de Oro para Agentes IA)

- Servicio Ingestor: `Ecomoviles-Ingestor-Reactivo` expone un servidor TCP, decodifica tramas (ej. JT808 de IRIS 807) con Kaitai Struct, aplica control de acceso no bloqueante en RAM (descartando GPS no autorizados sincronizados desde Kafka) y publica telemetría Avro en Kafka.
- Servicio Distribución Reactivo: `Seguimiento-Distribucion-Reactivo` Consume de Kafka la telemetría generada por el ingestor, aplica lógica de negocio y la distribuye hacia el broker mqtt (EMQX).
- Servicio de Gestión de inventario: `Gestion` Actua como una api REST para la gestión de la información de los vehículos, conductores, rutas y paradas, persiste la información en db-gestion y publica eventos de autorización de hardware GPS en el topic compactado `flota-dispositivos-autorizados`. Expone su API versionada bajo `/api/v1/**` (`/api/v1/publico/**` público, `/api/v1/conductores/mis-vehiculos` rol `driver`, `/api/v1/admin/**` rol `admin`); el detalle de rutas está en `Gestion/AGENTS.md`.
- Servicio de Autenticaión: `controlDeIdentidad` Actua como intermediario entre keycloak y los clientes web maneja las peticiones de registro, autenticación y gestión de usuarios 
- API-Gateway: `api-gateway` Gateway de los microservicio enruta las peticiones a su dirección correspondiente, valida tokens y aplica un RateLimiter para protección contra ataques de DoS. `GatewayConfig` enruta hacia `Gestion` los prefijos `/api/v1/admin/**`, `/api/v1/conductores/**` y `/api/v1/publico/**` (mantiene además `/admin/**` y `/public/**` por compatibilidad)
- Eureka-Server: `ServidorDeRegistroDeServicios` Levanta un servidor de Eureka para el descubriemiento y registro de los microservicios para comprobar estado y balancear la carga 
- Esquemas Compartidos: `eventos-flota` es una librería de esquemas Avro; TODOS los microservicios que usan Kafka deben importar sus clases generadas.
 

## Regla de Mantenimiento Obligatoria (Auto-Actualización):

Sincronización de Contexto: El agente debe actualizar este y cualquier archivo AGENTS.md tras cambios importantes en la arquitectura, dependencias o reglas de negocio para asegurar que la documentación técnica nunca quede obsoleta.

## Pila Tecnológica

- Lenguaje Java 21 
- Framework: Spring Boot 3.5 y 4.0 ->(Seguimiento-Entrada-Telemetria y Seguimiento-Distribucion-Reactivo ) 
- Arquitectura: Microservicios
- Infraestructura: 
     
    Edge Proxy (Traefik v3.0):

        Puertos: 80 (HTTP / Web), 8080 (Dashboard), 5001 (TCP Balanceado para hardware GPS).

        Rutas HTTP:
          - PathPrefix(`/`): Enruta hacia el `api-gateway` (puerto interno 8080). Todas las peticiones de los clientes frontend entran por el puerto 80/443 de Traefik.
          - Host(`auth.localhost`) / Subdominio: Enruta hacia `keycloak` (puerto interno 9090) para el flujo de pantallas web de Keycloak (ej. cambio de contraseñas por correo via Action Token).

    Keycloak (v26.2.4):

        Imagen: quay.io/keycloak/keycloak:26.2.4

        Puerto interno: 9090 (Configurado vía KC_HTTP_PORT). En producción se accede a través del subdominio en Traefik (ej. auth.ecomoviles.cu) sin exponer el puerto 9090 al exterior.

        Base de Datos: PostgreSQL en puerto 5433 (db_keycloak).

        Modo: start-dev.

        Gestión de Contraseñas por Correo: Los enlaces temporales enviados por email utilizan la URL pública configurada (`KC_HOSTNAME`).

    Kafka (v7.7.0 - Modo KRaft):

        Imagen: confluentinc/cp-kafka:7.7.0

        Arquitectura: Sin Zookeeper (usa broker,controller).

        Puertos: 9092 (interno), 9094 (externo/localhost).

        Administración: kafka-ui disponible en puerto 8085.

        Schema Registry: Puerto 8086. Esencial para la gestión de esquemas Avro.

    Bases de Datos (PostgreSQL 15.15):
    
        Gestión: Contenedor db-gestion en puerto 5431.
    
        Keycloak: Contenedor db-keycloak en puerto 5433.

    MQTT (EMQX v5.4.1):

        Puerto MQTT: 1883.

        WebSockets: 1884 (WS) y 1885 (WSS).

        Dashboard: Puerto 18083.

    Redis (v7-alpine):

        Puerto: 6379. Requiere contraseña (requirepass).

        Administración: redis-commander disponible en puerto 8087.

## Mapa de Microservicios (carpetas reales)

- `ServidorDeRegistroDeServicios`: Eureka Server
- `Ecomoviles-Ingestor-Reactivo`: Ingesta TCP, decodificación (Kaitai Struct / JT808) y publicación a Kafka
- `api-gateway`: Enrutamiento y validación de tokens
- `controlDeIdentidad`: Intermediario con Keycloak
- `Gestion`: API REST CRUD y gestión de entidades
- `Seguimiento-Entrada-Telemetria`: Ingesta MQTT a Kafka [DEPRECADO]
- `Seguimiento-Distribucion-Reactivo`: Distribución hacia el broker MQTT(Emqx)
- `Seguimiento-Distribucion-Telemetria`: [DEPRECADO]
- `eventos-flota`: Librería de esquemas Avro
