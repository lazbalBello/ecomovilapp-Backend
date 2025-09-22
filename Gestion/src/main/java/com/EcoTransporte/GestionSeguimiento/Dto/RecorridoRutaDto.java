package com.EcoTransporte.GestionSeguimiento.Dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecorridoRutaDto implements Serializable {

    @NotNull(message = "Seleccione la latitud")
    private Double latitud;

    @NotNull(message = "Selecciona la longitud")
    private Double longitud;
}
