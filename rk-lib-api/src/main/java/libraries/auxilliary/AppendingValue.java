package libraries.auxilliary;

/**
 * Created by osipenko on 11.02.15.
 */
public class AppendingValue {

    private int value;
    private byte size;
    private byte precision;

    public AppendingValue(int value, byte size, byte precision) {
        this.value = value;
        this.size = size;
        this.precision = precision;
    }

    public int getValue() {
        return value;
    }

    public byte getSize() {
        return size;
    }

    public int getPrecision() {
        return value;
    }

}
