package ru.uproom.libraries.zwave.enums;

/**
 * Created by osipenko on 05.04.15.
 */
public enum RkZWaveLevelDirection {

    UNKNOWN(0x00),
    UP(0x18),
    DOWN(0x58),
    INC(0xC8),
    DEC(0xC0);

    private int code;

    RkZWaveLevelDirection(int code) {
        this.code = code;
    }

    public static RkZWaveLevelDirection getByCode(int code) {

        for (RkZWaveLevelDirection value : values()) {
            if (value.getCode() == code) return value;
        }

        return UNKNOWN;
    }

    public int getCode() {
        return code;
    }
}
