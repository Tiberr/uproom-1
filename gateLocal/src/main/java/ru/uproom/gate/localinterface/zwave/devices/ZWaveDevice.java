package ru.uproom.gate.localinterface.zwave.devices;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.uproom.gate.localinterface.zwave.commands.ZWaveCommandClass;
import ru.uproom.gate.localinterface.zwave.commands.ZWaveMultiInstanceCommandClass;
import ru.uproom.gate.localinterface.zwave.commands.ZWaveVersionCommandClass;
import ru.uproom.gate.localinterface.zwave.driver.ZWaveMessage;
import ru.uproom.gate.localinterface.zwave.enums.*;
import ru.uproom.gate.transport.domain.LoggingHelper;
import ru.uproom.gate.transport.dto.DeviceDTO;
import ru.uproom.gate.transport.dto.parameters.DeviceParametersNames;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by osipenko on 15.01.15.
 */
public class ZWaveDevice {


    //##############################################################################################################
    //######    fields


    private static final Logger LOG = LoggerFactory.getLogger(ZWaveDevice.class);
    private final Map<ZWaveCommandClassNames, ZWaveCommandClass> commandClasses = new HashMap<>();
    private final Map<DeviceParametersNames, ZWaveDeviceParameterNames> serverParameterIds = new HashMap<>();

    private final Map<ZWaveDeviceParameterNames, ZWaveDeviceParameter> parameters = new HashMap<>();

    private int deviceId;

    private ZWaveDevicePool devicePool;


    //##############################################################################################################
    //######    constructors / destructors


    public ZWaveDevice(int deviceId, ZWaveDevicePool pool) {
        this.deviceId = deviceId;
        this.devicePool = pool;

        requestNodeProtocolInfo();
    }


    //##############################################################################################################
    //######    getters / setters


    public int getDeviceId() {
        return deviceId;
    }


    public ZWaveDevicePool getDevicePool() {
        return devicePool;
    }

    //##############################################################################################################
    //######    method


    //-----------------------------------------------------------------------------------

    private void requestNodeProtocolInfo() {

        ZWaveMessage message = new ZWaveMessage(
                ZWaveMessageTypes.Request,
                ZWaveFunctionID.GET_NODE_PROTOCOL_INFO,
                true
        );
        byte[] data = new byte[1];
        data[0] = (byte) deviceId;
        message.setParameters(data);

    }


    public void updateNodeProtocolInfo(byte[] info) {

    }


    //-----------------------------------------------------------------------------------

    private void addCommandClass(byte commandClassId) {

        ZWaveCommandClass commandClass = devicePool.getCommandClassFactory().getCommandClass(commandClassId);
        if (commandClass == null) {
            LOG.info("ADD COMMAND CLASS : {}, not implemented", new Object[]{
                    ZWaveCommandClassNames.getByCode(commandClassId).name()
            });
            return;
        }

        commandClasses.put(ZWaveCommandClassNames.getByCode(commandClassId), commandClass);
        // todo : probably fill parameter list in requestCommandClassInstances. Check it.
        commandClass.createParameterList(this, (byte) 0x01);
    }


    //-----------------------------------------------------------------------------------

    public void fillCommandClassList(byte[] commandClassList) {

        for (byte b : commandClassList) {
            if (b == ZWaveExtraEnums.END_OF_LIST_SUPPORTED_COMMAND_CLASS_MARK)
                break;
            addCommandClass(b);
        }

        requestCommandClassesVersions();
        requestCommandClassesInstances();
    }


    //-----------------------------------------------------------------------------------

    private void requestCommandClassesVersions() {

        ZWaveVersionCommandClass versionClass =
                (ZWaveVersionCommandClass) commandClasses.get(ZWaveCommandClassNames.Version);
        for (ZWaveCommandClassNames className : commandClasses.keySet()) {
            if (versionClass != null)
                versionClass.requestCommandClassVersion(this, className);
            else
                commandClasses.get(className).setVersion((byte) 0x01);
        }

    }


