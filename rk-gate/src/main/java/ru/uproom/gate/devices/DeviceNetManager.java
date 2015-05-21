package ru.uproom.gate.devices;

import libraries.api.RkLibraryDevice;
import libraries.api.RkLibraryDriver;
import libraries.api.RkLibraryManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.uproom.gate.devices.zwave.ZWaveDeviceParametersNames;
import ru.uproom.gate.transport.ServerTransport;
import ru.uproom.gate.transport.dto.DeviceDTO;
import ru.uproom.gate.transport.dto.parameters.DeviceStateEnum;
import ru.uproom.libraries.zwave.driver.RkZWaveDriver;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.List;

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
    private final RkLibraryDriver libraryDriver = new RkZWaveDriver();
    @Autowired
    private ServerTransport transport;
    private long homeId;
    private boolean ready;
    private boolean failed;
    private DeviceStateEnum requestState;


    //##############################################################################################################
    //######    constructors

    @PostConstruct
    public void create() {
        LOG.debug(" ==== >> ==== starting DeviceNetManager ==== >> ====");

        libraryDriver.setLibraryManager(this);
        libraryDriver.create();

        LOG.debug(" ==== >> ==== DeviceNetManager started ==== >> ====");
    }

    @PreDestroy
    public void destroy() {
        libraryDriver.destroy();
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
    }

    @Override
    public DeviceStateEnum getRequestedMode() {
        return requestState;
    }


    //------------------------------------------------------------------------
    // give transport object for all interested

    @Override
    public ServerTransport getTransport() {
        return transport;
    }


    //------------------------------------------------------------------------
    // state of Z-Wave Network Controller

    @Override
    public DeviceStateEnum getControllerState() {
        return DeviceStateEnum.Unknown;
    }

    @Override
    public void setControllerState(DeviceStateEnum state, boolean clearRequest) {
    }


    //##############################################################################################################
    //######    inner classes


    //------------------------------------------------------------------------
    // create Z-Wave driver and keep it work

    public List<DeviceDTO> getDeviceDTOList() {

        libraryDriver.requestDeviceList();
        return null;
    }


    //##############################################################################################################
    //######    methods


    //------------------------------------------------------------------------
    // get device list for data transfer to server

    @Override
    public void setDeviceDTO(DeviceDTO dto) {

        libraryDriver.applyDeviceParameters();
    }


    //------------------------------------------------------------------------
    //  find node by ServerID

    @Override
    public String toString() {
        return String.format("{\"id\":\"%d\"", homeId);
    }


    //##############################################################################################################
    //######    methods from RkLibraryManager


    @Override
    public void eventLibraryReady(boolean ready) {
        this.ready = ready;
        getDeviceDTOList();
    }

    @Override
    public void eventSendDeviceList(List<RkLibraryDevice> devices) {

    }

}
