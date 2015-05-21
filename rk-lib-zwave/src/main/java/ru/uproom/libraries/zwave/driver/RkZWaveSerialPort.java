package ru.uproom.libraries.zwave.driver;

import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import libraries.auxilliary.LoggingHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.uproom.libraries.zwave.enums.RkZWaveFrameMarker;

/**
 * Created by osipenko on 10.01.15.
 * <p/>
 * wrapper for native JSSC Serial Port
 */

public class RkZWaveSerialPort {


    //##############################################################################################################
    //######    fields


    private static final Logger LOG = LoggerFactory.getLogger(RkZWaveSerialPort.class);

    RkZWaveDriver driver;

    private SerialPort serialPort;
    private boolean serialPortOpen;

    private boolean messageCreating;
    private int lastPos = 0;
    private int[] message;


    //##############################################################################################################
    //######    constructors / destructors


    //------------------------------------------------------------------------

    public void create() {

        String serial = driver.getProperties().getProperty("port_name");
        if (serial == null || serial.isEmpty()) return;

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

            serialPort.purgePort(SerialPort.PURGE_RXCLEAR | SerialPort.PURGE_TXCLEAR);

            sendDataToPort(new int[]{RkZWaveFrameMarker.CAN.getCode()});
            driver.applyPortState(true);

            serialPortOpen = true;

        } catch (SerialPortException e) {
            LOG.error("Serial port ({}) can not open, reason : {}", new Object[]{
                    serial,
                    e.getMessage()
            });
        }

    }


    //------------------------------------------------------------------------

    public void destroy() {

        String serial = "";
        try {
            if (serialPort != null) {
                serial = serialPort.getPortName();
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

        serialPortOpen = false;
    }


    //##############################################################################################################
    //######    getters / setters


    public void setDriver(RkZWaveDriver driver) {
        this.driver = driver;
    }


    public boolean isSerialPortOpen() {
        return serialPortOpen;
    }


    //##############################################################################################################
    //######    methods


    //------------------------------------------------------------------------

    public void sendRequest(int[] request) {

        int[] data = new int[request.length + 3];
        data[0] = RkZWaveFrameMarker.SOF.getCode();
        data[1] = request.length + 1;
        System.arraycopy(request, 0, data, 2, request.length);
        data[data.length - 1] = createCheckSum(request);
        sendDataToPort(data);
    }


    //------------------------------------------------------------------------

    private void sendDataToPort(int[] data) {

        try {
            LOG.debug("REQUEST : {}", LoggingHelper.createHexStringFromIntArray(data, true));
            serialPort.writeIntArray(data);
        } catch (SerialPortException e) {
            LOG.error("Serial port ({}) can not write, reason : {}", new Object[]{
                    serialPort.getPortName(),
                    e.getMessage()
            });
        }

    }


    //------------------------------------------------------------------------

    public int createCheckSum(int[] frame) {

        int checksum = 0xFF;
        for (int i : frame)
            checksum ^= i;
        checksum ^= (frame.length + 1);
        LOG.debug("CHECKSUM : {}", LoggingHelper.createHexStringFromIntArray(new int[]{checksum}, true));

        return checksum;
    }


    //------------------------------------------------------------------------

    public boolean verifyCheckSum(int checksum) {

        return (createCheckSum(message) == checksum);
    }


    //------------------------------------------------------------------------

    private int putDataInMessage(int[] bytes, int next) {

        if (message == null) {
            message = new int[bytes[next] - 1];
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
                driver.addMessageToReceivingQueue(message);
                sendDataToPort(new int[]{RkZWaveFrameMarker.ACK.getCode()});
            } else {
                sendDataToPort(new int[]{RkZWaveFrameMarker.NAC.getCode()});
            }

            message = null;
            lastPos = 0;
            messageCreating = false;
        }

        return (checkSumPos + 1);
    }


    //------------------------------------------------------------------------
    //  frame: [SOF|length|type|function|data|checksum]
    //  length = from type to checksum (including); checksum = from length to data (including)

    private synchronized void sortingDataForMessage(int[] bytes) {

        int next = 0;
        while (next < bytes.length) {

            if (messageCreating) {

                next = putDataInMessage(bytes, next);

            } else {

                switch (RkZWaveFrameMarker.getByCode(bytes[next])) {

                    // begin data frame from controller
                    case SOF:
                        messageCreating = true;
                        next++;
                        break;

                    // node acknowledge our command
                    case ACK:
                        driver.receiveAcknowledge();
                        next++;
                        break;

                    // node mot acknowledge our command
                    case NAC:
                        driver.receiveNotAcknowledge();
                        next++;
                        break;

                    // node request for cancel
                    case CAN:
                        driver.receiveCancel();
                        next++;
                        break;

                    // unknown bytes without frame
                    default:
                        sendDataToPort(new int[]{RkZWaveFrameMarker.NAC.getCode()});
                        next = bytes.length;

                }

            }

        }

    }


    //##############################################################################################################
    //######    inner classes


    //------------------------------------------------------------------------

    private class PortReader implements SerialPortEventListener {

        @Override
        public void serialEvent(SerialPortEvent event) {

            if (!serialPortOpen) return;

            if (event.isRXCHAR() && event.getEventValue() > 0) {
                int[] bytes = null;
                try {
                    bytes = serialPort.readIntArray();
                    LOG.debug("ANSWER  : {}", LoggingHelper.createHexStringFromIntArray(bytes, true));
                    sortingDataForMessage(bytes);
                } catch (SerialPortException e) {
                    LOG.error("Serial port ({}) can not read, reason : {}", new Object[]{
                            serialPort.getPortName(),
                            e.getMessage()
                    });
                }
            }

        }

    }


}
