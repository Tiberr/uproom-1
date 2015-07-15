package ru.uproom.libraries.zwave.devices;

import libraries.api.RkLibraryDevice;
import libraries.api.RkLibraryDeviceParameterName;
import libraries.api.RkLibraryDeviceType;
import libraries.auxilliary.LoggingHelper;
import libraries.auxilliary.RunnableClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.uproom.libraries.zwave.commands.RkZWaveCommandClass;
import ru.uproom.libraries.zwave.commands.RkZWaveMultiInstanceCommandClass;
import ru.uproom.libraries.zwave.commands.RkZWaveVersionCommandClass;
import ru.uproom.libraries.zwave.driver.RkZWaveMessage;
import ru.uproom.libraries.zwave.enums.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Created by osipenko on 15.01.15.
 */
public class RkZWaveDevice implements RkLibraryDevice {


    //##############################################################################################################
    //######    fields


    private static final Logger LOG = LoggerFactory.getLogger(RkZWaveDevice.class);

    private final Map<RkZWaveCommandClassNames, RkZWaveCommandClass> commandClasses = new HashMap<>();
    private final Map<RkZWaveDeviceParameterNames, RkZWaveDeviceParameter> parameters = new HashMap<>();

    private int deviceId;
    private RkZWaveDeviceType type = RkZWaveDeviceType.Unknown;

    private RkZWaveDevicePool devicePool;

    private boolean ready;
    private boolean failed;
    private boolean hasInfo;

    private long timeBetweenCheckInitSeq;
    private RunInitialSequence runInitialSequence = new RunInitialSequence();


    //##############################################################################################################
    //######    constructors / destructors


    public RkZWaveDevice(int deviceId, RkZWaveDevicePool devicePool) {
        this.deviceId = deviceId;
        this.devicePool = devicePool;

        final Properties properties = devicePool.getDriver().getProperties();
        timeBetweenCheckInitSeq = Long.parseLong(
                properties.getProperty("time_between_check_init_seq"));
    }


    //##############################################################################################################
    //######    getters / setters


    @Override
    public int getDeviceId() {
        return deviceId;
    }


    //-----------------------------------------------------------------------------------

    @Override
    public RkLibraryDeviceType getDeviceType() {
        return RkZWaveDeviceType.convertToLibraryFromZWave(type);
    }


    //-----------------------------------------------------------------------------------

    public RkZWaveDevicePool getDevicePool() {
        return devicePool;
    }


    //-----------------------------------------------------------------------------------

    public boolean isFailed() {
        return failed;
    }


    //-----------------------------------------------------------------------------------

    public boolean isReady() {
        return ready;
    }

    private void setReady(boolean ready) {
        boolean readyWillBeSet = !this.ready && ready;
        this.ready = ready;

        if (readyWillBeSet) {
            LOG.info("DEVICE READY : device ({}) of type ({})", new Object[]{
                    String.valueOf(deviceId),
                    type.name()
            });
            devicePool.deviceReady();
        }
    }





    //##############################################################################################################
    //######    method


    // init sequence

    public void startInitSequence() {

        runInitialSequence.setTimeout(timeBetweenCheckInitSeq);
        new Thread(runInitialSequence).start();
        runInitialSequence.restartInitSequence();
    }


    //-----------------------------------------------------------------------------------

    public void updateDeviceProtocolInfo(int[] info) {

        if (info[4] == 0x00) {
            LOG.info("UPDATE PROTOCOL INFO : device ({}) nonexistent", deviceId);
            return;
        } else {

            //todo : library contains many useful info. For future use.
            type = RkZWaveDeviceType.getByPattern(info[4], info[5]);
            LOG.info("UPDATE PROTOCOL INFO : device ({}) has type ({})", new Object[]{
                    deviceId,
                    type.name()
            });
        }

        runInitialSequence.setInitSequenceStep(RkZWaveFunctionID.GET_NODE_PROTOCOL_INFO);
    }


    //-----------------------------------------------------------------------------------

    public void updateDeviceRoutingInfo(int[] info) {

        //todo : create this handler in future
        runInitialSequence.setInitSequenceStep(RkZWaveFunctionID.GET_ROUTING_INFO);
    }


