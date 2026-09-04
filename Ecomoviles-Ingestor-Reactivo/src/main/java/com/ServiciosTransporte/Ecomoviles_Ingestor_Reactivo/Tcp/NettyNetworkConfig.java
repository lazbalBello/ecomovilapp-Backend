package com.ServiciosTransporte.Ecomoviles_Ingestor_Reactivo.Tcp;

import com.ServiciosTransporte.Ecomoviles_Ingestor_Reactivo.Services.TelemetryIngestionService;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.util.AttributeKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import reactor.core.publisher.Flux;
import reactor.netty.tcp.TcpServer;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class NettyNetworkConfig {

    @Value("${network.tcp.enabled:true}")
    private boolean tcpEnabled;

    @Value("${network.tcp.port:5001}")
    private int tcpPort;

    private final TelemetryIngestionService ingestionService;
    private static final AttributeKey<ByteArrayOutputStream> BUFFER_KEY = AttributeKey.valueOf("TCP_BUFFER");

    private static String printableAscii(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            int value = b & 0xFF;
            if (value >= 32 && value < 127) {
                sb.append((char) value);
            } else if (value == 9 || value == 10 || value == 13) {
                sb.append(value == 9 ? "\\t" : value == 10 ? "\\n" : "\\r");
            } else {
                sb.append('.');
            }
        }
        return sb.toString();
    }

    private static String classifySocketTraffic(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return "EMPTY";
        }

        String ascii = printableAscii(bytes);
        if (ascii.startsWith(">") || ascii.startsWith("~") || bytes[0] == 0x7E) {
            return "IRIS";
        }
        if (ascii.startsWith("GET ") || ascii.startsWith("POST ") || ascii.startsWith("HEAD ")
                || ascii.contains("HTTP/") || ascii.startsWith("CONNECT ")) {
            return "HTTP/SCANNER";
        }
        return "UNKNOWN";
    }

    private static void logSocketFrame(Channel channel, byte[] bytes, String label) {
        if (bytes == null || bytes.length == 0) {
            return;
        }

        String remote = channel != null && channel.remoteAddress() != null ? channel.remoteAddress().toString() : "unknown";
        String hex = io.netty.buffer.ByteBufUtil.hexDump(bytes);
        String ascii = printableAscii(bytes);
        String classification = classifySocketTraffic(bytes);

        log.info("IRIS {} remote={} length={} kind={} hex={} ascii={}",
                label,
                remote,
                bytes.length,
                classification,
                hex,
                ascii);
    }

    static boolean isLikelyIrisFrame(byte[] bytes) {
        if (bytes == null || bytes.length < 5) {
            return false;
        }

        if (bytes[0] == '>' && bytes[bytes.length - 1] == '<') {
            String ascii = printableAscii(bytes);
            return ascii.startsWith(">32=") || ascii.startsWith(">80=") || ascii.startsWith(">84=");
        }

        return bytes[0] == 0x7E && bytes[bytes.length - 1] == 0x7E;
    }

    static List<byte[]> extractFrames(byte[] data) {
        List<byte[]> packets = new ArrayList<>();
        if (data == null || data.length == 0) {
            return packets;
        }

        int i = 0;
        while (i < data.length) {
            if (data[i] == 0x7E) {
                int end = -1;
                for (int j = i + 1; j < data.length; j++) {
                    if (data[j] == 0x7E) {
                        end = j;
                        break;
                    }
                }
                if (end != -1) {
                    packets.add(Arrays.copyOfRange(data, i, end + 1));
                    i = end + 1;
                } else {
                    break;
                }
            } else if (data[i] == '>') {
                int end = -1;
                for (int j = i + 1; j < data.length; j++) {
                    if (data[j] == '<') {
                        end = j;
                        break;
                    }
                }
                if (end != -1) {
                    packets.add(Arrays.copyOfRange(data, i, end + 1));
                    i = end + 1;
                } else {
                    break;
                }
            } else {
                i++;
            }
        }

        return packets;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void startTcpServer() {
        if (!tcpEnabled) {
            log.info("Servidor TCP IRIS deshabilitado para este perfil/entorno (network.tcp.enabled=false). No se intenta bind en el puerto {}.", tcpPort);
            return;
        }

        TcpServer.create()
                .port(tcpPort)
                .doOnConnection(conn -> {
                    Channel channel = conn.channel();
                    String remote = channel.remoteAddress() != null ? channel.remoteAddress().toString() : "unknown";
                    log.info("IRIS TCP CONNECTED {} -> {}", channel.localAddress(), remote);
                    log.info("IRIS TCP REMOTE ADDRESS local={} remote={}", channel.localAddress(), remote);
                    channel.attr(BUFFER_KEY).set(new ByteArrayOutputStream());
                    channel.closeFuture().addListener(future ->
                            log.info("IRIS TCP CLOSED {} -> {}", channel.localAddress(), remote));
                })
                .handle((inbound, outbound) -> {
                    Channel[] ch = new Channel[1];
                    inbound.withConnection(conn -> ch[0] = conn.channel());
                    Channel channel = ch[0];

                    return inbound.receive()
                            .asByteArray()
                            .doOnNext(bytes -> logSocketFrame(channel, bytes, "RX RAW"))
                            .doOnError(error -> log.error("IRIS SOCKET EXCEPTION remote={} local={}",
                                    channel.remoteAddress(), channel.localAddress(), error))
                            .flatMap(bytes -> {
                                ByteArrayOutputStream bufferStream = channel.attr(BUFFER_KEY).get();

                                if (bufferStream == null) {
                                    bufferStream = new ByteArrayOutputStream();
                                    channel.attr(BUFFER_KEY).set(bufferStream);
                                }

                                try {
                                    bufferStream.write(bytes);
                                } catch (Exception e) {
                                    log.error("Error escribiendo en el buffer TCP", e);
                                }

                                String asciiPreview = new String(bytes, StandardCharsets.US_ASCII)
                                        .replaceAll("[\\x00-\\x1F]", "Â·");
                                if (bytes.length > 0 && (bytes[0] == '>' || bytes[0] == 0x7E)) {
                                    log.info("IRIS RX {} bytes desde {} â†’ ASCII: [{}]",
                                            bytes.length,
                                            channel.remoteAddress(),
                                            asciiPreview);
                                } else {
                                    log.warn("IRIS RX NON-IRIS/TCP DATA remote={} length={} ascii=[{}]",
                                            channel.remoteAddress(),
                                            bytes.length,
                                            asciiPreview);
                                }

                                byte[] data = bufferStream.toByteArray();
                                List<byte[]> packets = extractFrames(data);

                                int lastConsumedIndex = 0;
                                for (byte[] packet : packets) {
                                    lastConsumedIndex = findLastConsumedIndex(data, packet, lastConsumedIndex);
                                }

                                byte[] remaining = Arrays.copyOfRange(data, lastConsumedIndex, data.length);
                                bufferStream.reset();
                                try {
                                    bufferStream.write(remaining);
                                } catch (Exception e) {}

                                return Flux.fromIterable(packets)
                                    .filter(packet -> {
                                        if (!isLikelyIrisFrame(packet)) {
                                            String preview = new String(packet, 0, Math.min(packet.length, 32), StandardCharsets.US_ASCII);
                                            log.debug("Tráfico no IRIS descartado antes del parser. preview={}", preview);
                                            return false;
                                        }
                                        return true;
                                    })
                                    .flatMap(packet -> {
                                        log.debug("Frame completado extraído: {}", ByteBufUtil.hexDump(packet));
                                        if (requiresIrisAck(packet)) {
                                            sendIrisAckIfNeeded(channel, packet);
                                        }
                                        return ingestionService.processRawData(packet, channel);
                                    });
                            })
                            .then()
                            .doFinally(signalType -> log.info("IRIS TCP CLOSED {}", channel.remoteAddress()));
                })
                .bindNow();

        log.info("Servidor TCP con soporte multiprotocolo iniciado en el puerto {}", tcpPort);
    }

    private static int findLastConsumedIndex(byte[] data, byte[] packet, int fromIndex) {
        if (packet == null || packet.length == 0) {
            return fromIndex;
        }

        for (int i = fromIndex; i <= data.length - packet.length; i++) {
            boolean matches = true;
            for (int j = 0; j < packet.length; j++) {
                if (data[i + j] != packet[j]) {
                    matches = false;
                    break;
                }
            }
            if (matches) {
                return i + packet.length;
            }
        }
        return fromIndex;
    }

    static boolean requiresIrisAck(byte[] packet) {
        if (packet == null || packet.length < 4) {
            return false;
        }

        String frame = new String(packet, StandardCharsets.US_ASCII);
        return frame.startsWith(">80=");
    }

    void sendIrisAckIfNeeded(Channel channel, byte[] packet) {
        if (channel == null || packet == null || packet.length < 4 || !requiresIrisAck(packet)) {
            return;
        }

        if (!channel.isActive()) {
            log.warn("IRIS TX descartado porque el socket ya no estÃ¡ activo para {}", channel.remoteAddress());
            return;
        }

        log.warn("IRIS TX bloqueado: el protocolo IRIS 807 no admite un ACK genÃ©rico tipo >ACK<. " +
                "Se requiere un paquete IRIS vÃ¡lido del manual del fabricante; no se escribe ninguna respuesta automÃ¡tica.");
    }
}
