package com.ServiciosTransporte.Gestion.Auditoria.Dto;

import com.ServiciosTransporte.Gestion.Auditoria.Modelos.AuditoriaLog;
import com.ServiciosTransporte.Gestion.Auditoria.Modelos.TipoOperacion;
import com.fasterxml.jackson.databind.JsonNode;

import java.time.LocalDateTime;

/**
 * Respuesta inmutable para visualización de logs de auditoría.
 */
public record AuditoriaResponseDto(
        Long id,
        String nombreTabla,
        Long entidadId,
        TipoOperacion tipoOperacion,
        JsonNode datosAnteriores,
        JsonNode datosNuevos,
        String usuarioId,
        LocalDateTime fechaOperacion
) {
    public static AuditoriaResponseDto from(AuditoriaLog log) {
        if (log == null) return null;
        return new AuditoriaResponseDto(
                log.getId(),
                log.getNombreTabla(),
                log.getEntidadId(),
                log.getTipoOperacion(),
                log.getDatosAnteriores(),
                log.getDatosNuevos(),
                log.getUsuarioId(),
                log.getFechaOperacion()
        );
    }
}
