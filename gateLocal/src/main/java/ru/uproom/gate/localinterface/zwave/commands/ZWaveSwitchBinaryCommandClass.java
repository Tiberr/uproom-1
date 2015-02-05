package ru.uproom.gate.localinterface.zwave.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.uproom.gate.localinterface.zwave.devices.ZWaveDevice;
import ru.uproom.gate.localinterface.zwave.devices.ZWaveDeviceParameter;
import ru.uproom.gate.localinterface.zwave.devices.parameters.ZWaveSwitchDeviceParameter;
import ru.uproom.gate.localinterface.zwave.enums.ZWaveCommandClasses;
import ru.uproom.gate.localinterface.zwave.enums.ZWaveDeviceParameterNames;
import ru.uproom.gate.transport.dto.parameters.DeviceParametersNames;

/**
 * z-wave command class
 * <p/>
 * Created by osipenko on 10.09.14.
 */
@ZWaveCommandClassesAnnotation(value = ZWaveCommandClasses.SwitchBinary)
public class ZWaveSwitchBinaryCommandClass extends ZWaveCommandClassImpl {


    private static final Logger LOG =
            LoggerFactory.getLogger(ZWaveSwitchBinaryCommandClass.class);


    @Override
    public int createExtraParameterList(ZWaveDevice device) {
        int parametersNumber = 0;
        String parameterNames = "";

        ZWaveDeviceParameter parameter = new ZWaveSwitchDeviceParameter(
                device,
                this,
                DeviceParametersNames.Switch
        );
        device.addParameter(parameter);
        parametersNumber++;
        parameterNames += ZWaveDeviceParameterNames.Switch.name();

        ZWaveCommandClasses annotation =
                (ZWaveCommandClasses) getClass().getAnnotation(ZWaveCommandClassesAnnotation.class).value();
        LOG.debug("ADD COMMAND CLASS : {}, implement {} parameter(s) ({}) ", new Object[]{
                annotation.name(),
                parametersNumber,
                parameterNames
        });

        return parametersNumber;
    }

    // todo : перенести обработку данных из параметров в командные классы

}

