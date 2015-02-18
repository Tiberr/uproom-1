package ru.uproom.gate.rkjamlib.driver;

/**
 * Created by osipenko on 18.02.15.
 */
public enum RkJamFrameMarker {

    UNK(0x00), // UNKnown symbol
    SOF(0x01), // Start Of Frame
    ACK(0x06), // ACKnowledge
    NAC(0x15); // Not ACknowledge

    private byte code;

    RkJamFrameMarker(int code) {
        this.code = (byte) code;
    }

    public static RkJamFrameMarker getByCode(byte code) {

        for (RkJamFrameMarker value : values()) {
            if (value.getCode() == code) return value;
        }

        return UNK;
    }

    public byte getCode() {
        return code;
    }

}
