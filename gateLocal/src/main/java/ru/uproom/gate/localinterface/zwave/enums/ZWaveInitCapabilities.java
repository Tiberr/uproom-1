package ru.uproom.gate.localinterface.zwave.enums;

/**
 * Return in Z-Wave Function GET_CONTROLLER_CAPABILITIES
 * <p/>
 * Created by osipenko on 16.01.15.
 */
public enum ZWaveInitCapabilities {

    Unknown(0x00),
    Slave(0x01),
    TimerSupport(0x02),
    Secondary(0x04),
    SUC(0x08);

    private byte code;

    ZWaveInitCapabilities(int code) {
        this.code = (byte) code;
    }

    public static ZWaveInitCapabilities getByCode(byte code) {

        for (ZWaveInitCapabilities value : values()) {
            if (value.getCode() == code) return value;
        }

        return Unknown;
    }

    public byte getCode() {
        return code;
    }


}
