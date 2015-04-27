package ru.uproom.libraries.zwave.functions;

import ru.uproom.libraries.zwave.driver.RkZWaveMessage;
import ru.uproom.libraries.zwave.enums.RkZWaveMessageTypes;

/**
 * marker interface for classes of command handling
 * </p>
 * Created by osipenko on 30.08.14.
 */
public interface RkZWaveFunctionHandler {

    public boolean execute(
            RkZWaveMessageTypes messageType,
            int[] parameters,
            RkZWaveFunctionHandlePool pool,
            RkZWaveMessage request
    );

}
