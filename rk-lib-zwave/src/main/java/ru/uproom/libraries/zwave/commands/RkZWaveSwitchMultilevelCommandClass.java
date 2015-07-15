package ru.uproom.libraries.zwave.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.uproom.gate.transport.dto.parameters.DeviceParametersNames;
import ru.uproom.libraries.zwave.devices.RkZWaveDevice;
import ru.uproom.libraries.zwave.devices.RkZWaveDeviceParameter;
import ru.uproom.libraries.zwave.driver.RkZWaveMessage;
import ru.uproom.libraries.zwave.enums.*;

/**
 * z-wave command class
 * <p/>
 * Created by osipenko on 10.09.14.
 */
@RkZWaveCommandClassesAnnotation(value = RkZWaveCommandClassNames.SwitchMultilevel)
public class RkZWaveSwitchMultilevelCommandClass extends RkZWaveCommandClass {


    private static final Logger LOG =
            LoggerFactory.getLogger(RkZWaveSwitchMultilevelCommandClass.class);

    String parameterNames = "";


    //-----------------------------------------------------------------------------------------------------------

    @Override
    public int createParameterList(int instance) {
        int parametersNumber = 0;
        parameterNames = "";

        switch (getVersion()) {

            case 3:
                parametersNumber += createParameterListVer3(device, instance);

            case 2:
                parametersNumber += createParameterListVer2(device, instance);

            case 1:
                parametersNumber += createParameterListVer1(device, instance);
                break;

            default:
        }

        RkZWaveCommandClassNames annotation =
                (RkZWaveCommandClassNames) getClass().getAnnotation(RkZWaveCommandClassesAnnotation.class).value();
        LOG.debug("ADD COMMAND CLASS : {}, implement {} parameter(s) ({}) ", new Object[]{
                annotation.name(),
                parametersNumber,
                parameterNames
        });

        return parametersNumber;
    }


    private int createParameterListVer1(RkZWaveDevice device, int instance) {
        RkZWaveDeviceParameterNames parameterName;
        RkZWaveDeviceParameter parameter;
        int parametersNumber = 0;

        parameterName = RkZWaveDeviceParameterNames.Level.getInstanceName(instance);
        parameter = new RkZWaveDeviceParameter(
                device,
                this,
                parameterName,
                DeviceParametersNames.Level
        );
        device.addParameter(parameter);
        parametersNumber++;
        parameterNames += parameter.getZWaveName().name();

        parameterName = RkZWaveDeviceParameterNames.Bright.getInstanceName(instance);
        parameter = new RkZWaveDeviceParameter(
                device,
                this,
                parameterName,
                DeviceParametersNames.Unknown
        );
        device.addParameter(parameter);
        parametersNumber++;
        parameterNames += parameter.getZWaveName().name();

        parameterName = RkZWaveDeviceParameterNames.Dim.getInstanceName(instance);
        parameter = new RkZWaveDeviceParameter(
                device,
                this,
                parameterName,
                DeviceParametersNames.Unknown
        );
        device.addParameter(parameter);
        parametersNumber++;
        parameterNames += parameter.getZWaveName().name();

        parameterName = RkZWaveDeviceParameterNames.IgnoreStartLevel.getInstanceName(instance);
        parameter = new RkZWaveDeviceParameter(
                device,
                this,
                parameterName,
                DeviceParametersNames.Unknown
        );
        device.addParameter(parameter);
        parametersNumber++;
        parameterNames += parameter.getZWaveName().name();

        parameterName = RkZWaveDeviceParameterNames.StartLevel.getInstanceName(instance);
        parameter = new RkZWaveDeviceParameter(
                device,
                this,
                parameterName,
                DeviceParametersNames.Level
        );
        device.addParameter(parameter);
        parametersNumber++;
        parameterNames += parameter.getZWaveName().name();

        return parametersNumber;
    }


    private int createParameterListVer2(RkZWaveDevice device, int instance) {
        RkZWaveDeviceParameterNames parameterName;
        RkZWaveDeviceParameter parameter;
        int parametersNumber = 0;

        parameterName = RkZWaveDeviceParameterNames.DimmingDuration.getInstanceName(instance);
        parameter = new RkZWaveDeviceParameter(
                device,
                this,
                parameterName,
                DeviceParametersNames.Unknown
        );
        device.addParameter(parameter);
        parametersNumber++;
        parameterNames += parameter.getZWaveName().name();

        return parametersNumber;
    }


