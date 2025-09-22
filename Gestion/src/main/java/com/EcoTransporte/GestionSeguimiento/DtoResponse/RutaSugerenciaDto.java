package com.EcoTransporte.GestionSeguimiento.DtoResponse;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RutaSugerenciaDto implements Serializable {

    private Long id;
    private String nombre;
}
