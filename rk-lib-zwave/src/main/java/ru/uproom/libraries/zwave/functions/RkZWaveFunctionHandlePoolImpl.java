package ru.uproom.libraries.zwave.functions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.uproom.libraries.auxilliary.ClassesSearcher;
import ru.uproom.libraries.zwave.devices.RkZWaveDevicePool;
import ru.uproom.libraries.zwave.driver.RkZWaveDriver;
import ru.uproom.libraries.zwave.driver.RkZWaveMessage;
import ru.uproom.libraries.zwave.enums.RkZWaveFunctionID;
import ru.uproom.libraries.zwave.enums.RkZWaveMessageTypes;

import java.util.EnumMap;
import java.util.Map;


/**
 * Main object for handling z-wave functions
 * </p>
 * Created by osipenko on 05.08.14.
 */
public class RkZWaveFunctionHandlePoolImpl implements RkZWaveFunctionHandlePool {


    //##############################################################################################################
    //######    fields


    private static final Logger LOG = LoggerFactory.getLogger(RkZWaveFunctionHandlePool.class);

    RkZWaveDriver driver;

    private Map<RkZWaveFunctionID, RkZWaveFunctionHandler> functionHandlers =
            new EnumMap<>(RkZWaveFunctionID.class);


    //##############################################################################################################
    //######    constructors


    @Override
    public void create() {
        prepareHandlers();
    }

    @Override
    public void destroy() {
        functionHandlers.clear();
    }


    //##############################################################################################################
    //######    getters / setters


    @Override
    public RkZWaveDriver getDriver() {
        return driver;
    }

    @Override
    public void setDriver(RkZWaveDriver driver) {
        this.driver = driver;
    }

    @Override
    public RkZWaveDevicePool getDevicePool() {
        return (RkZWaveDevicePool) driver;
    }


    //##############################################################################################################
    //######    methods-


    //---------------------------------------------------------------------------------
    //  prepare command handlers

    private void prepareHandlers() {

        for (Class<?> handler : ClassesSearcher.getAnnotatedClasses(
                RkZWaveFunctionHandlerAnnotation.class
        )) {
            RkZWaveFunctionHandlerAnnotation annotation =
                    handler.getAnnotation(RkZWaveFunctionHandlerAnnotation.class);
            if (annotation == null) continue;
            functionHandlers.put(
                    annotation.value(),
                    (RkZWaveFunctionHandler) ClassesSearcher.instantiate(handler)
            );
        }

    }


    //------------------------------------------------------------------------
    //  executioner of commands from server

    @Override
    public boolean execute(RkZWaveMessage request, int[] function) {
        if (function.length <= 1) return false;

        RkZWaveFunctionID functionID = RkZWaveFunctionID.getByCode(function[1]);
        if (functionID == RkZWaveFunctionID.UNKNOWN) return false;

        RkZWaveFunctionHandler handler = functionHandlers.get(functionID);
        if (handler == null) {
            LOG.error("Handler for z-wave function {} not found", functionID);
            return false;
        }

        RkZWaveMessageTypes type = RkZWaveMessageTypes.getByCode(function[0]);
        int[] parameters = new int[function.length - 2];
        System.arraycopy(function, 2, parameters, 0, parameters.length);
        handler.execute(type, parameters, this, request);

        return true;
    }

}
