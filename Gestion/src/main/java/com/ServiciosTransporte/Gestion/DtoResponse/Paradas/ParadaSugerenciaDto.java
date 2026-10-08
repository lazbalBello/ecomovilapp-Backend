package com.ServiciosTransporte.Gestion.DtoResponse.Paradas;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ParadaSugerenciaDto implements Serializable {

    private Long id;
    private String nombre;
}
