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
@RkZWaveFunctionHandlerAnnotation(value = RkZWaveFunctionID.GET_RANDOM)
public class RkZWaveFunctionGetRandomHandler implements RkZWaveFunctionHandler {


    private static final Logger LOG =
            LoggerFactory.getLogger(RkZWaveFunctionGetRandomHandler.class);


    @Override
    public boolean execute(RkZWaveMessageTypes messageType, int[] parameters,
                           RkZWaveFunctionHandlePool pool, RkZWaveMessage request) {

        RkZWaveFunctionID functionID = getClass().getAnnotation(RkZWaveFunctionHandlerAnnotation.class).value();
        LOG.debug("execute function : {}", new Object[]{
                functionID.name(),
                parameters[0] != 0 ? "true" : "false"
        });

        pool.getDriver().currentRequestReceived(functionID);
        return true;
    }

}
