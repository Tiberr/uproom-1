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
@ZWaveCommandClassesAnnotation(value = ZWaveCommandClassNames.SwitchBinary)
public class ZWaveSwitchBinaryCommandClass extends ZWaveCommandClassImpl {


    private static final Logger LOG =
            LoggerFactory.getLogger(ZWaveSwitchBinaryCommandClass.class);


    //-----------------------------------------------------------------------------------------------------------

    @Override
    public int createParameterList(ZWaveDevice device, byte instance) {
        int parametersNumber = 0;
        String parameterNames = "";

        ZWaveDeviceParameterNames parameterName =
                ZWaveDeviceParameterNames.Switch.getInstanceName(instance);
        ZWaveDeviceParameter parameter = new ZWaveDeviceParameter(
                device,
                this,
                parameterName,
                DeviceParametersNames.Switch
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
    public void messageHandler(ZWaveDevice device, byte[] data, byte instance) {
        super.messageHandler(device, data, instance);

        // command REPORT
        if (data[0] == 0x03) {
            ZWaveDeviceParameterNames parameterName =
                    ZWaveDeviceParameterNames.Switch.getInstanceName(instance);
            device.applyDeviceParametersFromName(
                    parameterName, (data[1] != 0 ? "true" : "false"));
        }
    }


    //-----------------------------------------------------------------------------------------------------------

    @Override
    public void requestDeviceState(ZWaveDevice device, byte instance) {
        super.requestDeviceState(device, instance);

        requestDeviceParameter(device, instance);
    }


    //-----------------------------------------------------------------------------------------------------------

    @Override
    public void requestDeviceParameter(ZWaveDevice device, byte instance) {
        super.requestDeviceParameter(device, instance);

        ZWaveMessage message = new ZWaveMessage(
                ZWaveMessageTypes.Request,
                ZWaveFunctionID.SEND_DATA,
                false
        );
        message.applyInstance(device, this, instance);
        byte[] data = new byte[5];
        data[0] = (byte) device.getDeviceId();
        data[1] = 0x02;
        data[2] = getId();
        data[3] = 0x02; // command GET
        data[4] = 0x00; // transmit options (?)
        message.setParameters(data);
        device.getDevicePool().getDriver().getSerialDataHandler().addMessageToSendingQueue(message);

    }


    //-----------------------------------------------------------------------------------------------------------

    @Override
    public void setDeviceParameter(ZWaveDeviceParameter parameter, String value) {
        super.setDeviceParameter(parameter, value);

        switch (parameter.getZWaveName().getBaseName()) {
            case Switch:
                setSwitchDeviceParameter(parameter, value);
                break;
            default:
        }
    }


    //-----------------------------------------------------------------------------------------------------------

    public void setSwitchDeviceParameter(ZWaveDeviceParameter parameter, String value) {

        if (!value.equalsIgnoreCase("true") && !value.equalsIgnoreCase("false")) return;

        ZWaveMessage message = new ZWaveMessage(
                ZWaveMessageTypes.Request,
                ZWaveFunctionID.SEND_DATA,
                false
        );
        message.applyInstance(parameter.getDevice(), this, parameter.getZWaveName().getInstance());
        byte[] data = new byte[6];
        data[0] = (byte) parameter.getDevice().getDeviceId();
        data[1] = 0x03;
        data[2] = getId();
        data[3] = 0x01; // command SET
        data[4] = (byte) (value.equalsIgnoreCase("true") ? 0xFF : 0x00);
        data[5] = 0x00; // transmit options (?)
        message.setParameters(data);
        parameter.getDevice().getDevicePool().getDriver().getSerialDataHandler().addMessageToSendingQueue(message);

    }


    //-----------------------------------------------------------------------------------------------------------

    @Override
    public void applyValueBasic(ZWaveDevice device, byte instance, byte value) {
        super.applyValueBasic(device, instance, value);

        // todo: apply WakeUp in this place
    }

}

