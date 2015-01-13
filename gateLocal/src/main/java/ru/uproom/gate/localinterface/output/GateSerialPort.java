package ru.uproom.gate.localinterface.output;

/**
 * Created by osipenko on 10.01.15.
 */
public interface GateSerialPort {

    public void sendRequest(byte[] request);

    public void stop();

}
