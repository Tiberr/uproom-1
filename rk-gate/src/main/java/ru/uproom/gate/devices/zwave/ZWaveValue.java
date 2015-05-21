package ru.uproom.gate.devices.zwave;

import libraries.auxilliary.RunnableClass;
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

    private ZWaveValueSetLevel setLevel = null;
    private int setLevelJitterTime = 200; //ms


    //=============================================================================================================
    //======    constructors


    public ZWaveValue(ValueId valueId) {
        this.valueId = valueId;
        this.id = ZWaveValueIndexFactory.createIndex(valueId);
        String zName = Manager.get().getValueLabel(valueId);
        this.valueName = ZWaveDeviceParametersNames.byZWaveUID(this.id, zName);
        this.readOnly = Manager.get().isValueReadOnly(valueId);

        if (
                valueName == ZWaveDeviceParametersNames.Level ||
                        valueName == ZWaveDeviceParametersNames.LevelRed ||
                        valueName == ZWaveDeviceParametersNames.LevelGreen ||
                        valueName == ZWaveDeviceParametersNames.LevelBlue ||
                        valueName == ZWaveDeviceParametersNames.LevelWhite
                ) {
            setLevel = new ZWaveValueSetLevel();
            setLevel.setTimeout(setLevelJitterTime);
            Thread threadSetLevel = new Thread(setLevel);
            threadSetLevel.start();
        }
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

        if (setLevel != null) {

            // range of level are number 0-99 and 255
            int iValue = Integer.parseInt(value);
            if (iValue <= 0) value = "0";
            else if (iValue >= 99) value = "99";
            // smoothing set for level
            setLevel.applyLevel(Integer.parseInt(value));

        } else
            result = Manager.get().setValueAsString(valueId, value);

        return result;
    }


    //------------------------------------------------------------------------
    //  set parameter value as string


    //=============================================================================================================
    //======    inner classes


    public class ZWaveValueSetLevel extends RunnableClass {

        private int setLevelJitter = 15;
        private int level;
        private int value;
        private int multiplier;
        private boolean work;

        public void applyLevel(int level) {
            this.level = level;
            this.value = getValueAsInt();
            if (value > level) multiplier = -1;
            else multiplier = 1;
            work = true;
        }

        @Override
        public void body() {

            if (!work) return;

            if ((level - value) * multiplier > setLevelJitter) {
                value += (setLevelJitter * multiplier);
                LOG.debug("{} set middle color : value = {}; level = {}",
                        new Object[]{valueName, value, level});
            } else {
                value = level;
                LOG.debug("{} set end color : value = {}; level = {}",
                        new Object[]{valueName, value, level});
                work = false;
            }

            Manager.get().setValueAsString(valueId, String.valueOf(value));
        }

    }
}
