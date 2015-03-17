package ru.uproom.gate.localinterface.domain;

/**
 * Created by osipenko on 11.02.15.
 */
public class ExtractingValue {

    private String value;
    private byte scale;
    private byte precision;

    public ExtractingValue(String value, byte scale, byte precision) {
        this.value = value;
        this.scale = scale;
        this.precision = precision;
    }

    public String getValue() {
        return value;
    }

    public byte getScale() {
        return scale;
    }

    public byte getPrecision() {
        return precision;
    }

}
