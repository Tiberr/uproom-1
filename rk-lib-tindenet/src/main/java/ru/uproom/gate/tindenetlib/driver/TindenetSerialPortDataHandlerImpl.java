package ru.uproom.gate.tindenetlib.driver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.uproom.gate.tindenetlib.commands.hub.TindenetHubCommandHandlersFactory;
import ru.uproom.gate.tindenetlib.commands.hub.TindenetHubCommandID;
import ru.uproom.libraries.auxilliary.RunnableClass;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.List;

/**
 * dispatcher for data getting from serial port and data sending to one
 * <p/>
 * Created by osipenko on 16.03.15.
 */

@Service
public class TindenetSerialPortDataHandlerImpl implements TindenetSerialPortDataHandler {


    //##############################################################################################################
    //######    fields


    private static final Logger LOG = LoggerFactory.getLogger(TindenetSerialPortImpl.class);

    private final List<TindenetMessage> messageQueueToSerialPort = new LinkedList<>();
    private final QueueToSerialPortHandling queueToSerialPortHandling = new QueueToSerialPortHandling();

    private final List<String> messageQueueFromSerialPort = new LinkedList<>();
    private final QueueFromSerialPortHandling queueFromSerialPortHandling = new QueueFromSerialPortHandling();

    private long lastPingTimePoint;
    private CheckPingTime checkPingTime = new CheckPingTime();
    private Thread threadCheckPingTime;

    @Value("${sending_message_lifetime}")
    private long sendingMessageLifetime;
    @Value("${max_number_of_resend_message}")
    private int maxNumberOfResendMessage;

    @Autowired
    private TindenetHubCommandHandlersFactory commandHandlersFactory;
    @Autowired
    private TindenetSerialPort serialPort;


    //##############################################################################################################
    //######    constructors / destructors

    @PostConstruct
    public void init() {
        queueToSerialPortHandling.setTimeout(10);
        new Thread(queueToSerialPortHandling).start();
        queueFromSerialPortHandling.setTimeout(0);
        new Thread(queueFromSerialPortHandling).start();
    }

    @PreDestroy
    public void stop() {
        checkPingTime.stop();
        queueFromSerialPortHandling.stop();
        queueToSerialPortHandling.stop();
    }


    //##############################################################################################################
    //######    methods


    @Override
    public void putMessageToHandlingQueueFromSerialPort(byte[] message, int length) {
        String command = null;

        try {
            command = new String(message, 0, length, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            LOG.error("can not create string from byte array : {}", e.getMessage());
        }
        if (command == null || command.isEmpty()) return;

        synchronized (messageQueueFromSerialPort) {
            messageQueueFromSerialPort.add(command);
        }

        synchronized (queueFromSerialPortHandling) {
            queueFromSerialPortHandling.notify();
        }

    }

    @Override
    public void putMessageToHandlingQueueToSerialPort(TindenetMessage message) {
        synchronized (messageQueueToSerialPort) {
            messageQueueToSerialPort.add(message);
        }

        synchronized (queueToSerialPortHandling) {
            queueToSerialPortHandling.notify();
        }
    }

    @Override
    public void handlePing() {

        lastPingTimePoint = System.currentTimeMillis();

        if (threadCheckPingTime == null || threadCheckPingTime.isInterrupted()) {
            checkPingTime.setTimeout(5000);
            threadCheckPingTime = new Thread(checkPingTime);
            threadCheckPingTime.start();
        }

    }


    //##############################################################################################################
    //######    inner classes


    //---------------------------------------------------------------------------------

    private class CheckPingTime extends RunnableClass {

        @Override
        public void body() {

            long time = System.currentTimeMillis();
            if (time - lastPingTimePoint > 10000) {
                serialPort.restart();
                stop();
                return;
            }

            putMessageToHandlingQueueToSerialPort(new TindenetMessage(TindenetHubCommandID.Ping));

        }

    }


    //---------------------------------------------------------------------------------

    private class QueueFromSerialPortHandling extends RunnableClass {

        @Override
        public void body() {

            String message;
            while (messageQueueFromSerialPort.size() > 0) {
                synchronized (messageQueueFromSerialPort) {
                    try {
                        message = messageQueueFromSerialPort.remove(0);
                    } catch (IndexOutOfBoundsException e) {
                        continue;
                    }
                }
                commandHandlersFactory.handleCommand(message);
            }

        }

    }


    //---------------------------------------------------------------------------------

    private class QueueToSerialPortHandling extends RunnableClass {

        @Override
        public void body() {

            TindenetMessage message;
            while (messageQueueFromSerialPort.size() > 0) {
                synchronized (messageQueueToSerialPort) {
                    try {
                        message = messageQueueToSerialPort.get(0);
                    } catch (IndexOutOfBoundsException e) {
                        continue;
                    }
                }

                if (message.isSending()) {

                    if (message.isAnswering()) {
                        synchronized (messageQueueToSerialPort) {
                            messageQueueToSerialPort.remove(message);
                        }
                        continue;
                    }

                    if (message.checkSendTimeout(sendingMessageLifetime)) {
                        if (message.checkSendNumber(maxNumberOfResendMessage)) {
                            synchronized (messageQueueToSerialPort) {
                                messageQueueToSerialPort.remove(message);
                            }
                        } else {
                            serialPort.sendRequest(message.asByteArray());
                            message.resend();
                        }
                    }

                } else {
                    serialPort.sendRequest(message.asByteArray());
                    message.send();
                }
            }

        }

    }

}
