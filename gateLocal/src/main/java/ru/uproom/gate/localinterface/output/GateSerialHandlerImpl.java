package ru.uproom.gate.localinterface.output;

import org.springframework.stereotype.Service;

/**
 * Created by osipenko on 11.01.15.
 */
@Service
public class GateSerialHandlerImpl implements GateSerialHandler {


    //##############################################################################################################
    //######    fields


    private final byte SOF = 0x01; // Start Of Frame
    private final byte ACK = 0x06; // ACKnowledge
    private final byte NAC = 0x15; // Negative ACk
    private final byte CAN = 0x18; // CANcel


    //##############################################################################################################
    //######    methods


    //------------------------------------------------------------------------
    //  get data from serial port

    @Override
    public void letDataFromSerial(byte[] data) {

        switch (data[0]) {
            case SOF:
                break;
            case ACK:
                break;
            case NAC:
                break;
            case CAN:
                break;
            default:
        }

    }

}
