package com.ServiciosTransporte.ControlDeIdentidad.Utils;

import com.ServiciosTransporte.ControlDeIdentidad.Dtos.TokenResponse;
import com.ServiciosTransporte.ControlDeIdentidad.Exceptions.Personalizadas.AuthenticationFailedException;
import org.keycloak.representations.AccessTokenResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class TokenClient {

    private final WebClient wc;

    @Value("${Keycloak.client-id}")
    private String clientId;

    @Value("${Keycloak.client-secret}")
    private String clientSecret;

    @Value("${Keycloak.mqtt-public-client-id}")
    private String mqttPublicClientId;

    @Value("${Keycloak.mqtt-public-client-secret}")
    private String mqttPublicClientSecret;

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


    public AccessTokenResponse getPublicMqttToken() {
        MultiValueMap<String,String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "client_credentials");
        form.add("client_id", mqttPublicClientId);
        form.add("client_secret", mqttPublicClientSecret);

        AccessTokenResponse tr = wc.post()
                .uri("/token")
                .bodyValue(form)
                .retrieve()
                .onStatus(status -> status != HttpStatus.OK,
                        resp -> Mono.error(new AuthenticationFailedException(
                                "MQTT public token failed: " + resp.statusCode())))
                .bodyToMono(AccessTokenResponse.class)
                .block();

        assert tr != null;
        return tr;
    }
    public TokenResponse refresh(String refreshToken) {
        MultiValueMap<String,String> form = new LinkedMultiValueMap<>();
        form.add("grant_type",    "refresh_token");
        form.add("client_id",     clientId);
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

        assert tr != null;
        return new TokenResponse(
                tr.getToken(),
                tr.getRefreshToken(),
                tr.getExpiresIn(),
                tr.getRefreshExpiresIn()
        );
    }
}
