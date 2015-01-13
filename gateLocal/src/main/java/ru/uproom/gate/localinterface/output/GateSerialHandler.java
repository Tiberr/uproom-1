package ru.uproom.gate.localinterface.output;

/**
 * Created by osipenko on 11.01.15.
 */
public interface GateSerialHandler {

    public void receiveMessage(byte[] data);

    public void receiveAcknowledge();

    public void receiveCancel();

    public void receiveNotAcknowledge();

}
