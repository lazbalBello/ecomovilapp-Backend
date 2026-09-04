package com.ServiciosTransporte.Gestion.DtoUpdate;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VehiculoUpdateDto implements Serializable {

    @Pattern( regexp = "^[A-Za-z]\\d{6}$",
            message = "Formato de matrícula no válido")
    private String matricula;

    @Min(value = 1, message = "La capacidad de personas debe ser al menos 1")
    private Integer capacidadPersonas;

    private String modelo;

    private String marca;

    private String tipoBateria;

    @Size(max = 15, message = "El IMEI del GPS no puede exceder 15 caracteres")
    @Pattern(regexp = "^\\d{1,15}$", message = "El IMEI del GPS solo puede contener números")
    private String imeiDispositivoGps;

    private Integer estado;

    private Long rutaId;
}
