package com.ServiciosTransporte.Seguimiento_Entrada_Telemetria.Dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TelemetriaJsonDto {

    private String vehicleId;
    private Double latitude;
    private Double longitude;
    private Double speed;
    private Double batteryLevel;
    private Long timestamp;
    private Map<String, String> additionalData;

    public TelemetriaJsonDto() {}
}
