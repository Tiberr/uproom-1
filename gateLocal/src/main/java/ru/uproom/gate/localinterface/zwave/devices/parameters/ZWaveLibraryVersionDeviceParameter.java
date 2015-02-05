package ru.uproom.gate.localinterface.zwave.devices.parameters;

import ru.uproom.gate.localinterface.zwave.commands.ZWaveCommandClass;
import ru.uproom.gate.localinterface.zwave.devices.ZWaveDevice;
import ru.uproom.gate.localinterface.zwave.devices.ZWaveDeviceParameter;
import ru.uproom.gate.localinterface.zwave.driver.ZWaveMessage;
import ru.uproom.gate.localinterface.zwave.enums.ZWaveDeviceParameterNames;
import ru.uproom.gate.localinterface.zwave.enums.ZWaveFunctionID;
import ru.uproom.gate.localinterface.zwave.enums.ZWaveMessageTypes;
import ru.uproom.gate.transport.dto.parameters.DeviceParametersNames;

/**
 * Created by osipenko on 15.01.15.
 */
public class ZWaveLibraryVersionDeviceParameter extends ZWaveDeviceParameter {


    //##############################################################################################################
    //######    fields


    //##############################################################################################################
    //######    constructors / destructors


    public ZWaveLibraryVersionDeviceParameter(
            ZWaveDevice device,
            ZWaveCommandClass commandClass,
            DeviceParametersNames serverName
    ) {
        super(device, commandClass, ZWaveDeviceParameterNames.LibraryVersion, serverName);
    }


    //##############################################################################################################
    //######    getters / setters


    //##############################################################################################################
    //######    methods


    @Override
    public String fetchValue() {
        return null;
    }

    @Override
    public void applyValue(String value) {
        if (!value.equalsIgnoreCase("true") && !value.equalsIgnoreCase("false")) return;

        ZWaveMessage message = new ZWaveMessage(
                ZWaveMessageTypes.Request,
                ZWaveFunctionID.SEND_DATA,
                false
        );
        byte[] data = new byte[6];
        data[0] = (byte) getDevice().getDeviceId();
        data[1] = 0x03;
        data[2] = getCommandClass().getId();
        data[3] = 0x01; // command SET
        data[4] = (byte) (value.equalsIgnoreCase("true") ? 0xFF : 0x00);
        data[5] = 0x00; // transmit options (?)
        message.setParameters(data);
        getDevice().getDevicePool().getDriver().getSerialDataHandler().addMessageToSendingQueue(message);
    }

}
