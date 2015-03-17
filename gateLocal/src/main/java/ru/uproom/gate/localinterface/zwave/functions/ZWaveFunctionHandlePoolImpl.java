package ru.uproom.gate.localinterface.zwave.functions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.uproom.gate.localinterface.zwave.devices.ZWaveDevicePool;
import ru.uproom.gate.localinterface.zwave.driver.ZWaveDriver;
import ru.uproom.gate.localinterface.zwave.driver.ZWaveMessage;
import ru.uproom.gate.localinterface.zwave.enums.ZWaveFunctionID;
import ru.uproom.gate.localinterface.zwave.enums.ZWaveMessageTypes;
import ru.uproom.gate.transport.domain.ClassesSearcher;

import javax.annotation.PostConstruct;
import java.util.EnumMap;
import java.util.Map;


/**
 * Main object for handling z-wave functions
 * </p>
 * Created by osipenko on 05.08.14.
 */
@Service
public class ZWaveFunctionHandlePoolImpl implements ZWaveFunctionHandlePool {


    //##############################################################################################################
    //######    fields


    private static final Logger LOG = LoggerFactory.getLogger(ZWaveFunctionHandlePoolImpl.class);

    @Autowired
    ZWaveDriver driver;
    @Autowired
    ZWaveDevicePool devicePool;

    private Map<ZWaveFunctionID, ZWaveFunctionHandler> functionHandlers =
            new EnumMap<>(ZWaveFunctionID.class);


    //##############################################################################################################
    //######    constructors

    @PostConstruct
    public void init() {
        prepareHandlers();
    }


    //##############################################################################################################
    //######    getters / setters


    @Override
    public ZWaveDriver getDriver() {
        return driver;
    }

    @Override
    public ZWaveDevicePool getDevicePool() {
        return devicePool;
    }


    //##############################################################################################################
    //######    methods-


    //---------------------------------------------------------------------------------
    //  prepare command handlers

    private void prepareHandlers() {

        for (Class<?> handler : ClassesSearcher.getAnnotatedClasses(
                ZWaveFunctionHandlerAnnotation.class
        )) {
            ZWaveFunctionHandlerAnnotation annotation =
                    handler.getAnnotation(ZWaveFunctionHandlerAnnotation.class);
            if (annotation == null) continue;
            functionHandlers.put(
                    annotation.value(),
                    (ZWaveFunctionHandler) ClassesSearcher.instantiate(handler)
            );
        }

    }


    //------------------------------------------------------------------------
    //  executioner of commands from server

    @Override
    public boolean execute(ZWaveMessage request, byte[] function) {
        if (function.length <= 1) return false;

        ZWaveFunctionID functionID = ZWaveFunctionID.getByCode(function[1]);
        if (functionID == ZWaveFunctionID.UNKNOWN) return false;

        ZWaveFunctionHandler handler = functionHandlers.get(functionID);
        if (handler == null) {
            LOG.error("Handler for z-wave function {} not found", functionID);
            return false;
        }

        ZWaveMessageTypes type = ZWaveMessageTypes.getByCode(function[0]);
        byte[] parameters = new byte[function.length - 2];
        System.arraycopy(function, 2, parameters, 0, parameters.length);
        handler.execute(type, parameters, this, request);

        return true;
    }

    public void setDevicePoolParameters(int homeId, byte controllerId) {
        devicePool.setParameters(homeId, controllerId);
    }
}
