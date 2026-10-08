package com.ServiciosTransporte.Gestion.DtoResponse.Estadisticas;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EstadisticasAsignacionesDto {

    private Long totalActivas;
    private Long totalInactivas;
    private Long vehiculosConAsignacionActiva;
    private Long rutasConVehiculosEnAsignacionActiva;
    private Map<String, Long> asignacionesPorConductor;
    private Map<String, Long> asignacionesPorVehiculo;
    private List<ConductorAsignacionesDto> topConductoresPorAsignaciones;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ConductorAsignacionesDto {
        private Long conductorId;
        private String nombreCompleto;
        private Long cantidad;
    }
}