package ru.uproom.libraries.zwave.commands;

import libraries.auxilliary.AppendingValue;
import libraries.auxilliary.Bitfield;
import libraries.auxilliary.ExtractingValue;
import libraries.auxilliary.RunnableClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.uproom.libraries.zwave.devices.RkZWaveDevice;
import ru.uproom.libraries.zwave.devices.RkZWaveDeviceParameter;
import ru.uproom.libraries.zwave.enums.RkZWaveCommandClassInitSeqStep;
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
    protected Bitfield instances = new Bitfield();
    protected RkZWaveDevice device;
    protected RunInitialSequence runInitialSequence = new RunInitialSequence();
    protected RkZWaveVersionCommandClass versionCommandClass;
    protected RkZWaveMultiInstanceCommandClass instanceCommandClass;
    protected int currentInstanceForRequest = 0;
    private RkZWaveCommandClassNames name;
    private int version;
    private boolean haveVersion;
    private boolean afterMark;
    private int instancesNumber = 0x01;
    private Map<Integer, Integer> endPointMap = new HashMap<>();
    private boolean ready;


    //##############################################################################################################
    //######    constructors / destructors


    public RkZWaveCommandClass() {

        name = (RkZWaveCommandClassNames) getClass().
                getAnnotation(RkZWaveCommandClassesAnnotation.class).value();
    }


    //##############################################################################################################
    //######    getters / setters


    public int getId() {
        return name.getCode();
    }


    //----------------------------------------------------------------------------------------------

    public RkZWaveCommandClassNames getName() {
        return name;
    }


    //----------------------------------------------------------------------------------------------

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {

        this.version = version;
        haveVersion = true;
        LOG.debug("COMMAND CLASS SET VERSION: command class = {}, version = {}", new Object[]{
                name.name(),
                version
        });

        runInitialSequence.setInitSequenceStep(RkZWaveCommandClassInitSeqStep.GetVersion);
    }


    //----------------------------------------------------------------------------------------------

    public boolean isReady() {
        return ready;
    }

    protected void setReady(boolean ready) {

        boolean condition = !this.ready && ready;
        this.ready = ready;

        if (condition) {
            LOG.info("COMMAND CLASS READY : command class ({}), device ({})", new Object[]{
                    name.name(),
                    String.valueOf(device.getDeviceId())
            });
            device.commandClassReady(this);
        }
    }


    //----------------------------------------------------------------------------------------------

    public void setDevice(RkZWaveDevice device) {
        this.device = device;
    }


    //----------------------------------------------------------------------------------------------

    public void setCommonCommandClasses(
            RkZWaveVersionCommandClass versionCommandClass,
            RkZWaveMultiInstanceCommandClass instanceCommandClass
    ) {

        this.versionCommandClass = versionCommandClass;
        this.instanceCommandClass = instanceCommandClass;
    }


    //##############################################################################################################
    //######    methods

    public int createParameterList(int instance) {
        return 0;
    }

    public void requestDeviceParameter(int instance) {
    }

    public void setDeviceParameter(RkZWaveDeviceParameter parameter, String value) {
    }


    //----------------------------------------------------------------------------------------------

    public void createInstance(int instance) {
        createParameterList(instance);
    }

    public void createInstances(int instances) {
        if (isAfterMark()) return;

        setInstancesNumber(instances);
        this.instances.clear();

        for (int i = 1; i <= instances; ++i) {
            createInstance(i);
        }

        runInitialSequence.setInitSequenceStep(RkZWaveCommandClassInitSeqStep.GetInstances);
    }

    public int getInstanceEndPoint(int instance) {
        return endPointMap.get(instance);
    }

    public void messageHandler(int[] data, int instance) {
        LOG.error("I am the method of base class. PLEASE, KILL ME!");
    }

    public void setInstanceEndPoint(int instance, int endPoint) {
        endPointMap.put(instance, endPoint);
    }


    //----------------------------------------------------------------------------------------------

    public void requestDeviceState(int instance) {
    }

    public void requestStateForNextInstance() {

        if (instances.getNumSetBits() >= instancesNumber) {
            currentInstanceForRequest = 0;
            runInitialSequence.setInitSequenceStep(RkZWaveCommandClassInitSeqStep.GetState);
            return;
        }

        if (currentInstanceForRequest <= 0)
            currentInstanceForRequest = 1;
        if (instances.isBit(currentInstanceForRequest))
            currentInstanceForRequest++;

        requestDeviceState(currentInstanceForRequest);
    }


    //----------------------------------------------------------------------------------------------

    public void applyValueBasic(int instance, int value) {
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
        LOG.info("COMMAND CLASS INSTANCES : device ({}) command class ({}) has a ({}) instances", new Object[]{
                device.getDeviceId(),
                name.name(),
                instancesNumber
        });
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
        commandClass.applyValueBasic(instance, level);
    }


    //----------------------------------------------------------------------------------------------

    public void startInitSequence(long timeBetweenCheckInitSeq) {

        runInitialSequence.setTimeout(timeBetweenCheckInitSeq);
        new Thread(runInitialSequence).start();
        runInitialSequence.restartInitSequence();
    }


    //##############################################################################################################
    //######    inner classes


    protected class RunInitialSequence extends RunnableClass {

        private RkZWaveCommandClassInitSeqStep initSequenceStep = RkZWaveCommandClassInitSeqStep.Unknown;


        public void restartInitSequence() {

            initSequenceStep = RkZWaveCommandClassInitSeqStep.Unknown;
            setInitSequenceStep(RkZWaveCommandClassInitSeqStep.Unknown);
        }


        public void setInitSequenceStep(RkZWaveCommandClassInitSeqStep stepId) {
            boolean quit = false;

            switch (initSequenceStep) {

                case Unknown:
                    if (stepId == RkZWaveCommandClassInitSeqStep.Unknown)
                        initSequenceStep = RkZWaveCommandClassInitSeqStep.GetVersion;
                    break;

                case GetVersion:
                    if (stepId == RkZWaveCommandClassInitSeqStep.GetVersion)
                        initSequenceStep = RkZWaveCommandClassInitSeqStep.GetInstances;
                    break;

                case GetInstances:
                    if (stepId == RkZWaveCommandClassInitSeqStep.GetInstances)
                        initSequenceStep = RkZWaveCommandClassInitSeqStep.GetState;
                    break;

                case GetState:
                    if (stepId == RkZWaveCommandClassInitSeqStep.GetState) {
                        quit = true;
                        initSequenceStep = RkZWaveCommandClassInitSeqStep.Unknown;
                    }
                    break;

                default:
            }

            if (!quit) {
                synchronized (this) {
                    notify();
                }
            } else {
                setReady(true);
                stop();
            }
        }


        @Override
        protected void body() {
            super.body();
            if (initSequenceStep == RkZWaveCommandClassInitSeqStep.Unknown) return;

            switch (initSequenceStep) {

                case GetVersion:
                    if (versionCommandClass != null)
                        versionCommandClass.requestCommandClassVersion(name);
                    else
                        setVersion(0x01);
                    break;

                case GetInstances:
                    if (instanceCommandClass != null)
                        instanceCommandClass.requestInstance(name);
                    else
                        createInstances(0x01);
                    break;

                case GetState:
                    requestStateForNextInstance();
                    // todo : must be implemented for all command classes
                    break;

                default:
            }

        }
    }

}

