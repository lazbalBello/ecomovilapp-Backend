# Contexto General del Microservicio: Ecomoviles-Ingestor-Reactivo

## Objetivo del Microservicio

Este servicio reactivo (Spring Boot) está dedicado a la ingesta directa y decodificación de tramas de dispositivos GPS (específicamente IRIS 807 usando el protocolo JT808, extensible a otras normativas). Expone un servidor TCP para recibir las conexiones entrantes de los dispositivos de la flota. Su función es traducir los datos binarios usando clases generadas por **Kaitai Struct**, realizar validaciones iniciales y publicar la telemetría resultante en Kafka en el formato unificado `Avro`.

## Aclaraciones Críticas (Reglas de Oro para Agentes IA)

- **Responsabilidad Única:** Este servicio SOLO debe realizar la recepción TCP, decodificar, validar formato y producir a Kafka. No debe guardar nada en bases relacionales ni distribuir la data para visualización.
- **Protocolo y Decodificación:** Actualmente utiliza el estándar JT808. La decodificación en Java se apoya en clases generadas a través del compilador de Kaitai Struct (`.ksy`).
- **Comunicación de Salida:** Publica la información validada en Kafka, requiriendo los esquemas base ubicados en la librería compartida de la organización (`eventos-flota`).

## Regla de Mantenimiento Obligatoria

Cualquier nuevo protocolo añadido o modificación en el árbol de parsing binario debe quedar estipulada en este documento. 

## Pila Tecnológica

- **Lenguaje:** Java 21
- **Framework:** Spring Boot WebFlux / Reactor Netty (TCP Server)
- **Herramienta de Decodificación:** Kaitai Struct
- **Interoperabilidad de Datos:** Apache Kafka (Productores Avro)

## Relación con otros servicios del Ecosistema

- **Seguimiento-Distribucion-Reactivo:** Una vez que este ingestor decodifica la trama TCP y transfiere la entidad hacia Kafka, `Seguimiento-Distribucion-Reactivo` asume el rol de consumo de ese topic, le aplica filtros avanzados (throttling, reglas de distancia en Redis) y encamina los eventos finales al entorno MQTT (EMQX) para las interfaces web.
