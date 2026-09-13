package com.ServiciosTransporte.Ecomoviles_Ingestor_Reactivo.Tcp;

import com.ServiciosTransporte.Ecomoviles_Ingestor_Reactivo.Infraestructura.DecoderFactory;
import com.ServiciosTransporte.Ecomoviles_Ingestor_Reactivo.Infraestructura.ProtocolDecoder;
import com.ServiciosTransporte.Ecomoviles_Ingestor_Reactivo.Infraestructura.ProtocolFrameDecoder;
import com.ServiciosTransporte.Ecomoviles_Ingestor_Reactivo.Services.TelemetryIngestionService;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.util.AttributeKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Handler de Netty por conexión que detecta dinámicamente el protocolo del vehículo,
 * realiza framing si es un protocolo delimitado (IRIS) o pasa tramas crudas si es binario (JT808).
 * Extiende ByteToMessageDecoder para garantizar el manejo seguro del ciclo de vida del buffer.
 */
@Slf4j
@RequiredArgsConstructor
public class ProtocolDemultiplexerHandler extends ByteToMessageDecoder {

    private static final AttributeKey<ProtocolDecoder> DECODER_KEY = AttributeKey.valueOf("DETECTED_DECODER");
    private static final AttributeKey<String> DEVICE_ID_KEY = AttributeKey.valueOf("CHANNEL_DEVICE_ID");
    private static final AttributeKey<Boolean> DETECTED_KEY = AttributeKey.valueOf("DETECTION_DONE");

    private final DecoderFactory decoderFactory;
    private final TelemetryIngestionService ingestionService;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        log.debug("Canal TCP {} - Bytes recibidos en socket: {}", ctx.channel().id().asShortText(), in.readableBytes());
        Boolean detected = ctx.channel().attr(DETECTED_KEY).get();

        if (detected == null || !detected) {
            if (in.readableBytes() < 4) {
                return; // Esperar a tener al menos 4 bytes para evaluar los Magic Bytes
            }

            byte[] sample = new byte[Math.min(in.readableBytes(), 64)];
            in.getBytes(in.readerIndex(), sample);

            ProtocolDecoder decoder = decoderFactory.getDecoder(sample);
            ctx.channel().attr(DECODER_KEY).set(decoder);
            ctx.channel().attr(DETECTED_KEY).set(true);

            log.info("Canal TCP {} - Protocolo detectado: {}",
                    ctx.channel().id().asShortText(),
                    decoder != null ? decoder.getClass().getSimpleName() : "Desconocido");
        }

        ProtocolDecoder decoder = ctx.channel().attr(DECODER_KEY).get();
        if (decoder == null) {
            log.warn("Canal TCP {} - Datos descartados: Protocolo no soportado.", ctx.channel().id().asShortText());
            in.skipBytes(in.readableBytes());
            return;
        }

        if (decoder instanceof ProtocolFrameDecoder frameDecoder) {
            List<byte[]> frames = frameDecoder.extractFrames(in);
            String channelDeviceId = ctx.channel().attr(DEVICE_ID_KEY).get();

            for (byte[] frame : frames) {
                String frameStr = new String(frame, StandardCharsets.US_ASCII).trim();
                log.debug("Canal TCP {} - Trama IRIS extraída: {}", ctx.channel().id().asShortText(), frameStr);

                // Detección de trama de identificación de canal (ej. >32=11558885<)
                if (frameStr.startsWith(">32=1") && frameStr.endsWith("<")) {
                    String deviceId = frameStr.substring(5, frameStr.length() - 1).trim();
                    if (!deviceId.isEmpty()) {
                        ctx.channel().attr(DEVICE_ID_KEY).set(deviceId);
                        channelDeviceId = deviceId;
                        log.info("Canal TCP {} - ID de dispositivo registrado: {}", ctx.channel().id().asShortText(), deviceId);
                    }
                    continue;
                }

                // Decodificación de trama IRIS y envío a la tubería de ingesta
                decoder.decode(frame, channelDeviceId)
                        .flatMap(telemetria -> {
                            log.info("Canal TCP {} - Telemetría IRIS decodificada: Vehículo={}, Lat={}, Lon={}",
                                    ctx.channel().id().asShortText(), telemetria.getVehicleId(), telemetria.getLatitude(), telemetria.getLongitude());
                            return ingestionService.processTelemetry(telemetria);
                        })
                        .subscribe();
            }
        } else {
            // Modo legacy (JT808 y protocolos sin framing por delimitadores)
            byte[] rawBytes = new byte[in.readableBytes()];
            in.readBytes(rawBytes);
            log.debug("Canal TCP {} - Tramas binarias enviadas a procesar: {} bytes", ctx.channel().id().asShortText(), rawBytes.length);
            ingestionService.processRawData(rawBytes).subscribe();
        }
    }
}
