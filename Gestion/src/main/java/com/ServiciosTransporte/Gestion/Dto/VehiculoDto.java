package com.ServiciosTransporte.Gestion.Dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VehiculoDto implements Serializable {

    @NotBlank(message = "Se requiere la matrícula")
    @Pattern( regexp = "^[A-Za-z]\\d{6}$",
            message = "Formato de matrícula no válido")
    private String matricula;

    @NotNull(message = "Se requiere la capacidad de personas")
    @Min(value = 1, message = "La capacidad de personas debe ser al menos 1")
    private Integer capacidadPersonas;

    @NotBlank(message = "Se requiere el modelo")
    private String modelo;

    @NotBlank(message = "Se requiere la marca")
    private String marca;

    @NotBlank(message = "Se requiere el tipo de bateria")
    private String tipoBateria;

    private String estado = "ACTIVO";
}
