package ru.uproom.gate.localinterface.zwave.functions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.uproom.gate.localinterface.zwave.driver.ZWaveDriver;
import ru.uproom.gate.localinterface.zwave.driver.ZWaveMessage;
import ru.uproom.gate.localinterface.zwave.enums.ZWaveExtraEnums;
import ru.uproom.gate.localinterface.zwave.enums.ZWaveFunctionID;
import ru.uproom.gate.localinterface.zwave.enums.ZWaveMessageTypes;

/**
 * z-wave function
 * <p/>
 * Created by osipenko on 10.09.14.
 */
@ZWaveFunctionHandlerAnnotation(value = ZWaveFunctionID.SERIAL_API_GET_INIT_DATA)
public class ZWaveFunctionSerialApiGetInitDataHandler implements ZWaveFunctionHandler {

    private static final Logger LOG =
            LoggerFactory.getLogger(ZWaveFunctionSerialApiGetInitDataHandler.class);


    @Override
    public boolean execute(ZWaveMessageTypes messageType, byte[] parameters, ZWaveFunctionHandlePool pool, ZWaveMessage request) {

        ZWaveDriver driver = pool.getDriver();

        driver.setControllerSerialApiInfo(parameters[0], parameters[1]);

        if (parameters[2] == ZWaveExtraEnums.NODES_BITFIELD_LENGTH_IN_BYTES) {
            byte[] nodeMap = new byte[ZWaveExtraEnums.NODES_BITFIELD_LENGTH_IN_BYTES];
            System.arraycopy(parameters, 3, nodeMap, 0, ZWaveExtraEnums.NODES_BITFIELD_LENGTH_IN_BYTES);
            driver.deviceMapProcessing(nodeMap);
        }

        driver.setDriverReady(true);

        ZWaveFunctionID functionID = getClass().getAnnotation(ZWaveFunctionHandlerAnnotation.class).value();
        LOG.debug("execute function : {}", functionID.name());

        driver.currentRequestReceived(functionID);

        return true;
    }

}
