package ru.uproom.gate.tindenetlib.driver;


import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.uproom.gate.transport.domain.LoggingHelper;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * Created by osipenko on 10.01.15.
 */

@Service
public class TindenetSerialPortImpl implements TindenetSerialPort {


    //##############################################################################################################
    //######    fields


    private static final Logger LOG = LoggerFactory.getLogger(TindenetSerialPortImpl.class);

    private static final int LENGTH_OF_FRAME = 1021; // bytes
    private byte[] message = new byte[LENGTH_OF_FRAME];
    @Value("${serial_port}")
    private String serialPortName;
    @Value("${period_between_restart_port}")
    private long periodBetweenRestarts;
    @Autowired
    private TindenetSerialPortDataHandler dataHandler;
    private SerialPort serialPort;
    private boolean serialPortOpen;
    private boolean messageCreating;
    private boolean messageEndFlag;
    private int lastPos = 0;


    //##############################################################################################################
    //######    constructors / destructors

    @PostConstruct
    public void init() {
        open();
    }


    @Override
    @PreDestroy
    public void stop() {

        applySerialPortOpen(false);

        try {
            if (serialPort != null) {
                serialPort.removeEventListener();
                serialPort.closePort();
            }

            LOG.debug("Serial port {} closed successfully", new Object[]{
                    serialPortName
            });

        } catch (SerialPortException e) {
            LOG.error("Serial port ({}) can not close, reason : {}", new Object[]{
                    serialPortName,
                    e.getMessage()
            });
        }

    }


    //##############################################################################################################
    //######    methods


    //------------------------------------------------------------------------
    //  restart port

    @Override
    public void restart() {
        stop();
        open();
    }


    //------------------------------------------------------------------------
    //  open serial port after all initialization

    @Override
    public void open() {

        serialPort = new SerialPort(serialPortName);
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
                    serialPortName
            });

            applySerialPortOpen(true);

        } catch (SerialPortException e) {
            LOG.error("Serial port ({}) can not open, reason : {}", new Object[]{
                    serialPortName,
                    e.getMessage()
            });
        }

    }


    //------------------------------------------------------------------------
    //  send command to serial port

    @Override
    public void sendRequest(byte[] request) {

        int length = (request.length + 3 < LENGTH_OF_FRAME) ? request.length : 1021;

        byte[] data = new byte[length + 3];
        data[0] = TindenetFrameMarker.STOF.getCode();
        System.arraycopy(request, 0, data, 2, length);
        data[length + 1] = TindenetFrameMarker.EOF1.getCode();
        data[length + 2] = TindenetFrameMarker.EOF2.getCode();
        sendDataToPort(data);
    }

    private void sendDataToPort(byte[] data) {

        try {
            LOG.debug("(RAW) CONTROLLER :{}", LoggingHelper.createHexStringFromByteArray(data));
            serialPort.writeBytes(data);
        } catch (SerialPortException e) {
            LOG.error("Serial port ({}) can not write, reason : {}", new Object[]{
                    serialPortName,
                    e.getMessage()
            });
        }

    }


    //------------------------------------------------------------------------
    //  create message from raw data
    //  frame: [SOF|function|DLT|data|EOF1|EOF2]

    private synchronized void createMessageFromRaw(byte[] bytes) {
        boolean messageCreated = false;
        boolean messageClear = false;

        int next = 0;
        while (next < bytes.length) {

            switch (TindenetFrameMarker.getByCode(bytes[next])) {

                case STOF:
                    messageCreating = true;
                    break;

                case EOF1:
                    messageEndFlag = true;
                    break;

                case EOF2:
                    messageCreated = messageEndFlag && lastPos > 0;
                    messageClear = !messageCreated;
                    break;

                default:
                    if (messageCreating) {
                        if (lastPos < message.length) {
                            message[lastPos] = bytes[next];
                            lastPos++;
                        } else {
                            messageClear = true;
                        }
                    }
            }

            if (messageCreated) {
                dataHandler.handleMessageFromSerialPort(message, lastPos);
            }

            if (messageCreated || messageClear) {
                messageClear = false;
                messageCreated = false;
                messageCreating = false;
                messageEndFlag = false;
                lastPos = 0;
            }

            next++;
        }

    }


    //------------------------------------------------------------------------
    //  additional handle after port open

    private void applySerialPortOpen(boolean open) {
        if (open)
            try {
                serialPort.purgePort(SerialPort.PURGE_RXCLEAR | SerialPort.PURGE_TXCLEAR);
            } catch (SerialPortException e) {
                LOG.error("Serial port ({}) can not purge, reason : {}", new Object[]{
                        serialPortName,
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
                byte[] data = null;
                try {
                    data = serialPort.readBytes();
                    LOG.debug("(RAW) TINDENET   :{}", LoggingHelper.createHexStringFromByteArray(data));
                    createMessageFromRaw(data);
                } catch (SerialPortException e) {
                    LOG.error("Serial port ({}) can not read, reason : {}", new Object[]{
                            serialPortName,
                            e.getMessage()
                    });
                }
            }

        }

    }

    //------------------------------------------------------------------------
    //

}
