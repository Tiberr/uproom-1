package ru.uproom.gate.localinterface.zwave.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.uproom.gate.localinterface.zwave.devices.ZWaveDevice;
import ru.uproom.gate.localinterface.zwave.devices.ZWaveDeviceParameter;
import ru.uproom.gate.localinterface.zwave.enums.ZWaveCommandClassNames;

/**
 * z-wave command class
 * <p/>
 * Created by osipenko on 10.09.14.
 */
public class ZWaveCommandClassImpl implements ZWaveCommandClass {


    protected static final Logger LOG =
            LoggerFactory.getLogger(ZWaveCommandClassImpl.class);

    private byte id;
    private byte version;


    public ZWaveCommandClassImpl() {
        ZWaveCommandClassNames annotation =
                (ZWaveCommandClassNames) getClass().getAnnotation(ZWaveCommandClassesAnnotation.class).value();
        id = annotation.getCode();
    }


    @Override
    public byte getId() {
        return id;
    }

    @Override
    public byte getVersion() {
        return version;
    }

    @Override
    public void setVersion(byte version) {
        this.version = version;
    }


    @Override
    public int createParameterList(ZWaveDevice device) {
        return 0;
    }

    @Override
    public void messageHandler(ZWaveDevice device, byte[] data) {
    }

    @Override
    public void requestDeviceState(ZWaveDevice device) {
    }

    @Override
    public void requestDeviceParameter(ZWaveDevice device) {

    }

    @Override
    public void setDeviceParameter(ZWaveDeviceParameter parameter, String value) {

    }

}

