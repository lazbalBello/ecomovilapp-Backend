# Seguimiento-Entrada-Telemetria

## Responsabilidad Principal

Consumir datos en tiempo real provenientes de vehículos vía MQTT (EMQX), validar payloads y publicar mensajes en Kafka.

## Reglas Técnicas

- Flujo de datos: escucha en MQTT y publica en Kafka.
- Validación: verificar que el payload de telemetría JSON sea correcto antes de enviar a Kafka.
- Carga ligera: este servicio actúa como pasamanos/validador rápido; evitar lógica de negocio compleja.
- Contratos: serializar mensajes usando las clases Avro generadas por `eventos-flota`.

## Dependencias Principales

| Dependencia | Versión | Propósito |
|---|---|---|
| `spring-boot-starter-integration` | (Spring Boot 4.0.0) | Framework de integración empresarial; orquesta el pipeline MQTT → Kafka |
| `spring-integration-mqtt` | (Spring Boot 4.0.0) | Adaptador MQTT para recibir mensajes del broker EMQX |
| `spring-integration-kafka` | (Spring Boot 4.0.0) | Adaptador Kafka en el framework de integración para publicar en Kafka |
| `spring-integration-http` | (Spring Boot 4.0.0) | Adaptador HTTP en el framework de integración |
| `org.eclipse.paho.client.mqttv3` | 1.2.5 | Cliente MQTT v3 de Eclipse Paho (conexión real al broker EMQX) |
| `spring-boot-starter-kafka` | (Spring Boot 4.0.0) | Abstracción de Kafka para publicación de telemetría procesada |
| `kafka-avro-serializer` | 7.7.0 | Serialización Avro de mensajes Kafka con Schema Registry de Confluent |
| `eventos-flota` | 1.0.0-SNAPSHOT | Librería interna con esquemas Avro compartidos (contratos de mensajes) |
| `spring-cloud-starter-netflix-eureka-client` | 5.0.0 | Registro del servicio en Eureka para descubrimiento |
| `spring-boot-starter-webmvc` | (Spring Boot 4.0.0) | Servidor web MVC para endpoints de gestión/estado |
| `jackson-databind` | (Spring Boot 4.0.0) | Parsing y validación de payloads JSON de telemetría |
| `jackson-datatype-jsr310` | (Spring Boot 4.0.0) | Soporte de tipos de fecha/hora Java 8+ en JSON |
| `spring-boot-starter-actuator` | (Spring Boot 4.0.0) | Endpoints de salud y métricas |
| `lombok` | (Spring Boot 4.0.0) | Reducción de boilerplate (solo compilación) |

> **Framework base:** Spring Boot `4.0.0` (independiente del POM padre multi-módulo). Spring Cloud Eureka Client `5.0.0`.

## Consideraciones de Seguridad

- **Autenticación MQTT (IMPLEMENTADO):** Configurar el cliente MQTT (`org.eclipse.paho`) con usuario y contraseña para conectarse a EMQX; no usar conexiones anónimas en producción. Las credenciales deben inyectarse como variables de entorno.
- **TLS en MQTT (PENDIENTE):** En producción, usar `ssl://` en lugar de `tcp://` para cifrar la comunicación entre los vehículos/dispositivos IoT y el broker EMQX (puerto 8883). Verificar certificados del broker.
- **Validación de payload (IMPLEMENTADO):** Antes de publicar en Kafka, **siempre** validar el JSON de telemetría: verificar campos obligatorios, rangos de valores y formato. Un payload malformado o malicioso no debe alcanzar Kafka.
- **Autorización por tópico MQTT (IMPLEMENTADO):** Configurar en EMQX reglas de ACL (Access Control List) para que solo los dispositivos autorizados puedan publicar en los tópicos de telemetría. Evitar tópicos tipo wildcards (`#`) sin restricciones.
- **Sin autenticación de red interna:** Este servicio no expone endpoints REST críticos, pero si se añaden, deben protegerse; por defecto, está en red interna detrás del api-gateway.
- **Kafka producer:** Configurar SSL/TLS para la comunicación con el broker de Kafka en producción. Asegurarse de que el Schema Registry también sea accesible solo desde la red interna.
- **Datos de telemetría (IMPLEMENTADO):** Los datos de ubicación de vehículos son sensibles; evitar que queden persistidos en logs sin ofuscación.
