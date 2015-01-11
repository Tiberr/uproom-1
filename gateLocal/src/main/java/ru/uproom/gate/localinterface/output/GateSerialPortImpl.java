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

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.UnsupportedEncodingException;

/**
 * Created by osipenko on 10.01.15.
 */

@Service
public class GateSerialPortImpl implements GateSerialPort {


    //##############################################################################################################
    //######    fields


    private static final Logger LOG = LoggerFactory.getLogger(GateLocalOutputImpl.class);

    @Value("${serial_port}")
    private String serial;

    @Autowired
    private GateSerialHandler handler;

    private SerialPort serialPort;


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
    public void sendCommand(String command) {

        try {
            byte[] bytes = command.getBytes("ISO-8859-1");
            logData("MESSAGE :", bytes);
            serialPort.writeBytes(bytes);
        } catch (SerialPortException | UnsupportedEncodingException e) {
            LOG.error("Serial port ({}) can not write, reason : {}", new Object[]{
                    serial,
                    e.getMessage()
            });
        }

    }


    //------------------------------------------------------------------------
    //  logging data exchange

    private void logData(String prefix, byte[] bytes) {
        String output = "";
        for (byte b : bytes) {
            output += String.format(" 0x%02X", b);
        }
        LOG.debug("{}{}", new Object[]{prefix, output});
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
                    logData("ANSWER  :", bytes);
                    handler.letDataFromSerial(bytes);
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
