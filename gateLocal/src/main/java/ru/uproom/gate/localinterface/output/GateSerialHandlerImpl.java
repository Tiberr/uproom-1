package ru.uproom.gate.localinterface.output;

import org.springframework.stereotype.Service;
import ru.uproom.gate.localinterface.domain.ByteQueue;
import ru.uproom.gate.localinterface.domain.ByteQueueNotify;

import javax.annotation.PostConstruct;

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

    private final DataNotify watcher = new DataNotify();
    private ByteQueue dataQueue;


    //##############################################################################################################
    //######    constructors / destructors


    @PostConstruct
    public void init() {
        dataQueue = new ByteQueue(4096, watcher);
    }


    //##############################################################################################################
    //######    methods


    //------------------------------------------------------------------------
    //  get data from serial port

    @Override
    public void letDataFromSerial(byte[] data) {
        dataQueue.put(data);
    }


    public void read() {

        // todo: continue with this point

        byte first = dataQueue.get();

        switch (first) {
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


    //##############################################################################################################
    //######    inner classes

    private class DataNotify implements ByteQueueNotify {

        @Override
        public void hasData() {
            read();
        }
    }

}