    private int createParameterListVer3(RkZWaveDevice device, int instance) {
        RkZWaveDeviceParameterNames parameterName;
        RkZWaveDeviceParameter parameter;
        int parametersNumber = 0;

        parameterName = RkZWaveDeviceParameterNames.StepSize.getInstanceName(instance);
        parameter = new RkZWaveDeviceParameter(
                device,
                this,
                parameterName,
                DeviceParametersNames.Unknown
        );
        device.addParameter(parameter);
        parametersNumber++;
        parameterNames += parameter.getZWaveName().name();

        parameterName = RkZWaveDeviceParameterNames.Inc.getInstanceName(instance);
        parameter = new RkZWaveDeviceParameter(
                device,
                this,
                parameterName,
                DeviceParametersNames.Unknown
        );
        device.addParameter(parameter);
        parametersNumber++;
        parameterNames += parameter.getZWaveName().name();

        parameterName = RkZWaveDeviceParameterNames.Dec.getInstanceName(instance);
        parameter = new RkZWaveDeviceParameter(
                device,
                this,
                parameterName,
                DeviceParametersNames.Unknown
        );
        device.addParameter(parameter);
        parametersNumber++;
        parameterNames += parameter.getZWaveName().name();

        return parametersNumber;
    }


    //-----------------------------------------------------------------------------------------------------------

    //@Override
    public void messageHandler(int[] data, int instance) {

        // command REPORT
        if (data[0] == 0x03) {
            RkZWaveDeviceParameterNames parameterName =
                    RkZWaveDeviceParameterNames.Level.getInstanceName(instance);
            device.applyDeviceParametersFromName(
                    parameterName, String.valueOf(data[1]));
        }

        // command SUPPORTED REPORT
        else if (data[0] == 0x07) {

            int switchType1 = data[1] & 0x1f;
            int switchType2 = data[2] & 0x1f;

            //ClearStaticRequest( StaticRequest_Version );
            // Set the labels on the values (maybe we need it in future)
        }

        instances.setBit(instance);
    }


    //-----------------------------------------------------------------------------------------------------------

    @Override
    public void requestDeviceState(int instance) {
        super.requestDeviceState(instance);

        if (getVersion() == 3) {
            RkZWaveMessage message = new RkZWaveMessage(
                    RkZWaveMessageTypes.Request,
                    RkZWaveFunctionID.SEND_DATA,
                    device, false
            );
            int[] data = new int[5];
            data[0] = device.getDeviceId();
            data[1] = 0x02;
            data[2] = getId();
            data[3] = 0x06; // command SUPPORTED GET
            data[4] = 0x00; // transmit options (?)
            message.setParameters(data);
            device.getDevicePool().getDriver().addMessageToSendingQueue(message);

        } else
            requestDeviceParameter(instance);
    }


    //-----------------------------------------------------------------------------------------------------------

