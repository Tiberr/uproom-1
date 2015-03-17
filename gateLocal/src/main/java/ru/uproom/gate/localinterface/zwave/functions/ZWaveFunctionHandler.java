package ru.uproom.gate.localinterface.zwave.functions;

import ru.uproom.gate.localinterface.zwave.driver.ZWaveMessage;
import ru.uproom.gate.localinterface.zwave.enums.ZWaveMessageTypes;

/**
 * marker interface for classes of command handling
 * </p>
 * Created by osipenko on 30.08.14.
 */
public interface ZWaveFunctionHandler {

    public boolean execute(
            ZWaveMessageTypes messageType,
            byte[] parameters,
            ZWaveFunctionHandlePool pool,
            ZWaveMessage request
    );

}
