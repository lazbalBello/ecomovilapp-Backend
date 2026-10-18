package com.ServiciosTransporte.Gestion.DtoRespuesta.Estadisticas;

import java.util.List;
import java.util.Map;

public record EstadisticasAsignacionesDto(
        Long totalActivas,
        Long totalInactivas,
        Long vehiculosConAsignacionActiva,
        Long rutasConVehiculosEnAsignacionActiva,
        Map<String, Long> asignacionesPorConductor,
        Map<String, Long> asignacionesPorVehiculo,
        List<ConductorAsignacionesDto> topConductoresPorAsignaciones
) {

    public record ConductorAsignacionesDto(
            Long conductorId,
            String nombreCompleto,
            Long cantidad
    ) {
    }
}
