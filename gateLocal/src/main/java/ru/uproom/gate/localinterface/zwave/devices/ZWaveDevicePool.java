package ru.uproom.gate.localinterface.zwave.devices;

/**
 * Created by osipenko on 15.01.15.
 */
public interface ZWaveDevicePool {

    public void addNewDevice(int deviceId);

    public void removeExistingDevice(int deviceId);

}
