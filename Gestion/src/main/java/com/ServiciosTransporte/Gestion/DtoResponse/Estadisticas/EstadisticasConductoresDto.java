package com.ServiciosTransporte.Gestion.DtoResponse.Estadisticas;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EstadisticasConductoresDto {

    private Long total;
    private Long disponibles;
    private Long noDisponibles;
    private Long ultimos7Dias;
    private Map<String, Long> porCategoriaLicencia;
}