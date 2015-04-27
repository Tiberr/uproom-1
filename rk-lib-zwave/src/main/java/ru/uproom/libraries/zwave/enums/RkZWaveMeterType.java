package ru.uproom.libraries.zwave.enums;

/**
 * Created by osipenko on 14.01.15.
 */
public enum RkZWaveMeterType {

    Unknown(0x00),
    Electric(0x01),
    Gas(0x02),
    Water(0x03);

    private int code;

    RkZWaveMeterType(int code) {
        this.code = code;
    }

    public static RkZWaveMeterType getByCode(int code) {

        for (RkZWaveMeterType value : values()) {
            if (value.getCode() == code) return value;
        }

        return Unknown;
    }

    public int getCode() {
        return code;
    }

}
