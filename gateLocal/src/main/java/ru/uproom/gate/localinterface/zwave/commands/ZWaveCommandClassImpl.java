package ru.uproom.gate.localinterface.zwave.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.uproom.gate.localinterface.zwave.devices.ZWaveDevice;
import ru.uproom.gate.localinterface.zwave.enums.ZWaveCommandClasses;

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
        ZWaveCommandClasses annotation =
                (ZWaveCommandClasses) getClass().getAnnotation(ZWaveCommandClassesAnnotation.class).value();
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
        return createExtraParameterList(device);
    }

    @Override
    public void messageHandler(ZWaveDevice device, byte[] data) {
        messageExtraHandler(device, data);
    }


    protected int createExtraParameterList(ZWaveDevice device) {
        return 0;
    }

    protected void messageExtraHandler(ZWaveDevice device, byte[] data) {
    }

}

