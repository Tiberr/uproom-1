package ru.uproom.libraries.zwave.devices;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.uproom.libraries.zwave.commands.RkZWaveCommandClassFactory;
import ru.uproom.libraries.zwave.driver.RkZWaveDriver;
import ru.uproom.libraries.zwave.driver.RkZWaveMessage;
import ru.uproom.libraries.zwave.enums.RkZWaveCommandClassNames;
import ru.uproom.libraries.zwave.enums.RkZWaveFunctionID;
import ru.uproom.libraries.zwave.enums.RkZWaveMessageTypes;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by osipenko on 03.05.15.
 */
public class RkZWaveDevicePool {


    //==================================================================================================
    //======      fields


    private static final Logger LOG = LoggerFactory.getLogger(RkZWaveDevicePool.class);
    private final RkZWaveCommandClassFactory commandClassFactory = new RkZWaveCommandClassFactory();
    private final Map<Integer, RkZWaveDevice> devices = new HashMap<>();
    private RkZWaveDriver driver;
    private long homeId;
    private int controllerId;

    private boolean ready;


    //==================================================================================================
    //======      constructors / destructors


    //-----------------------------------------------------------------------------------

    public int create() {
        commandClassFactory.create();

        return 0;
    }


    //-----------------------------------------------------------------------------------

    public void destroy() {
        commandClassFactory.destroy();
    }


    //==================================================================================================
    //======      getters / setters

    public RkZWaveDriver getDriver() {
        return driver;
    }

    public void setDriver(RkZWaveDriver driver) {
        this.driver = driver;
    }


    //------------------------------------------------------------------------

    public RkZWaveCommandClassFactory getCommandClassFactory() {
        return commandClassFactory;
    }


    //==================================================================================================
    //======      methods


    public void requestDeviceList() {
        if (!ready) return;

        //todo : realize getting device list
    }


    //-----------------------------------------------------------------------------------

    private boolean isDeviceVirtual(int deviceId) {
        return false;
    }


    //-----------------------------------------------------------------------------------

    private void requestDeviceInfo(RkZWaveDevice device) {
        RkZWaveMessage message = new RkZWaveMessage(
                RkZWaveMessageTypes.Request, RkZWaveFunctionID.REQUEST_NODE_INFO, device, true);
        int[] data = new int[1];
        data[0] = device.getDeviceId();
        message.setParameters(data);
        driver.addMessageToSendingQueue(message);
    }


    //-----------------------------------------------------------------------------------

    public void updateDeviceInfo(int deviceId, int[] info) {

        RkZWaveDevice device = devices.get(deviceId);
        if (device == null) return;

        device.fillCommandClassList(info);
        // todo: probably add to this point Node::AdvancedQueries
    }


    //-----------------------------------------------------------------------------------

    private void requestDevicesFailedId() {

        for (Map.Entry<Integer, RkZWaveDevice> entry : devices.entrySet()) {
            requestDeviceFailedId(entry.getValue());
        }
    }


    //-----------------------------------------------------------------------------------

    private void requestDeviceFailedId(RkZWaveDevice device) {

        RkZWaveMessage message = new RkZWaveMessage(
                RkZWaveMessageTypes.Request, RkZWaveFunctionID.IS_FAILED_NODE_ID, device, true);
        int[] data = new int[1];
        data[0] = device.getDeviceId();
        message.setParameters(data);
        driver.addMessageToSendingQueue(message);
    }


    //-----------------------------------------------------------------------------------

    public void updateDeviceFailedId(int deviceId, boolean failed) {

        RkZWaveDevice device = devices.get(deviceId);
        if (device == null) return;

        device.setFailedId(failed);
        if (!failed)
            requestDeviceInfo(device);
    }


    //-----------------------------------------------------------------------------------

    public void setControllerReady(boolean ready) {
        if (ready)
            requestDevicesFailedId();
    }


    //-----------------------------------------------------------------------------------

    public void setParameters(long homeId, int controllerId) {
        this.homeId = homeId;
        this.controllerId = controllerId;
    }


    //-----------------------------------------------------------------------------------

    public void deviceMapProcessing(int[] deviceMap) {

        for (int i = 0; i < deviceMap.length; i++) {
            for (int j = 0; j < 8; j++) {
                int deviceId = (i * 8) + (j + 1);
                int deviceBit = (int) deviceMap[i] & (0x01 << j);
                if (deviceBit != 0) {
                    addNewDevice(deviceId);
                } else {
                    removeExistingDevice(deviceId);
                }
            }
        }

    }


    //-----------------------------------------------------------------------------------

    public void addNewDevice(int deviceId) {
        if (isDeviceVirtual(deviceId)) return;

        synchronized (devices) {
            RkZWaveDevice device = devices.get(deviceId);
            if (device == null) {
                device = new RkZWaveDevice(deviceId, this);
                devices.put(deviceId, device);
                LOG.debug("ADD DEVICE : added id = {}", deviceId);
            }
        }

    }


    //-----------------------------------------------------------------------------------

    public void removeExistingDevice(int deviceId) {

        synchronized (devices) {
            devices.remove(deviceId);
        }

    }


    //-----------------------------------------------------------------------------------

    public void applyDeviceParametersFromIntArray(int[] data) {
        if (data.length < 2) return;

        RkZWaveDevice device = devices.get(data[1]);
        if (device == null) return;

        RkZWaveCommandClassNames commandClass = RkZWaveCommandClassNames.getByCode(data[3]);

        int[] parameters = new int[data[2] - 1];
        System.arraycopy(data, 4, parameters, 0, parameters.length);

        device.applyDeviceParametersFromByteArray(commandClass, parameters);
    }

}
