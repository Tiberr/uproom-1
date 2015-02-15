package ru.uproom.gate.localinterface.zwave.enums;

import static ru.uproom.gate.localinterface.zwave.enums.ZWaveMeterType.*;

/**
 * Created by osipenko on 14.01.15.
 */
public enum ZWaveMeterUnits {

    Unknown(ZWaveMeterType.Unknown, 0x00),
    // Electric
    Electric_kWh(Electric, 0x00),
    Electric_VAh(Electric, 0x01),
    Electric_W(Electric, 0x02),
    Electric_pulses(Electric, 0x03),
    Electric_V(Electric, 0x04),
    Electric_A(Electric, 0x05),
    Electric_pf(Electric, 0x06),
    Electric_clowns(Electric, 0x07),
    // Gas
    Gas_m3(Gas, 0x00),
    Gas_ft3(Gas, 0x01),
    Gas_not2(Gas, 0x02),
    Gas_puls(Gas, 0x03),
    Gas_not4(Gas, 0x04),
    Gas_not5(Gas, 0x05),
    Gas_not6(Gas, 0x06),
    Gas_not7(Gas, 0x07),
    // Water
    Water_m3(Water, 0x00),
    Water_ft3(Water, 0x01),
    Water_gall(Water, 0x02),
    Water_pulsW(Water, 0x03),
    Water_not4(Water, 0x04),
    Water_not5(Water, 0x05),
    Water_not6(Water, 0x06),
    Water_not7(Water, 0x07);

    private ZWaveMeterType meterType;
    private byte index;

    ZWaveMeterUnits(ZWaveMeterType meterType, int index) {
        this.index = (byte) index;
        this.meterType = meterType;
    }

    public static ZWaveMeterUnits getByIndex(ZWaveMeterType meterType, byte index) {

        for (ZWaveMeterUnits value : values()) {
            if (value.getMeterType() == meterType && value.getIndex() == index) return value;
        }

        return Unknown;
    }

    public byte getIndex() {
        return index;
    }

    public ZWaveMeterType getMeterType() {
        return meterType;
    }

}
