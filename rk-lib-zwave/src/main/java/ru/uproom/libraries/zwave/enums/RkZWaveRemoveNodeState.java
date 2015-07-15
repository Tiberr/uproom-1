package ru.uproom.libraries.zwave.enums;

/**
 * Return in Z-Wave Function ADD NODE TO NETWORK
 * <p/>
 * Created by osipenko on 16.01.15.
 */
public enum RkZWaveRemoveNodeState {

    Unknown(0x00),
    LearnReady(0x01),
    NodeFound(0x02),
    RemovingSlave(0x03),
    RemovingController(0x04),
    Done(0x06),
    Failed(0x07),;

    private int code;

    RkZWaveRemoveNodeState(int code) {
        this.code = code;
    }

    public static RkZWaveRemoveNodeState getByCode(int code) {

        for (RkZWaveRemoveNodeState value : values()) {
            if (value.getCode() == code) return value;
        }

        return Unknown;
    }

    public int getCode() {
        return code;
    }

}
