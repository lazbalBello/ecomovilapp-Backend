# Contexto General del Proyecto: Gestión y Seguimiento de flota de vehículos eléctricos espirituanos

## Objetivo del Negocio

Backend diseñado para el seguimiento de una flota de vehículos eléctricos y la gestión de su información, incluyendo visualización pública en tiempo real a tavés de una página web.

## Aclaraciones Críticas (Reglas de Oro para Agentes IA)

- Servicio Ingestor: `Ecomoviles-Ingestor-Reactivo` expone un servidor TCP, decodifica tramas (ej. JT808 de IRIS 807) con Kaitai Struct y publica telemetría Avro en Kafka.
- Servicio Distribución Reactivo: `Seguimiento-Distribucion-Reactivo` AHORA consume de Kafka la telemetría generada por el ingestor, aplica lógica de negocio y la distribuye hacia el broker mqtt (EMQX).
- Servicio Deprecado: `Seguimiento-Distribucion-Telemetria` y la integración con `Traccar` están DEPRECADOS.
- Esquemas Compartidos: `eventos-flota` es una librería de esquemas Avro; TODOS los microservicios que usan Kafka deben importar sus clases generadas.
 

## Regla de Mantenimiento Obligatoria (Auto-Actualización):

Sincronización de Contexto: El agente debe actualizar este y cualquier archivo AGENTS.md tras cambios importantes en la arquitectura, dependencias o reglas de negocio para asegurar que la documentación técnica nunca quede obsoleta.

## Pila Tecnológica

- Lenguaje Java 21 
- Framework: Spring Boot 3.5 y 4.0 ->(Seguimiento-Entrada-Telemetria y Seguimiento-Distribucion-Reactivo ) 
- Arquitectura: Microservicios
- Infraestructura: 
     
    Keycloak (v26.2.4):

        Imagen: quay.io/keycloak/keycloak:26.2.4

        Puerto: 9090 (Configurado vía KC_HTTP_PORT).

        Base de Datos: PostgreSQL en puerto 5433 (db_keycloak).

        Modo: start-dev.

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
