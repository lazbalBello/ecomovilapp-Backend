package com.ServiciosTransporte.Gestion.DtoRespuesta.Estadisticas;

import java.util.List;
import java.util.Map;

public record EstadisticasVehiculosDto(
        Long total,
        Long activos,
        Long inactivos,
        Long enMantenimiento,
        Long fueraDeServicio,
        Long ultimos7Dias,
        Map<String, Long> porEstado,
        Map<String, Long> porMarca,
        Map<String, Long> porTipoBateria,
        Long conRutaAsignada,
        Long conGpsAsignado,
        List<TopMarcaDto> topMarcas,
        List<TopModeloDto> topModelos
) {

    public record TopMarcaDto(
            String marca,
            Long cantidad
    ) {
    }

    public record TopModeloDto(
            String modelo,
            Long cantidad
    ) {
    }
}
