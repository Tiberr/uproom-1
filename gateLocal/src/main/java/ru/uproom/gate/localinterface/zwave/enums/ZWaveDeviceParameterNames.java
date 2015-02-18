package ru.uproom.gate.localinterface.zwave.enums;

/**
 * contains names of device parameters which not included in z-wave conception
 * <p/>
 * Created by osipenko on 18.09.14.
 */
public enum ZWaveDeviceParameterNames {

    // NoOperation
    Unknown(ZWaveCommandClassNames.NoOperation, 0x00, 0x00, 0x00),

    // Basic
    Basic(ZWaveCommandClassNames.Basic, 0x01, 0x00, 0x00),

    // SwitchBinary
    Switch(ZWaveCommandClassNames.SwitchBinary, 0x01, 0x00, 0x00),

    // SwitchMultilevel
    Level(ZWaveCommandClassNames.SwitchMultilevel, 0x01, 0x00, 0x00),
    Bright(ZWaveCommandClassNames.SwitchMultilevel, 0x01, 0x00, 0x01),
    Dim(ZWaveCommandClassNames.SwitchMultilevel, 0x01, 0x00, 0x02),
    IgnoreStartLevel(ZWaveCommandClassNames.SwitchMultilevel, 0x01, 0x00, 0x03),
    StartLevel(ZWaveCommandClassNames.SwitchMultilevel, 0x01, 0x00, 0x04),
    LevelWhite(ZWaveCommandClassNames.SwitchMultilevel, 0x02, 0x00, 0x00),
    LevelRed(ZWaveCommandClassNames.SwitchMultilevel, 0x03, 0x00, 0x00),
    LevelGreen(ZWaveCommandClassNames.SwitchMultilevel, 0x04, 0x00, 0x00),
    LevelBlue(ZWaveCommandClassNames.SwitchMultilevel, 0x05, 0x00, 0x00),
    Level5(ZWaveCommandClassNames.SwitchMultilevel, 0x06, 0x00, 0x00),

    // SwitchAll
    SwitchAll(ZWaveCommandClassNames.SwitchAll, 0x01, 0x00, 0x00),

    // Meter
    Exporting(ZWaveCommandClassNames.Meter, 0x01, ZWaveMeterType.Unknown.getCode(), 0x20),
    Reset(ZWaveCommandClassNames.Meter, 0x01, ZWaveMeterType.Unknown.getCode(), 0x21),

    EnergyW(ZWaveCommandClassNames.Meter, 0x01, ZWaveMeterType.Electric.getCode(), 0x00),
    EnergyVA(ZWaveCommandClassNames.Meter, 0x01, ZWaveMeterType.Electric.getCode(), 0x01),
    Power(ZWaveCommandClassNames.Meter, 0x01, ZWaveMeterType.Electric.getCode(), 0x02),
    Count(ZWaveCommandClassNames.Meter, 0x01, ZWaveMeterType.Electric.getCode(), 0x03),
    Voltage(ZWaveCommandClassNames.Meter, 0x01, ZWaveMeterType.Electric.getCode(), 0x04),
    Current(ZWaveCommandClassNames.Meter, 0x01, ZWaveMeterType.Electric.getCode(), 0x05),
    PowerFactor(ZWaveCommandClassNames.Meter, 0x01, ZWaveMeterType.Electric.getCode(), 0x06),
    DarkPower(ZWaveCommandClassNames.Meter, 0x01, ZWaveMeterType.Electric.getCode(), 0x07),

    Gas(ZWaveCommandClassNames.Meter, 0x01, ZWaveMeterType.Gas.getCode(), 0x00),

    Water(ZWaveCommandClassNames.Meter, 0x01, ZWaveMeterType.Water.getCode(), 0x00),

    // Configuration
    TruePeriod(ZWaveCommandClassNames.Configuration, 0x01, 0x00, 0x01),
    Color(ZWaveCommandClassNames.Configuration, 0x01, 0x00, 0x01),
    SendOutBasicCommand(ZWaveCommandClassNames.Configuration, 0x01, 0x00, 0x02),
    MeterReportPeriod(ZWaveCommandClassNames.Configuration, 0x01, 0x00, 0x03),

    // Version
    LibraryVersion(ZWaveCommandClassNames.Version, 0x01, 0x00, 0x00),
    ProtocolVersion(ZWaveCommandClassNames.Version, 0x01, 0x00, 0x01),
    ApplicationVersion(ZWaveCommandClassNames.Version, 0x01, 0x00, 0x02);


    private int parameterId;

    ZWaveDeviceParameterNames(ZWaveCommandClassNames commandClasses, int instance, int type, int index) {
        this.parameterId = fetchParameterId(commandClasses, instance, type, index);
    }

    public static ZWaveDeviceParameterNames byParameterId(int parameterId) {
        for (ZWaveDeviceParameterNames name : values()) {
            if (parameterId == name.getParameterId())
                return name;
        }
        return Unknown;
    }

    public static ZWaveDeviceParameterNames byParameterProperties(
            ZWaveCommandClassNames commandClass, int instance, int type, int index) {
        int parameterId = fetchParameterId(commandClass, instance, type, index);
        for (ZWaveDeviceParameterNames name : values()) {
            if (parameterId == name.getParameterId())
                return name;
        }
        return Unknown;
    }

    public static int fetchParameterId(ZWaveCommandClassNames commandClass, int instance, int type, int index) {
        // return = 0xAABBCCDD, AA = command class, BB = instance, CC = type, DD = index
        return (commandClass.getCode() << 24) | (instance << 16) | (type << 8) | index;
    }

    public String getParameterIdToHex() {
        return String.format(" 0x%08X", parameterId);
    }

    public int getParameterId() {
        return parameterId;
    }

    public byte getInstance() {
        return (byte) ((parameterId & 0x00FF0000) >> 16);
    }

    public ZWaveDeviceParameterNames getInstanceName(byte instance) {
        ZWaveDeviceParameterNames parameterName
                = byParameterId(parameterId & 0xFF00FFFF | (instance << 16));
        return (parameterName != Unknown) ? parameterName : this;
    }

    public ZWaveDeviceParameterNames getBaseName() {
        ZWaveDeviceParameterNames parameterName = byParameterId(parameterId & 0xFF01FFFF);
        return (parameterName != Unknown) ? parameterName : this;
    }
}
