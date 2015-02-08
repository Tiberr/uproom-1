package ru.uproom.gate.localinterface.zwave.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.uproom.gate.localinterface.zwave.devices.ZWaveDevice;
import ru.uproom.gate.localinterface.zwave.enums.ZWaveCommandClassNames;

/**
 * z-wave command class
 * <p/>
 * Created by osipenko on 10.09.14.
 */
@ZWaveCommandClassesAnnotation(value = ZWaveCommandClassNames.Meter)
public class ZWaveMeterCommandClass extends ZWaveCommandClassImpl {


    private static final Logger LOG =
            LoggerFactory.getLogger(ZWaveMeterCommandClass.class);


    @Override
    public int createParameterList(ZWaveDevice device) {

        // todo : 15.02.10 continue from this point

        int parametersNumber = 0;
        String parameterNames = "";

        ZWaveCommandClassNames annotation =
                (ZWaveCommandClassNames) getClass().getAnnotation(ZWaveCommandClassesAnnotation.class).value();
        LOG.debug("ADD COMMAND CLASS : {}, implement {} parameter(s) ({}) ", new Object[]{
                annotation.name(),
                parametersNumber,
                parameterNames
        });

        return parametersNumber;
    }


}

