package ru.uproom.libraries.zwave.enums;

/**
 * Created by osipenko on 14.01.15.
 */
public enum RkZWaveFrameMarker {

    UNK(0x00), // UNKnown symbol
    SOF(0x01), // Start Of Frame
    ACK(0x06), // ACKnowledge
    NAC(0x15), // Negative ACk
    CAN(0x18); // CANcel

    private int code;

    RkZWaveFrameMarker(int code) {
        this.code = code;
    }

    public static RkZWaveFrameMarker getByCode(int code) {

        for (RkZWaveFrameMarker value : values()) {
            if (value.getCode() == code) return value;
        }

        return UNK;
    }

    public int getCode() {
        return code;
    }

}
