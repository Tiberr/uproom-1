package ru.uproom.gate.localinterface.zwave.functions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.uproom.gate.localinterface.zwave.devices.ZWaveDevicePool;
import ru.uproom.gate.localinterface.zwave.driver.ZWaveDriver;
import ru.uproom.gate.localinterface.zwave.enums.ZWaveFunctionID;
import ru.uproom.gate.localinterface.zwave.enums.ZWaveMessageTypes;
import ru.uproom.gate.localinterface.zwave.enums.ZWaveUpdateState;

/**
 * z-wave function
 * <p/>
 * Created by osipenko on 10.09.14.
 */
@ZWaveFunctionHandlerAnnotation(value = ZWaveFunctionID.APPLICATION_UPDATE)
public class ZWaveFunctionApplicationUpdateHandler implements ZWaveFunctionHandler {


    private static final Logger LOG =
            LoggerFactory.getLogger(ZWaveFunctionApplicationUpdateHandler.class);


    @Override
    public boolean execute(ZWaveMessageTypes messageType, byte[] parameters, ZWaveFunctionHandlePool pool) {

        byte nodeId = parameters[1];
        ZWaveDriver driver = pool.getDriver();
        ZWaveDevicePool devices = pool.getDevicePool();
        ZWaveUpdateState state = ZWaveUpdateState.getByCode(parameters[0]);

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
                driver.currentRequestReceived(ZWaveFunctionID.REQUEST_NODE_INFO);
                break;

            case NODE_INFO_REQ_DONE:
                break;

            case NODE_INFO_RECEIVED:
                byte[] info = new byte[parameters[2] - 3];
                System.arraycopy(parameters, 6, info, 0, info.length);
                devices.updateDeviceInfo(nodeId, info);
                driver.currentRequestReceived(ZWaveFunctionID.REQUEST_NODE_INFO);
                break;

        }

        LOG.debug("execute function : {}; device : {}; state : {}", new Object[]{
                getClass().getAnnotation(ZWaveFunctionHandlerAnnotation.class).value().name(),
                String.format(" 0x%02X", nodeId),
                state.name()
        });

        return false;
    }

}
