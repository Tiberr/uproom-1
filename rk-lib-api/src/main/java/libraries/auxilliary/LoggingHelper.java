package libraries.auxilliary;

/**
 * Created by osipenko on 04.02.15.
 */
public class LoggingHelper {

    public static String createHexStringFromByteArray(byte[] bytes) {
        String output = "";
        for (byte b : bytes) {
            output += String.format(" 0x%02X", b);
        }

        return output;
    }

    public static String createHexStringFromIntArray(int[] integers, boolean isByte) {
        String output = "";
        for (int i : integers) {
            if (isByte) {
                if (i < 16) output += " 0x0";
                else output += " 0x";
                output += Integer.toHexString(i);
            } else output += String.format(" 0x%08X", i);
        }

        return output;
    }

}
