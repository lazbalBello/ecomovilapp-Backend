package com.ServiciosTransporte.Gestion.DtoResponse;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AsignacionLiteDto implements Serializable {

    private Long id;
    private String matriculaVehiculo;
    private String conductor;
    private String dniConductor;
    private LocalDate fechaInicio;
    private LocalDate fechaFinal;
    private boolean indefinido;
}
