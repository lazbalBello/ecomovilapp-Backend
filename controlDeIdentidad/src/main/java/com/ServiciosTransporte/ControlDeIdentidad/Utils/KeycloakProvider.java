package com.ServiciosTransporte.ControlDeIdentidad.Utils;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class KeycloakProvider {

    @Value("${Keycloak.server-url}")
    private String serverUrl;

    @Value("${keycloak.realm-name}")
    private String realmName;

    @Value("${Keycloak.realm-master}")
    private String realmMaster;

    @Value("${Keycloak.admin-cli}")
    private String adminCli;

    @Value("${Keycloak.user-console}")
    private String userConsole;

    @Value("${Keycloak.user-password}")
    private String passwordConsole;

    @Value("${Keycloak.client-id}")
    private String clientId;

    @Value("${Keycloak.client-secret}")
    private String clientSecret;

    private Keycloak adminClient;

    /** Los roles del realm son estáticos (admin/user/driver): se leen una vez por HTTP y se cachean en RAM. */
    private final Map<String, RoleRepresentation> rolesCache = new ConcurrentHashMap<>();

    @Autowired
    private ResteasyClient resteasyClientCompartido;

    @PostConstruct
    private void buildKeycloak() {
        this.adminClient = KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm(realmMaster)
                .clientId(adminCli)
                .username(userConsole)
                .password(passwordConsole)
                .clientSecret(clientSecret)
                .grantType(OAuth2Constants.PASSWORD)
                .resteasyClient(resteasyClientCompartido)
                .build();
    }

    public RealmResource getRealmResource() {
        return adminClient.realm(realmName);
    }

    public UsersResource getUserResource() {
        return getRealmResource().users();
    }

    public Keycloak getKeycloak() {
        return adminClient;
    }

    /**
     * Devuelve la representación del rol leyéndola de Keycloak solo la primera vez
     * (lazy: si el rol no existe o Keycloak no está disponible, falla igual que antes en ese momento).
     */
    public RoleRepresentation obtenerRol(String nombreRol) {
        return rolesCache.computeIfAbsent(nombreRol, nombre -> {
            RoleRepresentation rol = getRealmResource().roles().get(nombre).toRepresentation();
            log.info("Rol de Keycloak leído y cacheado: {}", nombre);
            return rol;
        });
    }

    @PreDestroy
    public void closeKeycloakInstance() {
        if (adminClient != null) {
            adminClient.close();
        }
    }
}
