package com.ServiciosTransporte.Gestion.Validacion;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.hibernate.Session;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class  HibernateFiltrerEnabler{

    @PersistenceContext
    private EntityManager entityManager;

    @Before( "within(com.EcoTransporte.GestionSeguimiento.Servicios..*)")
    public void enabledFilter(){
        Session session = entityManager.unwrap(Session.class);
        if (session.getEnabledFilter("notDeletedFilter") == null) {
            session.enableFilter("notDeletedFilter");
        }
    }
}


