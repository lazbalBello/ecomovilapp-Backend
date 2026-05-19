// This is a generated file! Please edit source .ksy file and use kaitai-struct-compiler to rebuild

package com.ServiciosTransporte.Ecomoviles_Ingestor_Reactivo.Infraestructura.Protocols;

import io.kaitai.struct.ByteBufferKaitaiStream;
import io.kaitai.struct.KaitaiStruct;
import io.kaitai.struct.KaitaiStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

public class Jt808Protocol extends KaitaiStruct {
    public static Jt808Protocol fromFile(String fileName) throws IOException {
        return new Jt808Protocol(new ByteBufferKaitaiStream(fileName));
    }

    public Jt808Protocol(KaitaiStream _io) {
        this(_io, null, null);
    }

    public Jt808Protocol(KaitaiStream _io, KaitaiStruct _parent) {
        this(_io, _parent, null);
    }

    public Jt808Protocol(KaitaiStream _io, KaitaiStruct _parent, Jt808Protocol _root) {
        super(_io);
        this._parent = _parent;
        this._root = _root == null ? this : _root;
        _read();
    }
    private void _read() {
        this.startFlag = this._io.readBytes(1);
        if (!(Arrays.equals(this.startFlag, new byte[] { 126 }))) {
            throw new KaitaiStream.ValidationNotEqualError(new byte[] { 126 }, this.startFlag, this._io, "/seq/0");
        }
        this.header = new MsgHeader(this._io, this, _root);
        switch (header().msgId()) {
        case 512: {
            KaitaiStream _io_body = this._io.substream(header().bodyLen());
            this.body = new LocationReport(_io_body, this, _root);
            break;
        }
        default: {
            this.body = this._io.readBytes(header().bodyLen());
            break;
        }
        }
        this.checkSum = this._io.readU1();
        this.endFlag = this._io.readBytes(1);
        if (!(Arrays.equals(this.endFlag, new byte[] { 126 }))) {
            throw new KaitaiStream.ValidationNotEqualError(new byte[] { 126 }, this.endFlag, this._io, "/seq/4");
        }
    }

    public void _fetchInstances() {
        this.header._fetchInstances();
        switch (header().msgId()) {
        case 512: {
            ((LocationReport) (this.body))._fetchInstances();
            break;
        }
        default: {
            break;
        }
        }
    }
    public static class ExtraInfoItem extends KaitaiStruct {
        public static ExtraInfoItem fromFile(String fileName) throws IOException {
            return new ExtraInfoItem(new ByteBufferKaitaiStream(fileName));
        }

        public ExtraInfoItem(KaitaiStream _io) {
            this(_io, null, null);
        }

        public ExtraInfoItem(KaitaiStream _io, Jt808Protocol.LocationReport _parent) {
            this(_io, _parent, null);
        }

        public ExtraInfoItem(KaitaiStream _io, Jt808Protocol.LocationReport _parent, Jt808Protocol _root) {
            super(_io);
            this._parent = _parent;
            this._root = _root;
            _read();
        }
        private void _read() {
            this.id = this._io.readU1();
            this.len = this._io.readU1();
            this.value = this._io.readBytes(len());
        }

        public void _fetchInstances() {
        }
        private int id;
        private int len;
        private byte[] value;
        private Jt808Protocol _root;
        private Jt808Protocol.LocationReport _parent;
        public int id() { return id; }
        public int len() { return len; }
        public byte[] value() { return value; }
        public Jt808Protocol _root() { return _root; }
        public Jt808Protocol.LocationReport _parent() { return _parent; }
    }
    public static class LocationReport extends KaitaiStruct {
        public static LocationReport fromFile(String fileName) throws IOException {
            return new LocationReport(new ByteBufferKaitaiStream(fileName));
        }

        public LocationReport(KaitaiStream _io) {
            this(_io, null, null);
        }

        public LocationReport(KaitaiStream _io, Jt808Protocol _parent) {
            this(_io, _parent, null);
        }

