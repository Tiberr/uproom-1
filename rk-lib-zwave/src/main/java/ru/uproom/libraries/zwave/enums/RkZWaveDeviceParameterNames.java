package ru.uproom.libraries.zwave.enums;

import libraries.api.RkLibraryDeviceParameterName;

/**
 * contains names of device parameters which not included in z-wave conception
 * <p/>
 * Created by osipenko on 18.09.14.
 */
public enum RkZWaveDeviceParameterNames {

    // NoOperation
    Unknown(RkZWaveCommandClassNames.NoOperation, 0x00, 0x00, 0x00),

    // Basic
    Basic(RkZWaveCommandClassNames.Basic, 0x01, 0x00, 0x00),

    // SwitchBinary
    Switch(RkZWaveCommandClassNames.SwitchBinary, 0x01, 0x00, 0x00),

    // SwitchMultilevel
    Level(RkZWaveCommandClassNames.SwitchMultilevel, 0x01, 0x00, 0x00),
    LevelWhite(RkZWaveCommandClassNames.SwitchMultilevel, 0x02, 0x00, 0x00),
    LevelRed(RkZWaveCommandClassNames.SwitchMultilevel, 0x03, 0x00, 0x00),
    LevelGreen(RkZWaveCommandClassNames.SwitchMultilevel, 0x04, 0x00, 0x00),
    LevelBlue(RkZWaveCommandClassNames.SwitchMultilevel, 0x05, 0x00, 0x00),
    Level5(RkZWaveCommandClassNames.SwitchMultilevel, 0x06, 0x00, 0x00),
    Bright(RkZWaveCommandClassNames.SwitchMultilevel, 0x01, 0x00, 0x01),
    Dim(RkZWaveCommandClassNames.SwitchMultilevel, 0x01, 0x00, 0x02),
    IgnoreStartLevel(RkZWaveCommandClassNames.SwitchMultilevel, 0x01, 0x00, 0x03),
    StartLevel(RkZWaveCommandClassNames.SwitchMultilevel, 0x01, 0x00, 0x04),
    DimmingDuration(RkZWaveCommandClassNames.SwitchMultilevel, 0x01, 0x00, 0x05),
    StepSize(RkZWaveCommandClassNames.SwitchMultilevel, 0x01, 0x00, 0x06),
    Inc(RkZWaveCommandClassNames.SwitchMultilevel, 0x01, 0x00, 0x07),
    Dec(RkZWaveCommandClassNames.SwitchMultilevel, 0x01, 0x00, 0x08),

    // SwitchAll
    SwitchAll(RkZWaveCommandClassNames.SwitchAll, 0x01, 0x00, 0x00),

    // Meter
    Exporting(RkZWaveCommandClassNames.Meter, 0x01, RkZWaveMeterType.Unknown.getCode(), 0x20),
    Reset(RkZWaveCommandClassNames.Meter, 0x01, RkZWaveMeterType.Unknown.getCode(), 0x21),

    EnergyW(RkZWaveCommandClassNames.Meter, 0x01, RkZWaveMeterType.Electric.getCode(), 0x00),
    EnergyVA(RkZWaveCommandClassNames.Meter, 0x01, RkZWaveMeterType.Electric.getCode(), 0x01),
    Power(RkZWaveCommandClassNames.Meter, 0x01, RkZWaveMeterType.Electric.getCode(), 0x02),
    Count(RkZWaveCommandClassNames.Meter, 0x01, RkZWaveMeterType.Electric.getCode(), 0x03),
    Voltage(RkZWaveCommandClassNames.Meter, 0x01, RkZWaveMeterType.Electric.getCode(), 0x04),
    Current(RkZWaveCommandClassNames.Meter, 0x01, RkZWaveMeterType.Electric.getCode(), 0x05),
    PowerFactor(RkZWaveCommandClassNames.Meter, 0x01, RkZWaveMeterType.Electric.getCode(), 0x06),
    DarkPower(RkZWaveCommandClassNames.Meter, 0x01, RkZWaveMeterType.Electric.getCode(), 0x07),

    Gas(RkZWaveCommandClassNames.Meter, 0x01, RkZWaveMeterType.Gas.getCode(), 0x00),

    Water(RkZWaveCommandClassNames.Meter, 0x01, RkZWaveMeterType.Water.getCode(), 0x00),

    // Configuration
    TruePeriod(RkZWaveCommandClassNames.Configuration, 0x01, 0x00, 0x01),
    Color(RkZWaveCommandClassNames.Configuration, 0x01, 0x00, 0x01),
    SendOutBasicCommand(RkZWaveCommandClassNames.Configuration, 0x01, 0x00, 0x02),
    MeterReportPeriod(RkZWaveCommandClassNames.Configuration, 0x01, 0x00, 0x03),

    // Version
    LibraryVersion(RkZWaveCommandClassNames.Version, 0x01, 0x00, 0x00),
    ProtocolVersion(RkZWaveCommandClassNames.Version, 0x01, 0x00, 0x01),
    ApplicationVersion(RkZWaveCommandClassNames.Version, 0x01, 0x00, 0x02);


    private int parameterId;

    RkZWaveDeviceParameterNames(RkZWaveCommandClassNames commandClasses, int instance, int type, int index) {
        this.parameterId = fetchParameterId(commandClasses, instance, type, index);
    }

    public static RkZWaveDeviceParameterNames byParameterId(int parameterId) {
        for (RkZWaveDeviceParameterNames name : values()) {
            if (parameterId == name.getParameterId())
                return name;
        }
        return Unknown;
    }

    public static RkZWaveDeviceParameterNames byParameterProperties(
            RkZWaveCommandClassNames commandClass, int instance, int type, int index) {
        int parameterId = fetchParameterId(commandClass, instance, type, index);
        for (RkZWaveDeviceParameterNames name : values()) {
            if (parameterId == name.getParameterId())
                return name;
        }
        return Unknown;
    }

    public static int fetchParameterId(RkZWaveCommandClassNames commandClass, int instance, int type, int index) {
        // return = 0xAABBCCDD, AA = command class, BB = instance, CC = type, DD = index
        return (commandClass.getCode() << 24) | (instance << 16) | (type << 8) | index;
    }

    // for library api
    public static RkZWaveDeviceParameterNames getConvertZWaveName(RkLibraryDeviceParameterName apiName) {
        return RkZWaveDeviceParameterNames.valueOf(apiName.name());
    }

    public static RkLibraryDeviceParameterName getConvertLibraryName(RkZWaveDeviceParameterNames zWaveName) {
        return RkLibraryDeviceParameterName.valueOf(zWaveName.name());
    }

    public int getParameterId() {
        return parameterId;
    }

    public int getInstance() {
        return (parameterId & 0x00FF0000) >> 16;
    }

    public RkZWaveDeviceParameterNames getInstanceName(int instance) {
        RkZWaveDeviceParameterNames parameterName
                = byParameterId(parameterId & 0xFF00FFFF | (instance << 16));
        return (parameterName != Unknown) ? parameterName : this;
    }

    public RkZWaveDeviceParameterNames getBaseName() {
        RkZWaveDeviceParameterNames parameterName = byParameterId(parameterId & 0xFF01FFFF);
        return (parameterName != Unknown) ? parameterName : this;
    }
}
