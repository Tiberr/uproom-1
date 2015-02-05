package ru.uproom.gate.localinterface.zwave.driver;

/**
 * Created by osipenko on 10.01.15.
 */
public interface ZWaveSerialPort {

    public void open();

    public void sendRequest(byte[] request);

    public void stop();

}
