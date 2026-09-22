package com.ServiciosTransporte.Gestion.Auditoria;

import com.ServiciosTransporte.Gestion.Auditoria.Anotaciones.Auditable;
import com.ServiciosTransporte.Gestion.Auditoria.Aspectos.AuditoriaAspect;
import com.ServiciosTransporte.Gestion.Auditoria.Modelos.TipoOperacion;
import com.ServiciosTransporte.Gestion.Auditoria.Servicios.AuditoriaService;
import com.ServiciosTransporte.Gestion.Modelos.Vehiculo;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditoriaAspectTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private AuditoriaService auditoriaService;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private MethodSignature methodSignature;

    @Mock
    private Auditable auditable;

    private ObjectMapper auditoriaObjectMapper;
    private AuditoriaAspect auditoriaAspect;

    @BeforeEach
    void setUp() {
        auditoriaObjectMapper = new ObjectMapper();
        auditoriaAspect = new AuditoriaAspect(auditoriaService, auditoriaObjectMapper);
        ReflectionTestUtils.setField(auditoriaAspect, "entityManager", entityManager);
    }

    // Método dummy para la reflexión del Aspect
    public Vehiculo dummyModificar(Long id, String nuevoNombre) {
        return new Vehiculo();
    }

    @Test
    void auditarOperacion_modificacion_capturaPreviaYPosterior() throws Throwable {
        Method method = this.getClass().getMethod("dummyModificar", Long.class, String.class);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);
        when(joinPoint.getArgs()).thenReturn(new Object[]{5L, "Nuevo"});

        when(auditable.idSpel()).thenReturn("");
        doReturn(Vehiculo.class).when(auditable).entidad();
        when(auditable.operacion()).thenReturn(TipoOperacion.MODIFICACION);
        when(auditable.tabla()).thenReturn("vehiculo");

        Vehiculo entidadMock = new Vehiculo();
        entidadMock.setId(5L);
        entidadMock.setMatricula("B123");
        when(entityManager.find(Vehiculo.class, 5L)).thenReturn(entidadMock);

        when(joinPoint.proceed()).thenReturn(entidadMock);

        Object resultado = auditoriaAspect.auditarOperacion(joinPoint, auditable);

        assertEquals(entidadMock, resultado);
        verify(auditoriaService).registrarCambio(
                eq("vehiculo"),
                eq(5L),
                eq(TipoOperacion.MODIFICACION),
                any(),
                any(),
                anyString(),
                any()
        );
    }

    @Test
    void auditarOperacion_cuandoOperacionLanzaExcepcion_noRegistraAuditoria() throws Throwable {
        Method method = this.getClass().getMethod("dummyModificar", Long.class, String.class);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);
        when(joinPoint.getArgs()).thenReturn(new Object[]{5L, "Nuevo"});

        when(auditable.idSpel()).thenReturn("");
        doReturn(Vehiculo.class).when(auditable).entidad();
        when(auditable.operacion()).thenReturn(TipoOperacion.MODIFICACION);

        Vehiculo entidadMock = new Vehiculo();
        entidadMock.setId(5L);
        when(entityManager.find(Vehiculo.class, 5L)).thenReturn(entidadMock);

        when(joinPoint.proceed()).thenThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Matrícula duplicada"));

        assertThrows(ResponseStatusException.class, () -> auditoriaAspect.auditarOperacion(joinPoint, auditable));

        verify(auditoriaService, never()).registrarCambio(any(), any(), any(), any(), any(), any(), any());
    }
}
