package com.ServiciosTransporte.Gestion.Dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EstadoOperativoDto {
    @NotBlank(message = "El estado es obligatorio")
    private String estado;
}
