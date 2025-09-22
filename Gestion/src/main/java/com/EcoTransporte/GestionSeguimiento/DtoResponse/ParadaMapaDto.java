package com.EcoTransporte.GestionSeguimiento.DtoResponse;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ParadaMapaDto {

    private Long id;
    private String nombre;
    private String ruta;
    private double latitud;
    private double longitud;
}
