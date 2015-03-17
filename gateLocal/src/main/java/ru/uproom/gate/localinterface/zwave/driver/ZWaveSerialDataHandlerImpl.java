package ru.uproom.gate.localinterface.zwave.driver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.uproom.gate.localinterface.zwave.enums.ZWaveFunctionID;
import ru.uproom.gate.localinterface.zwave.functions.ZWaveFunctionHandlePool;

import javax.annotation.PostConstruct;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by osipenko on 11.01.15.
 */
@Service
public class ZWaveSerialDataHandlerImpl implements ZWaveSerialDataHandler {


    //##############################################################################################################
    //######    fields


    private static final Logger LOG = LoggerFactory.getLogger(ZWaveSerialDataHandlerImpl.class);

    private final List<byte[]> receiveMessages = new LinkedList<>();
    private final ReceiveMessagesDispatcher receiveDispatcher = new ReceiveMessagesDispatcher();
    private final List<ZWaveMessage> sendMessages = new LinkedList<>();
    private final SendMessagesDispatcher sendDispatcher = new SendMessagesDispatcher();

    @Value("${sending_message_lifetime}")
    private long sendingMessageLifetime;
    @Value("${time_before_resend}")
    private long timeBeforeResend;
    @Value("${max_number_of_resend_message}")
    private int maxNumberOfResendMessage;

    @Autowired
    private ZWaveSerialPort serialPort;
    @Autowired
    private ZWaveFunctionHandlePool functionHandler;
    @Autowired
    private ZWaveDriver driver;


    //##############################################################################################################
    //######    constructors / destructors


    @PostConstruct
    public void init() {
        new Thread(receiveDispatcher).start();
        new Thread(sendDispatcher).start();
    }


    //##############################################################################################################
    //######    methods


    //------------------------------------------------------------------------
    //  logging bytes buffer in hex view

    private void logDataInHex(String prefix, byte[] bytes) {
        String output = "";
        for (byte b : bytes) {
            output += String.format(" 0x%02X", b);
        }
        LOG.debug("{}{}", new Object[]{prefix, output});
    }


    //------------------------------------------------------------------------
    //  stop handling

    @Override
    public void stop(boolean restart) {
        serialPort.stop();
        if (!restart) {
            receiveDispatcher.stop();
            sendDispatcher.stop();
        }

    }


    //------------------------------------------------------------------------
    //  add message to queue

    @Override
    public void addMessageToSendingQueue(ZWaveMessage message) {
        synchronized (sendMessages) {
            sendMessages.add(message);
        }
        logDataInHex("MESSAGE  :", message.asByteArray());
    }


    //------------------------------------------------------------------------
    //  get data from serial port

    @Override
    public void addMessageToReceivingQueue(byte[] data) {

        synchronized (receiveMessages) {
            receiveMessages.add(data);
        }
        logDataInHex("MESSAGE  :", data);
        synchronized (receiveDispatcher) {
            receiveDispatcher.notify();
        }

    }


    //------------------------------------------------------------------------
    //  have a answer from controller on our current request

    @Override
    public void currentRequestReceived(ZWaveFunctionID functionID) {
        ZWaveMessage message;

        synchronized (sendMessages) {
            message = sendMessages.get(0);
        }
        if (message == null) return;
        if (message.getFunctionID() == functionID)
            message.setHaveAnswer(true);

    }


    @Override
    public void receiveAcknowledge() {
        ZWaveMessage message;

        synchronized (sendMessages) {
            message = sendMessages.get(0);
        }
        if (message != null) message.setAcknowledge(true);

        LOG.debug("ACKNOWLEDGE");
    }

    @Override
    public void receiveCancel() {
        LOG.debug("CANCEL");
    }

    @Override
    public void receiveNotAcknowledge() {
        ZWaveMessage message;

        synchronized (sendMessages) {
            message = sendMessages.get(0);
        }
        if (message != null) message.setResend(true);

        LOG.debug("NOT ACKNOWLEDGE");
    }

    @Override
    public void setPortState(boolean open) {
        driver.setPortState(open);
    }


    //##############################################################################################################
    //######    inner classes


    //------------------------------------------------------------------------
    //  get messages from serial port

    private class ReceiveMessagesDispatcher implements Runnable {

        private boolean stopped;

        public void stop() {
            stopped = true;
            synchronized (this) {
                notify();
            }
        }

        private synchronized void waitForNotify(long timeout) {
            try {
                if (timeout <= 0) wait();
                else wait(timeout);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {

            while (!stopped) {

                waitForNotify(0);

                byte[] function;
                ZWaveMessage request;
                while (receiveMessages.size() > 0) {
                    synchronized (receiveMessages) {
                        function = receiveMessages.remove(0);
                    }
                    synchronized (sendMessages) {
                        request = sendMessages.get(0);
                    }
                    functionHandler.execute(request, function);
                }

            }

        }

    }


    //------------------------------------------------------------------------
    //  send messages to serial port

    private class SendMessagesDispatcher implements Runnable {

        long timePoint;
        int numberOfResend;
        private boolean stopped;

        public void stop() {
            stopped = true;
        }

        private synchronized void waitForNotify(long timeout) {
            try {
                if (timeout <= 0) wait();
                else wait(timeout);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        private boolean checkSending(ZWaveMessage message) {

            if (message.isResend()) {
                message.setResend(false);
                numberOfResend++;
                return true;
            } else if (!message.isSending()) {
                message.setSending(true);
                numberOfResend = 1;
                return true;
            }

            return false;
        }

        private void removeMessage(ZWaveMessage message) {
            synchronized (sendMessages) {
                sendMessages.remove(message);
            }
            message.setAcknowledge(false);
        }

        @Override
        public void run() {

            timePoint = System.currentTimeMillis();

            while (!stopped) {

                waitForNotify(10);

                ZWaveMessage message;
                while (sendMessages.size() > 0) {

                    synchronized (sendMessages) {
                        message = sendMessages.get(0);
                    }

                    if (checkSending(message)) {
                        serialPort.sendRequest(message.asByteArray());
                        continue;
                    }

                    if (message.checkUpLifeTime(sendingMessageLifetime)) {
                        removeMessage(message);
                        continue;
                    }

                    if (!message.isAcknowledge()) {

                        if (message.checkUpResendTime(timeBeforeResend) &&
                                !message.checkUpResendNumber(maxNumberOfResendMessage)) {
                            message.setResend(true);
                        }

                    } else {

                        if (!message.isNeedWaitAnswer() || message.isHaveAnswer())
                            removeMessage(message);

                    }

                }

            }

        }

    }


}
