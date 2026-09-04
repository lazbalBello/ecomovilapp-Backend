package com.ServiciosTransporte.Ecomoviles_Ingestor_Reactivo.Infraestructura;

import com.servicioTransporte.flota.eventos.vehiculo.seguimiento.TelemetriaVehiculo;
import io.netty.channel.Channel;
import reactor.core.publisher.Mono;

public interface ProtocolDecoder {

    // Verifica si los bytes corresponden a este protocolo (ej. mediante Magic Bytes)
    boolean supports(byte[] rawData);

    // Decodifica y mapea al POJO estándar de Avro (sin estado)
    Mono<TelemetriaVehiculo> decode(byte[] rawData);

    // Decodifica y mapea al POJO estándar usando el contexto asociado al canal (con estado)
    default Mono<TelemetriaVehiculo> decode(byte[] rawData, Channel channel) {
        return decode(rawData);
    }
}
