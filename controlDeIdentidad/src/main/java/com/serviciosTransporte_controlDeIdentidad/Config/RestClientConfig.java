package com.serviciosTransporte_controlDeIdentidad.Config;

import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.internal.ResteasyClientBuilderImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class RestClientConfig {

    @Bean(destroyMethod = "close")
    public ResteasyClient resteasyClient() {
        return new ResteasyClientBuilderImpl()
                .connectionPoolSize(20)
                .connectTimeout(3, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.SECONDS)
                .build();
    }

}
