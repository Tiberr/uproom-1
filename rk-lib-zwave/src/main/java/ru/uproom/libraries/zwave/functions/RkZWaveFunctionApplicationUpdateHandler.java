package ru.uproom.libraries.zwave.functions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.uproom.libraries.zwave.devices.RkZWaveDevicePool;
import ru.uproom.libraries.zwave.driver.RkZWaveDriver;
import ru.uproom.libraries.zwave.driver.RkZWaveMessage;
import ru.uproom.libraries.zwave.enums.RkZWaveFunctionID;
import ru.uproom.libraries.zwave.enums.RkZWaveMessageTypes;
import ru.uproom.libraries.zwave.enums.RkZWaveUpdateState;

/**
 * z-wave function
 * <p/>
 * Created by osipenko on 10.09.14.
 */
@RkZWaveFunctionHandlerAnnotation(value = RkZWaveFunctionID.APPLICATION_UPDATE)
public class RkZWaveFunctionApplicationUpdateHandler implements RkZWaveFunctionHandler {


    private static final Logger LOG =
            LoggerFactory.getLogger(RkZWaveFunctionApplicationUpdateHandler.class);


    //-----------------------------------------------------------------------------------

    @Override
    public boolean execute(RkZWaveMessageTypes messageType, int[] parameters,
                           RkZWaveFunctionHandlePool pool, RkZWaveMessage request) {

        int nodeId = parameters[1];
        RkZWaveDriver driver = pool.getDriver();
        RkZWaveDevicePool devices = pool.getDevicePool();
        RkZWaveUpdateState state = RkZWaveUpdateState.getByCode(parameters[0]);

        switch (state) {

            case SUC_ID:
                driver.setSucNodeId(nodeId);
                break;

            case DELETE_DONE:
                devices.removeExistingDevice(nodeId);
                break;

            case ROUTING_PENDING:
                break;

            case NODE_INFO_REQ_FAILED:
                // todo : resend request
                if (request != null && request.getFunctionID() == RkZWaveFunctionID.REQUEST_NODE_INFO)
                    request.setHaveAnswer(true);
                break;

            case NODE_INFO_REQ_DONE:
                break;

            case NODE_INFO_RECEIVED:
                int[] info = new int[parameters[2] - 3];
                System.arraycopy(parameters, 6, info, 0, info.length);
                devices.updateDeviceInfo(nodeId, info);
                if (request != null && request.getFunctionID() == RkZWaveFunctionID.REQUEST_NODE_INFO)
                    request.setHaveAnswer(true);
                break;

        }

        LOG.debug("execute function : {}; device : {}; state : {}", new Object[]{
                getClass().getAnnotation(RkZWaveFunctionHandlerAnnotation.class).value().name(),
                String.format(" 0x%02X", nodeId),
                state.name()
        });

        return false;
    }

}
