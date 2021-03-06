package ru.uproom.libraries.zwave.devices;

import libraries.api.RkLibraryDeviceParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.uproom.gate.transport.dto.parameters.DeviceParametersNames;
import ru.uproom.libraries.zwave.commands.RkZWaveCommandClass;
import ru.uproom.libraries.zwave.enums.RkZWaveDeviceParameterNames;
import ru.uproom.libraries.zwave.enums.RkZWaveMeterUnits;

/**
 * Base class for Z-Wave Node Value
 * <p/>
 * Created by osipenko on 15.01.15.
 */
public class RkZWaveDeviceParameter implements RkLibraryDeviceParameter {


    //##############################################################################################################
    //######    fields


    private static final Logger LOG = LoggerFactory.getLogger(RkZWaveDeviceParameter.class);
    private RkZWaveDevice device;
    private RkZWaveCommandClass commandClass;
    private RkZWaveDeviceParameterNames zWaveName;
    private DeviceParametersNames serverName;
    private String value = "";
    private int precision;
    private RkZWaveMeterUnits units;
    private String prevValue = "";
    private int prevPrecision;
    private int interval;


    //##############################################################################################################
    //######    constructors / destructors


    public RkZWaveDeviceParameter(
            RkZWaveDevice device, RkZWaveCommandClass commandClass,
            RkZWaveDeviceParameterNames zWaveName, DeviceParametersNames serverName
    ) {
        this.device = device;
        this.commandClass = commandClass;
        this.zWaveName = zWaveName;
        this.serverName = serverName;
    }


    //##############################################################################################################
    //######    getters / setters


    //-------------------------------------------------------------------------------------

    public RkZWaveDevice getDevice() {
        return device;
    }


    //-------------------------------------------------------------------------------------

    public RkZWaveCommandClass getCommandClass() {
        return commandClass;
    }


    //-------------------------------------------------------------------------------------

    public RkZWaveDeviceParameterNames getZWaveName() {
        return zWaveName;
    }

    public void setZWaveName(RkZWaveDeviceParameterNames name) {
        zWaveName = name;
    }


    //-------------------------------------------------------------------------------------

    public DeviceParametersNames getServerName() {
        return serverName;
    }


    //-------------------------------------------------------------------------------------

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        if (this.value.equalsIgnoreCase(value)) return;

        this.value = value;
        LOG.debug("PARAMETER CHANGED : parameter ({}) now set value ({})", new Object[]{
                zWaveName,
                value
        });

    }


    //-------------------------------------------------------------------------------------

    public RkZWaveMeterUnits getUnits() {
        return units;
    }

    public void setUnits(RkZWaveMeterUnits units) {
        this.units = units;
    }


    //-------------------------------------------------------------------------------------

    public int getPrecision() {
        return precision;
    }

    public void setPrecision(int precision) {
        this.precision = precision;
    }


    //-------------------------------------------------------------------------------------

    public String getPrevValue() {
        return prevValue;
    }

    public void setPrevValue(String prevValue) {
        this.prevValue = prevValue;
    }


    //-------------------------------------------------------------------------------------

    public int getPrevPrecision() {
        return prevPrecision;
    }

    public void setPrevPrecision(int prevPrecision) {
        this.prevPrecision = prevPrecision;
    }


    //-------------------------------------------------------------------------------------

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }


    //##############################################################################################################
    //######    methods


    //-------------------------------------------------------------------------------------
    // initiate get value request from device

    public void requestValue(RkZWaveDeviceParameterListener listener) {
        commandClass.requestDeviceParameter(zWaveName.getInstance());
    }


    //-------------------------------------------------------------------------------------
    // initiate set value request to device

    public void applyValue(String value) {
        commandClass.setDeviceParameter(this, value);
    }


}
