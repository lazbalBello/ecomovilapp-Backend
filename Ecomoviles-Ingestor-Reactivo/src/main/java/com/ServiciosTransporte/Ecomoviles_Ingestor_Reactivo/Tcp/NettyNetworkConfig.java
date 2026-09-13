package com.ServiciosTransporte.Ecomoviles_Ingestor_Reactivo.Tcp;

import com.ServiciosTransporte.Ecomoviles_Ingestor_Reactivo.Infraestructura.DecoderFactory;
import com.ServiciosTransporte.Ecomoviles_Ingestor_Reactivo.Services.TelemetryIngestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import reactor.netty.tcp.TcpServer;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class NettyNetworkConfig {

    private final DecoderFactory decoderFactory;
    private final TelemetryIngestionService ingestionService;

    // Escuchamos el evento de inicio de la aplicación para arrancar el servidor TCP
    @EventListener(ApplicationReadyEvent.class)
    public void startTcpServer() {
        TcpServer.create()
                .port(5001) // Puerto donde apuntarán los dispositivos GPS
                .doOnConnection(conn -> {
                    // Instalamos el demultiplexor dinámico por cada socket TCP conectado
                    conn.addHandlerLast(new ProtocolDemultiplexerHandler(decoderFactory, ingestionService));
                })
                .handle((inbound, outbound) -> inbound.receive().then())
                .bindNow();

        log.info("Servidor TCP para telemetría iniciado en el puerto 5001 (Demultiplexador activo)");
    }
}
