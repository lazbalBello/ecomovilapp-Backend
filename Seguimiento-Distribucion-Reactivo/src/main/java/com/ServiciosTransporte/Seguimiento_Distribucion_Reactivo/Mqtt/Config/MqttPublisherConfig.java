package com.ServiciosTransporte.Seguimiento_Distribucion_Reactivo.Mqtt.Config;

import org.eclipse.paho.mqttv5.client.IMqttClient;
import org.eclipse.paho.mqttv5.client.MqttClient;
import org.eclipse.paho.mqttv5.client.MqttConnectionOptions;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.eclipse.paho.mqttv5.client.persist.MemoryPersistence;

import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Configuration
@Slf4j
public class MqttPublisherConfig {

    @Value("${spring.mqtt.url:tcp://localhost:1883}")
    private String mqttUrl;

    @Value("${spring.mqtt.username:Seguimiento-Distribucion-Reactivo}")
    private String username;

    @Value("${spring.mqtt.password:public}")
    private String password;

    @Bean
    public IMqttClient mqttClient() throws MqttException {

        String clientId = "distribucion-reactivo-" + UUID.randomUUID().toString();
        IMqttClient client = new MqttClient(mqttUrl, clientId, new MemoryPersistence());

        MqttConnectionOptions options = new MqttConnectionOptions();
        options.setUserName(username);
        options.setPassword(password.getBytes());
        options.setAutomaticReconnect(true);
        options.setCleanStart(true);
        options.setConnectionTimeout(10);

        try {
            client.connect(options);
            log.info("Conectado a MQTT Broker en {}", mqttUrl);
        } catch (MqttException e) {
            log.error("Fallo al conectar con MQTT Broker en {}: {}", mqttUrl, e.getMessage());
            // No arrojar la excepción para no detener el arranque si EMQX no está listo,
            // pero Spring fallará si el Bean no se inicializa, lo dejamos por ahora
            throw e;
        }

        return client;
    }

    // Bean utilitario para publicar reactivamente
    @Bean
    public MqttPublisher mqttPublisher(IMqttClient mqttClient) {
        return new MqttPublisher(mqttClient);
    }

    public static class MqttPublisher {
        private final IMqttClient mqttClient;

        public MqttPublisher(IMqttClient mqttClient) {
            this.mqttClient = mqttClient;
        }

        public Mono<Void> publicar(String topic, String jsonPayload) {
            return Mono.fromRunnable(() -> {
                try {
                    if (mqttClient.isConnected()) {
                        MqttMessage message = new MqttMessage(jsonPayload.getBytes());
                        message.setQos(0); // QoS 0 para telemetría a browsers web (mayor rendimiento)
                        mqttClient.publish(topic, message);
                        log.info("Publicado en EMQX [{}]: {}", topic, jsonPayload);
                    } else {
                        log.warn("MQTT Client no conectado. Se descartó el msj para {}", topic);
                    }
                } catch (MqttException e) {
                    log.error("Error publicando en MQTT (topic {}): {}", topic, e.getMessage());
                }
            });
        }
    }
}
