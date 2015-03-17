package ru.uproom.gate.localinterface.zwave.functions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.uproom.gate.localinterface.zwave.driver.ZWaveDriver;
import ru.uproom.gate.localinterface.zwave.driver.ZWaveMessage;
import ru.uproom.gate.localinterface.zwave.enums.ZWaveFunctionID;
import ru.uproom.gate.localinterface.zwave.enums.ZWaveMessageTypes;

/**
 * z-wave function
 * <p/>
 * Created by osipenko on 10.09.14.
 */
@ZWaveFunctionHandlerAnnotation(value = ZWaveFunctionID.GET_SUC_NODE_ID)
public class ZWaveFunctionGetSucNodeIdHandler implements ZWaveFunctionHandler {


    private static final Logger LOG =
            LoggerFactory.getLogger(ZWaveFunctionGetSucNodeIdHandler.class);


    @Override
    public boolean execute(ZWaveMessageTypes messageType, byte[] parameters, ZWaveFunctionHandlePool pool, ZWaveMessage request) {

        ZWaveDriver driver = pool.getDriver();
        driver.setSucNodeId(parameters[0]);

        // maybe must be create handler for SUC in this place

        ZWaveFunctionID functionID = getClass().getAnnotation(ZWaveFunctionHandlerAnnotation.class).value();
        LOG.debug("execute function : {}", functionID.name());

        driver.currentRequestReceived(functionID);
        return false;
    }

}
