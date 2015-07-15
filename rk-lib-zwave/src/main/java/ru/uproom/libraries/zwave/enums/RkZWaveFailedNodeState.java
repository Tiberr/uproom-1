package ru.uproom.libraries.zwave.enums;

/**
 * Return in Z-Wave Function ADD NODE TO NETWORK
 * <p/>
 * Created by osipenko on 16.01.15.
 */
public enum RkZWaveFailedNodeState {

    Unknown(0x00),
    NotPrimaryController(0x02),
    NotFound(0x08),
    RemoveProcessBusy(0x10),
    RemoveFail(0x20),;

    private int code;

    RkZWaveFailedNodeState(int code) {
        this.code = code;
    }

    public static RkZWaveFailedNodeState getByCode(int code) {

        for (RkZWaveFailedNodeState value : values()) {
            if (value.getCode() == code) return value;
        }

        return Unknown;
    }

    public int getCode() {
        return code;
    }

}
