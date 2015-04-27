package ru.uproom.libraries.zwave.enums;

/**
 * Return in Z-Wave Function GET_CONTROLLER_CAPABILITIES
 * <p/>
 * Created by osipenko on 16.01.15.
 */
public enum RkZWaveControllerCapabilities {

    Unknown(0x00),
    Secondary(0x01),
    OnOtherNetwork(0x02),
    SIS(0x04),
    RealPrimary(0x08),
    SUC(0x10);

    private byte code;

    RkZWaveControllerCapabilities(int code) {
        this.code = (byte) code;
    }

    public static RkZWaveControllerCapabilities getByCode(byte code) {

        for (RkZWaveControllerCapabilities value : values()) {
            if (value.getCode() == code) return value;
        }

        return Unknown;
    }

    public byte getCode() {
        return code;
    }


}
