package com.ServiciosTransporte.Gestion.DtoResponse.Paradas;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ParadaMapaDto {

    private Long id;
    private String nombre;
    private double latitud;
    private double longitud;
}
