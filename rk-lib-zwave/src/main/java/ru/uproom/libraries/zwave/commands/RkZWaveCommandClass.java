package ru.uproom.libraries.zwave.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.uproom.libraries.auxilliary.AppendingValue;
import ru.uproom.libraries.auxilliary.Bitfield;
import ru.uproom.libraries.auxilliary.ExtractingValue;
import ru.uproom.libraries.zwave.devices.RkZWaveDevice;
import ru.uproom.libraries.zwave.devices.RkZWaveDeviceParameter;
import ru.uproom.libraries.zwave.enums.RkZWaveCommandClassNames;

import java.util.HashMap;
import java.util.Map;

/**
 * z-wave command class
 * <p/>
 * Created by osipenko on 10.09.14.
 */
public class RkZWaveCommandClass {


    //----------------------------------------------------------------------------------------------

    protected static final Logger LOG =
            LoggerFactory.getLogger(RkZWaveCommandClass.class);

    protected static final int SIZE_MASK = 0x07;
    protected static final int SCALE_MASK = 0x18;
    protected static final int SCALE_SHIFT = 0x03;
    protected static final int PRECISION_MASK = 0xE0;
    protected static final int PRECISION_SHIFT = 0x05;

    private RkZWaveCommandClassNames name;
    private int version;
    private boolean haveVersion;
    private boolean afterMark;
    private int instancesNumber = 0x01;
    private Bitfield instances = new Bitfield();

    private Map<Integer, Integer> endPointMap = new HashMap<>();


    //----------------------------------------------------------------------------------------------

    public RkZWaveCommandClass() {
        name = (RkZWaveCommandClassNames) getClass().
                getAnnotation(RkZWaveCommandClassesAnnotation.class).value();
    }


    //----------------------------------------------------------------------------------------------

    public int getId() {
        return name.getCode();
    }

    public RkZWaveCommandClassNames getName() {
        return name;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(RkZWaveDevice device, int version) {
        this.version = version;
        haveVersion = true;
        LOG.debug("COMMAND CLASS SET VERSION: command class = {}, version = {}", new Object[]{
                name.name(),
                version
        });
    }

    public int createParameterList(RkZWaveDevice device, int instance) {
        return 0;
    }

    public void requestDeviceParameter(RkZWaveDevice device, int instance) {
    }

    public void setDeviceParameter(RkZWaveDeviceParameter parameter, String value) {
    }

    public void createInstance(RkZWaveDevice device, int instance) {
        createParameterList(device, instance);
    }

    public void createInstances(RkZWaveDevice device, int instances) {
        if (isAfterMark()) return;

        for (int i = 1; i <= instances; ++i) {
            createInstance(device, i);
        }
    }

    public void requestDeviceState(RkZWaveDevice device, int instance) {
    }

    public int getInstanceEndPoint(int instance) {
        return endPointMap.get(instance);
    }

    public void messageHandler(RkZWaveDevice device, int[] data, int instance) {
        LOG.error("I am the method of base class. PLEASE, KILL ME!");
    }

    public void setInstanceEndPoint(int instance, int endPoint) {
        endPointMap.put(instance, endPoint);
    }

    public void requestStateForAllInstances(RkZWaveDevice device) {
        for (int i = 1; i <= instancesNumber; i++) {
            requestDeviceState(device, i);
        }
    }

    public void applyValueBasic(RkZWaveDevice device, int instance, int value) {
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

    protected int getInstancesNumber() {
        return instancesNumber;
    }

    protected void setInstancesNumber(int instancesNumber) {
        this.instancesNumber = instancesNumber;
    }

    protected int getInstance(int endPoint) {
        for (Map.Entry<Integer, Integer> entry : endPointMap.entrySet()) {
            if (entry.getValue() == endPoint) return entry.getKey();
        }
        return 0;
    }

    protected Bitfield getInstances() {
        return instances;
    }


    //----------------------------------------------------------------------------------------------

    protected ExtractingValue extractValueFromInts(int[] bytes, int offset) {
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


    protected ExtractingValue extractValueFromInts(int[] bytes) {
        return extractValueFromInts(bytes, 0x01);
    }


    //----------------------------------------------------------------------------------------------

    protected int[] appendValueToInts(String value, int scale) {
        AppendingValue appendingValue = valueToInteger(value);

        int[] bytes = new int[appendingValue.getSize()];
        bytes[0] = (appendingValue.getPrecision() << PRECISION_SHIFT) | (scale << SCALE_SHIFT)
                | appendingValue.getSize();
        int shift = (appendingValue.getSize() - 1) << 3;
        for (int i = appendingValue.getSize(), j = 1; i > 0; --i, j++, shift -= 8) {
            bytes[j] = appendingValue.getValue() >> shift;
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

    protected void updateMappedClass(RkZWaveDevice device, int instance,
                                     RkZWaveCommandClassNames commandClassName, int level) {
        RkZWaveCommandClass commandClass = device.getCommandClassByName(commandClassName);
        commandClass.applyValueBasic(device, instance, level);
    }

}

