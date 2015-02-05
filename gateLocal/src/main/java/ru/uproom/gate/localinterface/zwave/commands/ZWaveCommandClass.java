package ru.uproom.gate.localinterface.zwave.commands;

import ru.uproom.gate.localinterface.zwave.devices.ZWaveDevice;

/**
 * interface for command classes of Z-Wave
 * <p/>
 * Created by osipenko on 29.01.15.
 */
public interface ZWaveCommandClass {

    public byte getId();

    public byte getVersion();

    public void setVersion(byte version);

    public int createParameterList(ZWaveDevice device);

    public void messageHandler(ZWaveDevice device, byte[] data);

}
