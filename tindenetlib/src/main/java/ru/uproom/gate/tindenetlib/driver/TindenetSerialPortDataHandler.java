package ru.uproom.gate.tindenetlib.driver;

/**
 * Created by osipenko on 16.03.15.
 */
public interface TindenetSerialPortDataHandler {

    public void handleMessageFromSerialPort(byte[] message, int length);

}
