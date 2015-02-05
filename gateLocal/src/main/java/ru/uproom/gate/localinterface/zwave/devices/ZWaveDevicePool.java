package ru.uproom.gate.localinterface.zwave.devices;

import ru.uproom.gate.localinterface.zwave.commands.ZWaveCommandClassFactoryImpl;
import ru.uproom.gate.localinterface.zwave.driver.ZWaveDriver;
import ru.uproom.gate.transport.dto.DeviceDTO;

/**
 * Created by osipenko on 15.01.15.
 */
public interface ZWaveDevicePool {

    public ZWaveDriver getDriver();

    public ZWaveCommandClassFactoryImpl getCommandClassFactory();

    public void applyDeviceSet(boolean finished);

    public void setParameters(int homeId, byte controllerId);

    public void addNewDevice(int deviceId);
    public void removeExistingDevice(int deviceId);

    public void updateDeviceInfo(int deviceId, byte[] info);

    public void applyDeviceParametersFromDto(DeviceDTO dto);

    public void applyDeviceParametersFromByteArray(byte[] data);

}
