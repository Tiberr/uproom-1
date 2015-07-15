package ru.uproom.libraries.zwave.commands;

import libraries.auxilliary.ExtractingValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.uproom.gate.transport.dto.parameters.DeviceParametersNames;
import ru.uproom.libraries.zwave.devices.RkZWaveDeviceParameter;
import ru.uproom.libraries.zwave.driver.RkZWaveMessage;
import ru.uproom.libraries.zwave.enums.*;

/**
 * z-wave command class
 * <p/>
 * Created by osipenko on 10.09.14.
 */
@RkZWaveCommandClassesAnnotation(value = RkZWaveCommandClassNames.Meter)
public class RkZWaveMeterCommandClass extends RkZWaveCommandClass {


    private static final Logger LOG =
            LoggerFactory.getLogger(RkZWaveMeterCommandClass.class);


    //-----------------------------------------------------------------------------------------------------------

    @Override
    public int createParameterList(int instance) {

        int parametersNumber = 0;
        String parameterNames = "";

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
    public void messageHandler(int[] data, int instance) {

        if (!isHaveVersion()) return;

        // command SUPPORTED_REPORT
        if (data[0] == 0x04) {
            messageSupportedReportHandler(data, instance);
            instances.setBit(instance);
        }

        // command REPORT
        else if (data[0] == 0x02)
            messageReportHandler(data, instance);
    }


    //-----------------------------------------------------------------------------------------------------------

    @Override
    public void requestDeviceState(int instance) {
        super.requestDeviceState(instance);

        if (getVersion() > 1) {
            RkZWaveMessage message = new RkZWaveMessage(
                    RkZWaveMessageTypes.Request,
                    RkZWaveFunctionID.SEND_DATA,
                    device, true
            );
            message.applyInstance(this, instance);
            int[] data = new int[5];
            data[0] = device.getDeviceId();
            data[1] = 0x02;
            data[2] = getId();
            data[3] = 0x03; // command SUPPORTED GET
            data[4] = 0x00; // transmit options (?)
            message.setParameters(data);
            device.getDevicePool().getDriver().addMessageToSendingQueue(message);
        }
    }


    //-----------------------------------------------------------------------------------------------------------

    @Override
    public void requestDeviceParameter(int instance) {

        for (int i = 0; i < 8; ++i) {

            int index = i << 2;
            // todo : request only if value exist

            RkZWaveMessage message = new RkZWaveMessage(
                    RkZWaveMessageTypes.Request,
                    RkZWaveFunctionID.SEND_DATA,
                    device, false
            );
            message.applyInstance(this, instance);
            int[] data = new int[6];
            data[0] = device.getDeviceId();
            data[1] = 0x03;
            data[2] = getId();
            data[3] = 0x01; // command GET
            data[4] = i << 3;
            data[5] = 0x00; // transmit options (?)
            message.setParameters(data);
            device.getDevicePool().getDriver().addMessageToSendingQueue(message);
        }


    }


    //-----------------------------------------------------------------------------------------------------------

    @Override
    public void setDeviceParameter(RkZWaveDeviceParameter parameter, String value) {

        switch (parameter.getZWaveName()) {
            case Reset:
                setResetDeviceParameter(parameter, value);
                break;
            default:
        }
    }


    private void setResetDeviceParameter(RkZWaveDeviceParameter parameter, String value) {
        if (!value.equalsIgnoreCase("true")) return;

        RkZWaveMessage message = new RkZWaveMessage(
                RkZWaveMessageTypes.Request,
                RkZWaveFunctionID.SEND_DATA,
                device, false
        );
        int[] data = new int[6];
        message.applyInstance(parameter);
        data[0] = parameter.getDevice().getDeviceId();
        data[1] = 0x02;
        data[2] = getId();
        data[3] = 0x05; // command RESET
        data[5] = 0x00; // transmit options (?)
        message.setParameters(data);

        parameter.getDevice().getDevicePool().getDriver().addMessageToSendingQueue(message);
    }


    //-----------------------------------------------------------------------------------------------------------

    private void messageSupportedReportHandler(int[] data, int instance) {
        int parametersNumber = 0;
        String parameterNames = "";

        RkZWaveMeterType meterType = RkZWaveMeterType.getByCode((byte) (data[1] & 0x1f));

        int scaleSupported = data[2];
        if (getVersion() == 0x02) scaleSupported &= 0x0F;

        RkZWaveDeviceParameter parameter = null;
        for (int i = 0; i < 8; ++i) {
            if ((scaleSupported & (0x01 << i)) != 0x00) {

                RkZWaveDeviceParameterNames parameterName = RkZWaveDeviceParameterNames.
                        byParameterProperties(getName(), instance, meterType.getCode(), i);
                parameter = device.getDeviceParameterByName(parameterName);

                if (parameter != null) {
                    parameter.setUnits(RkZWaveMeterUnits.getByIndex(meterType, i));

                } else {
                    parameter = new RkZWaveDeviceParameter(
                            device,
                            this,
                            parameterName,
                            DeviceParametersNames.Unknown
                    );
                    device.addParameter(parameter);
                    parametersNumber++;
                    if (!parameterNames.isEmpty()) parameterNames += ", ";
                    parameterNames += parameter.getZWaveName().name();
                }

            }
        }

        parameter = new RkZWaveDeviceParameter(
                device,
                this,
                RkZWaveDeviceParameterNames.Exporting,
                DeviceParametersNames.Unknown
        );
        device.addParameter(parameter);
        parametersNumber++;
        if (!parameterNames.isEmpty()) parameterNames += ", ";
        parameterNames += parameter.getZWaveName().name();

        if ((data[1] & 0x80) != 0) {
            parameter = new RkZWaveDeviceParameter(
                    device,
                    this,
                    RkZWaveDeviceParameterNames.Reset,
                    DeviceParametersNames.Unknown
            );
            device.addParameter(parameter);
            parametersNumber++;
            if (!parameterNames.isEmpty()) parameterNames += ", ";
            parameterNames += parameter.getZWaveName().name();
        }

        LOG.debug("SUPPORT COMMAND CLASS : {}, implement {} parameter(s) ({}) ", new Object[]{
                getName(),
                parametersNumber,
                parameterNames
        });

        currentInstanceForRequest = instance + 1;
    }


    //-----------------------------------------------------------------------------------------------------------

    private void messageReportHandler(int[] data, int instance) {

        // todo: destroy in this place

        boolean exporting;
        if (getVersion() > 0x01) {
            exporting = ((data[1] & 0x60) == 0x40);
            RkZWaveDeviceParameter parameter =
                    device.getDeviceParameterByName(
                            RkZWaveDeviceParameterNames.Exporting);
            if (parameter != null) {
                parameter.setValue(Boolean.toString(exporting));
            }
        }

        int[] subData = new int[data.length - 2];
        System.arraycopy(data, 2, subData, 0, subData.length);
        ExtractingValue extractingValue = extractValueFromInts(subData);

        if (getVersion() == 1) {
            messageReportHandlerVer1(data, instance, extractingValue);
        } else {
            messageReportHandlerVer2(data, instance, extractingValue);
        }

    }

    private void messageReportHandlerVer1(int[] data, int instance, ExtractingValue extractingValue) {

        RkZWaveMeterType meterType = RkZWaveMeterType.getByCode(data[1] & 0x1f);
        int index = extractingValue.getScale();

        RkZWaveDeviceParameterNames parameterName =
                RkZWaveDeviceParameterNames.byParameterProperties
                        (getName(), instance, meterType.getCode(), index);
        RkZWaveDeviceParameter parameter = device.getDeviceParameterByName(parameterName);

        if (parameter != null) {
            parameter.setUnits
                    (RkZWaveMeterUnits.getByIndex(meterType, index));
            parameter.setPrecision(extractingValue.getPrecision());
            parameter.setValue(extractingValue.getValue());
        }
    }

    private void messageReportHandlerVer2(int[] data, int instance, ExtractingValue extractingValue) {

        RkZWaveMeterType meterType = RkZWaveMeterType.getByCode(data[1] & 0x1f);

        int index = extractingValue.getScale();
        if (getVersion() > 2)
            index |= ((data[1] & 0x80) >> 5);

        RkZWaveDeviceParameterNames parameterName =
                RkZWaveDeviceParameterNames.byParameterProperties
                        (getName(), instance, meterType.getCode(), index);
        RkZWaveDeviceParameter parameter = device.getDeviceParameterByName(parameterName);

        if (parameter != null) {

            parameter.setPrecision(extractingValue.getPrecision());
            parameter.setValue(extractingValue.getValue());

            byte size = (byte) (data[2] & 0x07);
            int delta = (data[3 + size] << 8) | data[4 + size];
            if (delta != 0) {
                readPreviousValue(parameter, data, size + 3);
                parameter.setInterval(delta);
            }

        }

    }

    private void readPreviousValue(RkZWaveDeviceParameter parameter, int[] data, int offset) {

        int[] subData = new int[data.length - 2];
        System.arraycopy(data, 2, subData, 0, subData.length);
        ExtractingValue extractingValue = extractValueFromInts(subData, offset);
        parameter.setPrevValue(extractingValue.getValue());
        parameter.setPrevPrecision(extractingValue.getPrecision());

    }

}
