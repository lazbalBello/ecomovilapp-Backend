package com.ServiciosTransporte.Gestion.Auditoria.Config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.hibernate6.Hibernate6Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuditoriaConfig {

    /**
     * ObjectMapper especializado para auditoría.
     * Registra Hibernate6Module para evitar LazyInitializationException en colecciones no cargadas
     * e ignorar proxies no inicializados de Hibernate, junto con soporte nativo de JavaTime.
     */
    @Bean(name = "auditoriaObjectMapper")
    public ObjectMapper auditoriaObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        Hibernate6Module hibernateModule = new Hibernate6Module();
        // Desactiva la carga forzada de proxies o colecciones lazy
        hibernateModule.disable(Hibernate6Module.Feature.FORCE_LAZY_LOADING);
        hibernateModule.enable(Hibernate6Module.Feature.SERIALIZE_IDENTIFIER_FOR_LAZY_NOT_LOADED_OBJECTS);

        mapper.registerModule(hibernateModule);
        mapper.registerModule(new JavaTimeModule());

        // MixIns para excluir colecciones de relaciones secundarias que poseen su propio flujo de auditoría
        // y evitar diferencias en los diffs entre datos previos y nuevos sin tocar los modelos.
        mapper.addMixIn(com.ServiciosTransporte.Gestion.Modelos.Vehiculo.class, VehiculoAuditoriaMixIn.class);
        mapper.addMixIn(com.ServiciosTransporte.Gestion.Modelos.Conductor.class, ConductorAuditoriaMixIn.class);

        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        return mapper;
    }

    private abstract static class VehiculoAuditoriaMixIn {
        @com.fasterxml.jackson.annotation.JsonIgnore
        private java.util.List<com.ServiciosTransporte.Gestion.Modelos.VehiculoAsignacion> asignaciones;
    }

    private abstract static class ConductorAuditoriaMixIn {
        @com.fasterxml.jackson.annotation.JsonIgnore
        private java.util.List<com.ServiciosTransporte.Gestion.Modelos.VehiculoAsignacion> historialAsignaciones;
    }
}
