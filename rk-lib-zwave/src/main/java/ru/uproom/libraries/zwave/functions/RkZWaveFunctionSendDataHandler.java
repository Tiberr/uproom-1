package ru.uproom.libraries.zwave.functions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.uproom.libraries.zwave.driver.RkZWaveMessage;
import ru.uproom.libraries.zwave.enums.RkZWaveFunctionID;
import ru.uproom.libraries.zwave.enums.RkZWaveMessageTypes;

/**
 * z-wave function
 * <p/>
 * Created by osipenko on 10.09.14.
 */
@RkZWaveFunctionHandlerAnnotation(value = RkZWaveFunctionID.SEND_DATA)
public class RkZWaveFunctionSendDataHandler implements RkZWaveFunctionHandler {


    private static final Logger LOG =
            LoggerFactory.getLogger(RkZWaveFunctionSendDataHandler.class);


    @Override
    public boolean execute(RkZWaveMessageTypes messageType, int[] parameters,
                           RkZWaveFunctionHandlePool pool, RkZWaveMessage request) {

        LOG.debug("execute function : {} {}", new Object[]{
                getClass().getAnnotation(RkZWaveFunctionHandlerAnnotation.class).value().name(),
                parameters[0] != 0 ? "delivered" : "NOT delivered"
        });

        return false;
    }

}
