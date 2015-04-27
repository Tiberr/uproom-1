package ru.uproom.libraries.zwave;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.uproom.gate.transport.dto.DeviceDTO;
import ru.uproom.libraries.RkLibraryEventListener;
import ru.uproom.libraries.RkLibraryManager;
import ru.uproom.libraries.auxilliary.RunnableClass;
import ru.uproom.libraries.events.RkLibraryEvent;
import ru.uproom.libraries.zwave.devices.RkZWaveDevice;
import ru.uproom.libraries.zwave.devices.RkZWaveDevicePool;
import ru.uproom.libraries.zwave.driver.RkZWaveDriver;
import ru.uproom.libraries.zwave.driver.RkZWaveDriverImpl;
import ru.uproom.libraries.zwave.driver.RkZWaveMessage;
import ru.uproom.libraries.zwave.enums.RkZWaveCommandClassNames;
import ru.uproom.libraries.zwave.enums.RkZWaveFunctionID;
import ru.uproom.libraries.zwave.enums.RkZWaveMessageTypes;

import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Created by osipenko on 31.03.15.
 */
public class RkZWaveManager implements RkLibraryManager, RkZWaveDevicePool {


    //==================================================================================================
    //======      fields


    private static final Logger LOG = LoggerFactory.getLogger(RkZWaveManager.class);

    private final List<RkLibraryEventListener> eventListeners = new ArrayList<>();
    private final List<RkLibraryEvent> events = new LinkedList<>();
    private final EventDispatcher eventDispatcher = new EventDispatcher();

    private final Properties properties = new Properties();

    private final RkZWaveDriver driver = new RkZWaveDriverImpl();

    private final Map<Integer, RkZWaveDevice> devices = new HashMap<>();
    private final Map<Integer, Integer> deviceServerMap = new HashMap<>();

    private long homeId;
    private int controllerId;


    //==================================================================================================
    //======      constructors / destructors

    //-----------------------------------------------------------------------------------

    @Override
    public int create() {

        LOG.info(" *****  Rezhiser Komforta Z-Wave Library starting...  ***** ");

        try {
            properties.load(new FileReader("rk-lib-zwave.properties"));
        } catch (IOException e) {
            LOG.error("library can not load properties : {}", e.getMessage());
        }

        driver.setDevicePool(this);
        driver.create();

        eventDispatcher.setTimeout(10);
        new Thread(eventDispatcher).start();

        LOG.info(" *****  Rezhiser Komforta Z-Wave Library STARTED successfully.  ***** ");
        return 0;
    }

    //-----------------------------------------------------------------------------------

    @Override
    public void destroy() {

        LOG.info(" *****  Rezhiser Komforta Z-Wave Library stopping...  ***** ");

        eventDispatcher.stop();
        events.clear();

        driver.destroy();

        LOG.info(" *****  Rezhiser Komforta Z-Wave Library STOPPED successfully.  ***** ");
    }


    //==================================================================================================
    //======      getters / setters


    //-----------------------------------------------------------------------------------

    @Override
    public Properties getProperties() {
        return properties;
    }


    //==================================================================================================
    //======      methods


    //-----------------------------------------------------------------------------------

    @Override
    public void addEventListener(RkLibraryEventListener listener) {
        if (!eventListeners.contains(listener))
            eventListeners.add(listener);
    }


    //-----------------------------------------------------------------------------------

    @Override
    public void removeEventListener(RkLibraryEventListener listener) {
        eventListeners.remove(listener);
    }


    //-----------------------------------------------------------------------------------

    @Override
    public void requestDeviceParameters(DeviceDTO device) {

    }


    //-----------------------------------------------------------------------------------

    @Override
    public void applyDeviceParameters(DeviceDTO device) {

    }


    //-----------------------------------------------------------------------------------

    public void addEventToQueue(RkLibraryEvent event) {
        synchronized (events) {
            events.add(event);
        }
    }


    //-----------------------------------------------------------------------------------

    private void callListeners(RkLibraryEvent event) {
        for (RkLibraryEventListener listener : eventListeners)
            listener.handleEvent(event);
    }


    //-----------------------------------------------------------------------------------

    private boolean isDeviceVirtual(int deviceId) {
        return false;
    }


    //-----------------------------------------------------------------------------------

    private void requestNodesInfo() {

        for (Map.Entry<Integer, RkZWaveDevice> entry : devices.entrySet()) {
            // todo: add to this point Node::AdvancedQueries
            RkZWaveMessage message = new RkZWaveMessage(
                    RkZWaveMessageTypes.Request, RkZWaveFunctionID.REQUEST_NODE_INFO, true);
            message.setParameters(new int[]{entry.getValue().getDeviceId()});
            driver.addMessageToSendingQueue(message);
        }
    }


    //-----------------------------------------------------------------------------------

    @Override
    public RkZWaveDriver getDriver() {
        return driver;
    }


    //-----------------------------------------------------------------------------------

    @Override
    public void setDriverReady(boolean ready) {
        requestNodesInfo();
    }


    //-----------------------------------------------------------------------------------

    @Override
    public void setParameters(int homeId, int controllerId) {
        this.homeId = homeId;
        this.controllerId = controllerId;
    }


    //-----------------------------------------------------------------------------------

    @Override
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

    @Override
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

    @Override
    public void removeExistingDevice(int deviceId) {

        synchronized (devices) {
            RkZWaveDevice device = devices.remove(deviceId);
            if (device != null) {
                deviceServerMap.remove(device.getDeviceServerId());
                LOG.debug("REMOVE DEVICE : removed id = {}", deviceId);
            }
        }

    }


    //-----------------------------------------------------------------------------------

    @Override
    public void updateDeviceInfo(int deviceId, int[] info) {

        RkZWaveDevice device = devices.get(deviceId);
        if (device == null) return;

        device.fillCommandClassList(info);
    }


    //-----------------------------------------------------------------------------------

    @Override
    public void applyDeviceParametersFromDto(DeviceDTO dto) {
        if (dto == null) return;

        RkZWaveDevice device = devices.get(dto.getZId());
        if (device == null) return;

        device.applyDeviceParametersFromDto(dto);
    }


    //-----------------------------------------------------------------------------------

    @Override
    public void applyDeviceParametersFromIntArray(int[] data) {
        if (data.length < 2) return;

        RkZWaveDevice device = devices.get(data[1]);
        if (device == null) return;

        RkZWaveCommandClassNames commandClass = RkZWaveCommandClassNames.getByCode(data[3]);

        int[] parameters = new int[data[2] - 1];
        System.arraycopy(data, 4, parameters, 0, parameters.length);

        device.applyDeviceParametersFromByteArray(commandClass, parameters);
    }


    //==================================================================================================
    //======      inner classes


    private class EventDispatcher extends RunnableClass {


        private void eventHandler(RkLibraryEvent event) {

            switch (event.getEventType()) {

                case HubConnected:
                    break;

                case HubNotConnected:
                    break;

                default:
                    callListeners(event);
            }

        }


        @Override
        protected void body() {

            RkLibraryEvent event = null;
            while (!events.isEmpty()) {
                try {
                    synchronized (events) {
                        event = events.remove(0);
                    }
                } catch (IndexOutOfBoundsException e) {
                    continue;
                }
                if (event != null) eventHandler(event);
            }

        }

    }

}



/*

    последовательность действий.

    1 . Инициализация библиотеки
    2 . Открытие порта
    3 . Отсылка пакета команд инициализации
    4 . Получение списка устройств
    5 . Получение типа каждого устройства
    6 . Получение списка классов команд для каждого устройства
    7 . Получение версии библиотеки для каждого класса








 */