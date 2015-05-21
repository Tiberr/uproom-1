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
@RkZWaveFunctionHandlerAnnotation(value = RkZWaveFunctionID.IS_FAILED_NODE_ID)
public class RkZWaveFunctionIsFailedNodeIDHandler implements RkZWaveFunctionHandler {


    private static final Logger LOG =
            LoggerFactory.getLogger(RkZWaveFunctionIsFailedNodeIDHandler.class);


    @Override
    public boolean execute(RkZWaveMessageTypes messageType, int[] parameters,
                           RkZWaveFunctionHandlePool pool, RkZWaveMessage request) {

        int deviceId = 0x00;
        if (request != null) {
            RkZWaveDevice device = request.getDevice();
            if (device != null) deviceId = device.getDeviceId();
            request.setHaveAnswer(true);
        }

        pool.getDevicePool().updateDeviceFailedId(deviceId, parameters[0] != 0x00);

        String deviceState = "exist";
        if (parameters[0] != 0x00) {
            deviceState = "failed";
        }

        //todo : create checking link process based of this command

        LOG.debug("execute function : {}, device ({}) state ({})", new Object[]{
                getClass().getAnnotation(RkZWaveFunctionHandlerAnnotation.class).value().name(),
                deviceId,
                deviceState
        });

        return false;
    }

}
