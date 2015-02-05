package ru.uproom.gate.localinterface.zwave.driver;

import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.uproom.gate.localinterface.zwave.enums.ZWaveFrameMarker;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * Created by osipenko on 10.01.15.
 */

@Service
public class ZWaveSerialPortImpl implements ZWaveSerialPort {


    //##############################################################################################################
    //######    fields


    private static final Logger LOG = LoggerFactory.getLogger(ZWaveSerialPortImpl.class);
    @Autowired
    ZWaveSerialDataHandler handler;
    @Value("${serial_port}")
    private String serial;
    @Value("${period_between_restart_port}")
    private long periodBetweenRestarts;
    private SerialPort serialPort;
    private boolean serialPortConnected;
    private boolean serialPortOpen;
    private CheckSerialPort checker = new CheckSerialPort();

    private boolean messageCreating;
    private int lastPos = 0;
    private byte[] message;


    //##############################################################################################################
    //######    constructors / destructors


    @PostConstruct
    public void init() {

        serialPortConnected = true;
        new Thread(checker).start();

    }


    @Override
    @PreDestroy
    public void stop() {

        checker.stopCheck();
        try {
            if (serialPort != null) {
                serialPort.removeEventListener();
                serialPort.closePort();
            }

            LOG.debug("Serial port {} closed successfully", new Object[]{
                    serial
            });
        } catch (SerialPortException e) {
            LOG.error("Serial port ({}) can not close, reason : {}", new Object[]{
                    serial,
                    e.getMessage()
            });
        }

    }


    //##############################################################################################################
    //######    methods


    //------------------------------------------------------------------------
    //  open serial port after all initialization

    @Override
    public void open() {

        serialPort = new SerialPort(serial);
        try {

            serialPort.openPort();
            serialPort.setParams(
                    SerialPort.BAUDRATE_115200,
                    SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE
            );
            serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
            serialPort.addEventListener(new PortReader(), SerialPort.MASK_RXCHAR);

            LOG.info("Serial port {} opened successfully", new Object[]{
                    serial
            });

            sendDataToPort(new byte[]{ZWaveFrameMarker.CAN.getCode()});
            applySerialPortOpen(true);
            handler.setPortState(true);

        } catch (SerialPortException e) {
            serialPortConnected = false;
            LOG.error("Serial port ({}) can not open, reason : {}", new Object[]{
                    serial,
                    e.getMessage()
            });
        }

    }


    //------------------------------------------------------------------------
    //  send command to serial port

    @Override
    public void sendRequest(byte[] request) {

        byte[] data = new byte[request.length + 3];
        data[0] = ZWaveFrameMarker.SOF.getCode();
        data[1] = (byte) (request.length + 1);
        System.arraycopy(request, 0, data, 2, request.length);
        data[data.length - 1] = createCheckSum(request);
        sendDataToPort(data);
    }

    private void sendDataToPort(byte[] data) {

        try {
            logDataInHex("REQUEST :", data);
            serialPort.writeBytes(data);
        } catch (SerialPortException e) {
            serialPortConnected = false;
            LOG.error("Serial port ({}) can not write, reason : {}", new Object[]{
                    serial,
                    e.getMessage()
            });
        }

    }


    //------------------------------------------------------------------------
    //  logging data exchange

    private void logDataInHex(String prefix, byte[] bytes) {
        String output = "";
        for (byte b : bytes) {
            output += String.format(" 0x%02X", b);
        }
        LOG.debug("{}{}", new Object[]{prefix, output});
    }


    //------------------------------------------------------------------------
    //  create message checksum

    public byte createCheckSum(byte[] frame) {

        byte checksum = (byte) 0xFF;
        for (byte b : frame)
            checksum ^= b;
        checksum ^= ((byte) frame.length + 1);
        logDataInHex("CHECKSUM :", new byte[]{checksum});
        return checksum;

    }


    //------------------------------------------------------------------------
    //  check message checksum

    public boolean verifyCheckSum(byte checksum) {

        return (createCheckSum(message) == checksum);
    }


    //------------------------------------------------------------------------
    //  create message from raw data

    private int putDataInMessage(byte[] bytes, int next) {

        if (message == null) {
            message = new byte[(int) bytes[next] - 1];
            next++;
        }

        int size = bytes.length - next;
        if (size >= (message.length + 1 - lastPos)) {
            size = message.length - lastPos;
        }
        System.arraycopy(bytes, next, message, lastPos, size);

        lastPos += size;
        int checkSumPos = next + size;
        if (lastPos >= message.length && checkSumPos < bytes.length) {

            if (verifyCheckSum(bytes[checkSumPos])) {
                handler.addMessageToReceivingQueue(message);
                sendDataToPort(new byte[]{ZWaveFrameMarker.ACK.getCode()});
            } else {
                sendDataToPort(new byte[]{ZWaveFrameMarker.NAC.getCode()});
            }

            message = null;
            lastPos = 0;
            messageCreating = false;
        }

        return (checkSumPos + 1);
    }


    //------------------------------------------------------------------------
    //  create message from raw data
    //  frame: [SOF|length|type|function|data|checksum]
    //  length = from type to checksum (including); checksum = from length to data (including)

    private synchronized void sortingDataForMessage(byte[] bytes) {

        int next = 0;
        while (next < bytes.length) {

            if (messageCreating) {

                next = putDataInMessage(bytes, next);

            } else {

                switch (ZWaveFrameMarker.getByCode(bytes[next])) {

                    // begin data frame from controller
                    case SOF:
                        messageCreating = true;
                        next++;
                        break;

                    // node acknowledge our command
                    case ACK:
                        handler.receiveAcknowledge();
                        next++;
                        break;

                    // node mot acknowledge our command
                    case NAC:
                        handler.receiveNotAcknowledge();
                        next++;
                        break;

                    // node request for cancel
                    case CAN:
                        handler.receiveCancel();
                        next++;
                        break;

                    // unknown bytes without frame
                    default:
                        sendDataToPort(new byte[]{ZWaveFrameMarker.NAC.getCode()});
                        next = bytes.length;

                }

            }

        }

    }


    private void applySerialPortOpen(boolean open) {
        if (open)
            try {
                serialPort.purgePort(SerialPort.PURGE_RXCLEAR | SerialPort.PURGE_TXCLEAR);
            } catch (SerialPortException e) {
                LOG.error("Serial port ({}) can not purge, reason : {}", new Object[]{
                        serial,
                        e.getMessage()
                });
            }
        serialPortOpen = open;
    }


    //##############################################################################################################
    //######    inner classes


    //------------------------------------------------------------------------
    //  reading data from serial port

    public class PortReader implements SerialPortEventListener {

        @Override
        public void serialEvent(SerialPortEvent event) {

            if (!serialPortOpen) return;

            if (event.isRXCHAR() && event.getEventValue() > 0) {
                byte[] bytes = null;
                try {
                    bytes = serialPort.readBytes();
                    logDataInHex("ANSWER  :", bytes);
                    sortingDataForMessage(bytes);
                } catch (SerialPortException e) {
                    LOG.error("Serial port ({}) can not read, reason : {}", new Object[]{
                            serial,
                            e.getMessage()
                    });
                }
            }

        }

    }


    //------------------------------------------------------------------------
    //  checking connection with serial port

    public class CheckSerialPort implements Runnable {

        private boolean stopped;

        public void stopCheck() {
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

                waitForNotify(10);

                if (!serialPortConnected) {
                    stop();
                    waitForNotify(periodBetweenRestarts);
                    if (stopped) break;
                    init();
                }

            }

        }

    }


}
