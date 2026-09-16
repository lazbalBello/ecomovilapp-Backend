package com.ServiciosTransporte.Ecomoviles_Ingestor_Reactivo.Kafka.Consumer;

import com.servicioTransporte.flota.eventos.vehiculo.configuracion.DispositivoGpsAutorizado;
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
import java.util.UUID;

@Configuration
public class GpsAuthorizationConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers:localhost:9094}")
    private String bootstrapServers;

    @Value("${spring.kafka.properties.schema.registry.url:http://localhost:8086}")
    private String schemaRegistryUrl;

    public static final String TOPIC_DISPOSITIVOS_AUTORIZADOS = "flota-dispositivos-autorizados";

    @Bean
    public ReceiverOptions<String, DispositivoGpsAutorizado> gpsAuthReceiverOptions() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        // Cada réplica del ingestor usa un identificador único para recibir todo el snapshot compactado
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "ecomoviles-ingestor-gps-auth-" + UUID.randomUUID());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class);
        props.put(KafkaAvroDeserializerConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);
        props.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, "true");
        // Siempre leer desde el inicio para reconstruir el estado actual en memoria
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        return ReceiverOptions.<String, DispositivoGpsAutorizado>create(props)
                .subscription(Collections.singleton(TOPIC_DISPOSITIVOS_AUTORIZADOS));
    }

    @Bean
    public KafkaReceiver<String, DispositivoGpsAutorizado> gpsAuthKafkaReceiver(
            ReceiverOptions<String, DispositivoGpsAutorizado> gpsAuthReceiverOptions) {
        return KafkaReceiver.create(gpsAuthReceiverOptions);
    }
}
