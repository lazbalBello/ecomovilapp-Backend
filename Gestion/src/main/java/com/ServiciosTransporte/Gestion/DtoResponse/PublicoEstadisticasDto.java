package com.ServiciosTransporte.Gestion.DtoResponse;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PublicoEstadisticasDto {

    private Long vehiculosActivos;
    private Long rutasActivas;
}