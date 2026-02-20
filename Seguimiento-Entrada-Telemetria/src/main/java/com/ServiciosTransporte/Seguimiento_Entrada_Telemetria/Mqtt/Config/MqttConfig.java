package com.ServiciosTransporte.Seguimiento_Entrada_Telemetria.Mqtt.Config;

import org.springframework.context.annotation.Configuration;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.core.MessageProducer;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.messaging.MessageChannel;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class MqttConfig {

    @Value("${mqtt.broker-url}")
    private String brokerUrl;

    @Value("${mqtt.client-id}")
    private String clientId;

    @Value("${mqtt.topic}")
    private String topic;

    @Value("${mqtt.username}")
    private String username;

    @Value("${mqtt.password}")
    private String password;

    @Bean
    public MqttPahoClientFactory mqttClientFactory() {
        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
        MqttConnectOptions options = new MqttConnectOptions();
        options.setServerURIs(new String[] { brokerUrl });
        options.setCleanSession(true); // O false si quieres persistencia QoS (Cambiar en prod)
        options.setAutomaticReconnect(true);
        options.setKeepAliveInterval(60);
        options.setUserName(username);
        options.setPassword(password.toCharArray());
        log.info("📥 Kafka Reactivo: Procesando lote de {} eventos", password);
        factory.setConnectionOptions(options);
        return factory;
    }

    // Canal de entrada: Aquí llegan los mensajes crudos
    @Bean
    public MessageChannel mqttInputChannel() {
        return new DirectChannel();
    }

    // El Adapter que escucha a EMQX y pone los mensajes en el canal
    @Bean
    public MessageProducer inbound() {
        String uniqueClientId = clientId + "-" + System.currentTimeMillis();

        MqttPahoMessageDrivenChannelAdapter adapter = new MqttPahoMessageDrivenChannelAdapter(uniqueClientId,
                mqttClientFactory(), topic);

        adapter.setCompletionTimeout(5000);
        adapter.setConverter(new DefaultPahoMessageConverter());
        adapter.setQos(1);
        adapter.setOutputChannel(mqttInputChannel());
        return adapter;
    }
}
