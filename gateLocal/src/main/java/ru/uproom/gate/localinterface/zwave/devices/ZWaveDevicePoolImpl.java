package ru.uproom.gate.localinterface.zwave.devices;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.uproom.gate.localinterface.zwave.commands.ZWaveCommandClassFactoryImpl;
import ru.uproom.gate.localinterface.zwave.driver.ZWaveDriver;
import ru.uproom.gate.localinterface.zwave.driver.ZWaveMessage;
import ru.uproom.gate.localinterface.zwave.enums.ZWaveCommandClassNames;
import ru.uproom.gate.localinterface.zwave.enums.ZWaveFunctionID;
import ru.uproom.gate.localinterface.zwave.enums.ZWaveMessageTypes;
import ru.uproom.gate.transport.dto.DeviceDTO;

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
    private int homeId;
    private int controllerId;
    @Autowired
    private ZWaveDriver driver;
    @Autowired
    private ZWaveCommandClassFactoryImpl commandClassFactory;


    //##############################################################################################################
    //######    getters / setters

    @Override
    public ZWaveDriver getDriver() {
        return driver;
    }

    @Override
    public ZWaveCommandClassFactoryImpl getCommandClassFactory() {
        return commandClassFactory;
    }


    //##############################################################################################################
    //######    methods


    //-----------------------------------------------------------------------------------

    private boolean isDeviceVirtual(int deviceId) {
        return false;
    }

    private void requestNodesInfo() {
        for (Map.Entry<Integer, ZWaveDevice> entry : devices.entrySet()) {

            // todo: add to this point Node::AdvancedQueries

            ZWaveMessage message = new ZWaveMessage(
                    ZWaveMessageTypes.Request, ZWaveFunctionID.REQUEST_NODE_INFO, true);
            message.setParameters(new byte[]{(byte) entry.getValue().getDeviceId()});
            driver.getSerialDataHandler().addMessageToSendingQueue(message);
        }
    }


    //-----------------------------------------------------------------------------------

    @Override
    public void applyDeviceSet(boolean finished) {
        requestNodesInfo();
    }


    //-----------------------------------------------------------------------------------

    @Override
    public void setParameters(int homeId, byte controllerId) {
        this.homeId = homeId;
        this.controllerId = controllerId;
    }


    //-----------------------------------------------------------------------------------

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


    //-----------------------------------------------------------------------------------

    @Override
    public void removeExistingDevice(int deviceId) {

        synchronized (devices) {
            ZWaveDevice device = devices.remove(deviceId);
            if (device != null)
                LOG.debug("REMOVE DEVICE : removed id = {}", deviceId);
        }

    }


    //-----------------------------------------------------------------------------------

    @Override
    public void updateDeviceInfo(int deviceId, byte[] info) {

        ZWaveDevice device = devices.get(deviceId);
        if (device == null) return;

        device.fillCommandClassList(info);
    }


    //-----------------------------------------------------------------------------------

    @Override
    public void applyDeviceParametersFromDto(DeviceDTO dto) {
        if (dto == null) return;

        ZWaveDevice device = devices.get(dto.getZId());
        if (device == null) return;

        device.applyDeviceParametersFromDto(dto);
    }


    //-----------------------------------------------------------------------------------

    @Override
    public void applyDeviceParametersFromByteArray(byte[] data) {
        if (data.length < 2) return;

        ZWaveDevice device = devices.get((int) data[1]);
        if (device == null) return;

        ZWaveCommandClassNames commandClass = ZWaveCommandClassNames.getByCode(data[3]);

        byte[] parameters = new byte[data[2] - 1];
        System.arraycopy(data, 4, parameters, 0, parameters.length);

        device.applyDeviceParametersFromByteArray(commandClass, parameters);
    }
}
