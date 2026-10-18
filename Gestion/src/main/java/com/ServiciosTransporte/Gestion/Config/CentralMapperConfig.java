package com.ServiciosTransporte.Gestion.Config;

import org.mapstruct.MapperConfig;
import org.mapstruct.ReportingPolicy;

/**
 * Configuración central de MapStruct para el servicio Gestion.
 * Aplica inyección de dependencias de Spring e ignora propiedades de destino no mapeadas
 * (como campos de auditoría heredados de EntidadBase), eliminando advertencias ruidosas en compilación.
 */
@MapperConfig(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface CentralMapperConfig {
}
