package ru.uproom.gate.localinterface.zwave.functions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * z-wave function
 * <p/>
 * Created by osipenko on 10.09.14.
 */
@ZWaveFunctionHandlerAnnotation(value = ZWaveFunctionID.SEND_SLAVE_NODE_INFO)
public class ZWaveFunctionSendSlaveNodeInfoHandler implements ZWaveFunctionHandler {


    private static final Logger LOG =
            LoggerFactory.getLogger(ZWaveFunctionSendSlaveNodeInfoHandler.class);


    @Override
    public boolean execute(byte[] parameters, ZWaveFunctionHandlePool pool) {

        LOG.debug("execute function : {}",
                getClass().getAnnotation(ZWaveFunctionHandlerAnnotation.class).value().name());

        return false;
    }

}
