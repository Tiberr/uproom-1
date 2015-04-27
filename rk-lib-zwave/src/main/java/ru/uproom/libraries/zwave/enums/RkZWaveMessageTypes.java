package ru.uproom.libraries.zwave.enums;

/**
 * Created by osipenko on 14.01.15.
 */
public enum RkZWaveMessageTypes {

    Request(0x00),
    Response(0x01);

    private int code;

    RkZWaveMessageTypes(int code) {
        this.code = code;
    }

    public static RkZWaveMessageTypes getByCode(int code) {

        for (RkZWaveMessageTypes value : values()) {
            if (value.getCode() == code) return value;
        }

        return Request;
    }

    public int getCode() {
        return code;
    }

}
