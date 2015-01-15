package ru.uproom.gate.localinterface.zwave.functions;

/**
 * marker interface for classes of command handling
 * </p>
 * Created by osipenko on 30.08.14.
 */
public interface ZWaveFunctionHandler {

    public boolean execute(byte[] parameters, ZWaveFunctionHandlePool pool);

}
