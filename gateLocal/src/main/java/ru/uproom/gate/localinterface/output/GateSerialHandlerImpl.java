package ru.uproom.gate.localinterface.output;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by osipenko on 11.01.15.
 */
@Service
public class GateSerialHandlerImpl implements GateSerialHandler {


    //##############################################################################################################
    //######    fields

    private static final Logger LOG = LoggerFactory.getLogger(GateSerialHandlerImpl.class);

    private final List<byte[]> messages = new ArrayList<>();

    @Autowired
    private GateSerialPort serialPort;


    //##############################################################################################################
    //######    constructors / destructors


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
    //  get data from serial port

    @Override
    public void receiveMessage(byte[] data) {

        synchronized (messages) {
            messages.add(data);
        }
        logDataInHex("MESSAGE  :", data);

    }

    @Override
    public void receiveAcknowledge() {
        LOG.debug("ACKNOWLEDGE");
    }

    @Override
    public void receiveCancel() {
        LOG.debug("CANCEL");
    }

    @Override
    public void receiveNotAcknowledge() {
        LOG.debug("NOT ACKNOWLEDGE");
    }


    //##############################################################################################################
    //######    inner classes

}
