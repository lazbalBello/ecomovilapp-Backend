package com.ServiciosTransporte.Ecomoviles_Ingestor_Reactivo.Utils;

import java.io.ByteArrayOutputStream;

public class Jt808Utis {

    public static byte[] unescape(byte[] data) {
        if (data == null || data.length < 2) return data;

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(data[0]); // Mantener el flag de inicio 0x7E

        for (int i = 1; i < data.length - 1; i++) {
            if (data[i] == 0x7D) {
                if (data[i + 1] == 0x01) {
                    out.write(0x7D);
                    i++;
                } else if (data[i + 1] == 0x02) {
                    out.write(0x7E);
                    i++;
                } else {
                    out.write(data[i]);
                }
            } else {
                out.write(data[i]);
            }
        }

        out.write(data[data.length - 1]); // Mantener el flag de fin 0x7E
        return out.toByteArray();
    }
}
