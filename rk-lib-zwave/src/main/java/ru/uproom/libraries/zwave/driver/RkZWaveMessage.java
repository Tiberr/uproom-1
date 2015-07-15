package ru.uproom.libraries.zwave.driver;

import ru.uproom.libraries.zwave.commands.RkZWaveCommandClass;
import ru.uproom.libraries.zwave.devices.RkZWaveDevice;
import ru.uproom.libraries.zwave.devices.RkZWaveDeviceParameter;
import ru.uproom.libraries.zwave.enums.RkZWaveCommandClassNames;
import ru.uproom.libraries.zwave.enums.RkZWaveFunctionID;
import ru.uproom.libraries.zwave.enums.RkZWaveMessageTypes;

/**
 * Created by osipenko on 20.01.15.
 */
public class RkZWaveMessage {


    //##############################################################################################################
    //######    fields


    private RkZWaveMessageTypes type;
    private RkZWaveFunctionID functionID;
    private RkZWaveDevice device;

    private boolean multiInstance;
    private boolean multiChannel;
    private int instance;
    private int instanceEndPoint;

    private int[] parameters;

    private boolean sending;
    private boolean resend;
    private boolean acknowledge;
    private long timeStampSend;
    private long timeStampLastResend;
    private int resendNumber;

    private boolean needWaitAnswer;
    private boolean haveAnswer;

    private boolean mustSendAgain;


    //##############################################################################################################
    //######    constructors / destructors


    public RkZWaveMessage(
            RkZWaveMessageTypes type, RkZWaveFunctionID functionID, RkZWaveDevice device, boolean needWaitAnswer) {
        this.type = type;
        this.functionID = functionID;
        this.device = device;
        this.needWaitAnswer = needWaitAnswer;
    }


    //##############################################################################################################
    //######    getters / setters


    public RkZWaveFunctionID getFunctionID() {
        return functionID;
    }


    //------------------------------------------------------------------------

    public RkZWaveDevice getDevice() {
        return device;
    }


    //------------------------------------------------------------------------

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


    //------------------------------------------------------------------------

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


    //------------------------------------------------------------------------

    public boolean isAcknowledge() {
        return acknowledge;
    }

    public void setAcknowledge(boolean acknowledge) {
        this.acknowledge = acknowledge;
    }


    //------------------------------------------------------------------------

    public void setParameters(int[] parameters) {
        this.parameters = parameters;
    }


    //------------------------------------------------------------------------

    public boolean isNeedWaitAnswer() {
        return needWaitAnswer;
    }

    public void setNeedWaitAnswer(boolean needWaitAnswer) {
        this.needWaitAnswer = needWaitAnswer;
    }


    //------------------------------------------------------------------------

    public boolean isHaveAnswer() {
        return haveAnswer;
    }

    public void setHaveAnswer(boolean haveAnswer) {
        this.haveAnswer = haveAnswer;
    }


    //------------------------------------------------------------------------

    public boolean isMustSendAgain() {
        return mustSendAgain;
    }

    public void setMustSendAgain(boolean mustSendAgain) {
        this.mustSendAgain = mustSendAgain;
    }


    //##############################################################################################################
    //######    methods


    public int[] asIntArray() {

        int length = 2;
        multiInstanceEncapsulation();

        if (parameters != null) length += parameters.length;
        int[] bytes = new int[length];

        bytes[0] = type.getCode();
        bytes[1] = functionID.getCode();
        int lastPosition = 2;

        if (parameters != null && parameters.length > 0) {
            System.arraycopy(parameters, 0, bytes, lastPosition, parameters.length);
            lastPosition += parameters.length;
        }

        return bytes;
    }


    //------------------------------------------------------------------------

    public void applyInstance(RkZWaveDeviceParameter parameter) {
        applyInstance(parameter.getCommandClass(), parameter.getZWaveName().getInstance());
    }

    public void applyInstance(RkZWaveCommandClass commandClass, int instance) {
        this.instance = instance;

        RkZWaveCommandClass miCc = device.getCommandClassByName(RkZWaveCommandClassNames.MultiInstance);
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


    //------------------------------------------------------------------------

    public void multiInstanceEncapsulation() {
        if (functionID != RkZWaveFunctionID.SEND_DATA) return;

        if (multiChannel) {

            int[] bytes = new int[parameters.length + 4];
            bytes[0] = parameters[0];
            bytes[1] = parameters[1] + 4;
            bytes[2] = RkZWaveCommandClassNames.MultiInstance.getCode();
            bytes[3] = 0x0D; // MultiChannel command ENCAPSULATION
            bytes[4] = 1;
            bytes[5] = instanceEndPoint;
            System.arraycopy(parameters, 2, bytes, 6, parameters.length - 2);
            parameters = bytes;

        } else if (multiInstance) {

            int[] bytes = new int[parameters.length + 3];
            bytes[0] = parameters[0];
            bytes[1] = parameters[1] + 3;
            bytes[2] = RkZWaveCommandClassNames.MultiInstance.getCode();
            bytes[3] = 0x06; // MultiInstance command ENCAPSULATION
            bytes[4] = instance;
            System.arraycopy(parameters, 2, bytes, 5, parameters.length - 2);
            parameters = bytes;

        }
    }


    //------------------------------------------------------------------------

    public boolean checkUpLifeTime(long timeout) {
        return (System.currentTimeMillis() - timeStampSend) > timeout;
    }


    //------------------------------------------------------------------------

    public boolean checkUpResendTime(long timeout) {
        return (System.currentTimeMillis() - timeStampLastResend) > timeout;
    }


    //------------------------------------------------------------------------

    public boolean checkUpResendNumber(long number) {
        return resendNumber >= number;
    }


    //------------------------------------------------------------------------

    public void reload() {

        setSending(false);
        setResend(false);
        setAcknowledge(false);
        resendNumber = 0;

    }

}
