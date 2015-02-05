package ru.uproom.gate.localinterface.zwave.functions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.uproom.gate.localinterface.zwave.enums.ZWaveFunctionID;
import ru.uproom.gate.localinterface.zwave.enums.ZWaveMessageTypes;

/**
 * z-wave function
 * <p/>
 * Created by osipenko on 10.09.14.
 */
@ZWaveFunctionHandlerAnnotation(value = ZWaveFunctionID.APPLICATION_COMMAND)
public class ZWaveFunctionApplicationCommandHandler implements ZWaveFunctionHandler {


    private static final Logger LOG =
            LoggerFactory.getLogger(ZWaveFunctionApplicationCommandHandler.class);


    @Override
    public boolean execute(ZWaveMessageTypes messageType, byte[] parameters, ZWaveFunctionHandlePool pool) {

        pool.getDevicePool().applyDeviceParametersFromByteArray(parameters);

        LOG.debug("execute function : {}",
                getClass().getAnnotation(ZWaveFunctionHandlerAnnotation.class).value().name());

        return false;
    }

}