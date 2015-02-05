package ru.uproom.gate.localinterface.zwave.devices;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.uproom.gate.localinterface.zwave.commands.ZWaveCommandClass;
import ru.uproom.gate.localinterface.zwave.enums.ZWaveDeviceParameterNames;
import ru.uproom.gate.transport.dto.parameters.DeviceParametersNames;

/**
 * Base class for Z-Wave Node Value
 * <p/>
 * Created by osipenko on 15.01.15.
 */
public class ZWaveDeviceParameter {


    //##############################################################################################################
    //######    fields


    private static final Logger LOG = LoggerFactory.getLogger(ZWaveDeviceParameter.class);

    private ZWaveDevice device;
    private ZWaveCommandClass commandClass;

    private ZWaveDeviceParameterNames zWaveName;
    private DeviceParametersNames serverName;

    private String value;


    //##############################################################################################################
    //######    constructors / destructors


    public ZWaveDeviceParameter(
            ZWaveDevice device, ZWaveCommandClass commandClass,
            ZWaveDeviceParameterNames zWaveName, DeviceParametersNames serverName
    ) {
        this.device = device;
        this.commandClass = commandClass;
        this.zWaveName = zWaveName;
        this.serverName = serverName;
    }


    //##############################################################################################################
    //######    getters / setters


    public ZWaveDevice getDevice() {
        return device;
    }


    public ZWaveCommandClass getCommandClass() {
        return commandClass;
    }


    public ZWaveDeviceParameterNames getZWaveName() {
        return zWaveName;
    }


    public DeviceParametersNames getServerName() {
        return serverName;
    }


    protected String getValue() {
        return value;
    }

    protected void setValue(String value) {
        this.value = value;
    }


    //##############################################################################################################
    //######    methods


    // must be overriding
    public String fetchValue() {
        return null;
    }

    // must be overriding
    public void applyValue(String value) {
    }


}
