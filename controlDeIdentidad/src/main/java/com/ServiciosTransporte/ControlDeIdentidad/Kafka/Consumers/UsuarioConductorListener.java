package com.ServiciosTransporte.ControlDeIdentidad.Kafka.Consumers;

import com.servicioTransporte.flota.eventos.conductor.registro.RegistroConductorFallido;
import com.ServiciosTransporte.ControlDeIdentidad.Servicios.ServicioUsuarios;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UsuarioConductorListener {

    private final ServicioUsuarios servicioUsuarios;

    @KafkaListener(topics = "registro-conductor-fallido", groupId = "servicios-flota")
    public void rollBackUser(RegistroConductorFallido evento) {
        servicioUsuarios.deleteUser(String.valueOf(evento.getKeycloakId()));
    }
}