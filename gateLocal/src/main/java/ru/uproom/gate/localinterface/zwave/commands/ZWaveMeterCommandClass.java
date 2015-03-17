package ru.uproom.gate.localinterface.zwave.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.uproom.gate.localinterface.domain.ExtractingValue;
import ru.uproom.gate.localinterface.zwave.devices.ZWaveDevice;
import ru.uproom.gate.localinterface.zwave.devices.ZWaveDeviceParameter;
import ru.uproom.gate.localinterface.zwave.driver.ZWaveMessage;
import ru.uproom.gate.localinterface.zwave.enums.*;
import ru.uproom.gate.transport.dto.parameters.DeviceParametersNames;

/**
 * z-wave command class
 * <p/>
 * Created by osipenko on 10.09.14.
 */
@ZWaveCommandClassesAnnotation(value = ZWaveCommandClassNames.Meter)
public class ZWaveMeterCommandClass extends ZWaveCommandClassImpl {


    private static final Logger LOG =
            LoggerFactory.getLogger(ZWaveMeterCommandClass.class);


    @Override
    public int createParameterList(ZWaveDevice device, byte instance) {

        int parametersNumber = 0;
        String parameterNames = "";

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

        if (!isHaveVersion()) return;

        // command SUPPORTED_REPORT
        if (data[0] == 0x04)
            messageSupportedReportHandler(device, data);

            // command REPORT
        else if (data[0] == 0x02)
            messageReportHandler(device, data);
    }


    //-----------------------------------------------------------------------------------------------------------

    @Override
    public void requestDeviceState(ZWaveDevice device, byte instance) {
        super.requestDeviceState(device, instance);

        if (getVersion() > 1) {
            ZWaveMessage message = new ZWaveMessage(
                    ZWaveMessageTypes.Request,
                    ZWaveFunctionID.SEND_DATA,
                    true
            );
            byte[] data = new byte[5];
            data[0] = (byte) device.getDeviceId();
            data[1] = 0x02;
            data[2] = getId();
            data[3] = 0x03; // command SUPPORTED GET
            data[4] = 0x00; // transmit options (?)
            message.setParameters(data);
            device.getDevicePool().getDriver().getSerialDataHandler().addMessageToSendingQueue(message);
        }
    }


    //-----------------------------------------------------------------------------------------------------------

    @Override
    public void requestDeviceParameter(ZWaveDevice device, byte instance) {
        super.requestDeviceParameter(device, instance);

        for (int i = 0; i < 8; ++i) {

            byte index = (byte) (i << 2);
            // todo : request only if value exist

            ZWaveMessage message = new ZWaveMessage(
                    ZWaveMessageTypes.Request,
                    ZWaveFunctionID.SEND_DATA,
                    false
            );
            byte[] data = new byte[6];
            data[0] = (byte) device.getDeviceId();
            data[1] = 0x03;
            data[2] = getId();
            data[3] = 0x01; // command GET
            data[4] = (byte) (i << 3);
            data[5] = 0x00; // transmit options (?)
            message.setParameters(data);
            device.getDevicePool().getDriver().getSerialDataHandler().addMessageToSendingQueue(message);
        }


    }


    //-----------------------------------------------------------------------------------------------------------

    @Override
    public void setDeviceParameter(ZWaveDeviceParameter parameter, String value) {
        super.setDeviceParameter(parameter, value);

        switch (parameter.getZWaveName()) {
            case Reset:
                setResetDeviceParameter(parameter, value);
                break;
            default:
        }
    }


    private void setResetDeviceParameter(ZWaveDeviceParameter parameter, String value) {
        if (!value.equalsIgnoreCase("true")) return;

        ZWaveMessage message = new ZWaveMessage(
                ZWaveMessageTypes.Request,
                ZWaveFunctionID.SEND_DATA,
                false
        );
        byte[] data = new byte[6];
        message.applyInstance(parameter);
        data[0] = (byte) parameter.getDevice().getDeviceId();
        data[1] = 0x02;
        data[2] = getId();
        data[3] = 0x05; // command RESET
        data[5] = 0x00; // transmit options (?)
        message.setParameters(data);

        parameter.getDevice().getDevicePool().getDriver().getSerialDataHandler().addMessageToSendingQueue(message);
    }


    //-----------------------------------------------------------------------------------------------------------

