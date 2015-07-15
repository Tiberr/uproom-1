package ru.uproom.gate.devices;

import libraries.api.RkLibraryDeviceType;
import ru.uproom.gate.transport.dto.DeviceType;

/**
 * Created by osipenko on 17.06.15.
 */
public class GateTypesConverter {

    public static DeviceType deviceTypeFromLibraryToServer(RkLibraryDeviceType libraryDeviceType) {

        DeviceType type = DeviceType.None;
        switch (libraryDeviceType) {

            case BinarySwitch:
                type = DeviceType.BinarySwitch;
                break;
            case BinarySensor:
                type = DeviceType.BinarySensor;
                break;
            case MultilevelSwitch:
                type = DeviceType.MultilevelSwitch;
                break;
            case MultilevelSensor:
                type = DeviceType.MultilevelSensor;
                break;
            case RGBW:
                type = DeviceType.Rgbw;
                break;
            case Meter:
                type = DeviceType.Meter;
                break;

            default:
                type = DeviceType.None;
        }

        return type;
    }

}
