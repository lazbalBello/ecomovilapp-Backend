package com.ServiciosTransporte.Gestion.Auditoria.Aspectos;

import com.ServiciosTransporte.Gestion.Auditoria.Anotaciones.Auditable;
import com.ServiciosTransporte.Gestion.Auditoria.Modelos.TipoOperacion;
import com.ServiciosTransporte.Gestion.Auditoria.Servicios.AuditoriaService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

@Slf4j
@Aspect
@Component
public class AuditoriaAspect {

    @PersistenceContext
    private EntityManager entityManager;

    private final AuditoriaService auditoriaService;
    private final ObjectMapper auditoriaObjectMapper;

    private final ExpressionParser spelParser = new SpelExpressionParser();
    private final ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

    public AuditoriaAspect(
            AuditoriaService auditoriaService,
            @Qualifier("auditoriaObjectMapper") ObjectMapper auditoriaObjectMapper
    ) {
        this.auditoriaService = auditoriaService;
        this.auditoriaObjectMapper = auditoriaObjectMapper;
    }

    @Around("@annotation(auditable)")
    public Object auditarOperacion(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Object[] args = joinPoint.getArgs();

        // 1. Extraer ID previo de la entidad
        Long entidadId = resolverEntidadId(joinPoint, method, args, auditable.idSpel());

        // 2. Snapshot previo (para MODIFICACION, ELIMINACION o SOFT_DELETE)
        JsonNode datosAnteriores = null;
        if (entidadId != null && auditable.operacion() != TipoOperacion.CREACION) {
            try {
                Object entidadPrevia = entityManager.find(auditable.entidad(), entidadId);
                if (entidadPrevia != null) {
                    datosAnteriores = auditoriaObjectMapper.valueToTree(entidadPrevia);
                }
            } catch (Exception e) {
                log.warn("No se pudo capturar el estado anterior de la entidad [{} id={}]: {}",
                        auditable.entidad().getSimpleName(), entidadId, e.getMessage());
            }
        }

        // 3. Ejecutar la lógica de negocio.
        // Si lanza cualquier excepción (validación, lógica de negocio, BD), se propaga directamente
        // y la transacción abortará. NO se registrará ninguna auditoría.
        Object resultado = joinPoint.proceed();

        // 4. Si es CREACION y no teníamos ID en argumentos, extraerlo del resultado
        if (entidadId == null && auditable.operacion() == TipoOperacion.CREACION) {
            entidadId = extraerIdDeResultado(resultado);
        }

        // 5. Snapshot posterior (para CREACION, MODIFICACION o SOFT_DELETE)
        JsonNode datosNuevos = null;
        if (auditable.operacion() != TipoOperacion.ELIMINACION && entidadId != null) {
            try {
                Object entidadActual = entityManager.find(auditable.entidad(), entidadId);
                if (entidadActual != null) {
                    datosNuevos = auditoriaObjectMapper.valueToTree(entidadActual);
                } else if (resultado != null) {
                    datosNuevos = auditoriaObjectMapper.valueToTree(resultado);
                }
            } catch (Exception e) {
                log.warn("No se pudo capturar el estado nuevo de la entidad [{} id={}]: {}",
                        auditable.entidad().getSimpleName(), entidadId, e.getMessage());
                if (resultado != null) {
                    try {
                        datosNuevos = auditoriaObjectMapper.valueToTree(resultado);
                    } catch (Exception ignored) {
                    }
                }
            }
        }

        // 6. Obtener usuario autenticado
        String usuarioId = obtenerUsuarioAutenticado();

        // 7. Registrar auditoría ÚNICAMENTE si la transacción de negocio hace COMMIT exitoso.
        // Si la transacción hace rollback (por ejemplo por violación de constraint o error de negocio),
        // afterCommit() NUNCA se invoca y no se genera ningún registro fantasma.
        if (entidadId != null) {
            final Long finalEntidadId = entidadId;
            final JsonNode finalDatosAnteriores = datosAnteriores;
            final JsonNode finalDatosNuevos = datosNuevos;

            if (TransactionSynchronizationManager.isSynchronizationActive()) {
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        try {
                            auditoriaService.registrarCambio(
                                    auditable.tabla(),
                                    finalEntidadId,
                                    auditable.operacion(),
                                    finalDatosAnteriores,
                                    finalDatosNuevos,
                                    usuarioId,
                                    LocalDateTime.now()
                            );
                        } catch (Exception e) {
                            log.error("Fallo al persistir log de auditoría en afterCommit para tabla={} entidadId={}: {}",
                                    auditable.tabla(), finalEntidadId, e.getMessage(), e);
                        }
                    }
                });
            } else {
                // Operación fuera de contexto transaccional: persistir de inmediato
                try {
                    auditoriaService.registrarCambio(
                            auditable.tabla(),
                            finalEntidadId,
                            auditable.operacion(),
                            finalDatosAnteriores,
                            finalDatosNuevos,
                            usuarioId,
                            LocalDateTime.now()
                    );
                } catch (Exception e) {
                    log.error("Fallo al persistir log de auditoría directa para tabla={} entidadId={}: {}",
                            auditable.tabla(), finalEntidadId, e.getMessage(), e);
                }
            }
        } else {
            log.warn("No se pudo determinar el entidadId para auditar la operación [{}] en tabla [{}]",
                    auditable.operacion(), auditable.tabla());
        }

        return resultado;
    }

    /**
     * Resuelve el ID de la entidad: primero intenta SpEL si está configurado,
     * luego convención (primer argumento Number/Long) o campo id en DTO.
     */
    private Long resolverEntidadId(ProceedingJoinPoint joinPoint, Method method, Object[] args, String idSpel) {
        if (idSpel != null && !idSpel.trim().isEmpty()) {
            try {
                EvaluationContext context = new MethodBasedEvaluationContext(
                        joinPoint.getTarget(),
                        method,
                        args,
                        parameterNameDiscoverer
                );
                Object valor = spelParser.parseExpression(idSpel).getValue(context);
                if (valor instanceof Number number) {
                    return number.longValue();
                }
            } catch (Exception e) {
                log.debug("No se pudo evaluar SpEL '{}': {}", idSpel, e.getMessage());
            }
        }

        // Convención: primer argumento de tipo Number/Long
        for (Object arg : args) {
            if (arg instanceof Long longVal) {
                return longVal;
            } else if (arg instanceof Number number) {
                return number.longValue();
            }
        }

        // Si el primer argumento es un objeto con getId() o id()
        if (args != null && args.length > 0 && args[0] != null) {
            Long idDesdeObjeto = extraerIdDeResultado(args[0]);
            if (idDesdeObjeto != null) {
                return idDesdeObjeto;
            }
        }

        return null;
    }

    /**
     * Extrae el ID de una entidad o DTO por reflexión buscando getId() o campo id.
     */
    private Long extraerIdDeResultado(Object obj) {
        if (obj == null) return null;

        if (obj instanceof Number number) {
            return number.longValue();
        }

        try {
            Method getIdMethod = obj.getClass().getMethod("getId");
            Object idVal = getIdMethod.invoke(obj);
            if (idVal instanceof Number number) {
                return number.longValue();
            }
        } catch (Exception ignored) {
        }

        try {
            Method idMethod = obj.getClass().getMethod("id");
            Object idVal = idMethod.invoke(obj);
            if (idVal instanceof Number number) {
                return number.longValue();
            }
        } catch (Exception ignored) {
        }

        return null;
    }

    private String obtenerUsuarioAutenticado() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
                return auth.getName();
            }
        } catch (Exception e) {
            log.debug("No se pudo extraer usuario del SecurityContext: {}", e.getMessage());
        }
        return "SISTEMA";
    }
}
