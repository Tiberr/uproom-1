package ru.uproom.gate.localinterface.zwave.commands;

import ru.uproom.gate.localinterface.zwave.devices.ZWaveDevice;
import ru.uproom.gate.localinterface.zwave.devices.ZWaveDeviceParameter;
import ru.uproom.gate.localinterface.zwave.enums.ZWaveCommandClassNames;

/**
 * interface for command classes of Z-Wave
 * <p/>
 * Created by osipenko on 29.01.15.
 */
public interface ZWaveCommandClass {

    public byte getId();

    public ZWaveCommandClassNames getName();

    public byte getVersion();

    public void setVersion(byte version);

    public int createParameterList(ZWaveDevice device, byte instance);

    public void createInstance(ZWaveDevice device, byte instance);

    public void createInstances(ZWaveDevice device, byte instances);

    public void messageHandler(ZWaveDevice device, byte[] data);

    public void messageHandler(ZWaveDevice device, byte[] data, byte instance);

    public void requestDeviceState(ZWaveDevice device);

    public void requestDeviceParameter(ZWaveDevice device);

    public void setDeviceParameter(ZWaveDeviceParameter parameter, String value);

}
