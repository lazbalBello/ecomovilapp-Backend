package com.ServiciosTransporte.ControlDeIdentidad.Servicios;

import com.ServiciosTransporte.ControlDeIdentidad.Utils.KeycloakProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Worker asíncrono para el flujo de recuperación de contraseña.
 *
 * <p>Se extrae en un {@code @Component} separado para que el proxy de Spring pueda
 * interceptar {@code @Async} correctamente (las llamadas {@code this.metodo()} dentro
 * de la misma clase no pasan por el proxy y el async no surte efecto).</p>
 *
 * <p>El método captura <b>todas</b> las excepciones internamente: nunca propaga
 * errores al hilo HTTP que lo disparó, y el pool está acotado (ver
 * {@code AsyncConfig}) para evitar que fallas de Keycloak o del servidor SMTP
 * monopolicen los hilos de la aplicación.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RecuperacionPasswordWorker {

    private final KeycloakProvider keycloakProvider;

    /**
     * Busca el usuario en Keycloak por email y, si existe, lanza el flujo
     * {@code UPDATE_PASSWORD} que envía el correo de restablecimiento.
     *
     * <p>El método es {@code @Async} y devuelve {@link CompletableFuture}{@code <Void>}
     * para permitir el monitoreo opcional desde tests; en producción el caller
     * no espera la resolución del future.</p>
     *
     * @param email dirección de correo del solicitante
     * @return future completado (siempre) — los errores se loguean y no se propagan
     */
    @Async("recuperacionPasswordExecutor")
    public CompletableFuture<Void> enviarCorreoRecuperacion(String email) {
        try {
            List<UserRepresentation> usuarios = keycloakProvider.getRealmResource()
                    .users()
                    .searchByEmail(email, true);

            if (usuarios != null && !usuarios.isEmpty()) {
                String userId = usuarios.get(0).getId();
                keycloakProvider.getUserResource()
                        .get(userId)
                        .executeActionsEmail(List.of("UPDATE_PASSWORD"));
                log.info("Correo de restablecimiento enviado para el usuario {}", userId);
            } else {
                // Log a nivel DEBUG para no facilitar enumeración de emails en logs de producción
                log.debug("Solicitud de recuperación para email no registrado");
            }
        } catch (Exception e) {
            // Captura total: errores de Keycloak, SMTP, timeout, etc.
            // No se relanza para no dejar el CompletableFuture en estado excepcional
            // ni bloquear el pool con tareas que nunca terminan.
            log.error("Error asíncrono en recuperación de contraseña: {}", e.getMessage());
        }
        return CompletableFuture.completedFuture(null);
    }
}
