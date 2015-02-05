package ru.uproom.gate.localinterface.zwave.devices;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.uproom.gate.localinterface.domain.WorkingHelper;
import ru.uproom.gate.localinterface.zwave.commands.ZWaveCommandClass;
import ru.uproom.gate.localinterface.zwave.enums.ZWaveCommandClasses;
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
    private final Map<Byte, ZWaveCommandClass> commandClasses = new HashMap<>();
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
                    ZWaveCommandClasses.getByCode(commandClassId).name()
            });
            return;
        }

        commandClass.createParameterList(this);

    }


    //-----------------------------------------------------------------------------------

    public void applyCommandClassList(byte[] commandClassList) {

        for (byte b : commandClassList) {
            if (b == ZWaveExtraEnums.END_OF_LIST_SUPPORTED_COMMAND_CLASS_MARK)
                break;
            addCommandClass(b);
        }
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

    public void applyDeviceParametersFromByteArray(ZWaveCommandClasses commandClass, byte[] parameters) {
        LOG.debug("RECEIVE PARAMETER : class ({}) parameters ({})", new Object[]{
                commandClass.name(),
                WorkingHelper.createHexStringFromByteArray(parameters)
        });
    }


    //-----------------------------------------------------------------------------------

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
