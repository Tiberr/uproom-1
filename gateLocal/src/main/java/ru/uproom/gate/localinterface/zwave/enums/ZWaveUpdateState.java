package ru.uproom.gate.localinterface.zwave.enums;

/**
 * Created by osipenko on 14.01.15.
 */
public enum ZWaveUpdateState {

    UNKNOWN(0x00),
    SUC_ID(0x10),
    DELETE_DONE(0x20),
    NEW_ID_ASSIGNED(0x40),
    ROUTING_PENDING(0x80),
    NODE_INFO_REQ_FAILED(0x81),
    NODE_INFO_REQ_DONE(0x82),
    NODE_INFO_RECEIVED(0x84);

    private byte code;

    ZWaveUpdateState(int code) {
        this.code = (byte) code;
    }

    public static ZWaveUpdateState getByCode(byte code) {

        for (ZWaveUpdateState value : values()) {
            if (value.getCode() == code) return value;
        }

        return UNKNOWN;
    }

    public byte getCode() {
        return code;
    }

}
