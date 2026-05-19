package com.ServiciosTransporte.Ecomoviles_Ingestor_Reactivo.Tcp;

import com.ServiciosTransporte.Ecomoviles_Ingestor_Reactivo.Services.TelemetryIngestionService;
import io.netty.buffer.ByteBufUtil;
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

    private final TelemetryIngestionService ingestionService;

    // Escuchamos el evento de inicio de la aplicación para arrancar el servidor TCP
    @EventListener(ApplicationReadyEvent.class)
    public void startTcpServer() {
        TcpServer.create()
                .port(5001) // Puerto donde apuntarás tus dispositivos GPS
                .handle((inbound, outbound) ->
                        inbound.receive()
                                .asByteArray() // Recibimos los bytes crudos del socket
                                .flatMap(bytes -> {
                                    log.debug("Datos recibidos: {}", ByteBufUtil.hexDump(bytes));
                                    // Se los pasamos al orquestador que ya tiene los decodificadores
                                    return ingestionService.processRawData(bytes);
                                })
                                .then()
                )
                .bindNow();

        log.info("Servidor TCP para telemetría iniciado en el puerto 5001");
    }
}
