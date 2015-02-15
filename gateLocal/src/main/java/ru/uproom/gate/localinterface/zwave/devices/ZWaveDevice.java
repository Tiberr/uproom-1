package ru.uproom.gate.localinterface.zwave.devices;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.uproom.gate.localinterface.domain.WorkingHelper;
import ru.uproom.gate.localinterface.zwave.commands.ZWaveCommandClass;
import ru.uproom.gate.localinterface.zwave.commands.ZWaveVersionCommandClass;
import ru.uproom.gate.localinterface.zwave.enums.ZWaveCommandClassNames;
import ru.uproom.gate.localinterface.zwave.enums.ZWaveDeviceParameterNames;
import ru.uproom.gate.localinterface.zwave.enums.ZWaveExtraEnums;
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

    private void addCommandClass(byte commandClassId) {

        ZWaveCommandClass commandClass = devicePool.getCommandClassFactory().getCommandClass(commandClassId);
        if (commandClass == null) {
            LOG.info("ADD COMMAND CLASS : {}, not implemented", new Object[]{
                    ZWaveCommandClassNames.getByCode(commandClassId).name()
            });
            return;
        }

        commandClasses.put(ZWaveCommandClassNames.getByCode(commandClassId), commandClass);
        commandClass.createParameterList(this);

    }


    //-----------------------------------------------------------------------------------

    public void fillCommandClassList(byte[] commandClassList) {

        for (byte b : commandClassList) {
            if (b == ZWaveExtraEnums.END_OF_LIST_SUPPORTED_COMMAND_CLASS_MARK)
                break;
            addCommandClass(b);
        }

        getCommandClassesVersions();
    }


    //-----------------------------------------------------------------------------------

    private void getCommandClassesVersions() {

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
        commandClass.requestDeviceState(this);
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

    public void applyDeviceParametersFromByteArray(ZWaveCommandClassNames commandClassId, byte[] parameters) {

        ZWaveCommandClass commandClass = commandClasses.get(commandClassId);
        if (commandClass == null) return;

        commandClass.messageHandler(this, parameters);

        LOG.debug("RECEIVE PARAMETER : class ({}) parameters ({} )", new Object[]{
                commandClassId.name(),
                WorkingHelper.createHexStringFromByteArray(parameters)
        });
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
