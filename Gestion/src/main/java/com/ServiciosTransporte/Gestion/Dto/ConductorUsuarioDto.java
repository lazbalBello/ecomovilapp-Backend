package com.ServiciosTransporte.Gestion.Dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConductorUsuarioDto {

    @NotBlank(message = "El nombre de usuario no puede estar en blanco")
    @Min(value = 4, message = "El nombre de usuaio debe contener al menos 4 caracteres")
    @Max(value = 10, message = "El nombre de usuaio no puede contener más de 10 caracteres")
    private String nombreUsuario;

    @NotBlank(message = "La contraseña no puede estar en blanco")
    private String password;

    @NotBlank(message = "El email no puede estar en blanco")
    @Email
    private String email;

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
    private List<@NotBlank(message = "Las categorias no pueden estar vacías")
    @Pattern(regexp = "^(?:FE|[ACD]1?|[BEF])$",
            message = "Categoria no válida")
            String> categoriasLicencia;

    private boolean disponibilidad = false;
}
