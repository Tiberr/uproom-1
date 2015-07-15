package ru.uproom.libraries.zwave.functions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.uproom.libraries.zwave.devices.RkZWaveDevicePool;
import ru.uproom.libraries.zwave.driver.RkZWaveMessage;
import ru.uproom.libraries.zwave.enums.*;

/**
 * z-wave function
 * <p/>
 * Created by osipenko on 10.09.14.
 */
@RkZWaveFunctionHandlerAnnotation(value = RkZWaveFunctionID.REMOVE_FAILED_NODE_ID)
public class RkZWaveFunctionRemoveFailedNodeIDHandler implements RkZWaveFunctionHandler {


    private static final Logger LOG =
            LoggerFactory.getLogger(RkZWaveFunctionRemoveFailedNodeIDHandler.class);


    @Override
    public boolean execute(RkZWaveMessageTypes messageType, int[] parameters,
                           RkZWaveFunctionHandlePool pool, RkZWaveMessage request) {

        RkZWaveDevicePool devicePool = pool.getDevicePool();

        RkZWaveControllerState state = RkZWaveControllerState.Normal;
        RkZWaveControllerError error = RkZWaveControllerError.None;

        String logState = "";
        RkZWaveFailedNodeState failedNodeState = RkZWaveFailedNodeState.getByCode(parameters[0]);
        switch (failedNodeState) {

            case Unknown:
                state = RkZWaveControllerState.InProgress;
                logState = "In Progress";
                break;

            case NotFound:
                logState = "Not Found";
                error = RkZWaveControllerError.NotFound;
                break;

            case RemoveProcessBusy:
                logState = "Remove Process Busy";
                error = RkZWaveControllerError.Busy;
                break;

            case RemoveFail:
                logState = "Remove Fail";
                error = RkZWaveControllerError.Failed;
                break;

            case NotPrimaryController:
                logState = "Not Primary Controller";
                error = RkZWaveControllerError.NotPrimary;
                break;

            default:
                logState = "Command Failed";
        }

        devicePool.setControllerState(state);
        devicePool.setControllerError(error);

        if (request != null)
            pool.getDriver().currentRequestReceived(request.getFunctionID());

        LOG.debug("execute function : {} state of removing failed process ({})", new Object[]{
                getClass().getAnnotation(RkZWaveFunctionHandlerAnnotation.class).value().name(),
                logState
        });

        return true;
    }

}
