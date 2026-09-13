package com.ServiciosTransporte.Ecomoviles_Ingestor_Reactivo.Infraestructura;

import io.netty.buffer.ByteBuf;
import java.util.List;

/**
 * Contrato para decodificadores de protocolo que requieren extracción de tramas
 * basadas en delimitadores desde un stream continuo TCP.
 */
public interface ProtocolFrameDecoder {

    /**
     * Extrae tramas completas del buffer sin modificar indebidamente el estado si la trama está incompleta.
     *
     * @param buffer Acumulador de bytes de Netty.
     * @return Lista de arreglos de bytes representando las tramas completas extraídas.
     */
    List<byte[]> extractFrames(ByteBuf buffer);
}
