package com.ServiciosTransporte.Gestion.Auditoria.Servicios;

import com.ServiciosTransporte.Gestion.Auditoria.Dto.AuditoriaFilterDto;
import com.ServiciosTransporte.Gestion.Auditoria.Dto.AuditoriaResponseDto;
import com.ServiciosTransporte.Gestion.Auditoria.Modelos.AuditoriaLog;
import com.ServiciosTransporte.Gestion.Auditoria.Modelos.TipoOperacion;
import com.ServiciosTransporte.Gestion.Auditoria.Repositorios.AuditoriaLogRepository;
import com.ServiciosTransporte.Gestion.Auditoria.Repositorios.AuditoriaSpecification;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditoriaService {

    private final AuditoriaLogRepository auditoriaLogRepository;

    /**
     * Persiste un registro de auditoría en una transacción completamente independiente (REQUIRES_NEW)
     * para asegurar el aislamiento y trazabilidad inmutable.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AuditoriaLog registrarCambio(
            String nombreTabla,
            Long entidadId,
            TipoOperacion tipoOperacion,
            JsonNode datosAnteriores,
            JsonNode datosNuevos,
            String usuarioId,
            LocalDateTime fechaOperacion
    ) {
        AuditoriaLog logEntry = AuditoriaLog.builder()
                .nombreTabla(nombreTabla)
                .entidadId(entidadId)
                .tipoOperacion(tipoOperacion)
                .datosAnteriores(datosAnteriores)
                .datosNuevos(datosNuevos)
                .usuarioId(usuarioId != null ? usuarioId : "SISTEMA")
                .fechaOperacion(fechaOperacion != null ? fechaOperacion : LocalDateTime.now())
                .build();

        AuditoriaLog guardado = auditoriaLogRepository.save(logEntry);
        log.debug("Auditoría registrada id={} tabla={} entidadId={} operacion={} usuario={}",
                guardado.getId(), nombreTabla, entidadId, tipoOperacion, usuarioId);
        return guardado;
    }

    /**
     * Consulta paginada y filtrada de logs de auditoría.
     */
    @Transactional(readOnly = true)
    public Page<AuditoriaResponseDto> obtenerAuditorias(AuditoriaFilterDto filtro, Pageable pageable) {
        return auditoriaLogRepository.findAll(AuditoriaSpecification.conFiltros(filtro), pageable)
                .map(AuditoriaResponseDto::from);
    }
}
