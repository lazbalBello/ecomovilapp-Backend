package com.ServiciosTransporte.ControlDeIdentidad.Config;

import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RestClientPoolConfig {

    @Bean(destroyMethod = "close")
    public GenericObjectPool<ResteasyClient> loginClientPool() {
        GenericObjectPoolConfig<ResteasyClient> poolConfig = new GenericObjectPoolConfig<>();
        poolConfig.setJmxEnabled(false);
        poolConfig.setMaxTotal(10);
        poolConfig.setMinIdle(2);
        poolConfig.setMaxIdle(5);
        return new GenericObjectPool<>(new ResteasyClientFactory(), poolConfig);
    }
}
