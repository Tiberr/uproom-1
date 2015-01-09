package ru.uproom.gate.devices.zwave;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zwave4j.Manager;
import org.zwave4j.ValueId;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by osipenko on 14.08.14.
 */
public class ZWaveValue {


    //=============================================================================================================
    //======    fields

    private static final Logger LOG = LoggerFactory.getLogger(ZWaveValue.class);

    private int id;
    private ValueId valueId;
    private ZWaveDeviceParametersNames valueName;
    private boolean readOnly;

    private ZWaveValueSetLevel setLevel;


    //=============================================================================================================
    //======    constructors


    public ZWaveValue(ValueId valueId) {
        this.valueId = valueId;
        this.id = ZWaveValueIndexFactory.createIndex(valueId);
        String zName = Manager.get().getValueLabel(valueId);
        this.valueName = ZWaveDeviceParametersNames.byZWaveUID(this.id, zName);
        this.readOnly = Manager.get().isValueReadOnly(valueId);
    }


    //=============================================================================================================
    //======    getters & setters


    //------------------------------------------------------------------------
    //  z-wave value label

    public String getValueLabel() {
        return Manager.get().getValueLabel(valueId);
    }


    //------------------------------------------------------------------------
    //  gate value id

    public int getId() {
        return id;
    }


    //=============================================================================================================
    //======    inner classes


    //------------------------------------------------------------------------
    //  change value "level" smoothing

    private Object getValue() {
        switch (valueId.getType()) {
            case BOOL:
                AtomicReference<Boolean> b = new AtomicReference<>();
                Manager.get().getValueAsBool(valueId, b);
                return b.get();
            case BYTE:
                AtomicReference<Short> bb = new AtomicReference<>();
                Manager.get().getValueAsByte(valueId, bb);
                return bb.get();
            case DECIMAL:
                AtomicReference<Float> f = new AtomicReference<>();
                Manager.get().getValueAsFloat(valueId, f);
                return f.get();
            case INT:
                AtomicReference<Integer> i = new AtomicReference<>();
                Manager.get().getValueAsInt(valueId, i);
                return i.get();
            case LIST:
                return null;
            case SCHEDULE:
                return null;
            case SHORT:
                AtomicReference<Short> s = new AtomicReference<>();
                Manager.get().getValueAsShort(valueId, s);
                return s.get();
            case STRING:
                AtomicReference<String> ss = new AtomicReference<>();
                Manager.get().getValueAsString(valueId, ss);
                return ss.get();
            case BUTTON:
                return null;
            case RAW:
                AtomicReference<short[]> sss = new AtomicReference<>();
                Manager.get().getValueAsRaw(valueId, sss);
                return sss.get();
            default:
                return null;
        }
    }


    //=============================================================================================================
    //======    methods


    //------------------------------------------------------------------------
    //  get parameter value

    public Boolean getValueAsBool() {
        return Boolean.parseBoolean(getValueAsString());
    }

    public Integer getValueAsInt() {
        return Integer.parseInt(getValueAsString());
    }

    public String getValueAsString() {
        Object obj = getValue();
        return (obj == null) ? "null" : obj.toString();
    }


    //------------------------------------------------------------------------
    //  get parameter value as string

    @Override
    public String toString() {

        return String.format("{\"id\":\"%d\",\"label\":\"%s\",\"value\":\"%s\"}",
                id,
                getValueLabel(),
                getValueAsString()
        );
    }


    //------------------------------------------------------------------------
    //  parameter information as string

    private boolean setLevelInit(String value) {

        // stop previous instance
        if (setLevel != null) {
            setLevel.setWork(false);
        }
        // create new instance
        setLevel = new ZWaveValueSetLevel(Integer.parseInt(value));
        Thread threadSetLevel = new Thread(setLevel);
        threadSetLevel.start();

        return true;
    }


    //------------------------------------------------------------------------
    //  initialize method for smoothing set value "level"

    public boolean setValue(String value) {

        if (this.readOnly) return false;
        if (getValueAsString().equalsIgnoreCase(value)) return false;

        boolean result = false;

        LOG.debug("set parameter ({}) to value ({}) ", new Object[]{
                valueName,
                value
        });

        if (
                valueName == ZWaveDeviceParametersNames.Level ||
                        valueName == ZWaveDeviceParametersNames.LevelRed ||
                        valueName == ZWaveDeviceParametersNames.LevelGreen ||
                        valueName == ZWaveDeviceParametersNames.LevelBlue ||
                        valueName == ZWaveDeviceParametersNames.LevelWhite
                ) {

            // range of level are number 0-99 and 255
            int iValue = Integer.parseInt(value);
            if (iValue <= 0) value = "0";
            else if (iValue >= 99) value = "99";
            // smoothing set for level
            result = setLevelInit(value);

        } else
            result = Manager.get().setValueAsString(valueId, value);

        return result;
    }


    //------------------------------------------------------------------------
    //  set parameter value as string

    public class ZWaveValueSetLevel implements Runnable {

        private boolean work = true;
        private int setLevelJitter = 5;
        private int setLevelJitterTime = 200; //ms
        private int level;

        ZWaveValueSetLevel(int level) {
            this.level = level;
        }

        public void setWork(boolean work) {
            this.work = work;
            synchronized (this) {
                notify();
            }
        }


        // ---- wait handler ----
        private void waitForNotify(long period) {
            try {
                wait(period);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {

            int value = getValueAsInt();
            int multiplier = 1;
            if (value > level) multiplier = -1;

            while (work && (level - value) * multiplier > 0) {
                synchronized (this) {
                    LOG.debug("set color : {}", value);
                    Manager.get().setValueAsString(valueId, String.valueOf(value));
                    value += (setLevelJitter * multiplier);
                    waitForNotify(setLevelJitterTime);
                }
            }

        }

    }
}
