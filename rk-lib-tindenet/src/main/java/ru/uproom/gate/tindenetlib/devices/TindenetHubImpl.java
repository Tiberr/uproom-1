package ru.uproom.gate.tindenetlib.devices;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.uproom.gate.tindenetlib.driver.TindenetSerialPortDataHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * this is modules pool (hub)
 * <p/>
 * Created by osipenko on 21.03.15.
 */
@Service
public class TindenetHubImpl implements TindenetHub {


    //##############################################################################################################
    //######    fields


    private static final Logger LOG = LoggerFactory.getLogger(TindenetHubImpl.class);

    private final Map<Integer, TindenetModule> modules = new HashMap<>();
    private final Map<Integer, Integer> moduleServerAddresses = new HashMap<>();

    @Autowired
    TindenetSerialPortDataHandler serialPortDataHandler;


    //##############################################################################################################
    //######    methods


    @Override
    public TindenetSerialPortDataHandler getSerialPortDataHandler() {
        return serialPortDataHandler;
    }

}
