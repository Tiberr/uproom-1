package ru.uproom.gate.tindenetlib.driver;

/**
 * Created by osipenko on 16.03.15.
 */
public interface TindenetSerialPortDataHandler {

    public void putMessageToHandlingQueueFromSerialPort(byte[] message, int length);

    public void putMessageToHandlingQueueToSerialPort(TindenetMessage message);

    public void handlePing();

}
