package com.serviciosTransporte_controlDeIdentidad.Servicios;

import com.serviciosTransporte_controlDeIdentidad.Config.PooledKeycloak;
import com.serviciosTransporte_controlDeIdentidad.Dtos.LoginDto;
import com.serviciosTransporte_controlDeIdentidad.Dtos.TokenResponse;
import com.serviciosTransporte_controlDeIdentidad.Dtos.UserDto;
import com.serviciosTransporte_controlDeIdentidad.Exceptions.Personalizadas.AuthenticationFailedException;
import com.serviciosTransporte_controlDeIdentidad.Exceptions.Personalizadas.UserAlreadyExistsException;
import com.serviciosTransporte_controlDeIdentidad.Exceptions.Personalizadas.UserCreationException;
import com.serviciosTransporte_controlDeIdentidad.Utils.IKeycloakService;
import com.serviciosTransporte_controlDeIdentidad.Utils.KeycloakProvider;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.core.Response;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;
import java.net.ConnectException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ServicioUsuarios implements IKeycloakService {

    private final KeycloakProvider keycloakProvider;

    @Override
    public List<UserRepresentation> findAllUsers() {
        return keycloakProvider.getRealmResource()
                .users()
                .list();
    }

    @Override
    public List<UserRepresentation> searchUserByUsername(String username) {
        return keycloakProvider.getRealmResource()
                .users()
                .searchByUsername(username, true);
    }

    @Override
    public void createAdmin(@NonNull UserDto userDto) throws RuntimeException {

        UsersResource users = keycloakProvider.getUserResource();

        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setFirstName(userDto.nombre());
        userRepresentation.setLastName(userDto.apellido());
        userRepresentation.setUsername(userDto.nombreUsuario());
        userRepresentation.setEmail(userDto.email());
        userRepresentation.setEmailVerified(true);
        userRepresentation.setEnabled(true);

        try (Response response = users.create(userRepresentation)) {

            if (response.getStatus() == 201) {
                String path = response.getLocation().getPath();
                String userId = path.substring(path.lastIndexOf("/") + 1);

                CredentialRepresentation credential = new CredentialRepresentation();
                credential.setTemporary(false);
                credential.setType(OAuth2Constants.PASSWORD);
                credential.setValue(userDto.password());

                users.get(userId).resetPassword(credential);

                RealmResource realmResource = keycloakProvider.getRealmResource();

                List<RoleRepresentation> roles = List.of(realmResource.roles().get("admin").toRepresentation());

                realmResource
                        .users()
                        .get(userId)
                        .roles()
                        .realmLevel()
                        .add(roles);
            } else if (response.getStatus() == 409) {
                log.error("Ya está registrado este administrador");
                throw new UserAlreadyExistsException("Ya está registrado este administrador");
            } else {
                log.error("Error al crear el administrador");
                throw new UserCreationException("Error al crear el usuario");
            }
        }
    }

    @Override
    public void createUser(@NonNull UserDto userDto)throws RuntimeException{

        UsersResource users = keycloakProvider.getUserResource();

        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setFirstName(userDto.nombre());
        userRepresentation.setLastName(userDto.apellido());
        userRepresentation.setUsername(userDto.nombreUsuario());
        userRepresentation.setEmail(userDto.email());
        userRepresentation.setEmailVerified(true);
        userRepresentation.setEnabled(true);

        try (Response response = users.create(userRepresentation)) {

            if (response.getStatus() == 201) {
                String path = response.getLocation().getPath();
                String userId = path.substring(path.lastIndexOf("/") + 1);

                CredentialRepresentation credential = new CredentialRepresentation();
                credential.setTemporary(false);
                credential.setType(OAuth2Constants.PASSWORD);
                credential.setValue(userDto.password());

                users.get(userId).resetPassword(credential);

                RealmResource realmResource = keycloakProvider.getRealmResource();

                List<RoleRepresentation> roles = List.of(realmResource.roles().get("user").toRepresentation());

                realmResource
                        .users()
                        .get(userId)
                        .roles()
                        .realmLevel()
                        .add(roles);
            } else if (response.getStatus() == 409) {
                log.error("Ya está registrado este usuario");
                throw new UserAlreadyExistsException("Ya está registrado este usuario");
            } else {
                log.error("Error al crear el usuario");
                throw new UserCreationException("Error al crear el usuario");
            }
        }
    }

    @Override
    public void deleteUser (String userId){
        keycloakProvider.getUserResource()
                .get(userId)
                .remove();
    }

    @Override
    public TokenResponse login(LoginDto loginDto) {
        try {

            try(PooledKeycloak userKeycloak = keycloakProvider.getUserKeycloak(loginDto)) {

                AccessTokenResponse tokenResponse = userKeycloak.getKeycloak().tokenManager().getAccessToken();

                return new TokenResponse(
                        tokenResponse.getToken(),
                        tokenResponse.getRefreshToken(),
                        tokenResponse.getExpiresIn(),
                        tokenResponse.getRefreshExpiresIn()
                );
            }
        } catch (AuthenticationFailedException afe) {
            throw afe;
        } catch (NotAuthorizedException nae) {
            log.error("Error inesperado durante el inicio de sesión: {}", nae.getMessage());
            throw new AuthenticationFailedException("Credenciales inválidas: usuario o contraseña incorrectos");
        } catch (ProcessingException pe) {
            if (pe.getCause() instanceof ConnectException) {
                throw new AuthenticationFailedException("Imposible conectar con el servidor de identidad");
            }
            throw new AuthenticationFailedException(
                    "Error de comunicación con el servidor de identidad: " + pe.getMessage()
            );
        } catch (Exception e) {
            log.error("Error inesperado en login", e);
            throw new AuthenticationFailedException("Error interno al iniciar sesión");
        }
    }
}
