package ru.uproom.libraries.zwave.commands;

/**
 * interface for command classes of Z-Wave
 * <p/>
 * Created by osipenko on 29.01.15.
 */
public interface RkZWaveCommandClassFactory {

    public void create();

    public void destroy();

    public RkZWaveCommandClass getCommandClass(int commandClassId);

}
