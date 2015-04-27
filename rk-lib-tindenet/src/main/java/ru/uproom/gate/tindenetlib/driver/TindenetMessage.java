package ru.uproom.gate.tindenetlib.driver;

import ru.uproom.gate.tindenetlib.commands.hub.TindenetHubCommandID;

/**
 * Created by osipenko on 16.03.15.
 */
public class TindenetMessage {

    private TindenetHubCommandID commandID;
    private int moduleId;
    private String messageBody;

    private long sendTimePoint;
    private int sendNumber;
    private boolean answering;

    public TindenetMessage(TindenetHubCommandID commandID) {
        this.commandID = commandID;
    }

    public void send() {
        sendTimePoint = System.currentTimeMillis();
        sendNumber = 1;
    }

    public void resend() {
        sendTimePoint = System.currentTimeMillis();
        sendNumber++;
    }

    public boolean checkSendTimeout(long timeout) {
        return (System.currentTimeMillis() - sendTimePoint) > timeout;
    }

    public boolean checkSendNumber(int number) {
        return sendNumber >= number;
    }

    public boolean isSending() {
        return sendNumber > 0;
    }

    public boolean isAnswering() {
        return answering;
    }

    public void setAnswering(boolean answering) {
        this.answering = answering;
    }

    public byte[] asByteArray() {
        String result = String.format("%c%d%c%s%c%c", TindenetFrameMarker.STOF, commandID.getCode(),
                TindenetFrameMarker.DLTR, messageBody, TindenetFrameMarker.EOF1, TindenetFrameMarker.EOF2);
        return result.getBytes();
    }

}