    //-----------------------------------------------------------------------------------

    public void updateFailedState(boolean failed) {

        if (this.failed && !failed)
            runInitialSequence.restartInitSequence();
        else if (failed) {
            setReady(true);
            if (deviceId == devicePool.getControllerId())
                runInitialSequence.setInitSequenceStep(RkZWaveFunctionID.IS_FAILED_NODE_ID);
        } else
            runInitialSequence.setInitSequenceStep(RkZWaveFunctionID.IS_FAILED_NODE_ID);

        this.failed = failed;
    }


    //-----------------------------------------------------------------------------------

    public void receivedRequestNodeInfo(boolean successful) {
        // if request failed we go to the next step
        if (!successful)
            setReady(true);
    }


    //-----------------------------------------------------------------------------------

    public void updateInfo(int[] commandClassList) {

        if (commandClassList == null) {
            setReady(true);
            if (deviceId == devicePool.getControllerId())
                runInitialSequence.setInitSequenceStep(RkZWaveFunctionID.APPLICATION_UPDATE);
            return;
        }

        for (int b : commandClassList) {
            if (b == RkZWaveExtraEnums.END_OF_LIST_SUPPORTED_COMMAND_CLASS_MARK)
                break;
            addCommandClass(b);
        }

        RkZWaveVersionCommandClass versionCommandClass =
                (RkZWaveVersionCommandClass) commandClasses.get(RkZWaveCommandClassNames.Version);
        RkZWaveMultiInstanceCommandClass instanceCommandClass =
                (RkZWaveMultiInstanceCommandClass) commandClasses.get(RkZWaveCommandClassNames.MultiInstance);
        for (Map.Entry<RkZWaveCommandClassNames, RkZWaveCommandClass> entry : commandClasses.entrySet()) {
            entry.getValue().setCommonCommandClasses(versionCommandClass, instanceCommandClass);
        }

        if (versionCommandClass != null)
            versionCommandClass.startInitSequence(timeBetweenCheckInitSeq);
        else if (instanceCommandClass != null)
            instanceCommandClass.startInitSequence(timeBetweenCheckInitSeq);
        else
            for (Map.Entry<RkZWaveCommandClassNames, RkZWaveCommandClass> entry : commandClasses.entrySet()) {
                entry.getValue().startInitSequence(timeBetweenCheckInitSeq);
            }

        runInitialSequence.setInitSequenceStep(RkZWaveFunctionID.APPLICATION_UPDATE);
    }


    //-----------------------------------------------------------------------------------

    public void addCommandClass(int commandClassId) {

        RkZWaveCommandClass commandClass =
                devicePool.getCommandClassFactory().getCommandClass(commandClassId);
        if (commandClass == null) {
            LOG.info("ADD COMMAND CLASS : device ({}) class ({}), not implemented", new Object[]{
                    deviceId,
                    RkZWaveCommandClassNames.getByCode(commandClassId).name()
            });
            return;
        }

        LOG.info("ADD COMMAND CLASS : device ({}) class ({})", new Object[]{
                deviceId,
                commandClass.getName().name()
        });
        commandClass.setDevice(this);

        commandClasses.put(RkZWaveCommandClassNames.getByCode(commandClassId), commandClass);
    }


    //-----------------------------------------------------------------------------------

    public void commandClassReady(RkZWaveCommandClass commandClass) {

        switch (commandClass.getName()) {

            case Version:
                RkZWaveMultiInstanceCommandClass instanceCommandClass =
                        (RkZWaveMultiInstanceCommandClass) commandClasses.
                                get(RkZWaveCommandClassNames.MultiInstance);
                if (instanceCommandClass != null)
                    instanceCommandClass.startInitSequence(timeBetweenCheckInitSeq);
                else
                    for (Map.Entry<RkZWaveCommandClassNames, RkZWaveCommandClass> entry
                            : commandClasses.entrySet()) {
                        entry.getValue().startInitSequence(timeBetweenCheckInitSeq);
                    }
                break;

            case MultiInstance:
                for (Map.Entry<RkZWaveCommandClassNames, RkZWaveCommandClass> entry
                        : commandClasses.entrySet()) {
                    entry.getValue().startInitSequence(timeBetweenCheckInitSeq);
                }
                break;

            default:
                boolean allCommandClassesReady = true;
                for (Map.Entry<RkZWaveCommandClassNames, RkZWaveCommandClass> entry : commandClasses.entrySet()) {
                    if (!entry.getValue().isReady()) {
                        allCommandClassesReady = false;
                        break;
                    }
                }

                if (allCommandClassesReady) {
                    LOG.info("DEVICE READY : device ({})", String.valueOf(deviceId));
                    setReady(true);
                }
        }

    }


