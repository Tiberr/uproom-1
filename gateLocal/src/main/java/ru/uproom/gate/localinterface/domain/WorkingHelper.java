package ru.uproom.gate.localinterface.domain;

/**
 * Created by osipenko on 04.02.15.
 */
public class WorkingHelper {

    public static String createHexStringFromByteArray(byte[] bytes) {
        String output = "";
        for (byte b : bytes) {
            output += String.format(" 0x%02X", b);
        }

        return output;
    }

}
