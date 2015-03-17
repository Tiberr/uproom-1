package ru.uproom.gate.localinterface.zwave.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.uproom.gate.localinterface.zwave.devices.ZWaveDevice;
import ru.uproom.gate.localinterface.zwave.devices.ZWaveDeviceParameter;
import ru.uproom.gate.localinterface.zwave.driver.ZWaveMessage;
import ru.uproom.gate.localinterface.zwave.enums.ZWaveCommandClassNames;
import ru.uproom.gate.localinterface.zwave.enums.ZWaveFunctionID;
import ru.uproom.gate.localinterface.zwave.enums.ZWaveMessageTypes;

/**
 * z-wave command class
 * <p/>
 * Created by osipenko on 10.09.14.
 */
@ZWaveCommandClassesAnnotation(value = ZWaveCommandClassNames.MultiInstance)
public class ZWaveMultiInstanceCommandClass extends ZWaveCommandClassImpl {


    private static final Logger LOG =
            LoggerFactory.getLogger(ZWaveMultiInstanceCommandClass.class);

    private boolean numberOfEndPointsCanChange;
    private boolean endPointsAreSameClass;
    private int numEndPoints;
    private int numEndPointsHint;


    //-----------------------------------------------------------------------------------------------------------

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

        switch (data[0]) {

            case 0x05:
                handleMultiInstanceReport(device, data);
                break;
            case 0x06:
                handleMultiInstanceEncap(device, data);
                break;
            case 0x08:
                handleMultiChannelEndPointReport(device, data);
                break;
            case 0x0A:
                handleMultiChannelCapabilityReport(device, data);
                break;
//            case 0x0C:
//                handleMultiChannelEndPointFindReport(device, data);
//                break;
//            case 0x0D:
//                handleMultiChannelEncap(device, data);
//                break;

            default:

        }
    }


    //-----------------------------------------------------------------------------------------------------------

    @Override
    public void requestDeviceState(ZWaveDevice device, byte instance) {
        super.requestDeviceState(device, instance);
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
        byte[] params = new byte[5];
        params[0] = (byte) device.getDeviceId();
        params[1] = 0x02;
        params[2] = getId();
        params[3] = 0x02; // command GET
        params[4] = 0x00; // transmit options (?)
        message.setParameters(params);
        device.getDevicePool().getDriver().getSerialDataHandler().addMessageToSendingQueue(message);

    }


    //-----------------------------------------------------------------------------------------------------------

    @Override
    public void setDeviceParameter(ZWaveDeviceParameter parameter, String value) {
        super.setDeviceParameter(parameter, value);

        switch (parameter.getZWaveName()) {
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
        byte[] params = new byte[6];
        params[0] = (byte) parameter.getDevice().getDeviceId();
        params[1] = 0x03;
        params[2] = getId();
        params[3] = 0x01; // command SET
        params[4] = (byte) (value.equalsIgnoreCase("true") ? 0xFF : 0x00);
        params[5] = 0x00; // transmit options (?)
        message.setParameters(params);
        parameter.getDevice().getDevicePool().getDriver().getSerialDataHandler().addMessageToSendingQueue(message);

    }


    //-----------------------------------------------------------------------------------------------------------

    public void requestInstance(ZWaveDevice device, ZWaveCommandClassNames commandClassName) {

        ZWaveMessage message = new ZWaveMessage(
                ZWaveMessageTypes.Request,
                ZWaveFunctionID.SEND_DATA,
                true
        );

        // multi instance
        if (getVersion() == 1) {

            byte[] params = new byte[6];
            params[0] = (byte) device.getDeviceId();
            params[1] = 0x03;
            params[2] = getId();
            params[3] = 0x04; // command MULTI INSTANCE GET
            params[4] = commandClassName.getCode();
            params[5] = 0x00; // transmit options (?)
            message.setParameters(params);
            device.getDevicePool().getDriver().getSerialDataHandler().addMessageToSendingQueue(message);

        }

        // multi channel
        else {

            byte[] params = new byte[5];
            params[0] = (byte) device.getDeviceId();
            params[1] = 0x02;
            params[2] = getId();
            params[3] = 0x07; // command MULTI CHANNEL END POINT GET
            params[4] = 0x00; // transmit options (?)
            message.setParameters(params);
            device.getDevicePool().getDriver().getSerialDataHandler().addMessageToSendingQueue(message);

        }

    }


    //-----------------------------------------------------------------------------------------------------------

    private void handleMultiInstanceReport(ZWaveDevice device, byte[] data) {

        ZWaveCommandClassNames commandClassName = ZWaveCommandClassNames.getByCode(data[1]);
        device.applyCommandClassInstances(commandClassName, data[2]);
    }


    //-----------------------------------------------------------------------------------------------------------

    private void handleMultiInstanceEncap(ZWaveDevice device, byte[] data) {

        byte instance = data[1];
        if (getVersion() > 1)
            instance &= 0x7F;

        ZWaveCommandClassNames commandClassName = ZWaveCommandClassNames.getByCode(data[2]);

        byte[] bytes = new byte[data.length - 3];
        System.arraycopy(data, 3, bytes, 0, bytes.length);
        device.applyDeviceParametersFromByteArray(commandClassName, bytes, instance);
    }


    //-----------------------------------------------------------------------------------------------------------

    private void handleMultiChannelEndPointReport(ZWaveDevice device, byte[] data) {

        numberOfEndPointsCanChange = ((data[1] & 0x80) != 0);
        endPointsAreSameClass = ((data[1] & 0x40) != 0);
        if (numEndPointsHint != 0)
            numEndPoints = numEndPointsHint;

        int len = numEndPoints;
        if (endPointsAreSameClass)
            len = 1;

        for (int i = 1; i <= len; i++) {

            ZWaveMessage message = new ZWaveMessage(
                    ZWaveMessageTypes.Request,
                    ZWaveFunctionID.SEND_DATA,
                    true
            );
            byte[] params = new byte[6];
            params[0] = (byte) device.getDeviceId();
            params[1] = 0x03;
            params[2] = getId();
            params[3] = 0x09; // command MULTI CHANNEL CAPABILITY GET
            params[4] = (byte) i;
            params[5] = 0x00; // transmit options (?)
            message.setParameters(params);
            device.getDevicePool().getDriver().getSerialDataHandler().addMessageToSendingQueue(message);
        }
    }


    //-----------------------------------------------------------------------------------------------------------

    private void handleMultiChannelCapabilityReport(ZWaveDevice device, byte[] data) {

        byte endPoint = (byte) (data[1] & 0x7F);
        boolean dynamic = ((data[1] & 0x80) != 0);

        for (int i = 0; i < (data.length - 5); ++i) {
            byte commandClassId = data[i + 4];
            if (commandClassId == (byte) 0xEF)
                break;

        }

        // todo : stop here
    }

}

