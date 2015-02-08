package ru.uproom.gate.localinterface.zwave.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.uproom.gate.localinterface.zwave.devices.ZWaveDevice;
import ru.uproom.gate.localinterface.zwave.devices.ZWaveDeviceParameter;
import ru.uproom.gate.localinterface.zwave.driver.ZWaveMessage;
import ru.uproom.gate.localinterface.zwave.enums.ZWaveCommandClassNames;
import ru.uproom.gate.localinterface.zwave.enums.ZWaveDeviceParameterNames;
import ru.uproom.gate.localinterface.zwave.enums.ZWaveFunctionID;
import ru.uproom.gate.localinterface.zwave.enums.ZWaveMessageTypes;
import ru.uproom.gate.transport.dto.parameters.DeviceParametersNames;

/**
 * z-wave command class
 * <p/>
 * Created by osipenko on 10.09.14.
 */
@ZWaveCommandClassesAnnotation(value = ZWaveCommandClassNames.Version)
public class ZWaveVersionCommandClass extends ZWaveCommandClassImpl {


    private static final Logger LOG =
            LoggerFactory.getLogger(ZWaveVersionCommandClass.class);


    //-----------------------------------------------------------------------------------------------------------

    @Override
    public int createParameterList(ZWaveDevice device) {
        int parametersNumber = 0;
        String parameterNames = "";

        ZWaveDeviceParameter parameter = new ZWaveDeviceParameter(
                device,
                this,
                ZWaveDeviceParameterNames.LibraryVersion,
                DeviceParametersNames.Unknown
        );
        device.addParameter(parameter);
        parametersNumber++;
        parameterNames += parameter.getZWaveName().name();

        parameter = new ZWaveDeviceParameter(
                device,
                this,
                ZWaveDeviceParameterNames.ProtocolVersion,
                DeviceParametersNames.Unknown
        );
        device.addParameter(parameter);
        parametersNumber++;
        parameterNames += parameter.getZWaveName().name();

        parameter = new ZWaveDeviceParameter(
                device,
                this,
                ZWaveDeviceParameterNames.ApplicationVersion,
                DeviceParametersNames.Unknown
        );
        device.addParameter(parameter);
        parametersNumber++;
        parameterNames += parameter.getZWaveName().name();

        ZWaveCommandClassNames annotation =
                (ZWaveCommandClassNames) getClass().getAnnotation(ZWaveCommandClassesAnnotation.class).value();
        LOG.debug("ADD COMMAND CLASS : {}, implement {} parameter(s) ({}) ", new Object[]{
                annotation.name(),
                parametersNumber,
                parameterNames
        });

        return parametersNumber;
    }


    //-----------------------------------------------------------------------------------------------------------

    @Override
    public void messageHandler(ZWaveDevice device, byte[] data) {
        super.messageHandler(device, data);

        // command REPORT
        if (data[0] == 0x12) {
            device.applyDeviceParametersFromName(
                    ZWaveDeviceParameterNames.LibraryVersion, String.format("%d", data[1]));
            device.applyDeviceParametersFromName(
                    ZWaveDeviceParameterNames.ProtocolVersion, String.format("%d.%.2d", data[2], data[3]));
            device.applyDeviceParametersFromName(
                    ZWaveDeviceParameterNames.ApplicationVersion, String.format("%d.%.2d", data[4], data[5]));
            return;
        }

        // command CLASS_REPORT
        if (data[0] == 0x14) {
            ZWaveCommandClassNames commandClassName = ZWaveCommandClassNames.getByCode(data[1]);
            device.applyCommandClassVersion(commandClassName, data[2]);
        }
    }


    //-----------------------------------------------------------------------------------------------------------

    @Override
    public void requestDeviceState(ZWaveDevice device) {
        super.requestDeviceState(device);
    }


    //-----------------------------------------------------------------------------------------------------------

    @Override
    public void requestDeviceParameter(ZWaveDevice device) {
        super.requestDeviceParameter(device);

        ZWaveMessage message = new ZWaveMessage(
                ZWaveMessageTypes.Request,
                ZWaveFunctionID.SEND_DATA,
                false
        );
        byte[] data = new byte[5];
        data[0] = (byte) device.getDeviceId();
        data[1] = 0x02;
        data[2] = getId();
        data[3] = 0x11; // command GET
        data[4] = 0x00; // transmit options (?)
        message.setParameters(data);
        device.getDevicePool().getDriver().getSerialDataHandler().addMessageToSendingQueue(message);

    }


    //-----------------------------------------------------------------------------------------------------------

    @Override
    public void setDeviceParameter(ZWaveDeviceParameter parameter, String value) {
        super.setDeviceParameter(parameter, value);

        // not supported
    }


    //-----------------------------------------------------------------------------------------------------------

    public void requestCommandClassVersion(ZWaveDevice device, ZWaveCommandClassNames commandClassName) {

        ZWaveMessage message = new ZWaveMessage(
                ZWaveMessageTypes.Request,
                ZWaveFunctionID.SEND_DATA,
                false
        );
        byte[] data = new byte[6];
        data[0] = (byte) device.getDeviceId();
        data[1] = 0x03;
        data[2] = getId();
        data[3] = 0x13; // command GET_VERSION
        data[4] = commandClassName.getCode();
        data[5] = 0x00; // transmit options (?)
        message.setParameters(data);
        device.getDevicePool().getDriver().getSerialDataHandler().addMessageToSendingQueue(message);

    }

}

