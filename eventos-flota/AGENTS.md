# eventos-flota

## Responsabilidad Principal

Librería centralizada para la definición y generación de los esquemas de datos (Apache Avro) usados por la plataforma.

## Reglas Técnicas

- Propósito: generar automáticamente las clases Java a partir de archivos de esquema (`.avsc`).
- No es un servicio ejecutable; es una dependencia utilizada por los microservicios que publican/consumen mensajes en Kafka.
- NO agregar lógica de negocio aquí: solo contener esquemas y elementos de serialización.

## Uso

Importar la dependencia `eventos-flota` en los microservicios (p. ej. `Gestion`, `identidad`, `Seguimiento-Entrada-Telemetria`, `Seguimiento-Distribucion-Reactivo`) para garantizar contratos fuertemente tipados en Kafka.

## Dependencias Principales

| Dependencia | Versión | Propósito |
|---|---|---|
| `avro` | 1.11.3 | Librería central de Apache Avro para serialización/deserialización binaria de datos |
| `avro-maven-plugin` | 1.11.3 | Plugin Maven que genera clases Java automáticamente desde archivos `.avsc` en la fase `generate-sources` |

> **Configuración plugin:** Los esquemas se leen desde `src/main/resources/avro/` y las clases se generan en `src/main/java/`. `enableDecimalLogicalType=true` habilita soporte de tipo decimal de alta precisión.

## Consideraciones de Seguridad

- **Esquemas como contrato de datos (IMPLEMENTADO):** Cualquier cambio en los archivos `.avsc` es un cambio de contrato que afecta a todos los consumidores (Gestion, identidad, Seguimiento-Entrada-Telemetria, Seguimiento-Distribucion-Reactivo). Los cambios de esquema deben ser **retrocompatibles** (agregar campos opcionales con valores por defecto); nunca eliminar o renombrar campos en producción sin coordinación.
- **Schema Registry (IMPLEMENTADO):** Los esquemas Avro generados se registran en Confluent Schema Registry. El Schema Registry debe estar protegido y accesible solo desde la red interna; un acceso público podría exponer la estructura de datos del sistema.
- **Evolución de esquemas (IMPLEMENTADO):** Configurar la compatibilidad del Schema Registry en modo `BACKWARD` o `FULL` para evitar que un productor publique una versión de esquema que rompa a consumidores existentes.
- **No persistir datos sensibles en Avro sin cifrar (IMPLEMENTADO):** Si en el futuro los esquemas incluyen datos personales (ej. ubicación precisa + identificador de conductor), considerar cifrado a nivel de campo antes de serializar.
- **Generación de código en CI/CD:** Ejecutar `mvn generate-sources` en el pipeline de CI para garantizar que las clases generadas estén siempre sincronizadas con los esquemas; nunca subir al repositorio clases generadas manualmente.
