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
@ZWaveFunctionHandlerAnnotation(value = ZWaveFunctionID.SERIAL_API_SET_TIMEOUTS)
public class ZWaveFunctionSerialApiSetTimeoutsHandler implements ZWaveFunctionHandler {


    private static final Logger LOG =
            LoggerFactory.getLogger(ZWaveFunctionSerialApiSetTimeoutsHandler.class);


    @Override
    public boolean execute(ZWaveMessageTypes messageType, byte[] parameters, ZWaveFunctionHandlePool pool, ZWaveMessage request) {

        ZWaveFunctionID functionID = getClass().getAnnotation(ZWaveFunctionHandlerAnnotation.class).value();
        LOG.debug("execute function : {}", functionID.name());

        pool.getDriver().currentRequestReceived(functionID);
        return false;
    }

}
