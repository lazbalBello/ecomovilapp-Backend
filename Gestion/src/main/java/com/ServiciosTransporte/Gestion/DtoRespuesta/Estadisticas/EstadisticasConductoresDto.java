package com.ServiciosTransporte.Gestion.DtoRespuesta.Estadisticas;

import java.util.Map;

public record EstadisticasConductoresDto(
        Long total,
        Long disponibles,
        Long noDisponibles,
        Long ultimos7Dias,
        Map<String, Long> porCategoriaLicencia
) {
}
