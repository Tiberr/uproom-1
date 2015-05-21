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
@RkZWaveCommandClassesAnnotation(value = RkZWaveCommandClassNames.Basic)
public class RkZWaveBasicCommandClass extends RkZWaveCommandClass {


    private static final Logger LOG =
            LoggerFactory.getLogger(RkZWaveBasicCommandClass.class);

    private boolean setAsReport;
    private boolean ignoreMapping;
    private RkZWaveCommandClassNames mapping = RkZWaveCommandClassNames.NoOperation;


    //-----------------------------------------------------------------------------------------------------------

    @Override
    public int createParameterList(RkZWaveDevice device, int instance) {

        if (mapping == RkZWaveCommandClassNames.NoOperation) return 0;

        int parametersNumber = 0;
        String parameterNames = "";

        RkZWaveDeviceParameterNames parameterName =
                RkZWaveDeviceParameterNames.Basic.getInstanceName(instance);
        RkZWaveDeviceParameter parameter = new RkZWaveDeviceParameter(
                device,
                this,
                parameterName,
                DeviceParametersNames.Unknown
        );
        device.addParameter(parameter);
        parametersNumber++;
        parameterNames += parameter.getZWaveName().name();

        RkZWaveCommandClassNames annotation =
                getClass().getAnnotation(RkZWaveCommandClassesAnnotation.class).value();
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
            messageHandlerSetBasic(device, data, instance);
            return;
        }

        // command SET
        if (data[0] == 0x01) {
            if (setAsReport) {
                messageHandlerSetBasic(device, data, instance);
            } else {
                // Command received from the node.  Handle as a notification event
                // notification->SetEvent( _data[1] );
            }
        }
    }

    public void messageHandlerSetBasic(RkZWaveDevice device, int[] data, int instance) {
        if (!ignoreMapping && mapping != RkZWaveCommandClassNames.NoOperation) {
            updateMappedClass(device, instance, mapping, data[1]);
        } else {
            RkZWaveDeviceParameterNames parameterName =
                    RkZWaveDeviceParameterNames.Basic.getInstanceName(instance);
            device.applyDeviceParametersFromName(parameterName, String.valueOf(data[1]));
        }
    }


    //-----------------------------------------------------------------------------------------------------------

    @Override
    public void requestDeviceState(RkZWaveDevice device, int instance) {
        requestDeviceParameter(device, instance);
    }


    //-----------------------------------------------------------------------------------------------------------

    @Override
    public void requestDeviceParameter(RkZWaveDevice device, int instance) {

        RkZWaveMessage message = new RkZWaveMessage(
                RkZWaveMessageTypes.Request,
                RkZWaveFunctionID.SEND_DATA,
                null, false
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
            case Basic:
                setBasicDeviceParameter(parameter, value);
                break;
            default:
        }
    }


    //-----------------------------------------------------------------------------------------------------------

    private void setBasicDeviceParameter(RkZWaveDeviceParameter parameter, String value) {

        RkZWaveMessage message = new RkZWaveMessage(
                RkZWaveMessageTypes.Request,
                RkZWaveFunctionID.SEND_DATA,
                null, false
        );
        message.applyInstance(parameter.getDevice(), this, parameter.getZWaveName().getInstance());
        int[] data = new int[6];
        data[0] = (byte) parameter.getDevice().getDeviceId();
        data[1] = 0x03;
        data[2] = getId();
        data[3] = 0x01; // command SET
        data[4] = Byte.parseByte(parameter.getValue());
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


    //-----------------------------------------------------------------------------------------------------------

    public RkZWaveCommandClassNames getMapping() {
        return mapping;
    }

    public void setMapping(RkZWaveDevice device, RkZWaveCommandClassNames commandClass) {
        if (commandClass != RkZWaveCommandClassNames.NoOperation) {
            mapping = commandClass;
            device.removeParameter(RkZWaveDeviceParameterNames.Basic);
        }
    }

}