    //-----------------------------------------------------------------------------------

    public void applyCommandClassVersion(ZWaveCommandClassNames commandClassName, byte version) {
        ZWaveCommandClass commandClass = commandClasses.get(commandClassName);
        if (commandClass == null) return;

        commandClass.setVersion(version);
        commandClass.requestDeviceState(this, (byte) 0x01);
    }


    //-----------------------------------------------------------------------------------

    private void requestCommandClassesInstances() {

        ZWaveMultiInstanceCommandClass instanceClass =
                (ZWaveMultiInstanceCommandClass) commandClasses.get(ZWaveCommandClassNames.MultiInstance);
        for (ZWaveCommandClassNames className : commandClasses.keySet()) {
            if (instanceClass != null)
                instanceClass.requestInstance(this, className);
            else
                commandClasses.get(className).createInstances(this, (byte) 0x01);
        }

    }


    //-----------------------------------------------------------------------------------

    public void applyCommandClassInstances(ZWaveCommandClassNames commandClassName, byte instances) {
        ZWaveCommandClass commandClass = commandClasses.get(commandClassName);
        if (commandClass == null) return;

        commandClass.createInstances(this, instances);
    }


    //-----------------------------------------------------------------------------------

    public ZWaveCommandClass getCommandClassById(byte commandClassId) {
        return getCommandClassByName(ZWaveCommandClassNames.getByCode(commandClassId));
    }


    public ZWaveCommandClass getCommandClassByName(ZWaveCommandClassNames commandClassName) {
        return commandClasses.get(commandClassName);
    }


    //-----------------------------------------------------------------------------------

    public void addParameter(ZWaveDeviceParameter parameter) {
        parameters.put(parameter.getZWaveName(), parameter);
        DeviceParametersNames name = parameter.getServerName();
        if (name != DeviceParametersNames.Unknown)
            serverParameterIds.put(parameter.getServerName(), parameter.getZWaveName());
    }


    //-----------------------------------------------------------------------------------

    public void applyDeviceParametersFromDto(DeviceDTO dto) {

        Object o = dto.getParameters().get(DeviceParametersNames.Switch);
        if (o != null) {
            LOG.debug("apply device ({}) parameter (Switch) value ({})", new Object[]{
                    deviceId,
                    o.toString()
            });
            ZWaveDeviceParameter parameter = parameters.get(ZWaveDeviceParameterNames.Switch);
            if (parameter != null) {
                parameter.applyValue(o.toString());
            }
        }

    }


    //-----------------------------------------------------------------------------------

    public void applyDeviceParametersFromByteArray(
            ZWaveCommandClassNames commandClassId, byte[] parameters, byte instance) {

        ZWaveCommandClass commandClass = commandClasses.get(commandClassId);
        if (commandClass == null) return;

        commandClass.messageHandler(this, parameters, instance);

        LOG.debug("RECEIVE PARAMETER : class ({}) parameters ({} )", new Object[]{
                commandClassId.name(),
                LoggingHelper.createHexStringFromByteArray(parameters)
        });
    }

    public void applyDeviceParametersFromByteArray(
            ZWaveCommandClassNames commandClassId, byte[] parameters) {
        applyDeviceParametersFromByteArray(commandClassId, parameters, (byte) 0x01);
    }


    //-----------------------------------------------------------------------------------

    public void applyDeviceParametersFromName(ZWaveDeviceParameterNames parameterName, String value) {
        ZWaveDeviceParameter parameter = parameters.get(parameterName);
        if (parameter == null) return;

        parameter.setValue(value);
    }


    //-----------------------------------------------------------------------------------

    public ZWaveDeviceParameter getDeviceParameterByName(ZWaveDeviceParameterNames parameterName) {
        return parameters.get(parameterName);
    }


    //-----------------------------------------------------------------------------------

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
