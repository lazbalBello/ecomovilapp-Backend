package com.ServiciosTransporte.Ecomoviles_Ingestor_Reactivo.Infraestructura;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.List;

@Slf4j
@Component
public class DecoderFactory {

    private final List<ProtocolDecoder> decoders;

    // Spring inyecta automaticamente todos los beans que implementan la interfaz ProtocolDecoder
    public DecoderFactory(List<ProtocolDecoder> decoders) {
        this.decoders = decoders;
        log.info("DecoderFactory inicializada con {} protocolos soportados.", decoders.size());
    }

    /**
     * Evalua la trama de bytes contra todos los decodificadores registrados.
     * Es altamente eficiente ya que el metodo supports() usualmente solo revisa
     * los primeros bytes (Magic Bytes).
     * * @param rawData Datos binarios crudos provenientes del socket TCP.
     * @return El decodificador correspondiente o null si no se reconoce el protocolo.
     */
    public ProtocolDecoder getDecoder(byte[] rawData) {
        if (rawData == null || rawData.length == 0) {
            return null;
        }

        return decoders.stream()
                .filter(decoder -> {
                    try {
                        return decoder.supports(rawData);
                    } catch (Exception e) {
                        log.error("Error al evaluar soporte del decodificador: {}", decoder.getClass().getSimpleName(), e);
                        return false;
                    }
                })
                .findFirst()
                .orElse(null);
    }
}
