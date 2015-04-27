package ru.uproom.libraries.zwave.functions;

import ru.uproom.libraries.zwave.devices.RkZWaveDevicePool;
import ru.uproom.libraries.zwave.driver.RkZWaveDriver;
import ru.uproom.libraries.zwave.driver.RkZWaveMessage;

/**
 * Marker interface for command commands pool
 * <p/>
 * Created by osipenko on 06.09.14.
 */
public interface RkZWaveFunctionHandlePool {

    public void create();

    public void destroy();

    public RkZWaveDriver getDriver();

    public void setDriver(RkZWaveDriver driver);

    public RkZWaveDevicePool getDevicePool();

    public boolean execute(RkZWaveMessage request, int[] function);

}