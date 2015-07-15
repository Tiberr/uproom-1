package ru.uproom.gate.devices;

import libraries.api.RkLibraryDevice;
import libraries.api.RkLibraryDriver;
import libraries.api.RkLibraryManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.uproom.gate.devices.zwave.ZWaveDeviceParametersNames;
import ru.uproom.gate.transport.ServerTransport;
import ru.uproom.gate.transport.command.SendDeviceListCommand;
import ru.uproom.gate.transport.dto.DeviceDTO;
import ru.uproom.gate.transport.dto.parameters.DeviceStateEnum;
import ru.uproom.libraries.zwave.driver.RkZWaveDriver;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * inner class which contain list of Z-Wave devices
 * <p/>
 * Created by osipenko on 10.08.14.
 */
@Service
public class DeviceNetManager implements GateDevicesSet, RkLibraryManager {


    //##############################################################################################################
    //######    fields

    private static final Logger LOG = LoggerFactory.getLogger(GateDevicesSet.class);
    private final Map<Integer, GateDevice> devices = new HashMap<>();
    @Autowired
    private ServerTransport transport;
    private long homeId;
    private boolean ready;
    private boolean failed;
    private DeviceStateEnum requestState;
    @Value("${used_library}")
    private String usedLibrary;
    private RkLibraryDriver libraryDriver;


    //##############################################################################################################
    //######    constructors / destructors

    @PostConstruct
    public void create() {
        LOG.debug(" ==== >> ==== preparing DeviceNetManager ==== >> ====");

        switch (usedLibrary) {
            case "zwave":
                libraryDriver = new RkZWaveDriver();
                break;

            case "tindenet":
                //libraryDriver = new RkTindenetDriver();
                break;

            default:
        }

        libraryDriver.setLibraryManager(this);

        LOG.debug(" ==== >> ==== DeviceNetManager prepared ==== >> ====");
    }

    @PreDestroy
    public void destroy() {
        libraryDriver.destroy();
    }

    @Override
    public void start() {
        LOG.info(" ==== >> ==== starting DeviceNetManager ==== >> ====");
        libraryDriver.create();
        LOG.info(" ==== >> ==== DeviceNetManager started ==== >> ====");
    }


    //##############################################################################################################
    //######    getters and setters


    //------------------------------------------------------------------------
    //  home ID

    public long getHomeId() {
        return homeId;
    }

    public void setHomeId(long homeId) {
        this.homeId = homeId;
    }


    //------------------------------------------------------------------------
    //  z-wave system ready to work

    public boolean isReady() {
        return ready;
    }

    public void setReady(boolean ready) {
    }

    public boolean isFailed() {
        boolean temp = failed;
        failed = false;
        return temp;
    }

    public void setFailed(boolean failed) {
        this.failed = failed;
    }


    //------------------------------------------------------------------------
    // node list

    @Override
    public void addGateDevice(int index) {
    }

    @Override
    public void removeGateDevice(int index) {
    }


    //------------------------------------------------------------------------
    // node groups

    @Override
    public void addDeviceGroup(int indexDevice, int indexGroup) {
    }


    //------------------------------------------------------------------------
    // device parameters

    @Override
    public void addGateDeviceParameter(int indexDevice, ZWaveDeviceParametersNames paramName, Object paramValue) {
    }

    @Override
    public void removeGateDeviceParameter(int indexDevice, ZWaveDeviceParametersNames paramName) {
    }


    //------------------------------------------------------------------------
    // changing devices set mode will activate

    @Override
    public void requestMode(DeviceStateEnum mode) {

        switch (mode) {

            case Add:
                libraryDriver.toggleControllerToAddingMode();
                break;

            case Remove:
                libraryDriver.toggleControllerToRemovingMode();
                break;

            case Cancel:
                libraryDriver.interruptCurrentCommandInController();
                break;

            default:
        }
    }

    @Override
    public DeviceStateEnum getRequestedMode() {
        return requestState;
    }


    //------------------------------------------------------------------------

    @Override
    public ServerTransport getTransport() {
        return transport;
    }


    //------------------------------------------------------------------------

    @Override
    public DeviceStateEnum getControllerState() {
        return DeviceStateEnum.Unknown;
    }

    @Override
    public void setControllerState(DeviceStateEnum state, boolean clearRequest) {
    }


    //##############################################################################################################
    //######    methods


    //------------------------------------------------------------------------

    @Override
    public List<DeviceDTO> getDeviceDtoList() {

        List<DeviceDTO> dtoList = new ArrayList<>();
        for (Map.Entry<Integer, GateDevice> entry : devices.entrySet()) {
            dtoList.add(entry.getValue().getDeviceDto());
        }

        if (ready)
            transport.sendCommand(new SendDeviceListCommand(dtoList));

        return dtoList;
    }


    //------------------------------------------------------------------------

    @Override
    public void applyParametersFromDeviceDto(DeviceDTO dto) {

        GateDevice device = devices.get(dto.getZId());
        if (device != null)
            device.applyParametersFromDto(dto);
    }


    //------------------------------------------------------------------------

    @Override
    public String toString() {
        return String.format("{\"id\":\"%d\"", homeId);
    }


    //##############################################################################################################
    //######    methods from RkLibraryManager


    @Override
    public void eventLibraryReady(boolean ready) {

        boolean readyWillBeSet = !this.ready && ready;
        this.ready = ready;

        if (readyWillBeSet) {

            LOG.info("LIBRARY READY");
            List<RkLibraryDevice> libraryDevices = libraryDriver.getDeviceList();
            for (RkLibraryDevice libraryDevice : libraryDevices) {
                devices.put(libraryDevice.getDeviceId(), new GateDevice(this, libraryDevice));
            }
            getDeviceDtoList();
        }
    }

    @Override
    public void eventSendDeviceList(List<RkLibraryDevice> devices) {

    }

}
