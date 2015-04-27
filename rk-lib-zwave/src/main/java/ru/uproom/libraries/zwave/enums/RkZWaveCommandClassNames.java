package ru.uproom.libraries.zwave.enums;

/**
 * Return in Z-Wave Function GET_CONTROLLER_CAPABILITIES
 * <p/>
 * Created by osipenko on 16.01.15.
 */
public enum RkZWaveCommandClassNames {

    NoOperation(0x00),
    Alarm(0x71),
    ApplicationStatus(0x22),
    Association(0x85),
    AssociationCommandConfiguration(0x9B),
    Basic(0x20),
    BasicWindowCovering(0x50),
    Battery(0x80),
    ClimateControlSchedule(0x46),
    Clock(0x81),
    ColorControl(0x33), // command ColorSet = 0x05
    Configuration(0x70),
    ControllerReplication(0x21),
    CRC16Encap(0x56),
    EnergyProduction(0x90),
    Hail(0x82),
    Indicator(0x87),
    Language(0x89),
    Lock(0x76),
    ManufacturerSpecific(0x72),
    Meter(0x32),
    MeterPulse(0x35),
    MultiCommand(0x8F),
    MultiInstance(0x60),
    MultiInstanceAssociation(0x8E),
    NodeNaming(0x77),
    PowerLevel(0x73),
    Proprietary(0x88),
    Protection(0x75),
    SceneActivation(0x2B),
    SensorAlarm(0x9C),
    SensorBinary(0x30),
    SensorMultilevel(0x31),
    SwitchAll(0x27),
    SwitchBinary(0x25),
    SwitchMultilevel(0x26),
    SwitchToggleBinary(0x28),
    SwitchToggleMultilevel(0x29),
    ThermostatFanMode(0x44),
    ThermostatFanState(0x45),
    ThermostatMode(0x40),
    ThermostatOperatingState(0x42),
    ThermostatSetpoint(0x43),
    UserCode(0x63),
    Version(0x86),
    WakeUp(0x84);

    private int code;

    RkZWaveCommandClassNames(int code) {
        this.code = code;
    }

    public static RkZWaveCommandClassNames getByCode(int code) {

        for (RkZWaveCommandClassNames value : values()) {
            if (value.getCode() == code) return value;
        }

        return NoOperation;
    }

    public int getCode() {
        return code;
    }

}
