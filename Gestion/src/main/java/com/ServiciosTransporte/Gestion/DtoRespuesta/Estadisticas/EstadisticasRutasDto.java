package com.ServiciosTransporte.Gestion.DtoRespuesta.Estadisticas;

import java.util.Map;

public record EstadisticasRutasDto(
        Long total,
        Long conVehiculos,
        Long sinVehiculos,
        Long conParadas,
        Long sinParadas,
        Long ultimos7Dias,
        Double promedioParadasPorRuta,
        Map<String, Long> vehiculosPorRuta,
        Map<String, Long> paradasPorRuta
) {
}
