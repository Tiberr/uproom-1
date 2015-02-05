package ru.uproom.gate.localinterface.zwave.functions;

import ru.uproom.gate.localinterface.zwave.devices.ZWaveDevicePool;
import ru.uproom.gate.localinterface.zwave.driver.ZWaveDriver;

/**
 * Marker interface for command commands pool
 * <p/>
 * Created by osipenko on 06.09.14.
 */
public interface ZWaveFunctionHandlePool {

    public void setDevicePoolParameters(int homeId, byte controllerId);

    public ZWaveDriver getDriver();

    public ZWaveDevicePool getDevicePool();

    public boolean execute(byte[] function);

}