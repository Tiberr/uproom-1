package ru.uproom.gate.tindenetlib.driver;

/**
 * Created by osipenko on 18.02.15.
 */
public enum TindenetFrameMarker {

    UNKN(0x00), // UNKnown symbol
    STOF(0x23), // Start Of Frame '#'
    EOF1(0x0D), // End Of Frame 1 'CR'
    EOF2(0x0A), // End Of Frame 2 'LF'
    DLTR(0x20); // Delimiter ' '

    private byte code;

    TindenetFrameMarker(int code) {
        this.code = (byte) code;
    }

    public static TindenetFrameMarker getByCode(byte code) {

        for (TindenetFrameMarker value : values()) {
            if (value.getCode() == code) return value;
        }

        return UNKN;
    }

    public byte getCode() {
        return code;
    }

    public String getCodeAsString() {
        return new String(new byte[]{code});
    }
}
