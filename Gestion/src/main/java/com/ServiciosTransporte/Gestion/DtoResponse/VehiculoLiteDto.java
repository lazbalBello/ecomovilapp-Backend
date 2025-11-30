package com.ServiciosTransporte.Gestion.DtoResponse;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VehiculoLiteDto implements Serializable {

    private Long id;
    private String matricula;
    private int capacidadPersonas;
    private String modelo;
    private String marca;
    private String tipoBateria;
    private String estado;
    private List<AsignacionLiteDto> asignaciones;
    private RutaLiteDto ruta;
}