        public LocationReport(KaitaiStream _io, Jt808Protocol _parent, Jt808Protocol _root) {
            super(_io);
            this._parent = _parent;
            this._root = _root;
            _read();
        }
        private void _read() {
            this.alarmFlag = this._io.readU4be();
            this.statusFlag = this._io.readU4be();
            this.latitudeRaw = this._io.readU4be();
            this.longitudeRaw = this._io.readU4be();
            this.altitude = this._io.readU2be();
            this.speedRaw = this._io.readU2be();
            this.direction = this._io.readU2be();
            this.timeBcd = this._io.readBytes(6);
            this.extraInfo = new ArrayList<ExtraInfoItem>();
            {
                int i = 0;
                while (!this._io.isEof()) {
                    this.extraInfo.add(new ExtraInfoItem(this._io, this, _root));
                    i++;
                }
            }
        }

        public void _fetchInstances() {
            for (int i = 0; i < this.extraInfo.size(); i++) {
                this.extraInfo.get(((Number) (i)).intValue())._fetchInstances();
            }
        }
        private long alarmFlag;
        private long statusFlag;
        private long latitudeRaw;
        private long longitudeRaw;
        private int altitude;
        private int speedRaw;
        private int direction;
        private byte[] timeBcd;
        private List<ExtraInfoItem> extraInfo;
        private Jt808Protocol _root;
        private Jt808Protocol _parent;
        public long alarmFlag() { return alarmFlag; }
        public long statusFlag() { return statusFlag; }

        /**
         * Mapear a latitude: latitude_raw / 1000000.0
         */
        public long latitudeRaw() { return latitudeRaw; }

        /**
         * Mapear a longitude: longitude_raw / 1000000.0
         */
        public long longitudeRaw() { return longitudeRaw; }
        public int altitude() { return altitude; }

        /**
         * Mapear a speed: speed_raw / 10.0 (km/h)
         */
        public int speedRaw() { return speedRaw; }
        public int direction() { return direction; }

        /**
         * YYMMDDHHMMSS en formato BCD. Mapear a timestamp
         */
        public byte[] timeBcd() { return timeBcd; }

        /**
         * Aquí buscaremos el batteryLevel si existe
         */
        public List<ExtraInfoItem> extraInfo() { return extraInfo; }
        public Jt808Protocol _root() { return _root; }
        public Jt808Protocol _parent() { return _parent; }
    }
    public static class MsgHeader extends KaitaiStruct {
        public static MsgHeader fromFile(String fileName) throws IOException {
            return new MsgHeader(new ByteBufferKaitaiStream(fileName));
        }

        public MsgHeader(KaitaiStream _io) {
            this(_io, null, null);
        }

        public MsgHeader(KaitaiStream _io, Jt808Protocol _parent) {
            this(_io, _parent, null);
        }

        public MsgHeader(KaitaiStream _io, Jt808Protocol _parent, Jt808Protocol _root) {
            super(_io);
            this._parent = _parent;
            this._root = _root;
            _read();
        }
        private void _read() {
            this.msgId = this._io.readU2be();
            this.bodyProps = this._io.readU2be();
            this.terminalId = this._io.readBytes(6);
            this.msgSerialNo = this._io.readU2be();
        }

        public void _fetchInstances() {
        }
        private Integer bodyLen;
        public Integer bodyLen() {
            if (this.bodyLen != null)
                return this.bodyLen;
            this.bodyLen = ((Number) (bodyProps() & 1023)).intValue();
            return this.bodyLen;
        }
        private int msgId;
        private int bodyProps;
        private byte[] terminalId;
        private int msgSerialNo;
        private Jt808Protocol _root;
        private Jt808Protocol _parent;
        public int msgId() { return msgId; }
        public int bodyProps() { return bodyProps; }

        /**
         * Número de teléfono del terminal (BCD). Mapear a vehicleId
         */
        public byte[] terminalId() { return terminalId; }
        public int msgSerialNo() { return msgSerialNo; }
        public Jt808Protocol _root() { return _root; }
        public Jt808Protocol _parent() { return _parent; }
    }
    private byte[] startFlag;
    private MsgHeader header;
    private Object body;
    private int checkSum;
    private byte[] endFlag;
    private Jt808Protocol _root;
    private KaitaiStruct _parent;
    public byte[] startFlag() { return startFlag; }
    public MsgHeader header() { return header; }
    public Object body() { return body; }
    public int checkSum() { return checkSum; }
    public byte[] endFlag() { return endFlag; }
    public Jt808Protocol _root() { return _root; }
    public KaitaiStruct _parent() { return _parent; }
}
