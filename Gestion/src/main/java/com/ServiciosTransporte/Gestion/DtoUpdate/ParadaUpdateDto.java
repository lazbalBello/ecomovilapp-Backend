package com.ServiciosTransporte.Gestion.DtoUpdate;

import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ParadaUpdateDto implements Serializable {

    private Long rutaID;

    @Pattern(regexp = "^[\\p{L}\\p{N}\\s]+$",
            message = "El nombre no debe tener caracteres especiales")
    private String nombre;

    private Double latitud;

    private Double longitud;
}
