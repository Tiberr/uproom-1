package ru.uproom.gate.localinterface.zwave.devices;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.uproom.gate.localinterface.zwave.commands.ZWaveCommandClass;
import ru.uproom.gate.localinterface.zwave.enums.ZWaveDeviceParameterNames;
import ru.uproom.gate.transport.dto.parameters.DeviceParametersNames;

import java.util.ArrayList;
import java.util.List;

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

    private String value = "";

    private List<ZWaveDeviceParameterListener> listeners = new ArrayList<>();


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
        if (this.value.equalsIgnoreCase(value)) return;

        this.value = value;
        LOG.debug("PARAMETER CHANGED : parameter ({}) now set value ({})", new Object[]{
                zWaveName,
                value
        });

        while (listeners.size() > 0) {
            ZWaveDeviceParameterListener listener = null;
            synchronized (listeners) {
                listener = listeners.get(0);
            }
            if (listener == null) continue;
            listener.onChange();
            synchronized (listeners) {
                listeners.remove(listener);
            }
        }

    }


    //##############################################################################################################
    //######    methods


    // initiate get value request from device
    public void fetchValue(ZWaveDeviceParameterListener listener) {
        commandClass.requestDeviceParameter(device);
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    // initiate set value request to device
    public void applyValue(String value) {
        commandClass.setDeviceParameter(this, value);
    }


}
