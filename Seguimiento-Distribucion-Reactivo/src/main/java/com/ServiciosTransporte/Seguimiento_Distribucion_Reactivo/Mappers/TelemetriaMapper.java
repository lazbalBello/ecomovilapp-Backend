package com.ServiciosTransporte.Seguimiento_Distribucion_Reactivo.Mappers;

import com.ServiciosTransporte.Seguimiento_Distribucion_Reactivo.Dtos.EstadoCirculacion;
import com.ServiciosTransporte.Seguimiento_Distribucion_Reactivo.Dtos.VehiculoRedisDto;
import com.servicioTransporte.flota.eventos.vehiculo.seguimiento.TelemetriaVehiculo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Map;

@Mapper(componentModel = "spring")
public interface TelemetriaMapper {

    @Mapping(target = "status", ignore = true)
    @Mapping(target = "batteryLevel", source = "batteryLevel")
    VehiculoRedisDto avroToDto(TelemetriaVehiculo avro);

    default VehiculoRedisDto mapFromRedis(String vehicleId, Map<Object, Object> redisMap) {
        if (redisMap == null || redisMap.isEmpty()) return null;

        return VehiculoRedisDto.builder()
                .vehicleId(vehicleId)
                .latitude(parseDouble(redisMap.get("lat")))
                .longitude(parseDouble(redisMap.get("lon")))
                .speed(parseDouble(redisMap.get("speed")))
                .batteryLevel(parseDouble(redisMap.get("bat")))
                .timestamp(parseLong(redisMap.get("ts")))
                .status(parseEstado(redisMap.get("status")))
                .build();
    }

    default Double parseDouble(Object val) {
        return val != null ? Double.valueOf(val.toString()) : null;
    }

    default Long parseLong(Object val) {
        if (val == null) return null;

        if (val instanceof Number) {
            return ((Number) val).longValue();
        }

        String strVal = val.toString();

        try {
            return Long.parseLong(strVal);
        } catch (NumberFormatException e) {
            try {
                Instant instant = Instant.parse(strVal);
                return instant.toEpochMilli();
            } catch (DateTimeParseException dtpe) {
                System.err.println("Error crítico parseando fecha: " + strVal);
                return System.currentTimeMillis();
            }
        }
    }

    default EstadoCirculacion parseEstado(Object val) {
        if (val == null) return EstadoCirculacion.ACTIVO; // Default por seguridad
        try {
            return EstadoCirculacion.valueOf(val.toString());
        } catch (IllegalArgumentException e) {
            return EstadoCirculacion.ACTIVO;
        }
    }

    default String map(CharSequence value) {
        return value == null ? null : value.toString();
    }
}
