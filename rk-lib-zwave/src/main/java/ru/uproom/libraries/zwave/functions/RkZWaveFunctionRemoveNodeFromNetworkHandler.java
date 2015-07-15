package ru.uproom.libraries.zwave.functions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.uproom.libraries.zwave.devices.RkZWaveDevicePool;
import ru.uproom.libraries.zwave.driver.RkZWaveMessage;
import ru.uproom.libraries.zwave.enums.RkZWaveControllerState;
import ru.uproom.libraries.zwave.enums.RkZWaveFunctionID;
import ru.uproom.libraries.zwave.enums.RkZWaveMessageTypes;
import ru.uproom.libraries.zwave.enums.RkZWaveRemoveNodeState;

/**
 * z-wave function
 * <p/>
 * Created by osipenko on 10.09.14.
 */
@RkZWaveFunctionHandlerAnnotation(value = RkZWaveFunctionID.REMOVE_NODE_FROM_NETWORK)
public class RkZWaveFunctionRemoveNodeFromNetworkHandler implements RkZWaveFunctionHandler {


    private static final Logger LOG =
            LoggerFactory.getLogger(RkZWaveFunctionRemoveNodeFromNetworkHandler.class);

    private int removeDeviceId = 0;

    @Override
    public boolean execute(RkZWaveMessageTypes messageType, int[] parameters,
                           RkZWaveFunctionHandlePool pool, RkZWaveMessage request) {

        RkZWaveDevicePool devicePool = pool.getDevicePool();

        RkZWaveControllerState state = RkZWaveControllerState.Normal;
        if (devicePool.getCurrentControllerCommand() != RkZWaveFunctionID.UNKNOWN)
            state = devicePool.getControllerState();

        RkZWaveRemoveNodeState removeNodeState = RkZWaveRemoveNodeState.getByCode(parameters[1]);
        switch (removeNodeState) {

            case LearnReady:

                state = RkZWaveControllerState.Waiting;
                break;

            case NodeFound:

                state = RkZWaveControllerState.InProgress;
                break;

            case RemovingSlave:

                removeDeviceId = parameters[2];
                LOG.info("EXISTING DEVICE MARKED : device ({})", parameters[2]);
                break;

            case RemovingController:

                removeDeviceId = parameters[2];
                if (removeDeviceId == 0) {
                    //todo : remove device by another way
                }
                LOG.info("EXISTING CONTROLLER MARKED : device ({})", parameters[2]);
                break;

            case Done:

                state = RkZWaveControllerState.Completed;
                if (removeDeviceId == 0)
                    removeDeviceId = parameters[2];
                if (removeDeviceId != 0)
                    pool.getDevicePool().removeExistingDevice(removeDeviceId);
                break;

            case Failed:
                state = RkZWaveControllerState.Failed;
                pool.getDriver().interruptCurrentCommandInController();
                break;

            default:
        }

        devicePool.setControllerState(state);

        LOG.debug("execute function : {} state of removing process ({})", new Object[]{
                getClass().getAnnotation(RkZWaveFunctionHandlerAnnotation.class).value().name(),
                removeNodeState.name()
        });

        return true;
    }

}
