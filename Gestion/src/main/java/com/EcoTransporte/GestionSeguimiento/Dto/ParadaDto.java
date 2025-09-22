package com.EcoTransporte.GestionSeguimiento.Dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ParadaDto implements Serializable {

    @NotNull(message = "Se requere una ruta")
    private Long rutaID;

    @NotBlank(message = "El nombre de la parada mo debe estar en blanco")
    @Pattern(regexp = "^[\\p{L}\\p{N}\\s]+$",
            message = "El nombre no debe tener caracteres especiales")
    private String nombre;

    @NotNull(message = "Seleccione la ubicacion(latitud)")
    private Double latitud;

    @NotNull(message = "Seleccione la ubicacion(longitud)")
    private Double longitud;
}
