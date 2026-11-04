package com.ServiciosTransporte.Gestion.DtoRespuesta.Estadisticas;

import java.util.Map;

public record EstadisticasResumenDto(
        Long totalVehiculos,
        Long vehiculosActivos,
        Long vehiculosInactivos,
        Long vehiculosMantenimiento,
        Long vehiculosFueraDeServicio,
        Long vehiculosUltimos7Dias,
        Map<String, Long> vehiculosPorEstado,
        Map<String, Long> vehiculosPorMarca,
        Map<String, Long> vehiculosPorTipoBateria,
        Long totalRutas,
        Long rutasConVehiculos,
        Long rutasSinVehiculos,
        Long rutasConParadas,
        Long rutasSinParadas,
        Long rutasUltimos7Dias,
        Double promedioParadasPorRuta,
        Long totalConductores,
        Long conductoresDisponibles,
        Long conductoresNoDisponibles,
        Long conductoresUltimos7Dias,
        Long totalAsignacionesActivas,
        Long totalAsignacionesInactivas,
        Long vehiculosConAsignacionActiva,
        Long rutasConVehiculosEnAsignacionActiva
) {
}
