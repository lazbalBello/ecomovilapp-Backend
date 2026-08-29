package com.ServiciosTransporte.Ecomoviles_Ingestor_Reactivo.Tcp;

import io.netty.buffer.ByteBuf;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class NettyNetworkConfigTest {

    @Test
    void shouldNotWriteGenericAckForIrisPacket80() {
        NettyNetworkConfig config = new NettyNetworkConfig(null);
        EmbeddedChannel channel = new EmbeddedChannel();

        byte[] packet = ">80=+4048128-0037032907770000001220040116094239AA21<".getBytes(StandardCharsets.US_ASCII);

        config.sendIrisAckIfNeeded(channel, packet);

        assertNull(channel.readOutbound());
    }

    @Test
    void shouldNotWriteAckForIrisPacket32() {
        NettyNetworkConfig config = new NettyNetworkConfig(null);
        EmbeddedChannel channel = new EmbeddedChannel();

        byte[] packet = ">32=987654321012345<".getBytes(StandardCharsets.US_ASCII);

        config.sendIrisAckIfNeeded(channel, packet);

        assertNull(channel.readOutbound());
    }
}
