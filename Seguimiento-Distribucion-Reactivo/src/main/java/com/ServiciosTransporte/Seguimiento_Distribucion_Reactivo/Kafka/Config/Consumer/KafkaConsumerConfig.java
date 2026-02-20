package com.ServiciosTransporte.Seguimiento_Distribucion_Reactivo.Kafka.Config.Consumer;

import com.servicioTransporte.flota.eventos.vehiculo.seguimiento.TelemetriaVehiculo;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOptions;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.properties.schema.registry.url}")
    private String schemaRegistryUrl;

    @Bean
    public ReceiverOptions<String, TelemetriaVehiculo> receiverOptions() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "servicio-distribucion-group-reactive");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class);
        props.put(KafkaAvroDeserializerConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);
        props.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, true);
        // Optimización para Reactor Kafka
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "500");

        return ReceiverOptions.<String, TelemetriaVehiculo>create(props)
                .subscription(Collections.singleton("vehiculos-entrada-telemetria"));
    }

    @Bean
    public KafkaReceiver<String, TelemetriaVehiculo> kafkaReceiver(ReceiverOptions<String, TelemetriaVehiculo> options) {
        return KafkaReceiver.create(options);
    }
}
