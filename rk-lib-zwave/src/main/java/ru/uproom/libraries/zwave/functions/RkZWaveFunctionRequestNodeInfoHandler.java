package ru.uproom.libraries.zwave.functions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.uproom.libraries.zwave.devices.RkZWaveDevice;
import ru.uproom.libraries.zwave.driver.RkZWaveMessage;
import ru.uproom.libraries.zwave.enums.RkZWaveFunctionID;
import ru.uproom.libraries.zwave.enums.RkZWaveMessageTypes;

/**
 * z-wave function
 * <p/>
 * Created by osipenko on 10.09.14.
 */
@RkZWaveFunctionHandlerAnnotation(value = RkZWaveFunctionID.REQUEST_NODE_INFO)
public class RkZWaveFunctionRequestNodeInfoHandler implements RkZWaveFunctionHandler {


    private static final Logger LOG =
            LoggerFactory.getLogger(RkZWaveFunctionRequestNodeInfoHandler.class);


    @Override
    public boolean execute(RkZWaveMessageTypes messageType, int[] parameters,
                           RkZWaveFunctionHandlePool pool, RkZWaveMessage request) {

        String result = "request successful";
        if (parameters[0] == 0) result = "request failed";

        RkZWaveDevice device = null;
        if (request != null) {
            device = request.getDevice();
        }

        RkZWaveFunctionID functionID = getClass().getAnnotation(RkZWaveFunctionHandlerAnnotation.class).value();
        LOG.debug("execute function : {}; {} for device ({})", new Object[]{
                functionID.name(),
                result,
                String.valueOf(device != null ? device.getDeviceId() : "0")
        });

        // if request failed - remove corresponding sending message
        if (parameters[0] == 0)
            pool.getDriver().currentRequestReceived(functionID);
        // else current message will be removed in ZWaveFunctionID.APPLICATION_UPDATE handler

        if (device != null) device.receivedRequestNodeInfo(parameters[0] != 0);

        return false;
    }

}
