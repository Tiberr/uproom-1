package ru.uproom.gate.tindenetlib.driver;

/**
 * Created by osipenko on 10.01.15.
 */
public interface TindenetSerialPort {

    public void open();

    public void stop();

    public void restart();

    public void sendRequest(byte[] request);

}
