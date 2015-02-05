package ru.uproom.gate.localinterface.zwave.enums;

/**
 * contains names of device parameters which not included in z-wave conception
 * <p/>
 * Created by osipenko on 18.09.14.
 */
public enum ZWaveDeviceParameterNames {

    Unknown(ZWaveCommandClasses.NoOperation, 0x00, 0x00),

    Basic(ZWaveCommandClasses.Basic, 0x01, 0x00),

    Switch(ZWaveCommandClasses.SwitchBinary, 0x01, 0x00),

    Level(ZWaveCommandClasses.SwitchMultilevel, 0x01, 0x00),
    Bright(ZWaveCommandClasses.SwitchMultilevel, 0x01, 0x01),
    Dim(ZWaveCommandClasses.SwitchMultilevel, 0x01, 0x02),
    IgnoreStartLevel(ZWaveCommandClasses.SwitchMultilevel, 0x01, 0x03),
    StartLevel(ZWaveCommandClasses.SwitchMultilevel, 0x01, 0x04),
    LevelWhite(ZWaveCommandClasses.SwitchMultilevel, 0x02, 0x00),
    LevelRed(ZWaveCommandClasses.SwitchMultilevel, 0x03, 0x00),
    LevelGreen(ZWaveCommandClasses.SwitchMultilevel, 0x04, 0x00),
    LevelBlue(ZWaveCommandClasses.SwitchMultilevel, 0x05, 0x00),
    Level5(ZWaveCommandClasses.SwitchMultilevel, 0x06, 0x00),

    SwitchAll(ZWaveCommandClasses.SwitchAll, 0x01, 0x00),

    Energy(ZWaveCommandClasses.Meter, 0x01, 0x00),
    PreviousReading(ZWaveCommandClasses.Meter, 0x01, 0x01),
    Interval(ZWaveCommandClasses.Meter, 0x01, 0x02),
    Power(ZWaveCommandClasses.Meter, 0x01, 0x08),
    Exporting(ZWaveCommandClasses.Meter, 0x01, 0x20),
    Reset(ZWaveCommandClasses.Meter, 0x01, 0x21),

    TruePeriod(ZWaveCommandClasses.Configuration, 0x01, 0x01),
    Color(ZWaveCommandClasses.Configuration, 0x01, 0x01),
    SendOutBasicCommand(ZWaveCommandClasses.Configuration, 0x01, 0x02),
    MeterReportPeriod(ZWaveCommandClasses.Configuration, 0x01, 0x03),

    LibraryVersion(ZWaveCommandClasses.Version, 0x01, 0x00),
    ProtocolVersion(ZWaveCommandClasses.Version, 0x01, 0x01),
    ApplicationVersion(ZWaveCommandClasses.Version, 0x01, 0x02);

    private int parameterId;

    ZWaveDeviceParameterNames(ZWaveCommandClasses commandClasses, int instance, int index) {
        this.parameterId = (commandClasses.getCode() << 16) | (instance << 8) | index;
    }

    public static ZWaveDeviceParameterNames byParameterId(int parameterId) {
        for (ZWaveDeviceParameterNames name : values()) {
            if (parameterId == name.getParameterId())
                return name;
        }
        return Unknown;
    }

    public static int fetchParameterId(ZWaveCommandClasses commandClass, int instance, int index) {
        // return = 0xAABBCC, AA = command class, BB = instance, CC = index
        return (commandClass.getCode() << 16) | (instance << 8) | index;
    }

    public int getParameterId() {
        return parameterId;
    }
}
