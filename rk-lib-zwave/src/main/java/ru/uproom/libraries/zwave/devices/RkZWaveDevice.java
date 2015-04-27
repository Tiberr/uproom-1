package ru.uproom.libraries.zwave.devices;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.uproom.gate.transport.dto.DeviceDTO;
import ru.uproom.gate.transport.dto.parameters.DeviceParametersNames;
import ru.uproom.libraries.auxilliary.LoggingHelper;
import ru.uproom.libraries.zwave.commands.RkZWaveCommandClass;
import ru.uproom.libraries.zwave.commands.RkZWaveMultiInstanceCommandClass;
import ru.uproom.libraries.zwave.commands.RkZWaveVersionCommandClass;
import ru.uproom.libraries.zwave.driver.RkZWaveMessage;
import ru.uproom.libraries.zwave.enums.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by osipenko on 15.01.15.
 */
public class RkZWaveDevice {


    //##############################################################################################################
    //######    fields


    private static final Logger LOG = LoggerFactory.getLogger(RkZWaveDevice.class);
    private final Map<RkZWaveCommandClassNames, RkZWaveCommandClass> commandClasses = new HashMap<>();
    private final Map<DeviceParametersNames, RkZWaveDeviceParameterNames> serverParameterIds = new HashMap<>();

    private final Map<RkZWaveDeviceParameterNames, RkZWaveDeviceParameter> parameters = new HashMap<>();

    private int deviceId;
    private int deviceServerId;

    private RkZWaveDevicePool devicePool;


    //##############################################################################################################
    //######    constructors / destructors


    public RkZWaveDevice(int deviceId, RkZWaveDevicePool pool) {
        this.deviceId = deviceId;
        this.devicePool = pool;

        requestNodeProtocolInfo();
    }


    //##############################################################################################################
    //######    getters / setters


    public int getDeviceId() {
        return deviceId;
    }

    public int getDeviceServerId() {
        return deviceServerId;
    }

    public RkZWaveDevicePool getDevicePool() {
        return devicePool;
    }

    //##############################################################################################################
    //######    method


    //-----------------------------------------------------------------------------------

    private void requestNodeProtocolInfo() {

        RkZWaveMessage message = new RkZWaveMessage(
                RkZWaveMessageTypes.Request,
                RkZWaveFunctionID.GET_NODE_PROTOCOL_INFO,
                true
        );
        int[] data = new int[1];
        data[0] = deviceId;
        message.setParameters(data);

    }


    public void updateNodeProtocolInfo(int[] info) {

    }


    //-----------------------------------------------------------------------------------

    public void addCommandClass(int commandClassId) {

        RkZWaveCommandClass commandClass =
                devicePool.getDriver().getCommandClassFactory().getCommandClass(commandClassId);
        if (commandClass == null) {
            LOG.info("ADD COMMAND CLASS : {}, not implemented", new Object[]{
                    RkZWaveCommandClassNames.getByCode(commandClassId).name()
            });
            return;
        }

        commandClasses.put(RkZWaveCommandClassNames.getByCode(commandClassId), commandClass);
        // todo : probably fill parameter list in requestCommandClassInstances. Check it.
        commandClass.createParameterList(this, 0x01);
    }


    //-----------------------------------------------------------------------------------

    public void fillCommandClassList(int[] commandClassList) {

        for (int b : commandClassList) {
            if (b == RkZWaveExtraEnums.END_OF_LIST_SUPPORTED_COMMAND_CLASS_MARK)
                break;
            addCommandClass(b);
        }

        requestCommandClassesVersions();
        requestCommandClassesInstances();
    }


    //-----------------------------------------------------------------------------------

    private void requestCommandClassesVersions() {

        RkZWaveVersionCommandClass versionClass =
                (RkZWaveVersionCommandClass) commandClasses.get(RkZWaveCommandClassNames.Version);
        for (RkZWaveCommandClassNames className : commandClasses.keySet()) {
            if (versionClass != null)
                versionClass.requestCommandClassVersion(this, className);
            else
                applyCommandClassVersion(className, 0x01);
        }

    }


    //-----------------------------------------------------------------------------------

    public void applyCommandClassVersion(RkZWaveCommandClassNames commandClassName, int version) {
        RkZWaveCommandClass commandClass = commandClasses.get(commandClassName);
        if (commandClass == null) return;

        commandClass.setVersion(this, version);
        commandClass.requestDeviceState(this, 0x01);
    }


    //-----------------------------------------------------------------------------------

    private void requestCommandClassesInstances() {

        RkZWaveMultiInstanceCommandClass instanceClass =
                (RkZWaveMultiInstanceCommandClass) commandClasses.get(RkZWaveCommandClassNames.MultiInstance);
        for (RkZWaveCommandClassNames className : commandClasses.keySet()) {
            if (instanceClass != null)
                instanceClass.requestInstance(this, className);
            else
                commandClasses.get(className).createInstances(this, 0x01);
        }

    }


    //-----------------------------------------------------------------------------------

    public void applyCommandClassInstances(RkZWaveCommandClassNames commandClassName, int instances) {
        RkZWaveCommandClass commandClass = commandClasses.get(commandClassName);
        if (commandClass == null) return;

        commandClass.createInstances(this, instances);
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
        DeviceParametersNames name = parameter.getServerName();
        if (name != DeviceParametersNames.Unknown)
            serverParameterIds.put(parameter.getServerName(), parameter.getZWaveName());
    }


    //-----------------------------------------------------------------------------------

    public void removeParameter(RkZWaveDeviceParameterNames parameterName) {

        RkZWaveDeviceParameter parameter = parameters.remove(parameterName);
        if (parameter == null) return;
        serverParameterIds.remove(parameter.getServerName());
    }


    //-----------------------------------------------------------------------------------

    public void applyDeviceParametersFromDto(DeviceDTO dto) {

        Object o = dto.getParameters().get(DeviceParametersNames.Switch);
        if (o != null) {
            LOG.debug("apply device ({}) parameter (Switch) value ({})", new Object[]{
                    deviceId,
                    o.toString()
            });
            RkZWaveDeviceParameter parameter = parameters.get(RkZWaveDeviceParameterNames.Switch);
            if (parameter != null) {
                parameter.applyValue(o.toString());
            }
        }

    }


    //-----------------------------------------------------------------------------------

    public void applyDeviceParametersFromByteArray(
            RkZWaveCommandClassNames commandClassId, int[] parameters, int instance) {

        RkZWaveCommandClass commandClass = commandClasses.get(commandClassId);
        if (commandClass == null) return;

        commandClass.messageHandler(this, parameters, instance);

        LOG.debug("RECEIVE PARAMETER : class ({}) parameters ({} )", new Object[]{
                commandClassId.name(),
                LoggingHelper.createHexStringFromIntArray(parameters)
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
}
