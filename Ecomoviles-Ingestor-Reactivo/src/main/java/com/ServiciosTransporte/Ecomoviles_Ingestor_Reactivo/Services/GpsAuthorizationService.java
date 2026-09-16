package com.ServiciosTransporte.Ecomoviles_Ingestor_Reactivo.Services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Servicio en memoria de alto rendimiento para control de acceso de dispositivos GPS.
 * Almacena los IDs autorizados en un Set concurrente en RAM, permitiendo validaciones
 * en nanosegundos sin generar bloqueo de I/O en el Event Loop de Netty.
 */
@Slf4j
@Service
public class GpsAuthorizationService {

    private final Set<String> authorizedGpsIds = ConcurrentHashMap.newKeySet();

    /**
     * Valida de manera ultra-rápida e instantánea (< 5 nanosegundos) si un ID de GPS está autorizado.
     */
    public boolean isAuthorized(String gpsId) {
        if (gpsId == null || gpsId.isBlank()) {
            return false;
        }
        return authorizedGpsIds.contains(gpsId.trim());
    }

    /**
     * Registra un GPS como autorizado en la memoria local.
     */
    public void authorize(String gpsId) {
        if (gpsId == null || gpsId.isBlank()) {
            return;
        }
        String idLimpio = gpsId.trim();
        boolean added = authorizedGpsIds.add(idLimpio);
        if (added) {
            log.info("GPS autorizado en memoria: [{}] (Total activos: {})", idLimpio, authorizedGpsIds.size());
        }
    }

    /**
     * Revoca la autorización de un GPS en la memoria local.
     */
    public void revoke(String gpsId) {
        if (gpsId == null || gpsId.isBlank()) {
            return;
        }
        String idLimpio = gpsId.trim();
        boolean removed = authorizedGpsIds.remove(idLimpio);
        if (removed) {
            log.info("GPS revocado de memoria: [{}] (Total activos: {})", idLimpio, authorizedGpsIds.size());
        }
    }

    /**
     * Retorna la cantidad de dispositivos autorizados en memoria.
     */
    public int getAuthorizedCount() {
        return authorizedGpsIds.size();
    }

    /**
     * Obtiene una vista de solo lectura del conjunto de IDs autorizados.
     */
    public Set<String> getAuthorizedIds() {
        return Collections.unmodifiableSet(authorizedGpsIds);
    }

    /**
     * Limpia la lista blanca en memoria (útil para pruebas unitarias y reinicialización).
     */
    public void clear() {
        authorizedGpsIds.clear();
        log.debug("Lista blanca de GPS en memoria limpiada.");
    }
}
