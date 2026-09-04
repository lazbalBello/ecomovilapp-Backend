package com.ServiciosTransporte.Ecomoviles_Ingestor_Reactivo.Tcp;

import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    @Test
    void shouldRecognizeRealIris84Frame() {
        byte[] packet = ">84=1558885+2192325-0794445600440000581320070116045753030000000000<".getBytes(StandardCharsets.US_ASCII);

        assertTrue(NettyNetworkConfig.isLikelyIrisFrame(packet));
    }

    @Test
    void shouldRejectHttpScannerTraffic() {
        byte[] packet = "GET / HTTP/1.0\r\nHost: 127.0.0.1:5001\r\nUser-Agent: portwarp-server-detector/1\r\n".getBytes(StandardCharsets.US_ASCII);

        assertFalse(NettyNetworkConfig.isLikelyIrisFrame(packet));
    }

    @Test
    void shouldRejectUnknownBinaryTraffic() {
        byte[] packet = new byte[] {
                0x13, 0x00, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
                0x0f, 0x09, 'l', 'o', 'c', 'a', 'l', 'h', 'o', 's', 't', 0x13, (byte) 0x89,
                0x01, 0x01, 0x00
        };

        assertFalse(NettyNetworkConfig.isLikelyIrisFrame(packet));
    }

    @Test
    void shouldExtractMultipleFramesFromSameChunk() {
        byte[] data = (
                ">84=1558885+2192325-0794445600440000581320070116045753030000000000<" +
                ">80=+4048128-0037032907770000001220040116094239AA21<"
        ).getBytes(StandardCharsets.US_ASCII);

        List<byte[]> frames = NettyNetworkConfig.extractFrames(data);

        assertEquals(2, frames.size());
        assertTrue(NettyNetworkConfig.isLikelyIrisFrame(frames.get(0)));
        assertTrue(NettyNetworkConfig.isLikelyIrisFrame(frames.get(1)));
    }

    @Test
    void shouldExtractFragmentedIrisFrameAcrossChunks() {
        byte[] firstChunk = ">84=1558885+2192325-0794445600440000581320070116045753030000000000".getBytes(StandardCharsets.US_ASCII);
        byte[] secondChunk = "<".getBytes(StandardCharsets.US_ASCII);

        List<byte[]> frames = NettyNetworkConfig.extractFrames(concat(firstChunk, secondChunk));

        assertEquals(1, frames.size());
        assertTrue(NettyNetworkConfig.isLikelyIrisFrame(frames.get(0)));
    }

    private byte[] concat(byte[] left, byte[] right) {
        byte[] merged = new byte[left.length + right.length];
        System.arraycopy(left, 0, merged, 0, left.length);
        System.arraycopy(right, 0, merged, left.length, right.length);
        return merged;
    }
}
