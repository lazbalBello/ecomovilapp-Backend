package com.ServiciosTransporte.Gestion.Auditoria;

import com.ServiciosTransporte.Gestion.Auditoria.Dto.AuditoriaFilterDto;
import com.ServiciosTransporte.Gestion.Auditoria.Dto.AuditoriaResponseDto;
import com.ServiciosTransporte.Gestion.Auditoria.Modelos.AuditoriaLog;
import com.ServiciosTransporte.Gestion.Auditoria.Modelos.TipoOperacion;
import com.ServiciosTransporte.Gestion.Auditoria.Repositorios.AuditoriaLogRepository;
import com.ServiciosTransporte.Gestion.Auditoria.Servicios.AuditoriaService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditoriaServiceTest {

    @Mock
    private AuditoriaLogRepository auditoriaLogRepository;

    @InjectMocks
    private AuditoriaService auditoriaService;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    void registrarCambio_debeGuardarCorrectamente() {
        JsonNode prev = objectMapper.createObjectNode().put("matricula", "ABC-123");
        JsonNode next = objectMapper.createObjectNode().put("matricula", "XYZ-999");
        LocalDateTime ahora = LocalDateTime.now();

        when(auditoriaLogRepository.save(any(AuditoriaLog.class))).thenAnswer(inv -> {
            AuditoriaLog log = inv.getArgument(0);
            log.setId(10L);
            return log;
        });

        AuditoriaLog resultado = auditoriaService.registrarCambio(
                "vehiculo",
                1L,
                TipoOperacion.MODIFICACION,
                prev,
                next,
                "admin-user",
                ahora
        );

        assertNotNull(resultado);
        assertEquals(10L, resultado.getId());

        ArgumentCaptor<AuditoriaLog> captor = ArgumentCaptor.forClass(AuditoriaLog.class);
        verify(auditoriaLogRepository).save(captor.capture());

        AuditoriaLog guardado = captor.getValue();
        assertEquals("vehiculo", guardado.getNombreTabla());
        assertEquals(1L, guardado.getEntidadId());
        assertEquals(TipoOperacion.MODIFICACION, guardado.getTipoOperacion());
        assertEquals("admin-user", guardado.getUsuarioId());
        assertEquals(prev, guardado.getDatosAnteriores());
        assertEquals(next, guardado.getDatosNuevos());
    }

    @Test
    void registrarCambio_usuarioNulo_asignaUsuarioSistema() {
        when(auditoriaLogRepository.save(any(AuditoriaLog.class))).thenAnswer(inv -> inv.getArgument(0));

        AuditoriaLog resultado = auditoriaService.registrarCambio(
                "conductor",
                5L,
                TipoOperacion.CREACION,
                null,
                null,
                null,
                null
        );

        assertEquals("SISTEMA", resultado.getUsuarioId());
        assertNotNull(resultado.getFechaOperacion());
    }

    @Test
    void obtenerAuditorias_debeRetornarPaginaMapeada() {
        AuditoriaLog log = AuditoriaLog.builder()
                .id(1L)
                .nombreTabla("ruta")
                .entidadId(20L)
                .tipoOperacion(TipoOperacion.SOFT_DELETE)
                .usuarioId("admin")
                .fechaOperacion(LocalDateTime.now())
                .build();

        Page<AuditoriaLog> pagina = new PageImpl<>(List.of(log));
        when(auditoriaLogRepository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(pagina);

        AuditoriaFilterDto filtro = new AuditoriaFilterDto("ruta", TipoOperacion.SOFT_DELETE, 20L, "admin", null, null);
        Page<AuditoriaResponseDto> respuesta = auditoriaService.obtenerAuditorias(filtro, PageRequest.of(0, 10));

        assertNotNull(respuesta);
        assertEquals(1, respuesta.getTotalElements());
        assertEquals("ruta", respuesta.getContent().get(0).nombreTabla());
        assertEquals(TipoOperacion.SOFT_DELETE, respuesta.getContent().get(0).tipoOperacion());
    }
}
