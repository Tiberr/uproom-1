package ru.uproom.libraries.zwave.driver;

/**
 * Created by osipenko on 10.01.15.
 */
public interface RkZWaveSerialPort {

    public void open();

    public void close();

    public void sendRequest(int[] request);

}
