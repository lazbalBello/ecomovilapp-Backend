package com.EcoTransporte.GestionSeguimiento.DtoResponse;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ParadaLiteDto implements Serializable {

    private Long id;
    private String nombre;
    private String ruta;
}
