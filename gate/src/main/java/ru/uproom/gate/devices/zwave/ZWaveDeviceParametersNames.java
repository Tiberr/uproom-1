package ru.uproom.gate.devices.zwave;

/**
 * contains names of device parameters which not included in z-wave conception
 * <p/>
 * Created by osipenko on 18.09.14.
 */
public enum ZWaveDeviceParametersNames {
    Unknown(0, "", true),
    Basic(2097408, "Basic", true),
    Switch(2425088, "Switch", false),
    Level(2490624, "Level", false),
    Bright(2490625, "Bright", true),
    Dim(2490626, "Dim", false),
    IgnoreStartLevel(2490627, "Ignore Start Level", false),
    StartLevel(2490628, "Start Level", false),
    LevelWhite(2490880, "Level", false),
    LevelRed(2491136, "Level", false),
    LevelGreen(2491392, "Level", false),
    LevelBlue(2491648, "Level", false),
    Level5(2491904, "Level", false),
    SwitchAll(2556160, "Switch All", true),
    Energy(3277056, "Energy", true),
    PreviousReading(3277057, "Previous Reading", true),
    Interval(3277058, "Interval", false),
    Power(3277064, "Power", true),
    Exporting(3277088, "Exporting", true),
    Reset(3277089, "Reset", false),
    TruePeriod(7340289, "True Period", true),
    Color(7340289, "RGB Color", false),
    SendOutBasicCommand(7340290, "Send Out Basic Command", true),
    MeterReportPeriod(7340291, "Meter Report Period", false),
    LibraryVersion(8782080, "Library Version", true),
    ProtocolVersion(8782081, "Protocol Version", true),
    ApplicationVersion(8782082, "Application Version", true);

    private int zwaveCode;
    private String zwaveName;
    private boolean readOnly;

    ZWaveDeviceParametersNames(int zwaveCode, String zwaveName, boolean readOnly) {
        this.zwaveCode = zwaveCode;
        this.zwaveName = zwaveName;
        this.readOnly = readOnly;
    }

    public static ZWaveDeviceParametersNames byZWaveUID(int zwaveCode, String zwaveName) {
        for (ZWaveDeviceParametersNames name : values()) {
            if (zwaveCode == name.getZwaveCode() && zwaveName.equalsIgnoreCase(name.getZwaveName()))
                return name;
        }
        return Unknown;
    }

    public int getZwaveCode() {
        return zwaveCode;
    }

    public String getZwaveName() {
        return zwaveName;
    }

    public boolean isReadOnly() {
        return readOnly;
    }
}
