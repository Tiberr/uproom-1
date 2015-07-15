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
@RkZWaveFunctionHandlerAnnotation(value = RkZWaveFunctionID.GET_NODE_PROTOCOL_INFO)
public class RkZWaveFunctionGetNodeProtocolInfoHandler implements RkZWaveFunctionHandler {


    private static final Logger LOG =
            LoggerFactory.getLogger(RkZWaveFunctionGetNodeProtocolInfoHandler.class);


    @Override
    public boolean execute(RkZWaveMessageTypes messageType, int[] parameters,
                           RkZWaveFunctionHandlePool pool, RkZWaveMessage request) {

        int deviceId = 0;
        if (request != null && request.getFunctionID() == RkZWaveFunctionID.GET_NODE_PROTOCOL_INFO) {
            RkZWaveDevice device = request.getDevice();
            if (device != null) {
                deviceId = device.getDeviceId();
                device.updateDeviceProtocolInfo(parameters);
            }
            request.setHaveAnswer(true);
        }

        LOG.debug("execute function : {} for device ({})", new Object[]{
                getClass().getAnnotation(RkZWaveFunctionHandlerAnnotation.class).value().name(),
                deviceId
        });

        return false;
    }

}
