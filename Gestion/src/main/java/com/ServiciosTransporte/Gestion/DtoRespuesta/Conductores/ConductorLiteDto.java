package com.ServiciosTransporte.Gestion.DtoRespuesta.Conductores;

import com.ServiciosTransporte.Gestion.DtoRespuesta.Asignaciones.AsignacionLiteDto;

import java.util.List;

public record ConductorLiteDto(
        Long id,
        String usuarioId,
        String dni,
        String nombre,
        String apellidos,
        List<AsignacionLiteDto> historialAsignaciones,
        List<String> categoriasLicencia
) {
}
