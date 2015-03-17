package ru.uproom.gate.transport.domain;

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

    public static String createHexStringFromIntArray(int[] integers) {
        String output = "";
        for (int i : integers) {
            output += String.format(" 0x%08X", i);
        }

        return output;
    }

}
