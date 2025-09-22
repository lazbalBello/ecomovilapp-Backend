package com.EcoTransporte.GestionSeguimiento.Dto;

import com.EcoTransporte.GestionSeguimiento.Modelos.VehiculoAsignacion;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConductorDto implements Serializable {

    @NotBlank(message = "El dni no puede estar en blanco")
    @Pattern(
            regexp = "^\\d{11}$",
            message = "El dni deben ser 11 dígitos númericos sin espacios"
    )
    private String dni;

    @NotBlank(message = "El nombre no puede estar en blanco")
    @Pattern(
            regexp = "^[A-Za-zÁÉÍÓÚáéíóúñÑ\\s]+$",
            message = "El nombre solo puede contener letras y espacios"
    )
    private String nombre;

    @NotBlank(message = "Los apellidos no pueden estar en blanco")
    @Pattern(
            regexp = "^[A-Za-zÁÉÍÓÚáéíóúñÑ\\s]+$",
            message = "Los apellidos solo pueden contener letras y espacios"
    )
    private String apellidos;

    @NotEmpty(message = "El conducor debe tener al menos una categoria de licencia")
    private List<@NotBlank(message = "La categoria no puede estar vacía")
                 @Pattern(regexp = "^(?:FE|[ACD]1?|[BEF])$",
                         message = "Categoria no válida")
                 String> categoriasLicencia;

    private boolean disponibilidad = true;

}
