package com.ServiciosTransporte.Ecomoviles_Ingestor_Reactivo.Infraestructura;

import com.servicioTransporte.flota.eventos.vehiculo.seguimiento.TelemetriaVehiculo;
import reactor.core.publisher.Mono;

public interface ProtocolDecoder {

    // Verifica si los bytes corresponden a este protocolo (ej. mediante Magic Bytes)
    boolean supports(byte[] rawData);

    // Decodifica y mapea al POJO estándar de Avro
    Mono<TelemetriaVehiculo> decode(byte[] rawData);
}
