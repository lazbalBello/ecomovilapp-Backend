package com.ServiciosTransporte.Gestion.Kafka.Config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class kafkaTopicConfig {

    @Bean
    public NewTopic topicDeRegistroDeConductores(){
        return TopicBuilder.name("inicio-registro-conductor")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic topicDeSucesoDeRegistroDeConductores(){
        return TopicBuilder.name("suceso-registro-conductor")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic topicDeRegistroDeConductoresFallido(){
        return TopicBuilder.name("registro-conductor-fallido")
                .partitions(3)
                .replicas(1)
                .build();
    }
}
