package ru.uproom.gate.localinterface.zwave.enums;

/**
 * contains names of device parameters which not included in z-wave conception
 * <p/>
 * Created by osipenko on 18.09.14.
 */
public enum ZWaveDeviceParameterNames {

    Unknown(ZWaveCommandClassNames.NoOperation, 0x00, 0x00),

    Basic(ZWaveCommandClassNames.Basic, 0x01, 0x00),

    Switch(ZWaveCommandClassNames.SwitchBinary, 0x01, 0x00),

    Level(ZWaveCommandClassNames.SwitchMultilevel, 0x01, 0x00),
    Bright(ZWaveCommandClassNames.SwitchMultilevel, 0x01, 0x01),
    Dim(ZWaveCommandClassNames.SwitchMultilevel, 0x01, 0x02),
    IgnoreStartLevel(ZWaveCommandClassNames.SwitchMultilevel, 0x01, 0x03),
    StartLevel(ZWaveCommandClassNames.SwitchMultilevel, 0x01, 0x04),
    LevelWhite(ZWaveCommandClassNames.SwitchMultilevel, 0x02, 0x00),
    LevelRed(ZWaveCommandClassNames.SwitchMultilevel, 0x03, 0x00),
    LevelGreen(ZWaveCommandClassNames.SwitchMultilevel, 0x04, 0x00),
    LevelBlue(ZWaveCommandClassNames.SwitchMultilevel, 0x05, 0x00),
    Level5(ZWaveCommandClassNames.SwitchMultilevel, 0x06, 0x00),

    SwitchAll(ZWaveCommandClassNames.SwitchAll, 0x01, 0x00),

    Energy(ZWaveCommandClassNames.Meter, 0x01, 0x00),
    PreviousReading(ZWaveCommandClassNames.Meter, 0x01, 0x01),
    Interval(ZWaveCommandClassNames.Meter, 0x01, 0x02),
    Power(ZWaveCommandClassNames.Meter, 0x01, 0x08),
    Exporting(ZWaveCommandClassNames.Meter, 0x01, 0x20),
    Reset(ZWaveCommandClassNames.Meter, 0x01, 0x21),

    TruePeriod(ZWaveCommandClassNames.Configuration, 0x01, 0x01),
    Color(ZWaveCommandClassNames.Configuration, 0x01, 0x01),
    SendOutBasicCommand(ZWaveCommandClassNames.Configuration, 0x01, 0x02),
    MeterReportPeriod(ZWaveCommandClassNames.Configuration, 0x01, 0x03),

    LibraryVersion(ZWaveCommandClassNames.Version, 0x01, 0x00),
    ProtocolVersion(ZWaveCommandClassNames.Version, 0x01, 0x01),
    ApplicationVersion(ZWaveCommandClassNames.Version, 0x01, 0x02);

    private int parameterId;

    ZWaveDeviceParameterNames(ZWaveCommandClassNames commandClasses, int instance, int index) {
        this.parameterId = (commandClasses.getCode() << 16) | (instance << 8) | index;
    }

    public static ZWaveDeviceParameterNames byParameterId(int parameterId) {
        for (ZWaveDeviceParameterNames name : values()) {
            if (parameterId == name.getParameterId())
                return name;
        }
        return Unknown;
    }

    public static ZWaveDeviceParameterNames byParameterProperties(
            ZWaveCommandClassNames commandClass, int instance, int index) {
        int parameterId = fetchParameterId(commandClass, instance, index);
        for (ZWaveDeviceParameterNames name : values()) {
            if (parameterId == name.getParameterId())
                return name;
        }
        return Unknown;
    }

    public static int fetchParameterId(ZWaveCommandClassNames commandClass, int instance, int index) {
        // return = 0xAABBCC, AA = command class, BB = instance, CC = index
        return (commandClass.getCode() << 16) | (instance << 8) | index;
    }

    public int getParameterId() {
        return parameterId;
    }
}
