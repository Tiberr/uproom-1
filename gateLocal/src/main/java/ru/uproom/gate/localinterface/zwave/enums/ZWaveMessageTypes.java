package ru.uproom.gate.localinterface.zwave.enums;

/**
 * Created by osipenko on 14.01.15.
 */
public enum ZWaveMessageTypes {

    Request(0x00),
    Response(0x01);

    private byte code;

    ZWaveMessageTypes(int code) {
        this.code = (byte) code;
    }

    public static ZWaveMessageTypes getByCode(byte code) {

        for (ZWaveMessageTypes value : values()) {
            if (value.getCode() == code) return value;
        }

        return Request;
    }

    public byte getCode() {
        return code;
    }

}
