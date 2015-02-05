package ru.uproom.gate.localinterface.zwave.commands;

/**
 * interface for command classes of Z-Wave
 * <p/>
 * Created by osipenko on 29.01.15.
 */
public interface ZWaveCommandClassFactory {

    public ZWaveCommandClass getCommandClass(byte commandClassId);

}
