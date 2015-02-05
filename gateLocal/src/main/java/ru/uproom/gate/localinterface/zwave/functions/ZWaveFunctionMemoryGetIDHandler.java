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
@ZWaveFunctionHandlerAnnotation(value = ZWaveFunctionID.MEMORY_GET_ID)
public class ZWaveFunctionMemoryGetIDHandler implements ZWaveFunctionHandler {


    private static final Logger LOG =
            LoggerFactory.getLogger(ZWaveFunctionMemoryGetIDHandler.class);


    @Override
    public boolean execute(ZWaveMessageTypes messageType, byte[] parameters, ZWaveFunctionHandlePool pool) {

        int homeId = ((int) parameters[0] << 24) | ((int) parameters[1] << 16) |
                ((int) parameters[2] << 8) | (int) parameters[3];
        pool.getDevicePool().setParameters(homeId, parameters[4]);

        // todo : handle for command class ControllerReplication (?)

        ZWaveFunctionID functionID = getClass().getAnnotation(ZWaveFunctionHandlerAnnotation.class).value();
        LOG.debug("execute function : {}, home ID = {}, controller ID = {}", new Object[]{
                functionID.name(),
                String.format("0x%08x", homeId),
                String.format("0x%02x", parameters[4])
        });

        pool.getDriver().currentRequestReceived(functionID);
        return false;
    }

}
