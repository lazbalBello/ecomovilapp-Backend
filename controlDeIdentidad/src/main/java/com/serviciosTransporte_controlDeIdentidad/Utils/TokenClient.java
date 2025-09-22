package com.serviciosTransporte_controlDeIdentidad.Utils;

import com.serviciosTransporte_controlDeIdentidad.Dtos.TokenResponse;
import com.serviciosTransporte_controlDeIdentidad.Exceptions.Personalizadas.AuthenticationFailedException;
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
