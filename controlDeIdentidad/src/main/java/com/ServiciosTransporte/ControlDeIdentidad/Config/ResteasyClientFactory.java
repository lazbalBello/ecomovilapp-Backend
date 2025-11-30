package com.ServiciosTransporte.ControlDeIdentidad.Config;

import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.internal.ResteasyClientBuilderImpl;
import java.util.concurrent.TimeUnit;

public class ResteasyClientFactory implements PooledObjectFactory<ResteasyClient> {

    @Override
    public PooledObject<ResteasyClient> makeObject() throws Exception {
        ResteasyClient client = new ResteasyClientBuilderImpl()
                .connectionPoolSize(20)
                .connectTimeout(3, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.SECONDS)
                .build();
        return new DefaultPooledObject<>(client);
    }

    @Override
    public void destroyObject(PooledObject<ResteasyClient> pooledObject) throws Exception {
        pooledObject.getObject().close();
    }

    @Override
    public boolean validateObject(PooledObject<ResteasyClient> pooledObject) {
        // realizar una validación del cliente
        return true;
    }

    @Override
    public void activateObject(PooledObject<ResteasyClient> pooledObject) throws Exception {
    }

    @Override
    public void passivateObject(PooledObject<ResteasyClient> pooledObject) throws Exception {
    }
}