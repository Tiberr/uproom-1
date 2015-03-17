package ru.uproom.gate.localinterface.zwave.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.uproom.gate.localinterface.domain.AppendingValue;
import ru.uproom.gate.localinterface.domain.ExtractingValue;
import ru.uproom.gate.localinterface.zwave.devices.ZWaveDevice;
import ru.uproom.gate.localinterface.zwave.devices.ZWaveDeviceParameter;
import ru.uproom.gate.localinterface.zwave.enums.ZWaveCommandClassNames;

import java.util.HashMap;
import java.util.Map;

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
    private boolean afterMark;
    private byte instancesNumber = 0x01;

    private Map<Byte, Byte> instancesEndPoints = new HashMap<>();


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
    }

    @Override
    public void createInstances(ZWaveDevice device, byte instances) {
        if (isAfterMark()) return;

        for (byte i = 1; i <= instances; ++i) {
            createInstance(device, i);
        }
    }

    @Override
    public byte getInstanceEndPoint(byte instance) {
        return instancesEndPoints.get(instance);
    }

    @Override
    public void setInstanceEndPoint(byte instance, byte endPoint) {
        instancesEndPoints.put(instance, endPoint);
    }

    @Override
    public void messageHandler(ZWaveDevice device, byte[] data) {
        messageHandler(device, data, (byte) 0x01);
    }

    @Override
    public void messageHandler(ZWaveDevice device, byte[] data, byte instance) {
    }

    @Override
    public void requestDeviceState(ZWaveDevice device, byte instance) {
    }

    @Override
    public void requestDeviceParameter(ZWaveDevice device, byte instance) {

    }

    @Override
    public void setDeviceParameter(ZWaveDeviceParameter parameter, String value) {

    }

    @Override
    public void requestStateForAllInstances(ZWaveDevice device) {
        for (byte i = 1; i <= instancesNumber; i++) {
            requestDeviceState(device, i);
        }
    }

    @Override
    public void applyValueBasic(ZWaveDevice device, byte instance, byte value) {

    }

    protected boolean isHaveVersion() {
        return haveVersion;
    }

    protected boolean isAfterMark() {
        return afterMark;
    }

    protected void setAfterMark(boolean afterMark) {
        this.afterMark = afterMark;
    }

    protected byte getInstancesNumber() {
        return instancesNumber;
    }

    protected void setInstancesNumber(byte instancesNumber) {
        this.instancesNumber = instancesNumber;
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

    protected byte[] appendValueToBytes(String value, byte scale) {
        AppendingValue appendingValue = valueToInteger(value);

        byte[] bytes = new byte[appendingValue.getSize()];
        bytes[0] = (byte) ((appendingValue.getPrecision() << PRECISION_SHIFT) | (scale << SCALE_SHIFT)
                | appendingValue.getSize());
        int shift = (appendingValue.getSize() - 1) << 3;
        for (int i = appendingValue.getSize(), j = 1; i > 0; --i, j++, shift -= 8) {
            bytes[j] = (byte) (appendingValue.getValue() >> shift);
        }

        return bytes;
    }


    private AppendingValue valueToInteger(String value) {
        int valueInt;
        byte size = 4, precision = 0;

        int pos = value.indexOf(".");
        if (pos < 0)
            pos = value.indexOf(",");

        if (pos < 0)
            valueInt = Integer.parseInt(value);
        else {
            precision = (byte) ((value.length() - pos) - 1);
            valueInt = Integer.parseInt(value.substring(0, pos) + value.substring(pos + 1));
        }

        if (valueInt < 0) {
            if ((valueInt & 0xffffff80) == 0xffffff80)
                size = 1;
            else if ((valueInt & 0xffff8000) == 0xffff8000)
                size = 2;
        } else {
            if ((valueInt & 0xffffff00) == 0)
                size = 1;
            else if ((valueInt & 0xffff0000) == 0)
                size = 2;
        }

        return new AppendingValue(valueInt, size, precision);
    }


    //----------------------------------------------------------------------------------------------

    protected void updateMappedClass(ZWaveDevice device, byte instance, byte commandClassId, byte level) {
        ZWaveCommandClass commandClass = device.getCommandClassById(commandClassId);
        commandClass.applyValueBasic(device, instance, level);
    }

}

