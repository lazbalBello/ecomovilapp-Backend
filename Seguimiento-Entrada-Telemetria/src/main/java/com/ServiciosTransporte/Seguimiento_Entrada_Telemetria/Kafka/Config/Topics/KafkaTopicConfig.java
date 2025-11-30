package com.ServiciosTransporte.Seguimiento_Entrada_Telemetria.Kafka.Config.Topics;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    public static final String TELEMETRY_TOPIC = "vehiculos-entrada-telemetria";

    @Bean
    public NewTopic telemetryTopic() {
        return TopicBuilder.name(TELEMETRY_TOPIC)
                .partitions(3)
                .replicas(1)
                .configs(java.util.Map.of("min.insync.replicas", "1"))
                .build();
    }
}
