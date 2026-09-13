package com.ServiciosTransporte.Gestion.DtoResponse;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EstadisticasSemanalesDto {

    private long vehiculos;
    private long conductores;
    private long rutas;
}
