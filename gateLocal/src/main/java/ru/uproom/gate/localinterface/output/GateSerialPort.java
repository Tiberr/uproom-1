package ru.uproom.gate.localinterface.output;

/**
 * Created by osipenko on 10.01.15.
 */
public interface GateSerialPort {

    public void sendCommand(String command);

    public void stop();

}
