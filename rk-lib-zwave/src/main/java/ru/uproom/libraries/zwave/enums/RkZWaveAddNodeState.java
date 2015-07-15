package ru.uproom.libraries.zwave.enums;

/**
 * Return in Z-Wave Function ADD NODE TO NETWORK
 * <p/>
 * Created by osipenko on 16.01.15.
 */
public enum RkZWaveAddNodeState {

    Unknown(0x00),
    LearnReady(0x01),
    NodeFound(0x02),
    AddingSlave(0x03),
    AddingController(0x04),
    ProtocolDone(0x05),
    Done(0x06),
    Failed(0x07),;

    private int code;

    RkZWaveAddNodeState(int code) {
        this.code = code;
    }

    public static RkZWaveAddNodeState getByCode(int code) {

        for (RkZWaveAddNodeState value : values()) {
            if (value.getCode() == code) return value;
        }

        return Unknown;
    }

    public int getCode() {
        return code;
    }

}
