package ru.uproom.gate.localinterface.zwave.enums;

/**
 * Created by osipenko on 14.01.15.
 */
public enum ZWaveMeterType {

    Unknown(0x00),
    Electric(0x01),
    Gas(0x02),
    Water(0x03);

    private byte code;

    ZWaveMeterType(int code) {
        this.code = (byte) code;
    }

    public static ZWaveMeterType getByCode(byte code) {

        for (ZWaveMeterType value : values()) {
            if (value.getCode() == code) return value;
        }

        return Unknown;
    }

    public byte getCode() {
        return code;
    }

}
