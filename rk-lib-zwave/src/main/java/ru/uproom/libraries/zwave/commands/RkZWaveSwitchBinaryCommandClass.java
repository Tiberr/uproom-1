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
@RkZWaveCommandClassesAnnotation(value = RkZWaveCommandClassNames.SwitchBinary)
public class RkZWaveSwitchBinaryCommandClass extends RkZWaveCommandClass {


    private static final Logger LOG =
            LoggerFactory.getLogger(RkZWaveSwitchBinaryCommandClass.class);


    //-----------------------------------------------------------------------------------------------------------

    @Override
    public int createParameterList(RkZWaveDevice device, int instance) {
        int parametersNumber = 0;
        String parameterNames = "";

        RkZWaveDeviceParameterNames parameterName =
                RkZWaveDeviceParameterNames.Switch.getInstanceName(instance);
        RkZWaveDeviceParameter parameter = new RkZWaveDeviceParameter(
                device,
                this,
                parameterName,
                DeviceParametersNames.Switch
        );
        device.addParameter(parameter);
        parametersNumber++;
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

    //@Override
    public void messageHandler(RkZWaveDevice device, int[] data, int instance) {

        // command REPORT
        if (data[0] == 0x03) {
            RkZWaveDeviceParameterNames parameterName =
                    RkZWaveDeviceParameterNames.Switch.getInstanceName(instance);
            device.applyDeviceParametersFromName(
                    parameterName, (data[1] != 0 ? "true" : "false"));
        }
    }


    //-----------------------------------------------------------------------------------------------------------

    @Override
    public void requestDeviceState(RkZWaveDevice device, int instance) {
        super.requestDeviceState(device, instance);

        requestDeviceParameter(device, instance);
    }


    //-----------------------------------------------------------------------------------------------------------

    @Override
    public void requestDeviceParameter(RkZWaveDevice device, int instance) {

        RkZWaveMessage message = new RkZWaveMessage(
                RkZWaveMessageTypes.Request,
                RkZWaveFunctionID.SEND_DATA,
                false
        );
        message.applyInstance(device, this, instance);
        int[] data = new int[5];
        data[0] = device.getDeviceId();
        data[1] = 0x02;
        data[2] = getId();
        data[3] = 0x02; // command GET
        data[4] = 0x00; // transmit options (?)
        message.setParameters(data);
        device.getDevicePool().getDriver().addMessageToSendingQueue(message);

    }


    //-----------------------------------------------------------------------------------------------------------

    @Override
    public void setDeviceParameter(RkZWaveDeviceParameter parameter, String value) {

        switch (parameter.getZWaveName().getBaseName()) {
            case Switch:
                setSwitchDeviceParameter(parameter, value);
                break;
            default:
        }
    }


    //-----------------------------------------------------------------------------------------------------------

    public void setSwitchDeviceParameter(RkZWaveDeviceParameter parameter, String value) {

        if (!value.equalsIgnoreCase("true") && !value.equalsIgnoreCase("false")) return;

        RkZWaveMessage message = new RkZWaveMessage(
                RkZWaveMessageTypes.Request,
                RkZWaveFunctionID.SEND_DATA,
                false
        );
        message.applyInstance(parameter.getDevice(), this, parameter.getZWaveName().getInstance());
        int[] data = new int[6];
        data[0] = parameter.getDevice().getDeviceId();
        data[1] = 0x03;
        data[2] = getId();
        data[3] = 0x01; // command SET
        data[4] = value.equalsIgnoreCase("true") ? 0xFF : 0x00;
        data[5] = 0x00; // transmit options (?)
        message.setParameters(data);
        parameter.getDevice().getDevicePool().getDriver().addMessageToSendingQueue(message);

    }


    //-----------------------------------------------------------------------------------------------------------

    @Override
    public void applyValueBasic(RkZWaveDevice device, int instance, int value) {
        super.applyValueBasic(device, instance, value);

        // todo: apply WakeUp in this place
    }

}

