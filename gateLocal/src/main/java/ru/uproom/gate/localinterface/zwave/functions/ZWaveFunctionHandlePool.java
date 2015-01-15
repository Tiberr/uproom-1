package ru.uproom.gate.localinterface.zwave.functions;

import ru.uproom.gate.localinterface.zwave.driver.ZWaveDriver;

/**
 * Marker interface for command commands pool
 * <p/>
 * Created by osipenko on 06.09.14.
 */
public interface ZWaveFunctionHandlePool {

    public ZWaveDriver getDriver();

    public boolean execute(byte[] function);

}