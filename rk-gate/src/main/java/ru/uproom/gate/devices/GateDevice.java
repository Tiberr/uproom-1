package ru.uproom.gate.devices;

import libraries.api.RkLibraryDevice;
import libraries.api.RkLibraryDeviceParameterName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.uproom.gate.transport.dto.DeviceDTO;
import ru.uproom.gate.transport.dto.DeviceType;
import ru.uproom.gate.transport.dto.parameters.DeviceParametersNames;

import java.util.HashMap;
import java.util.Map;

/**
 * device in Z-Wave net
 * <p/>
 * Created by osipenko on 31.07.14.
 */
public class GateDevice {


    //=============================================================================================================
    //======    fields

    private static final Logger LOG = LoggerFactory.getLogger(GateDevice.class);


    private RkLibraryDevice libraryDevice;

    private GateDevicesSet devicePool;

    private int deviceId;
    private DeviceType deviceType = DeviceType.None;


    //=============================================================================================================
    //======    constructors


    public GateDevice(GateDevicesSet devicePool, RkLibraryDevice libraryDevice) {

        this.devicePool = devicePool;
        this.libraryDevice = libraryDevice;

        this.deviceType = GateTypesConverter.deviceTypeFromLibraryToServer(libraryDevice.getDeviceType());
    }


    //=============================================================================================================
    //======    getters and setters


    public GateDevicesSet getDevicePool() {
        return devicePool;
    }


    public int getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }


    //##############################################################################################################
    //######    methods


    public DeviceDTO getDeviceDto() {

        DeviceDTO dto = new DeviceDTO(deviceId, libraryDevice.getDeviceId(), deviceType);
        Map<DeviceParametersNames, Object> parameters = dto.getParameters();

        for (Map.Entry<RkLibraryDeviceParameterName, String> entry
                : libraryDevice.getParameterList().entrySet()) {

            switch (entry.getKey()) {
                case Switch:
                    parameters.put(DeviceParametersNames.Switch, Boolean.valueOf(entry.getValue()));
                    break;
                case Level:
                    parameters.put(DeviceParametersNames.Level, Integer.parseInt(entry.getValue()));
                    break;
                case Color:
                    parameters.put(DeviceParametersNames.Color, Integer.parseInt(entry.getValue()));
                    break;
                default:
            }

        }

        return dto;
    }


    //------------------------------------------------------------------------

    public void applyParametersFromDto(DeviceDTO dto) {

        Map<RkLibraryDeviceParameterName, String> parameters = new HashMap<>();

        for (Map.Entry<DeviceParametersNames, Object> entry : dto.getParameters().entrySet()) {

            switch (entry.getKey()) {
                case Switch:
                    parameters.put(RkLibraryDeviceParameterName.Switch, entry.getValue().toString());
                    break;
                case Level:
                    parameters.put(RkLibraryDeviceParameterName.Level, entry.getValue().toString());
                    break;
                case Color:
                    parameters.put(RkLibraryDeviceParameterName.Color, entry.getValue().toString());
                    break;
                default:
            }
        }

        libraryDevice.applyParameterList(parameters);
    }


    //------------------------------------------------------------------------
    //  получение краткой информации об узле в виде строки

    @Override
    public String toString() {

        return String.format(
                "{\"zWaveId\":\"%d\",\"zWaveType\":\"%s\",\"serverId\":\"%d\",\"serverType\":\"%s\"}",
                libraryDevice.getDeviceId(),
                libraryDevice.getDeviceType(),
                deviceId,
                deviceType
        );
    }

}