    private void messageSupportedReportHandler(ZWaveDevice device, byte[] data) {
        int parametersNumber = 0;
        String parameterNames = "";

        ZWaveMeterType meterType = ZWaveMeterType.getByCode((byte) (data[1] & 0x1f));

        byte scaleSupported = data[2];
        if (getVersion() == 0x02) scaleSupported &= 0x0F;

        ZWaveDeviceParameter parameter = null;
        for (int i = 0; i < 8; ++i) {
            if (((int) scaleSupported & (0x01 << i)) != 0x00) {
                byte index = (byte) i;

                ZWaveDeviceParameterNames parameterName = ZWaveDeviceParameterNames.
                        byParameterProperties(getName(), 1, meterType.getCode(), index);
                parameter = device.getDeviceParameterByName(parameterName);

                if (parameter != null) {
                    parameter.setUnits(ZWaveMeterUnits.getByIndex(meterType, index));

                } else {
                    parameter = new ZWaveDeviceParameter(
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

        parameter = new ZWaveDeviceParameter(
                device,
                this,
                ZWaveDeviceParameterNames.Exporting,
                DeviceParametersNames.Unknown
        );
        device.addParameter(parameter);
        parametersNumber++;
        if (!parameterNames.isEmpty()) parameterNames += ", ";
        parameterNames += parameter.getZWaveName().name();

        if ((data[1] & 0x80) != 0) {
            parameter = new ZWaveDeviceParameter(
                    device,
                    this,
                    ZWaveDeviceParameterNames.Reset,
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
    }


    //-----------------------------------------------------------------------------------------------------------

    private void messageReportHandler(ZWaveDevice device, byte[] data) {

        // todo: stop in this place

        boolean exporting;
        if (getVersion() > 0x01) {
            exporting = ((data[1] & 0x60) == 0x40);
            ZWaveDeviceParameter parameter =
                    device.getDeviceParameterByName(ZWaveDeviceParameterNames.Exporting);
            if (parameter != null) {
                parameter.setValue(new Boolean(exporting).toString());
            }
        }

        byte[] subData = new byte[data.length - 2];
        System.arraycopy(data, 2, subData, 0, subData.length);
        ExtractingValue extractingValue = extractValueFromBytes(subData);

        if (getVersion() == 1) {
            messageReportHandlerVer1(device, data, extractingValue);
        } else {
            messageReportHandlerVer2(device, data, extractingValue);
        }

    }

    private void messageReportHandlerVer1(ZWaveDevice device, byte[] data, ExtractingValue extractingValue) {

        ZWaveMeterType meterType = ZWaveMeterType.getByCode((byte) (data[1] & 0x1f));
        byte index = extractingValue.getScale();

        ZWaveDeviceParameterNames parameterName =
                ZWaveDeviceParameterNames.byParameterProperties
                        (getName(), 1, meterType.getCode(), index);
        ZWaveDeviceParameter parameter = device.getDeviceParameterByName(parameterName);

        if (parameter != null) {
            parameter.setUnits
                    (ZWaveMeterUnits.getByIndex(meterType, index));
            parameter.setPrecision(extractingValue.getPrecision());
            parameter.setValue(extractingValue.getValue());
        }
    }

    private void messageReportHandlerVer2(ZWaveDevice device, byte[] data, ExtractingValue extractingValue) {

        ZWaveMeterType meterType = ZWaveMeterType.getByCode((byte) (data[1] & 0x1f));

        byte index = extractingValue.getScale();
        if (getVersion() > 2)
            index |= ((data[1] & 0x80) >> 5);

        ZWaveDeviceParameterNames parameterName =
                ZWaveDeviceParameterNames.byParameterProperties
                        (getName(), 1, meterType.getCode(), index);
        ZWaveDeviceParameter parameter = device.getDeviceParameterByName(parameterName);

        if (parameter != null) {

            parameter.setPrecision(extractingValue.getPrecision());
            parameter.setValue(extractingValue.getValue());

            byte size = (byte) (data[2] & 0x07);
            int delta = (data[3 + size] << 8) | data[4 + size];
            if (delta != 0) {
                readPreviousValue(parameter, data, (byte) (size + 3));
                parameter.setInterval(delta);
            }

        }

    }

    private void readPreviousValue(ZWaveDeviceParameter parameter, byte[] data, byte offset) {

        byte[] subData = new byte[data.length - 2];
        System.arraycopy(data, 2, subData, 0, subData.length);
        ExtractingValue extractingValue = extractValueFromBytes(subData, offset);
        parameter.setPrevValue(extractingValue.getValue());
        parameter.setPrevPrecision(extractingValue.getPrecision());

    }

}
