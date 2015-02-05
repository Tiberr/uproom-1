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
@ZWaveFunctionHandlerAnnotation(value = ZWaveFunctionID.REQUEST_NODE_INFO)
public class ZWaveFunctionRequestNodeInfoHandler implements ZWaveFunctionHandler {


    private static final Logger LOG =
            LoggerFactory.getLogger(ZWaveFunctionRequestNodeInfoHandler.class);


    @Override
    public boolean execute(ZWaveMessageTypes messageType, byte[] parameters, ZWaveFunctionHandlePool pool) {

        String result = "request successful";
        if (parameters[0] == 0) result = "request failed";

        ZWaveFunctionID functionID = getClass().getAnnotation(ZWaveFunctionHandlerAnnotation.class).value();
        LOG.debug("execute function : {}; {}", new Object[]{
                functionID.name(),
                result
        });

        // if request failed - remove corresponding sending message
        if (parameters[0] == 0)
            pool.getDriver().currentRequestReceived(functionID);
        // else current message will be removed in ZWaveFunctionID.APPLICATION_UPDATE handler

        return false;
    }

}
