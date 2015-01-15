package ru.uproom.gate.localinterface.zwave.devices;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by osipenko on 15.01.15.
 */

@Service
public class ZWaveDevicePoolImpl implements ZWaveDevicePool {


    //##############################################################################################################
    //######    fields


    private static final Logger LOG = LoggerFactory.getLogger(ZWaveDevicePoolImpl.class);

    private final Map<Integer, ZWaveDevice> devices = new HashMap<>();


    //##############################################################################################################
    //######    methods


    private boolean isDeviceVirtual(int deviceId) {
        return false;
    }


    @Override
    public void addNewDevice(int deviceId) {
        if (isDeviceVirtual(deviceId)) return;

        synchronized (devices) {
            ZWaveDevice device = devices.get(deviceId);
            if (device == null) {
                device = new ZWaveDevice(deviceId, this);
                devices.put(deviceId, device);
                LOG.debug("ADD DEVICE : added id = {}", deviceId);
            }
        }

    }


    @Override
    public void removeExistingDevice(int deviceId) {

        synchronized (devices) {
            ZWaveDevice device = devices.remove(deviceId);
            if (device != null)
                LOG.debug("REMOVE DEVICE : removed id = {}", deviceId);
        }

    }
}
