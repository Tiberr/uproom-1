package ru.uproom.gate.tindenetlib.driver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.uproom.gate.tindenetlib.commands.TindenetCommandHandlersFactory;

import javax.annotation.PostConstruct;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by osipenko on 16.03.15.
 */

@Service
public class TindenetSerialPortDataHandlerImpl implements TindenetSerialPortDataHandler {


    //##############################################################################################################
    //######    fields


    private static final Logger LOG = LoggerFactory.getLogger(TindenetSerialPortImpl.class);
    private final List<TindenetMessage> sendingQueue = new LinkedList<>();
    @Autowired
    private TindenetCommandHandlersFactory commandHandlersFactory;


    //##############################################################################################################
    //######    constructors / destructors

    @PostConstruct
    public void init() {

    }


    //##############################################################################################################
    //######    methods


    @Override
    public void handleMessageFromSerialPort(byte[] message, int length) {
        String command = null;

        try {
            command = new String(message, 0, length, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            LOG.error("can not create string from byte array : {}", e.getMessage());
        }
        if (command == null || command.isEmpty()) return;

        commandHandlersFactory.handleCommand(command);
    }


}
