package com.ServiciosTransporte.Gestion.Kafka.Consumers;

import com.ServiciosTransporte.Gestion.Dto.ConductorDto;
import com.ServiciosTransporte.Gestion.Servicios.ServicioConductor;
import com.servicioTransporte.flota.eventos.conductor.registro.RegistroConductorFallido;
import com.servicioTransporte.flota.eventos.conductor.registro.RegistroConductorIniciado;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegistroConductorConsumer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ServicioConductor servicioConductor;

    @RetryableTopic(attempts = "4", backoff = @Backoff(delay = 3000, multiplier = 1.5, maxDelay = 15000))
    @KafkaListener(topics = "inicio-registro-conductor", groupId = "servicios-flota")
    public void handleSuceso(ConsumerRecord<String, RegistroConductorIniciado> recordEvento){
        RegistroConductorIniciado evento = recordEvento.value();
        ConductorDto conductor = new ConductorDto();
        conductor.setDni(String.valueOf(evento.getDni()));
        conductor.setUsuarioId(String.valueOf(evento.getKeycloakId()));
        conductor.setNombre(String.valueOf(evento.getNombre()));
        conductor.setApellidos(String.valueOf(evento.getApellido()));
        conductor.setCategoriasLicencia(evento.getCategoriasLicencia().stream().map(CharSequence::toString).toList());
        try {
            servicioConductor.registrarConductor(conductor);
        }catch (Exception e){
            RegistroConductorFallido eventoFallido = RegistroConductorFallido.newBuilder()
                    .setKeycloakId(evento.getKeycloakId())
                    .setError(e.getMessage())
                    .setTimestamp(System.currentTimeMillis())
                    .build();
           kafkaTemplate.send("registro-conductor-fallido",String.valueOf(eventoFallido.getKeycloakId()), eventoFallido);
        }
    }

    @DltHandler
    public void dedHandle(ConsumerRecord<String, RegistroConductorIniciado> recordEvento){
        RegistroConductorIniciado evento = recordEvento.value();

        log.info("Dlt Recived: {}, topic; {}, ofset: {}", evento.toString(), recordEvento.topic(), recordEvento.offset());
    }
}
