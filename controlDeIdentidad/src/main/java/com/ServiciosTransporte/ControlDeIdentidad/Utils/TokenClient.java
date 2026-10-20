package com.ServiciosTransporte.ControlDeIdentidad.Utils;

import com.ServiciosTransporte.ControlDeIdentidad.Dtos.TokenResponse;
import com.ServiciosTransporte.ControlDeIdentidad.Exceptions.Personalizadas.AuthenticationFailedException;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.representations.AccessTokenResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Mono;

import java.net.ConnectException;

@Slf4j
@Component
public class TokenClient {

    private final WebClient wc;

    @Value("${Keycloak.client-id}")
    private String clientId;

    @Value("${Keycloak.client-secret}")
    private String clientSecret;

    @Value("${Keycloak.guest-client-id}")
    private String guestClientId;

    @Value("${Keycloak.guest-client-secret}")
    private String guestClientSecret;

    public TokenClient(@Value("${Keycloak.server-url}") String serverUrl,
                       @Value("${keycloak.realm-name}") String realmName,
                       WebClient.Builder wb) {
        this.wc = wb
                .baseUrl(serverUrl + "/realms/" + realmName
                        + "/protocol/openid-connect")
                .defaultHeader(HttpHeaders.CONTENT_TYPE,
                        MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .build();
    }

    /**
     * Intercambio de credenciales por tokens contra el token endpoint (grant_type=password).
     * Sustituye al flujo previo con KeycloakBuilder + pool de ResteasyClients: una sola conexión
     * HTTP compartida y el mismo mapeo de errores que antes.
     */
    public TokenResponse login(String username, String password) {
        try {
            AccessTokenResponse tr = wc.post()
                    .uri("/token")
                    .bodyValue(formularioLogin(username, password))
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError(), resp -> {
                        if (resp.statusCode().value() == HttpStatus.UNAUTHORIZED.value()) {
                            return Mono.error(new AuthenticationFailedException(
                                    "Credenciales inválidas: usuario o contraseña incorrectos"));
                        }
                        return Mono.error(new AuthenticationFailedException(
                                "Error de comunicación con el servidor de identidad: " + resp.statusCode()));
                    })
                    .bodyToMono(AccessTokenResponse.class)
                    .block();

            return mapearToken(tr, "Error interno al iniciar sesión");
        } catch (AuthenticationFailedException afe) {
            throw afe;
        } catch (WebClientRequestException wcre) {
            if (wcre.getCause() instanceof ConnectException) {
                throw new AuthenticationFailedException("Imposible conectar con el servidor de identidad");
            }
            throw new AuthenticationFailedException("Error de comunicación con el servidor de identidad: " + wcre.getMessage());
        } catch (Exception e) {
            log.error("Error inesperado en login", e);
            throw new AuthenticationFailedException("Error interno al iniciar sesión");
        }
    }

    public TokenResponse refresh(String refreshToken) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "refresh_token");
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);
        form.add("refresh_token", refreshToken);

        AccessTokenResponse tr = wc.post()
                .uri("/token")
                .bodyValue(form)
                .retrieve()
                .onStatus(status -> status != HttpStatus.OK,
                        resp -> Mono.error(new AuthenticationFailedException(
                                "Refresh failed: " + resp.statusCode())))
                .bodyToMono(AccessTokenResponse.class)
                .block();

        return mapearToken(tr, "El servidor de identidad no devolvió un token");
    }

    public TokenResponse getGuestToken() {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "client_credentials");
        form.add("client_id", guestClientId);
        form.add("client_secret", guestClientSecret);

        AccessTokenResponse tr = wc.post()
                .uri("/token")
                .bodyValue(form)
                .retrieve()
                .onStatus(status -> status != HttpStatus.OK,
                        resp -> Mono.error(new AuthenticationFailedException(
                                "Guest token request failed: " + resp.statusCode())))
                .bodyToMono(AccessTokenResponse.class)
                .block();

        return mapearToken(tr, "El servidor de identidad no devolvió un token");
    }

    /**
     * Sustituye a los `assert` (que no se ejecutan en producción sin -ea): si la respuesta no trae
     * token (p. ej. 200 con cuerpo vacío) se escala explícitamente como error de autenticación.
     */
    private TokenResponse mapearToken(AccessTokenResponse tr, String mensajePorDefecto) {
        if (tr == null || tr.getToken() == null) {
            throw new AuthenticationFailedException(mensajePorDefecto);
        }
        return new TokenResponse(
                tr.getToken(),
                tr.getRefreshToken(),
                tr.getExpiresIn(),
                tr.getRefreshExpiresIn()
        );
    }

    private MultiValueMap<String, String> formularioLogin(String username, String password) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "password");
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);
        form.add("username", username);
        form.add("password", password);
        return form;
    }
}
