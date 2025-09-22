package com.EcoTransporte.GestionSeguimiento.DtoResponse;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RutaLiteDto implements Serializable {

    private Long id;
    private String nombre;
    private String descripcion;
    private List<ParadaLiteDto> paradas;
    private List<String> vehiculosAsignados;
}
