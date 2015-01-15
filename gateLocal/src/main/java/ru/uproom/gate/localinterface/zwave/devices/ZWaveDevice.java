package ru.uproom.gate.localinterface.zwave.devices;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by osipenko on 15.01.15.
 */
public class ZWaveDevice {


    //##############################################################################################################
    //######    fields


    private static final Logger LOG = LoggerFactory.getLogger(ZWaveDevice.class);
    private final Map<Integer, ZWaveDeviceParameter> parameters = new HashMap<>();
    private int deviceId;
    private ZWaveDevicePool devicePool;


    //##############################################################################################################
    //######    constructors / destructors


    public ZWaveDevice(int deviceId, ZWaveDevicePool pool) {
        this.deviceId = deviceId;
        this.devicePool = pool;
    }


    //##############################################################################################################
    //######    method


    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
