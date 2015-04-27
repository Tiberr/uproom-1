package ru.uproom.libraries.zwave.enums;

/**
 * Created by osipenko on 14.01.15.
 */
public enum RkZWaveUpdateState {

    UNKNOWN(0x00),
    SUC_ID(0x10),
    DELETE_DONE(0x20),
    NEW_ID_ASSIGNED(0x40),
    ROUTING_PENDING(0x80),
    NODE_INFO_REQ_FAILED(0x81),
    NODE_INFO_REQ_DONE(0x82),
    NODE_INFO_RECEIVED(0x84);

    private int code;

    RkZWaveUpdateState(int code) {
        this.code = code;
    }

    public static RkZWaveUpdateState getByCode(int code) {

        for (RkZWaveUpdateState value : values()) {
            if (value.getCode() == code) return value;
        }

        return UNKNOWN;
    }

    public int getCode() {
        return code;
    }

}
