package ru.uproom.gate.localinterface.zwave.driver;

import ru.uproom.gate.localinterface.zwave.enums.ZWaveFunctionID;

/**
 * Created by osipenko on 11.01.15.
 */
public interface ZWaveSerialDataHandler {

    public void stop(boolean restart);

    public void addMessageToSendingQueue(ZWaveMessage message);

    public void addMessageToReceivingQueue(byte[] data);

    public void currentRequestReceived(ZWaveFunctionID functionID);

    public void receiveAcknowledge();

    public void receiveCancel();

    public void receiveNotAcknowledge();

    public void setPortState(boolean open);

}
