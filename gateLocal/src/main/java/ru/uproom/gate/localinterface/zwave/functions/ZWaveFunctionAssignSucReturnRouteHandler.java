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
@ZWaveFunctionHandlerAnnotation(value = ZWaveFunctionID.ASSIGN_SUC_RETURN_ROUTE)
public class ZWaveFunctionAssignSucReturnRouteHandler implements ZWaveFunctionHandler {


    private static final Logger LOG =
            LoggerFactory.getLogger(ZWaveFunctionAssignSucReturnRouteHandler.class);


    @Override
    public boolean execute(ZWaveMessageTypes messageType, byte[] parameters, ZWaveFunctionHandlePool pool) {

        LOG.debug("execute function : {}",
                getClass().getAnnotation(ZWaveFunctionHandlerAnnotation.class).value().name());

        return false;
    }

}