    //-----------------------------------------------------------------------------------

    public RkZWaveCommandClass getCommandClassById(int commandClassId) {

        return getCommandClassByName(RkZWaveCommandClassNames.getByCode(commandClassId));
    }


    public RkZWaveCommandClass getCommandClassByName(RkZWaveCommandClassNames commandClassName) {

        return commandClasses.get(commandClassName);
    }


    //-----------------------------------------------------------------------------------

    public void addParameter(RkZWaveDeviceParameter parameter) {

        parameters.put(parameter.getZWaveName(), parameter);
    }


    //-----------------------------------------------------------------------------------

    public void removeParameter(RkZWaveDeviceParameterNames parameterName) {

        parameters.remove(parameterName);
    }


    //-----------------------------------------------------------------------------------

    public void applyDeviceParametersFromByteArray(
            RkZWaveCommandClassNames commandClassId, int[] parameters, int instance) {

        RkZWaveCommandClass commandClass = commandClasses.get(commandClassId);
        if (commandClass == null) return;

        commandClass.messageHandler(parameters, instance);

        LOG.debug("RECEIVE PARAMETER : class ({}) parameters ({} )", new Object[]{
                commandClassId.name(),
                LoggingHelper.createHexStringFromIntArray(parameters, true)
        });
    }

    public void applyDeviceParametersFromByteArray(
            RkZWaveCommandClassNames commandClassId, int[] parameters) {
        applyDeviceParametersFromByteArray(commandClassId, parameters, 0x01);
    }


    //-----------------------------------------------------------------------------------

    public void applyDeviceParametersFromName(RkZWaveDeviceParameterNames parameterName, String value) {
        RkZWaveDeviceParameter parameter = parameters.get(parameterName);
        if (parameter == null) return;

        parameter.setValue(value);
    }


    //-----------------------------------------------------------------------------------

    public RkZWaveDeviceParameter getDeviceParameterByName(RkZWaveDeviceParameterNames parameterName) {
        return parameters.get(parameterName);
    }


    //-----------------------------------------------------------------------------------

    @Override
    public int hashCode() {
        return super.hashCode();
    }


    //##############################################################################################################
    //######    interface RkLibraryDevice


    @Override
    public Map<RkLibraryDeviceParameterName, String> getParameterList() {

        Map<RkLibraryDeviceParameterName, String> outParams = new HashMap<>();
        int levelRed = 0;
        int levelGreen = 0;
        int levelBlue = 0;
        boolean createColor = false;

        for (Map.Entry<RkZWaveDeviceParameterNames, RkZWaveDeviceParameter> entry : parameters.entrySet()) {

            switch (entry.getKey()) {
                case LevelRed:
                    createColor = true;
                    levelRed = 256 * 256 * Integer.parseInt(entry.getValue().getValue());
                    break;
                case LevelGreen:
                    createColor = true;
                    levelGreen = 256 * Integer.parseInt(entry.getValue().getValue());
                    break;
                case LevelBlue:
                    createColor = true;
                    levelBlue = Integer.parseInt(entry.getValue().getValue());
                    break;
                default:
                    RkLibraryDeviceParameterName name =
                            RkZWaveDeviceParameterNames.getConvertLibraryName(entry.getKey());
                    if (name != RkLibraryDeviceParameterName.Unknown) {
                        outParams.put(name, entry.getValue().getValue());
                    }
            }

        }

        if (createColor) {
            outParams.put(RkLibraryDeviceParameterName.Color, String.valueOf(levelRed + levelGreen + levelBlue));
        }

        return outParams;
    }


    //-----------------------------------------------------------------------------------

