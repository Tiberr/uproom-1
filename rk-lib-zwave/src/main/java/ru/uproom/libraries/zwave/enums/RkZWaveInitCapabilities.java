package ru.uproom.libraries.zwave.enums;

/**
 * Return in Z-Wave Function GET_CONTROLLER_CAPABILITIES
 * <p/>
 * Created by osipenko on 16.01.15.
 */
public enum RkZWaveInitCapabilities {

    Unknown(0x00),
    Slave(0x01),
    TimerSupport(0x02),
    Secondary(0x04),
    SUC(0x08);

    private byte code;

    RkZWaveInitCapabilities(int code) {
        this.code = (byte) code;
    }

    public static RkZWaveInitCapabilities getByCode(byte code) {

        for (RkZWaveInitCapabilities value : values()) {
            if (value.getCode() == code) return value;
        }

        return Unknown;
    }

    public byte getCode() {
        return code;
    }


}
