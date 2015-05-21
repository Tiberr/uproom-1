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
@RkZWaveFunctionHandlerAnnotation(value = RkZWaveFunctionID.MEMORY_GET_ID)
public class RkZWaveFunctionMemoryGetIDHandler implements RkZWaveFunctionHandler {


    private static final Logger LOG =
            LoggerFactory.getLogger(RkZWaveFunctionMemoryGetIDHandler.class);


    @Override
    public boolean execute(RkZWaveMessageTypes messageType, int[] parameters,
                           RkZWaveFunctionHandlePool pool, RkZWaveMessage request) {

        long homeId = (parameters[0] << 24) | (parameters[1] << 16) |
                (parameters[2] << 8) | parameters[3];
        pool.getDriver().getDevicePool().setParameters(homeId, parameters[4]);

        // todo : handle for command class ControllerReplication (?)

        RkZWaveFunctionID functionID = getClass().getAnnotation(RkZWaveFunctionHandlerAnnotation.class).value();
        LOG.debug("execute function : {}, home ID = {}, controller ID = {}", new Object[]{
                functionID.name(),
                String.format("0x%08x", homeId),
                String.format("0x%02x", parameters[4])
        });

        pool.getDriver().currentRequestReceived(functionID);
        return false;
    }

}
