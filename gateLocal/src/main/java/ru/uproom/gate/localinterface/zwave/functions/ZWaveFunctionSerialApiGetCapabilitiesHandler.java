package ru.uproom.gate.localinterface.zwave.functions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.uproom.gate.localinterface.zwave.driver.ZWaveDriver;
import ru.uproom.gate.localinterface.zwave.driver.ZWaveSerialDataHandler;
import ru.uproom.gate.localinterface.zwave.enums.ZWaveFunctionID;
import ru.uproom.gate.localinterface.zwave.enums.ZWaveMessageTypes;

/**
 * z-wave function
 * <p/>
 * Created by osipenko on 10.09.14.
 */
@ZWaveFunctionHandlerAnnotation(value = ZWaveFunctionID.SERIAL_API_GET_CAPABILITIES)
public class ZWaveFunctionSerialApiGetCapabilitiesHandler implements ZWaveFunctionHandler {


    private static final Logger LOG =
            LoggerFactory.getLogger(ZWaveFunctionSerialApiGetCapabilitiesHandler.class);


    @Override
    public boolean execute(ZWaveMessageTypes messageType, byte[] parameters, ZWaveFunctionHandlePool pool) {

        ZWaveDriver driver = pool.getDriver();
        ZWaveSerialDataHandler dataHandler = driver.getSerialDataHandler();

        // parameters[8] to _data[39] are a 256-bit mask with one bit set for
        // each ZWaveFunctionID method supported by the controller.
        // Bit 0 is ZWaveFunctionID.0x01. So ZWaveFunctionID.SERIAL_API_GET_CAPABILITIES (0x07) will be
        // bit 6 of the first byte.

        driver.setSerialApiVersion(parameters[0], parameters[1]);
        driver.setControllerProductInfo(
                ((int) parameters[2] << 8) | (int) parameters[3],
                ((int) parameters[4] << 8) | (int) parameters[5],
                ((int) parameters[6] << 8) | (int) parameters[7]
        );
        byte[] apiMask = new byte[32];
        System.arraycopy(parameters, 8, apiMask, 0, apiMask.length);
        driver.setControllerApiMask(apiMask);

        ZWaveFunctionID functionID = getClass().getAnnotation(ZWaveFunctionHandlerAnnotation.class).value();
        LOG.debug("execute function : {}", functionID.name());

        driver.currentRequestReceived(functionID);
        return false;
    }

}
