package libraries.auxilliary;

/**
 * Created by osipenko on 11.02.15.
 */
public class ExtractingValue {

    private String value;
    private int scale;
    private int precision;

    public ExtractingValue(String value, int scale, int precision) {
        this.value = value;
        this.scale = scale;
        this.precision = precision;
    }

    public String getValue() {
        return value;
    }

    public int getScale() {
        return scale;
    }

    public int getPrecision() {
        return precision;
    }

}
