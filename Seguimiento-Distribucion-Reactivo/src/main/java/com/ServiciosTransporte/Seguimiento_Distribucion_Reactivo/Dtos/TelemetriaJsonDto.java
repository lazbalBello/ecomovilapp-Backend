package com.ServiciosTransporte.Seguimiento_Distribucion_Reactivo.Dtos;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.HashMap;
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
    private Map<String, String> additionalData = new HashMap<>();

    // Variables extras no tipadas de traccar (sirven para identificar eventos /
    // desconexiones)
    private Map<String, Object> rawFields = new HashMap<>();

    @JsonAnySetter
    public void addRawField(String key, Object value) {
        this.rawFields.put(key, value);
    }
}
