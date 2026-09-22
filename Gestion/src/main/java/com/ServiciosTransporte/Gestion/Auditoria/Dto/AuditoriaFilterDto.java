package com.ServiciosTransporte.Gestion.Auditoria.Dto;

import com.ServiciosTransporte.Gestion.Auditoria.Modelos.TipoOperacion;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * Criterios de filtrado dinámico para la consulta de logs de auditoría.
 */
public record AuditoriaFilterDto(
        String nombreTabla,
        TipoOperacion tipoOperacion,
        Long entidadId,
        String usuarioId,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        LocalDateTime fechaInicio,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        LocalDateTime fechaFin
) {
}
