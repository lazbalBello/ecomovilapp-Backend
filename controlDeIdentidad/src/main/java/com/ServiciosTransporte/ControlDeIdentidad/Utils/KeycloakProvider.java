package com.ServiciosTransporte.ControlDeIdentidad.Utils;

import com.ServiciosTransporte.ControlDeIdentidad.Config.PooledKeycloak;
import com.ServiciosTransporte.ControlDeIdentidad.Dtos.LoginDto;
import com.ServiciosTransporte.ControlDeIdentidad.Exceptions.Personalizadas.AuthenticationFailedException;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.ws.rs.NotAuthorizedException;
import java.net.ConnectException;
import jakarta.ws.rs.ProcessingException;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

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

    @Autowired
    private ResteasyClient resteasyClientCompartido;

    @Autowired
    private GenericObjectPool<ResteasyClient> loginClientPool;

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

    public   UsersResource getUserResource(){
        RealmResource realmResource = getRealmResource();
        return realmResource.users();
    }

    public Keycloak getKeycloak(){
        return adminClient;
    }

    public PooledKeycloak getUserKeycloak(LoginDto loginDto) {
        try {
            ResteasyClient tempClient =  loginClientPool.borrowObject();
            Keycloak keycloak = KeycloakBuilder.builder()
                    .serverUrl(serverUrl)
                    .realm(realmName)
                    .clientId(clientId)
                    .clientSecret(clientSecret)
                    .username(loginDto.email())
                    .password(loginDto.password())
                    .grantType(OAuth2Constants.PASSWORD)
                    .resteasyClient(tempClient)
                    .build();
            return new PooledKeycloak(keycloak,tempClient,loginClientPool);
        } catch (NotAuthorizedException e) {
            throw new AuthenticationFailedException("Credenciales inválidas: usuario o contraseña incorrectos");
        } catch (ProcessingException e) {
            if (e.getCause() instanceof ConnectException) {
                throw new AuthenticationFailedException("No se pudo conectar al servidor de autenticación");
            }
            throw new AuthenticationFailedException("Error en el procesamiento de la autenticación: " + e.getMessage());
        } catch (Exception e) {
            throw new AuthenticationFailedException("Error durante la autenticación: " + e.getMessage());
        }
    }

    @PreDestroy
    public void closeKeycloakInstance() {
        if (adminClient != null) {
            adminClient.close();
        }
    }
}
