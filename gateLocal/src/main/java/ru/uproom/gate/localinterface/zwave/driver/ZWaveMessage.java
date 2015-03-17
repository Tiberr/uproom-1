package ru.uproom.gate.localinterface.zwave.driver;

import ru.uproom.gate.localinterface.zwave.commands.ZWaveCommandClass;
import ru.uproom.gate.localinterface.zwave.devices.ZWaveDevice;
import ru.uproom.gate.localinterface.zwave.devices.ZWaveDeviceParameter;
import ru.uproom.gate.localinterface.zwave.enums.ZWaveCommandClassNames;
import ru.uproom.gate.localinterface.zwave.enums.ZWaveFunctionID;
import ru.uproom.gate.localinterface.zwave.enums.ZWaveMessageTypes;

/**
 * Created by osipenko on 20.01.15.
 */
public class ZWaveMessage {


    //##############################################################################################################
    //######    fields


    private ZWaveMessageTypes type;
    private ZWaveFunctionID functionID;

    private boolean multiInstance;
    private boolean multiChannel;
    private byte instance;
    private byte instanceEndPoint;

    private byte[] parameters;

    private boolean sending;
    private boolean resend;
    private boolean acknowledge;
    private long timeStampSend;
    private long timeStampLastResend;
    private int resendNumber;

    private boolean needWaitAnswer;
    private boolean haveAnswer;


    //##############################################################################################################
    //######    constructors / destructors


    public ZWaveMessage(ZWaveMessageTypes type, ZWaveFunctionID functionID, boolean needWaitAnswer) {
        this.type = type;
        this.functionID = functionID;
        this.needWaitAnswer = needWaitAnswer;
    }


    //##############################################################################################################
    //######    getters / setters


    public ZWaveFunctionID getFunctionID() {
        return functionID;
    }

    public boolean isSending() {
        return sending;
    }

    public void setSending(boolean sending) {
        if (!this.sending && sending) {
            timeStampSend = System.currentTimeMillis();
            timeStampLastResend = timeStampSend;
            resendNumber++;
        }
        this.sending = sending;
    }

    public boolean isResend() {
        return resend;
    }

    public void setResend(boolean resend) {
        if (resend) {
            timeStampLastResend = System.currentTimeMillis();
            resendNumber++;
        }
        this.resend = resend;
    }

    public boolean isAcknowledge() {
        return acknowledge;
    }

    public void setAcknowledge(boolean acknowledge) {
        this.acknowledge = acknowledge;
    }

    public void setParameters(byte[] parameters) {
        this.parameters = parameters;
    }

    public boolean isNeedWaitAnswer() {
        return needWaitAnswer;
    }

    public void setNeedWaitAnswer(boolean needWaitAnswer) {
        this.needWaitAnswer = needWaitAnswer;
    }

    public boolean isHaveAnswer() {
        return haveAnswer;
    }

    public void setHaveAnswer(boolean haveAnswer) {
        this.haveAnswer = haveAnswer;
    }


    //##############################################################################################################
    //######    methods


    public byte[] asByteArray() {

        int length = 2;
        multiInstanceEncapsulation();

        if (parameters != null) length += parameters.length;
        byte[] bytes = new byte[length];

        bytes[0] = type.getCode();
        bytes[1] = functionID.getCode();
        int lastPosition = 2;

        if (parameters != null && parameters.length > 0) {
            System.arraycopy(parameters, 0, bytes, lastPosition, parameters.length);
            lastPosition += parameters.length;
        }

        return bytes;
    }


    public void applyInstance(ZWaveDeviceParameter parameter) {
        applyInstance(parameter.getDevice(), parameter.getCommandClass(), parameter.getZWaveName().getInstance());
    }

    public void applyInstance(ZWaveDevice device, ZWaveCommandClass commandClass, byte instance) {
        this.instance = instance;

        ZWaveCommandClass miCc = device.getCommandClassByName(ZWaveCommandClassNames.MultiInstance);
        if (miCc != null) {
            if (miCc.getVersion() > 1) {
                instanceEndPoint = commandClass.getInstanceEndPoint(instance);
                if (instanceEndPoint != 0)
                    multiInstance = true;
            } else if (instance > 1) {
                multiChannel = true;
            }
        }
    }


    public void multiInstanceEncapsulation() {
        if (functionID != ZWaveFunctionID.SEND_DATA) return;

        if (multiChannel) {

            byte[] bytes = new byte[parameters.length + 4];
            bytes[0] = parameters[0];
            bytes[1] = (byte) (parameters[1] + 4);
            bytes[2] = ZWaveCommandClassNames.MultiInstance.getCode();
            bytes[3] = 0x0D; // MultiChannel command ENCAPSULATION
            bytes[4] = 1;
            bytes[5] = instanceEndPoint;
            System.arraycopy(parameters, 2, bytes, 6, parameters.length - 2);
            parameters = bytes;

        } else if (multiInstance) {

            byte[] bytes = new byte[parameters.length + 3];
            bytes[0] = parameters[0];
            bytes[1] = (byte) (parameters[1] + 3);
            bytes[2] = ZWaveCommandClassNames.MultiInstance.getCode();
            bytes[3] = 0x06; // MultiInstance command ENCAPSULATION
            bytes[4] = instance;
            System.arraycopy(parameters, 2, bytes, 5, parameters.length - 2);
            parameters = bytes;

        }
    }


    public boolean checkUpLifeTime(long timeout) {
        return (System.currentTimeMillis() - timeStampSend) > timeout;
    }


    public boolean checkUpResendTime(long timeout) {
        return (System.currentTimeMillis() - timeStampLastResend) > timeout;
    }

    public boolean checkUpResendNumber(long number) {
        return resendNumber >= number;
    }

}
