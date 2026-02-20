# ServidorDeRegistroDeServicios

## Responsabilidad Principal

Servidor central de registro y descubrimiento de microservicios basado en Netflix Eureka.

## Reglas Técnicas

- No contiene lógica de negocio.
- Requiere únicamente la configuración mínima para funcionar como servidor Eureka.
- Configurar `@EnableEurekaServer` en la clase principal y parámetros de zona/puerto en `application.yml`.

## Dependencias Principales

| Dependencia | Versión | Propósito |
|---|---|---|
| `spring-cloud-starter-netflix-eureka-server` | 2025.0.0 (BOM) | Núcleo del servidor de registro y descubrimiento Eureka |
| `spring-boot-starter-actuator` | (BOM padre) | Endpoints de salud y métricas (`/actuator/health`) |
| `spring-boot-devtools` | (BOM padre) | Recarga en caliente durante desarrollo (runtime, excluir en producción) |
| `lombok` | (BOM padre) | Reducción de boilerplate en código Java (solo compilación) |

> **Versión Spring Cloud:** `2025.0.0` — gestionada mediante BOM en `dependencyManagement`.

## Consideraciones de Seguridad

- **Sin autenticación activa (PENDIENTE):** La dependencia `spring-boot-starter-security` está comentada en el `pom.xml`. En producción, se **debe** habilitar y proteger la consola de Eureka con usuario/contraseña o restricción de red para evitar que servicios no autorizados se registren.
- **Exposición de topología:** La consola web de Eureka expone las IPs y puertos de todos los microservicios registrados. Debe restringirse su acceso a la red interna (no exponer el puerto al exterior).
- **Sin HTTPS (PENDIENTE):** En despliegue productivo, configurar TLS para el servidor Eureka; los clientes deben comunicarse únicamente por HTTPS para evitar interceptación de datos del registro.
- **Actuator:** Los endpoints de `spring-boot-starter-actuator` deben restringirse vía `management.endpoints.web.exposure.include` y protegerse con autenticación en producción.
- **Acceso exclusivo interno:** El puerto del servidor Eureka (por defecto 8761) no debe mapearse hacia internet; usar una red Docker interna o reglas de firewall.
