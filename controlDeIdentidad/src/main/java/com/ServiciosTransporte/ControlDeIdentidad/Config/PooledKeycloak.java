package com.ServiciosTransporte.ControlDeIdentidad.Config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.keycloak.admin.client.Keycloak;

@RequiredArgsConstructor
public class PooledKeycloak implements AutoCloseable{

    @Getter
    private final Keycloak keycloak;
    private final ResteasyClient client;
    private final GenericObjectPool<ResteasyClient> pool;

    @Override
    public void close() throws Exception {
        Exception firstEx = null;
        try {
            pool.returnObject(client);
        } catch (Exception e) {
            firstEx = e;
        }
        if (firstEx != null) {
            throw firstEx;
        }
    }
}
