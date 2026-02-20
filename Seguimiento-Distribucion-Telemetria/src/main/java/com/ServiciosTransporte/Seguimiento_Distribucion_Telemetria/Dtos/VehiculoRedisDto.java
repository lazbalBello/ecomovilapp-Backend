package com.ServiciosTransporte.Seguimiento_Distribucion_Telemetria.Dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehiculoRedisDto {
    private String vehicleId;
    private Double latitude;
    private Double longitude;
    private Double speed;
    private Double batteryLevel;
    private EstadoCirculacion status;
    private Long timestamp;
}