    @Override
    public void applyParameterList(Map<RkLibraryDeviceParameterName, String> inputParams) {

        boolean expandedColor = false;
        int color = 0;

        for (Map.Entry<RkLibraryDeviceParameterName, String> entry : inputParams.entrySet()) {

            RkZWaveDeviceParameterNames name =
                    RkZWaveDeviceParameterNames.getConvertZWaveName(entry.getKey());
            if (name != RkZWaveDeviceParameterNames.Unknown) {
                RkZWaveDeviceParameter parameter = parameters.get(name);
                if (parameter != null)
                    parameter.applyValue(entry.getValue());
                else {
                    if (name == RkZWaveDeviceParameterNames.Color) {
                        expandedColor = true;
                        color = Integer.getInteger(entry.getValue());
                    }
                }

            }
        }

        if (expandedColor) {
            RkZWaveDeviceParameter level = parameters.get(RkZWaveDeviceParameterNames.LevelBlue);
            if (level != null)
                level.applyValue(String.valueOf(color & 0xFF));
            level = parameters.get(RkZWaveDeviceParameterNames.LevelGreen);
            if (level != null)
                level.applyValue(String.valueOf((color & 0xFF00) >> 8));
            level = parameters.get(RkZWaveDeviceParameterNames.LevelRed);
            if (level != null)
                level.applyValue(String.valueOf((color & 0xFF0000) >> 16));
        }
    }


    //##############################################################################################################
    //######    inner classes


    private class RunInitialSequence extends RunnableClass {

        private RkZWaveFunctionID initSequenceStep = RkZWaveFunctionID.UNKNOWN;
        private boolean pingModeEnabled = false;


        public void restartInitSequence() {

            pingModeEnabled = false;
            initSequenceStep = RkZWaveFunctionID.UNKNOWN;
            setInitSequenceStep(RkZWaveFunctionID.UNKNOWN);
        }


        public void setInitSequenceStep(RkZWaveFunctionID stepId) {
            boolean quit = false;

            switch (initSequenceStep) {

                case UNKNOWN:
                    if (stepId == RkZWaveFunctionID.UNKNOWN)
                        initSequenceStep = RkZWaveFunctionID.GET_NODE_PROTOCOL_INFO;
                    break;

                case GET_NODE_PROTOCOL_INFO:
                    if (stepId == RkZWaveFunctionID.GET_NODE_PROTOCOL_INFO)
                        initSequenceStep = RkZWaveFunctionID.GET_ROUTING_INFO;
                    break;
                case GET_ROUTING_INFO:
                    if (stepId == RkZWaveFunctionID.GET_ROUTING_INFO)
                        initSequenceStep = RkZWaveFunctionID.IS_FAILED_NODE_ID;
                    break;
                case IS_FAILED_NODE_ID:
                    if (stepId == RkZWaveFunctionID.IS_FAILED_NODE_ID)
                        if (!pingModeEnabled)
                            initSequenceStep = RkZWaveFunctionID.REQUEST_NODE_INFO;
                    break;

                // todo: probably add to this point Node::AdvancedQueries

                case REQUEST_NODE_INFO:
                    if (stepId == RkZWaveFunctionID.APPLICATION_UPDATE) {
                        pingModeEnabled = true;
                        initSequenceStep = RkZWaveFunctionID.IS_FAILED_NODE_ID;
                    }
                    break;

                default:
            }

            if (!pingModeEnabled) {
                synchronized (this) {
                    notify();
                }
            }
        }


        @Override
        protected void body() {
            super.body();
            if (initSequenceStep == RkZWaveFunctionID.UNKNOWN) return;

            int[] data;
            switch (initSequenceStep) {

                case GET_ROUTING_INFO:
                    data = new int[4];
                    data[1] = 0x00; // don't remove bad links
                    data[2] = 0x00; // don't remove non-repeaters
                    data[3] = 0x03; // funcId
                    break;

                default:
                    data = new int[1];
            }

            RkZWaveMessage message = new RkZWaveMessage(
                    RkZWaveMessageTypes.Request,
                    initSequenceStep,
                    RkZWaveDevice.this,
                    true
            );
            data[0] = getDeviceId();

            message.setParameters(data);
            devicePool.getDriver().addMessageToSendingQueue(message);
        }
    }

}
