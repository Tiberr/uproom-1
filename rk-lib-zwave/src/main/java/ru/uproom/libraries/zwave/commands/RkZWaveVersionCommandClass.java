package ru.uproom.libraries.zwave.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.uproom.gate.transport.dto.parameters.DeviceParametersNames;
import ru.uproom.libraries.zwave.devices.RkZWaveDevice;
import ru.uproom.libraries.zwave.devices.RkZWaveDeviceParameter;
import ru.uproom.libraries.zwave.driver.RkZWaveMessage;
import ru.uproom.libraries.zwave.enums.RkZWaveCommandClassNames;
import ru.uproom.libraries.zwave.enums.RkZWaveDeviceParameterNames;
import ru.uproom.libraries.zwave.enums.RkZWaveFunctionID;
import ru.uproom.libraries.zwave.enums.RkZWaveMessageTypes;

/**
 * z-wave command class
 * <p/>
 * Created by osipenko on 10.09.14.
 */
@RkZWaveCommandClassesAnnotation(value = RkZWaveCommandClassNames.Version)
public class RkZWaveVersionCommandClass extends RkZWaveCommandClass {


    private static final Logger LOG =
            LoggerFactory.getLogger(RkZWaveVersionCommandClass.class);


    //-----------------------------------------------------------------------------------------------------------

    @Override
    public int createParameterList(RkZWaveDevice device, int instance) {
        int parametersNumber = 0;
        String parameterNames = "";

        RkZWaveDeviceParameter parameter = new RkZWaveDeviceParameter(
                device,
                this,
                RkZWaveDeviceParameterNames.LibraryVersion,
                DeviceParametersNames.Unknown
        );
        device.addParameter(parameter);
        parametersNumber++;
        if (!parameterNames.isEmpty()) parameterNames += ", ";
        parameterNames += parameter.getZWaveName().name();

        parameter = new RkZWaveDeviceParameter(
                device,
                this,
                RkZWaveDeviceParameterNames.ProtocolVersion,
                DeviceParametersNames.Unknown
        );
        device.addParameter(parameter);
        parametersNumber++;
        if (!parameterNames.isEmpty()) parameterNames += ", ";
        parameterNames += parameter.getZWaveName().name();

        parameter = new RkZWaveDeviceParameter(
                device,
                this,
                RkZWaveDeviceParameterNames.ApplicationVersion,
                DeviceParametersNames.Unknown
        );
        device.addParameter(parameter);
        parametersNumber++;
        if (!parameterNames.isEmpty()) parameterNames += ", ";
        parameterNames += parameter.getZWaveName().name();

        RkZWaveCommandClassNames annotation =
                (RkZWaveCommandClassNames) getClass().getAnnotation(RkZWaveCommandClassesAnnotation.class).value();
        LOG.debug("ADD COMMAND CLASS : {}, implement {} parameter(s) ({}) ", new Object[]{
                annotation.name(),
                parametersNumber,
                parameterNames
        });

        return parametersNumber;
    }


    //-----------------------------------------------------------------------------------------------------------

    @Override
    public void messageHandler(RkZWaveDevice device, int[] data, int instance) {

        // command REPORT
        if (data[0] == 0x12) {
            device.applyDeviceParametersFromName(
                    RkZWaveDeviceParameterNames.LibraryVersion, String.format("%d", data[1]));
            device.applyDeviceParametersFromName(
                    RkZWaveDeviceParameterNames.ProtocolVersion, String.format("%d.%.2d", data[2], data[3]));
            device.applyDeviceParametersFromName(
                    RkZWaveDeviceParameterNames.ApplicationVersion, String.format("%d.%.2d", data[4], data[5]));
            return;
        }

        // command CLASS_REPORT
        if (data[0] == 0x14) {
            RkZWaveCommandClassNames commandClassName = RkZWaveCommandClassNames.getByCode(data[1]);
            device.applyCommandClassVersion(commandClassName, data[2]);
        }
    }


    //-----------------------------------------------------------------------------------------------------------

    @Override
    public void requestDeviceState(RkZWaveDevice device, int instance) {
        super.requestDeviceState(device, instance);
    }


    //-----------------------------------------------------------------------------------------------------------

    @Override
    public void requestDeviceParameter(RkZWaveDevice device, int instance) {

        RkZWaveMessage message = new RkZWaveMessage(
                RkZWaveMessageTypes.Request,
                RkZWaveFunctionID.SEND_DATA,
                false
        );
        int[] data = new int[5];
        data[0] = device.getDeviceId();
        data[1] = 0x02;
        data[2] = getId();
        data[3] = 0x11; // command GET
        data[4] = 0x00; // transmit options (?)
        message.setParameters(data);
        device.getDevicePool().getDriver().addMessageToSendingQueue(message);

    }


    //-----------------------------------------------------------------------------------------------------------

    @Override
    public void setDeviceParameter(RkZWaveDeviceParameter parameter, String value) {

        // not supported
    }


    //-----------------------------------------------------------------------------------------------------------

    public void requestCommandClassVersion(RkZWaveDevice device, RkZWaveCommandClassNames commandClassName) {

        RkZWaveMessage message = new RkZWaveMessage(
                RkZWaveMessageTypes.Request,
                RkZWaveFunctionID.SEND_DATA,
                true
        );
        int[] data = new int[6];
        data[0] = device.getDeviceId();
        data[1] = 0x03;
        data[2] = getId();
        data[3] = 0x13; // command GET_VERSION
        data[4] = commandClassName.getCode();
        data[5] = 0x00; // transmit options (?)
        message.setParameters(data);
        device.getDevicePool().getDriver().addMessageToSendingQueue(message);

    }

}

