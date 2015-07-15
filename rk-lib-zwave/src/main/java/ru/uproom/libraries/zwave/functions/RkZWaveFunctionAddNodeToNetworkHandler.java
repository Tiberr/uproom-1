package ru.uproom.libraries.zwave.functions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.uproom.libraries.zwave.devices.RkZWaveDevicePool;
import ru.uproom.libraries.zwave.driver.RkZWaveMessage;
import ru.uproom.libraries.zwave.enums.RkZWaveAddNodeState;
import ru.uproom.libraries.zwave.enums.RkZWaveControllerState;
import ru.uproom.libraries.zwave.enums.RkZWaveFunctionID;
import ru.uproom.libraries.zwave.enums.RkZWaveMessageTypes;

/**
 * z-wave function
 * <p/>
 * Created by osipenko on 10.09.14.
 */
@RkZWaveFunctionHandlerAnnotation(value = RkZWaveFunctionID.ADD_NODE_TO_NETWORK)
public class RkZWaveFunctionAddNodeToNetworkHandler implements RkZWaveFunctionHandler {


    private static final Logger LOG =
            LoggerFactory.getLogger(RkZWaveFunctionAddNodeToNetworkHandler.class);

    private int newDeviceId = 0;


    @Override
    public boolean execute(RkZWaveMessageTypes messageType, int[] parameters,
                           RkZWaveFunctionHandlePool pool, RkZWaveMessage request) {

        RkZWaveDevicePool devicePool = pool.getDevicePool();

        RkZWaveControllerState state = RkZWaveControllerState.Normal;
        if (devicePool.getCurrentControllerCommand() != RkZWaveFunctionID.UNKNOWN)
            state = devicePool.getControllerState();

        RkZWaveAddNodeState addNodeState = RkZWaveAddNodeState.getByCode(parameters[1]);
        switch (addNodeState) {

            case LearnReady:
                state = RkZWaveControllerState.Waiting;
                break;

            case NodeFound:
                state = RkZWaveControllerState.InProgress;
                break;

            case AddingSlave:
                newDeviceId = parameters[2];
                LOG.info("NEW DEVICE FOUND : device ({})", parameters[2]);
                break;

            case AddingController:
                newDeviceId = parameters[2];
                LOG.info("NEW CONTROLLER FOUND : device ({})", parameters[2]);
                break;

            case ProtocolDone:
                pool.getDriver().interruptCurrentCommandInController();
                break;

            case Done:
                state = RkZWaveControllerState.Completed;
                if (newDeviceId != 0)
                    pool.getDevicePool().addNewDevice(newDeviceId);
                break;

            case Failed:
                state = RkZWaveControllerState.Failed;
                pool.getDriver().interruptCurrentCommandInController();
                break;

            default:
        }

        devicePool.setControllerState(state);

        LOG.debug("execute function : {} state of adding process ({})", new Object[]{
                getClass().getAnnotation(RkZWaveFunctionHandlerAnnotation.class).value().name(),
                addNodeState.name()
        });

        return true;
    }

}
