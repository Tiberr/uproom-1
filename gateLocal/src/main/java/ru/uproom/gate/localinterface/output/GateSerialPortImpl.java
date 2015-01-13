package ru.uproom.gate.localinterface.output;

import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.uproom.gate.localinterface.domain.GateLocalConstants;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * Created by osipenko on 10.01.15.
 */

@Service
public class GateSerialPortImpl implements GateSerialPort {


    //##############################################################################################################
    //######    fields


    private static final Logger LOG = LoggerFactory.getLogger(GateSerialPortImpl.class);

    @Value("${serial_port}")
    private String serial;

    @Autowired
    private GateSerialHandler handler;

    private SerialPort serialPort;

    private boolean messageCreating;
    private int lastPos = 1;
    private byte[] message;


    //##############################################################################################################
    //######    constructors / destructors


    @PostConstruct
    public void init() {

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
        } catch (SerialPortException e) {
            LOG.error("Serial port ({}) can not open, reason : {}", new Object[]{
                    serial,
                    e.getMessage()
            });
        }

        LOG.debug("Serial port {} opened successfully", new Object[]{
                serial
        });
    }


    @Override
    @PreDestroy
    public void stop() {

        try {
            if (serialPort != null) serialPort.closePort();
        } catch (SerialPortException e) {
            LOG.error("Serial port ({}) can not close, reason : {}", new Object[]{
                    serial,
                    e.getMessage()
            });
        }

        LOG.debug("Serial port {} closed successfully", new Object[]{
                serial
        });
    }


    //##############################################################################################################
    //######    methods


    //------------------------------------------------------------------------
    //  send command to serial port

    @Override
    public void sendRequest(byte[] request) {

        try {
            logDataInHex("REQUEST :", request);
            serialPort.writeBytes(request);
        } catch (SerialPortException e) {
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
        logDataInHex("CHECKSUM :", new byte[]{checksum});
        return checksum;

    }


    //------------------------------------------------------------------------
    //  check message checksum

    public boolean verifyCheckSum() {
        byte[] frame = new byte[message.length - 1];
        System.arraycopy(message, 0, frame, 0, frame.length);
        return (createCheckSum(frame) == message[message.length - 1]);
    }


    //------------------------------------------------------------------------
    //  create message from raw data

    private int putDataInMessage(byte[] bytes, int next) {

        if (message == null) {
            message = new byte[bytes[next] + 1];
            message[0] = bytes[next];
            next++;
        }

        int size = bytes.length - next;
        if (size >= (message.length - lastPos)) {
            size = message.length - lastPos;
        }
        System.arraycopy(bytes, next, message, lastPos, size);

        lastPos += size;
        if (lastPos >= message.length) {

            if (verifyCheckSum()) {
                byte[] frame = new byte[message.length - 2];
                System.arraycopy(message, 1, frame, 0, frame.length);
                handler.receiveMessage(frame);
                sendRequest(new byte[]{GateLocalConstants.ACK});
            } else {
                sendRequest(new byte[]{GateLocalConstants.NAC});
            }

            message = null;
            lastPos = 1;
            messageCreating = false;
        }

        return (next + size);
    }


    //------------------------------------------------------------------------
    //  create message from raw data

    private synchronized void sortingDataForMessage(byte[] bytes) {

        int next = 0;
        while (next < bytes.length) {

            if (messageCreating) {

                next = putDataInMessage(bytes, next);

            } else {

                switch (bytes[next]) {

                    // begin data frame from controller
                    case GateLocalConstants.SOF:
                        messageCreating = true;
                        next++;
                        break;

                    // node acknowledge our command
                    case GateLocalConstants.ACK:
                        handler.receiveAcknowledge();
                        break;

                    // node mot acknowledge our command
                    case GateLocalConstants.NAC:
                        handler.receiveNotAcknowledge();
                        break;

                    // node request for cancel
                    case GateLocalConstants.CAN:
                        handler.receiveCancel();
                        break;

                    // unknown bytes without frame
                    default:
                        sendRequest(new byte[]{GateLocalConstants.NAC});
                        next = bytes.length;

                }

            }

        }

    }


    //##############################################################################################################
    //######    inner classes


    //------------------------------------------------------------------------
    //  reading data from serial port

    public class PortReader implements SerialPortEventListener {

        @Override
        public void serialEvent(SerialPortEvent event) {
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


}
