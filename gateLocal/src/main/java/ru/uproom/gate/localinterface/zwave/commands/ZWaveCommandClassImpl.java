package ru.uproom.gate.localinterface.zwave.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.uproom.gate.localinterface.domain.ExtractingValue;
import ru.uproom.gate.localinterface.zwave.devices.ZWaveDevice;
import ru.uproom.gate.localinterface.zwave.devices.ZWaveDeviceParameter;
import ru.uproom.gate.localinterface.zwave.enums.ZWaveCommandClassNames;

/**
 * z-wave command class
 * <p/>
 * Created by osipenko on 10.09.14.
 */
public class ZWaveCommandClassImpl implements ZWaveCommandClass {


    //----------------------------------------------------------------------------------------------

    protected static final Logger LOG =
            LoggerFactory.getLogger(ZWaveCommandClassImpl.class);

    protected static final int SIZE_MASK = 0x07;
    protected static final int SCALE_MASK = 0x18;
    protected static final int SCALE_SHIFT = 0x03;
    protected static final int PRECISION_MASK = 0xE0;
    protected static final int PRECISION_SHIFT = 0x05;

    private ZWaveCommandClassNames name;
    private byte version;

    private boolean haveVersion;


    //----------------------------------------------------------------------------------------------

    public ZWaveCommandClassImpl() {
        name = (ZWaveCommandClassNames) getClass().
                getAnnotation(ZWaveCommandClassesAnnotation.class).value();
    }


    //----------------------------------------------------------------------------------------------

    @Override
    public byte getId() {
        return name.getCode();
    }

    @Override
    public ZWaveCommandClassNames getName() {
        return name;
    }

    @Override
    public byte getVersion() {
        return version;
    }

    @Override
    public void setVersion(byte version) {
        this.version = version;
        haveVersion = true;
        LOG.debug("COMMAND CLASS SET VERSION: command class = {}, version = {}", new Object[]{
                name.name(),
                version
        });
    }

    @Override
    public int createParameterList(ZWaveDevice device, byte instance) {
        return 0;
    }

    @Override
    public void createInstance(ZWaveDevice device, byte instance) {
        createParameterList(device, instance);

        // todo : stop here

    }

    @Override
    public void createInstances(ZWaveDevice device, byte instances) {
        for (byte i = 1; i <= instances; ++i) {
            createInstance(device, i);
        }
    }

    @Override
    public void messageHandler(ZWaveDevice device, byte[] data) {
        messageHandler(device, data, (byte) 0x01);
    }

    @Override
    public void messageHandler(ZWaveDevice device, byte[] data, byte instance) {
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

    protected boolean isHaveVersion() {
        return haveVersion;
    }


    //----------------------------------------------------------------------------------------------

    protected ExtractingValue extractValueFromBytes(byte[] bytes, byte offset) {
        StringBuffer resultValue = new StringBuffer();

        // get properties
        byte size = (byte) (bytes[0] & SIZE_MASK);
        byte scale = (byte) ((bytes[0] & SCALE_MASK) >> SCALE_SHIFT);
        byte precision = (byte) ((bytes[0] & PRECISION_MASK) >> PRECISION_SHIFT);

        // extract raw value
        int value = 0;
        for (int i = 0; i < size; ++i) {
            value <<= 8;
            value |= bytes[i + offset];
        }

        // check sign
        if ((bytes[offset] & 0x80) != 0x00) {
            resultValue.append("-");
            value |= (0xffffffff << (size * 8));
        }

        // raw value will be fine
        resultValue.append(String.format("%d", value));

        // oh, no! We have a floating point value!
        if (precision > 0) {
            resultValue.insert(resultValue.length() - precision, ".");
        }

        return new ExtractingValue(resultValue.toString(), scale, precision);
    }

    protected ExtractingValue extractValueFromBytes(byte[] bytes) {
        return extractValueFromBytes(bytes, (byte) 0x01);
    }


    //----------------------------------------------------------------------------------------------

}

