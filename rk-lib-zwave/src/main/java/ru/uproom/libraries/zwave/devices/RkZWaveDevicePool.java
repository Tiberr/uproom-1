package ru.uproom.libraries.zwave.devices;

import libraries.api.RkLibraryDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.uproom.libraries.zwave.commands.RkZWaveCommandClassFactory;
import ru.uproom.libraries.zwave.driver.RkZWaveDriver;
import ru.uproom.libraries.zwave.enums.RkZWaveCommandClassNames;
import ru.uproom.libraries.zwave.enums.RkZWaveControllerError;
import ru.uproom.libraries.zwave.enums.RkZWaveControllerState;
import ru.uproom.libraries.zwave.enums.RkZWaveFunctionID;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
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
    private RkZWaveFunctionID currentControllerCommand = RkZWaveFunctionID.UNKNOWN;
    private RkZWaveControllerState controllerState = RkZWaveControllerState.Normal;
    private RkZWaveControllerError controllerError = RkZWaveControllerError.None;


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


    //------------------------------------------------------------------------

    public int getControllerId() {
        return controllerId;
    }


    //------------------------------------------------------------------------

    public RkZWaveFunctionID getCurrentControllerCommand() {
        return currentControllerCommand;
    }

    public void setCurrentControllerCommand(RkZWaveFunctionID currentControllerCommand) {
        this.currentControllerCommand = currentControllerCommand;
    }


    //------------------------------------------------------------------------

    public RkZWaveControllerState getControllerState() {
        return controllerState;
    }

    public void setControllerState(RkZWaveControllerState controllerState) {
        this.controllerState = controllerState;
    }


    //------------------------------------------------------------------------

    public RkZWaveControllerError getControllerError() {
        return controllerError;
    }

    public void setControllerError(RkZWaveControllerError controllerError) {
        this.controllerError = controllerError;
    }


    //==================================================================================================
    //======      methods


    public List<RkLibraryDevice> getDeviceList() {
        if (!ready) return null;

        final List<RkLibraryDevice> libraryDevices = new LinkedList<>();
        for (Map.Entry<Integer, RkZWaveDevice> entry : devices.entrySet()) {
            libraryDevices.add(entry.getValue());
        }

        return libraryDevices;
    }


    //-----------------------------------------------------------------------------------

    private boolean isDeviceVirtual(int deviceId) {
        return false;
    }


    //-----------------------------------------------------------------------------------

    private void startDevicesInitSequence() {

        synchronized (devices) {
            for (Map.Entry<Integer, RkZWaveDevice> entry : devices.entrySet()) {
                entry.getValue().startInitSequence();
            }
        }
    }


    //-----------------------------------------------------------------------------------

    public void updateDeviceInfo(int deviceId, int[] info) {

        RkZWaveDevice device = devices.get(deviceId);
        if (device == null) return;

        device.updateInfo(info);
    }


    //-----------------------------------------------------------------------------------

    public void setControllerReady(boolean ready) {
        if (ready)
            startDevicesInitSequence();
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


    //-----------------------------------------------------------------------------------

    public void deviceReady() {

        boolean allDevicesReady = true;
        for (Map.Entry<Integer, RkZWaveDevice> entry : devices.entrySet()) {
            if (!entry.getValue().isReady()) {
                allDevicesReady = false;
                break;
            }
        }
        if (allDevicesReady) {
            LOG.info("DEVICE POOL READY");
            ready = true;
            driver.devicePoolReady(true);
        }
    }

}
