package libraries.api;

/**
 * name list of all parameters that can be used in some libraries
 * <p/>
 * Created by osipenko on 18.09.14.
 */
public enum RkLibraryDeviceParameterName {

    // Parameter Unknown
    Unknown,

    // Basic
    Basic,

    // BinarySwitch
    Switch,

    // MultilevelSwitch
    Level,
    Color,
    Bright,
    Dim,
    IgnoreStartLevel,
    StartLevel,
    DimmingDuration,
    StepSize,
    Inc,
    Dec,

    // SwitchAll
    SwitchAll,

    // Meter
    Exporting,
    Reset,

    EnergyW,
    EnergyVA,
    Power,
    Count,
    Voltage,
    Current,
    PowerFactor,
    DarkPower,

    Gas,

    Water,

    // Configuration
    TruePeriod,
    ColorConfig,
    SendOutBasicCommand,
    MeterReportPeriod,

    // Version
    LibraryVersion,
    ProtocolVersion,
    ApplicationVersion,;

}
