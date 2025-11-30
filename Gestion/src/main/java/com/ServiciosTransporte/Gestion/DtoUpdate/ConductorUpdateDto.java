package com.ServiciosTransporte.Gestion.DtoUpdate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConductorUpdateDto implements Serializable {

    @Pattern(
            regexp = "^\\d{11}$",
            message = "El dni deben ser 11 dígitos númericos sin espacios"
    )
    private String dni;

    @Pattern(
            regexp = "^[A-Za-zÁÉÍÓÚáéíóúñÑ\\s]+$",
            message = "El nombre solo puede contener letras y espacios"
    )
    private String nombre;

    @Pattern(
            regexp = "^[A-Za-zÁÉÍÓÚáéíóúñÑ\\s]+$",
            message = "Los apellidos solo pueden contener letras y espacios"
    )
    private String apellidos;

    private List<@NotBlank(message = "La categoria no puede estar vacía")
    @Pattern(regexp = "^(?:FE|[ACD]1?|[BEF])$",
            message = "Categoria no válida")
            String> categoriasLicencia;

    private boolean disponibilidad = true;
}
