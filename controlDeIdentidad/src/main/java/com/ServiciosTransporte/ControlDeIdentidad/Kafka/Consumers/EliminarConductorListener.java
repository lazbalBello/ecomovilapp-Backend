package com.ServiciosTransporte.ControlDeIdentidad.Kafka.Consumers;

import com.ServiciosTransporte.ControlDeIdentidad.Servicios.ServicioUsuarios;
import com.servicioTransporte.flota.eventos.conductor.eliminar.ConductorEliminado;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EliminarConductorListener {

    private final ServicioUsuarios servicioUsuarios;

    @KafkaListener(topics = "eliminar-conductor", groupId = "servicios-flota")
    public void handleConductorEliminado(ConsumerRecord<String, ConductorEliminado> record) {
        ConductorEliminado evento = record.value();
        String keycloakId = String.valueOf(evento.getKeycloakId());

        log.info("Recibido evento ConductorEliminado para usuario Keycloak: {} (conductor: {} {})",
                keycloakId, evento.getNombre(), evento.getApellido());

        try {
            servicioUsuarios.deleteUser(keycloakId);
            log.info("Usuario Keycloak eliminado correctamente: {}", keycloakId);
        } catch (Exception e) {
            log.error("Error al eliminar el usuario Keycloak con id {}: {}", keycloakId, e.getMessage(), e);
        }
    }
}
