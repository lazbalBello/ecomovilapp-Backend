package com.ServiciosTransporte.Gestion.DtoResponse;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EstadisticasRutasDto {

    private Long total;
    private Long conVehiculos;
    private Long sinVehiculos;
    private Long conParadas;
    private Long sinParadas;
    private Long ultimos7Dias;
    private Double promedioParadasPorRuta;
    private Map<String, Long> vehiculosPorRuta;
    private Map<String, Long> paradasPorRuta;
}