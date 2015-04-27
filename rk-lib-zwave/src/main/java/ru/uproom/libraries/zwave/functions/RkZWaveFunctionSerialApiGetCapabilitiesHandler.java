package ru.uproom.libraries.zwave.functions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.uproom.libraries.zwave.driver.RkZWaveDriver;
import ru.uproom.libraries.zwave.driver.RkZWaveMessage;
import ru.uproom.libraries.zwave.enums.RkZWaveFunctionID;
import ru.uproom.libraries.zwave.enums.RkZWaveMessageTypes;

/**
 * z-wave function
 * <p/>
 * Created by osipenko on 10.09.14.
 */
@RkZWaveFunctionHandlerAnnotation(value = RkZWaveFunctionID.SERIAL_API_GET_CAPABILITIES)
public class RkZWaveFunctionSerialApiGetCapabilitiesHandler implements RkZWaveFunctionHandler {


    private static final Logger LOG =
            LoggerFactory.getLogger(RkZWaveFunctionSerialApiGetCapabilitiesHandler.class);


    @Override
    public boolean execute(RkZWaveMessageTypes messageType, int[] parameters,
                           RkZWaveFunctionHandlePool pool, RkZWaveMessage request) {

        RkZWaveDriver driver = pool.getDriver();

        // parameters[8] to _data[39] are a 256-bit mask with one bit set for
        // each ZWaveFunctionID method supported by the controller.
        // Bit 0 is ZWaveFunctionID.0x01. So ZWaveFunctionID.SERIAL_API_GET_CAPABILITIES (0x07) will be
        // bit 6 of the first byte.

        driver.setSerialApiVersion(parameters[0], parameters[1]);
        driver.setControllerProductInfo(
                (parameters[2] << 8) | parameters[3],
                (parameters[4] << 8) | parameters[5],
                (parameters[6] << 8) | parameters[7]
        );
        int[] apiMask = new int[32];
        System.arraycopy(parameters, 8, apiMask, 0, apiMask.length);
        driver.setControllerApiMask(apiMask);

        RkZWaveFunctionID functionID = getClass().getAnnotation(RkZWaveFunctionHandlerAnnotation.class).value();
        LOG.debug("execute function : {}", functionID.name());

        driver.currentRequestReceived(functionID);
        return false;
    }

}
