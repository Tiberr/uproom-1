package ru.uproom.gate.rkjamlib.driver;

/**
 * Created by osipenko on 10.01.15.
 */
public interface RkJamSerialPort {

    public void open();

    public void sendRequest(byte[] request);

    public void stop();

}