    @Override
    public void requestDeviceParameter(int instance) {

        RkZWaveMessage message = new RkZWaveMessage(
                RkZWaveMessageTypes.Request,
                RkZWaveFunctionID.SEND_DATA,
                device, false
        );
        message.applyInstance(this, instance);
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

            case Level:
                setLevelDeviceParameter(parameter, value);
                break;

            case Bright:
                setBrightDeviceParameter(parameter, value);
                break;

            case Dim:
                setDimDeviceParameter(parameter, value);
                break;

            case IgnoreStartLevel:

            case StartLevel:

            case DimmingDuration:

            case StepSize:
                parameter.setValue(value);
                break;

            case Inc:
                setIncDeviceParameter(parameter, value);
                break;

            case Dec:
                setDecDeviceParameter(parameter, value);
                break;

            default:
        }

    }


    //-----------------------------------------------------------------------------------------------------------

    public void setLevelDeviceParameter(RkZWaveDeviceParameter parameter, String value) {

        RkZWaveDeviceParameter duration = parameter.getDevice().getDeviceParameterByName(
                RkZWaveDeviceParameterNames.DimmingDuration.getInstanceName(parameter.getZWaveName().getInstance())
        );

        RkZWaveMessage message = new RkZWaveMessage(
                RkZWaveMessageTypes.Request,
                RkZWaveFunctionID.SEND_DATA,
                device, false
        );
        message.applyInstance(this, parameter.getZWaveName().getInstance());
        int[] data = new int[(duration != null) ? 7 : 6];
        data[0] = parameter.getDevice().getDeviceId();
        if (duration != null) {
            data[1] = 0x04;
            data[2] = getId();
            data[3] = 0x01; // command SET
            data[5] = Integer.parseInt(duration.getValue());
            data[6] = 0x00; // transmit options (?)
        } else {
            data[1] = 0x03;
            data[2] = getId();
            data[3] = 0x01; // command SET
            data[4] = Integer.parseInt(value);
            data[5] = 0x00; // transmit options (?)
        }
        message.setParameters(data);
        parameter.getDevice().getDevicePool().getDriver().addMessageToSendingQueue(message);

    }

    //-----------------------------------------------------------------------------------------------------------

    public void setBrightDeviceParameter(RkZWaveDeviceParameter parameter, String value) {
        if (parameter.getValue().equalsIgnoreCase("true"))
            startLevelChange(parameter, RkZWaveLevelDirection.UP);
        else
            stopLevelChange(parameter);
    }

    //-----------------------------------------------------------------------------------------------------------

    public void setDimDeviceParameter(RkZWaveDeviceParameter parameter, String value) {
        if (parameter.getValue().equalsIgnoreCase("true"))
            startLevelChange(parameter, RkZWaveLevelDirection.DOWN);
        else
            stopLevelChange(parameter);
    }

    //-----------------------------------------------------------------------------------------------------------

    public void setIncDeviceParameter(RkZWaveDeviceParameter parameter, String value) {
        if (parameter.getValue().equalsIgnoreCase("true"))
            startLevelChange(parameter, RkZWaveLevelDirection.INC);
        else
            stopLevelChange(parameter);
    }

    //-----------------------------------------------------------------------------------------------------------

    public void setDecDeviceParameter(RkZWaveDeviceParameter parameter, String value) {
        if (parameter.getValue().equalsIgnoreCase("true"))
            startLevelChange(parameter, RkZWaveLevelDirection.DEC);
        else
            stopLevelChange(parameter);
    }

    //-----------------------------------------------------------------------------------------------------------

    private void startLevelChange(RkZWaveDeviceParameter parameter, RkZWaveLevelDirection direction) {

        LOG.debug("start level change to ({}) for device ({})",
                new Object[]{direction.name(), device.getDeviceId()});

        int length = 4;
        int directionCode = direction.getCode();

        RkZWaveDeviceParameter ignoreStartLevel = device.getDeviceParameterByName(
                RkZWaveDeviceParameterNames.IgnoreStartLevel.getInstanceName(parameter.getZWaveName().getInstance())
        );
        if (ignoreStartLevel != null) {
            if (ignoreStartLevel.getValue().equalsIgnoreCase("true"))
                directionCode |= 0x20;
        }

        int startLevel = 0;
        RkZWaveDeviceParameter startLevelParameter = device.getDeviceParameterByName(
                RkZWaveDeviceParameterNames.StartLevel.getInstanceName(parameter.getZWaveName().getInstance())
        );
        if (startLevelParameter != null)
            startLevel = Integer.parseInt(startLevelParameter.getValue());

        int duration = 0;
        RkZWaveDeviceParameter durationParameter = device.getDeviceParameterByName(
                RkZWaveDeviceParameterNames.DimmingDuration.getInstanceName(parameter.getZWaveName().getInstance())
        );
        if (durationParameter != null) {
            length = 5;
            duration = Integer.parseInt(durationParameter.getValue());
        }

        int step = 0;
        if (direction == RkZWaveLevelDirection.INC || direction == RkZWaveLevelDirection.DEC) {
            RkZWaveDeviceParameter stepSize = device.getDeviceParameterByName(
                    RkZWaveDeviceParameterNames.StepSize.getInstanceName(parameter.getZWaveName().getInstance())
            );
            if (stepSize != null) {
                length = 6;
                step = Integer.parseInt(stepSize.getValue());
            }
        }

        RkZWaveMessage message = new RkZWaveMessage(
                RkZWaveMessageTypes.Request,
                RkZWaveFunctionID.SEND_DATA,
                device, false
        );
        message.applyInstance(this, parameter.getZWaveName().getInstance());
        int[] data = new int[2 + length];
        data[0] = device.getDeviceId();
        data[1] = length;
        data[2] = getId();
        data[3] = 0x04; // command START LEVEL CHANGE
        data[4] = direction.getCode();
        data[5] = startLevel;
        if (length >= 5) data[6] = duration;
        if (length >= 6) data[7] = startLevel;
        data[2 + length] = 0x00; // transmit options (?)
        message.setParameters(data);
        device.getDevicePool().getDriver().addMessageToSendingQueue(message);

    }

    //-----------------------------------------------------------------------------------------------------------

    private void stopLevelChange(RkZWaveDeviceParameter parameter) {

        RkZWaveMessage message = new RkZWaveMessage(
                RkZWaveMessageTypes.Request,
                RkZWaveFunctionID.SEND_DATA,
                device, false
        );
        message.applyInstance(this, parameter.getZWaveName().getInstance());
        int[] data = new int[5];
        data[0] = device.getDeviceId();
        data[1] = 2;
        data[2] = getId();
        data[3] = 0x05; // command STOP LEVEL CHANGE
        data[4] = 0x00; // transmit options (?)
        message.setParameters(data);
        device.getDevicePool().getDriver().addMessageToSendingQueue(message);
    }


    //-----------------------------------------------------------------------------------------------------------

    @Override
    public void applyValueBasic(int instance, int value) {
        super.applyValueBasic(instance, value);

        // todo: apply WakeUp in this place
    }

}

