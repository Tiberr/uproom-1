package ru.uproom.gate.localinterface.zwave.functions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.uproom.gate.localinterface.zwave.driver.ZWaveDriver;

/**
 * z-wave function
 * <p/>
 * Created by osipenko on 10.09.14.
 */
@ZWaveFunctionHandlerAnnotation(value = ZWaveFunctionID.SERIAL_API_GET_INIT_DATA)
public class ZWaveFunctionSerialApiGetInitDataHandler implements ZWaveFunctionHandler {

    private static final byte NODES_BITFIELD_LENGTH_IN_BYTES = 29;

    private static final Logger LOG =
            LoggerFactory.getLogger(ZWaveFunctionSerialApiGetInitDataHandler.class);


    @Override
    public boolean execute(byte[] parameters, ZWaveFunctionHandlePool pool) {

        ZWaveDriver driver = pool.getDriver();

        driver.setControllerSerialApiInfo(parameters[0], parameters[1]);

        if (parameters[2] == NODES_BITFIELD_LENGTH_IN_BYTES) {
            byte[] nodeMap = new byte[NODES_BITFIELD_LENGTH_IN_BYTES];
            System.arraycopy(parameters, 3, nodeMap, 0, NODES_BITFIELD_LENGTH_IN_BYTES);
            driver.nodeMapProcessing(nodeMap);
        }

        driver.setDriverReady(true);

        LOG.debug("execute function : {}",
                getClass().getAnnotation(ZWaveFunctionHandlerAnnotation.class).value().name());

        return true;
    }

}
