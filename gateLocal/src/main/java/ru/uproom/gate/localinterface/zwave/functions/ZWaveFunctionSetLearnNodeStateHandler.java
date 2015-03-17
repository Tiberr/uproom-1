package ru.uproom.gate.localinterface.zwave.functions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.uproom.gate.localinterface.zwave.driver.ZWaveMessage;
import ru.uproom.gate.localinterface.zwave.enums.ZWaveFunctionID;
import ru.uproom.gate.localinterface.zwave.enums.ZWaveMessageTypes;

/**
 * z-wave function
 * <p/>
 * Created by osipenko on 10.09.14.
 */
@ZWaveFunctionHandlerAnnotation(value = ZWaveFunctionID.SET_LEARN_NODE_STATE)
public class ZWaveFunctionSetLearnNodeStateHandler implements ZWaveFunctionHandler {


    private static final Logger LOG =
            LoggerFactory.getLogger(ZWaveFunctionSetLearnNodeStateHandler.class);


    @Override
    public boolean execute(ZWaveMessageTypes messageType, byte[] parameters, ZWaveFunctionHandlePool pool, ZWaveMessage request) {

        LOG.debug("execute function : {}",
                getClass().getAnnotation(ZWaveFunctionHandlerAnnotation.class).value().name());

        return false;
    }

}
