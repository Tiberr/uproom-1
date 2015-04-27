package ru.uproom.libraries.zwave.devices;

import ru.uproom.gate.transport.dto.DeviceDTO;
import ru.uproom.libraries.zwave.driver.RkZWaveDriver;

import java.util.Properties;

/**
 * Created by osipenko on 15.01.15.
 */
public interface RkZWaveDevicePool {

    public RkZWaveDriver getDriver();

    public void setDriverReady(boolean ready);

    public Properties getProperties();

    public void setParameters(int homeId, int controllerId);

    public void deviceMapProcessing(int[] deviceMap);

    public void addNewDevice(int deviceId);

    public void removeExistingDevice(int deviceId);

    public void updateDeviceInfo(int deviceId, int[] info);

    public void applyDeviceParametersFromDto(DeviceDTO dto);

    public void applyDeviceParametersFromIntArray(int[] data);

}
