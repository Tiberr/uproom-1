package ru.uproom.gate.localinterface.zwave.enums;

/**
 * Created by osipenko on 14.01.15.
 */
public enum ZWaveFrameMarker {

    UNK(0x00), // UNKnown symbol
    SOF(0x01), // Start Of Frame
    ACK(0x06), // ACKnowledge
    NAC(0x15), // Negative ACk
    CAN(0x18); // CANcel

    private byte code;

    ZWaveFrameMarker(int code) {
        this.code = (byte) code;
    }

    public static ZWaveFrameMarker getByCode(byte code) {

        for (ZWaveFrameMarker value : values()) {
            if (value.getCode() == code) return value;
        }

        return UNK;
    }

    public byte getCode() {
        return code;
    }

}
