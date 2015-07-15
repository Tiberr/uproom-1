package ru.uproom.libraries.zwave.functions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.uproom.libraries.zwave.devices.RkZWaveDevicePool;
import ru.uproom.libraries.zwave.driver.RkZWaveDriver;
import ru.uproom.libraries.zwave.driver.RkZWaveMessage;
import ru.uproom.libraries.zwave.enums.RkZWaveExtraEnums;
import ru.uproom.libraries.zwave.enums.RkZWaveFunctionID;
import ru.uproom.libraries.zwave.enums.RkZWaveMessageTypes;

/**
 * z-wave function
 * <p/>
 * Created by osipenko on 10.09.14.
 */
@RkZWaveFunctionHandlerAnnotation(value = RkZWaveFunctionID.SERIAL_API_GET_INIT_DATA)
public class RkZWaveFunctionSerialApiGetInitDataHandler implements RkZWaveFunctionHandler {

    private static final Logger LOG =
            LoggerFactory.getLogger(RkZWaveFunctionSerialApiGetInitDataHandler.class);


    @Override
    public boolean execute(RkZWaveMessageTypes messageType, int[] parameters,
                           RkZWaveFunctionHandlePool pool, RkZWaveMessage request) {

        RkZWaveDriver driver = pool.getDriver();
        RkZWaveDevicePool devicePool = pool.getDevicePool();

        driver.setControllerSerialApiInfo(parameters[0], parameters[1]);

        if (parameters[2] == RkZWaveExtraEnums.NODES_BITFIELD_LENGTH_IN_BYTES) {
            int[] nodeMap = new int[RkZWaveExtraEnums.NODES_BITFIELD_LENGTH_IN_BYTES];
            System.arraycopy(parameters, 3, nodeMap, 0, RkZWaveExtraEnums.NODES_BITFIELD_LENGTH_IN_BYTES);
            devicePool.deviceMapProcessing(nodeMap);
        }

        RkZWaveFunctionID functionID = getClass().getAnnotation(RkZWaveFunctionHandlerAnnotation.class).value();
        LOG.debug("execute function : {}", functionID.name());

        driver.currentRequestReceived(functionID);

        return true;
    }

}
