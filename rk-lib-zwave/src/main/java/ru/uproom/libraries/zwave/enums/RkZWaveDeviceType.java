package ru.uproom.libraries.zwave.enums;

import libraries.api.RkLibraryDeviceType;

/**
 * Created by osipenko on 14.01.15.
 */
public enum RkZWaveDeviceType {

    Unknown(0x00, 0x00),

    ControllerPortable(0x01, 0x00),
    ControllerPortableRemote(0x01, 0x01),
    ControllerPortableScene(0x01, 0x02),
    ControllerPortableInstaller(0x01, 0x03),

    ControllerStatic(0x02, 0x00),
    ControllerStaticPc(0x02, 0x01),
    ControllerStaticScene(0x02, 0x02),
    ControllerStaticInstaller(0x02, 0x03),

    AvControlPoint(0x03, 0x00),
    AvControlPointSatelliteReceiver(0x03, 0x04),
    AvControlPointSatelliteReceiverV2(0x03, 0x11),
    AvControlPointDoorbell(0x03, 0x12),

    Display(0x04, 0x00),
    DisplaySimple(0x04, 0x01),

    Thermostat(0x08, 0x00),
    ThermostatHeating(0x08, 0x01),
    ThermostatGeneral(0x08, 0x02),
    ThermostatSetbackSchedule(0x08, 0x03),
    ThermostatSetpoint(0x08, 0x04),
    ThermostatSetback(0x08, 0x05),
    ThermostatGeneralV2(0x08, 0x06),

    WindowCovering(0x09, 0x00),
    WindowCoveringSimple(0x09, 0x01),

    RepeaterSlave(0x0F, 0x00),
    RepeaterSlaveBasic(0x0F, 0x01),

    BinarySwitch(0x10, 0x00),
    BinarySwitchPower(0x10, 0x01),
    BinarySwitchScene(0x10, 0x03),

    MultilevelSwitch(0x11, 0x00),
    MultilevelSwitchPower(0x11, 0x01),
    MultilevelSwitchMotor(0x11, 0x03),
    MultilevelSwitchScene(0x11, 0x04),
    MultilevelSwitchMotorClassA(0x11, 0x05),
    MultilevelSwitchMotorClassB(0x11, 0x06),
    MultilevelSwitchMotorClassC(0x11, 0x07),

    RemoteSwitch(0x12, 0x00),
    RemoteSwitchBinary(0x12, 0x01),
    RemoteSwitchMultilevel(0x12, 0x02),
    RemoteSwitchBinaryToggle(0x12, 0x03),
    RemoteSwitchMultilevelToggle(0x12, 0x04),

    ToggleSwitch(0x13, 0x00),
    ToggleSwitchBinary(0x13, 0x01),
    ToggleSwitchMultilevel(0x13, 0x02),

    ZIpGateway(0x14, 0x00),
    ZIpGatewayTunneling(0x14, 0x01),
    ZIpGatewayAdvanced(0x14, 0x02),

    ZIpNode(0x15, 0x00),
    ZIpNodeTunnelling(0x15, 0x01),
    ZIpNodeAdvanced(0x15, 0x02),

    Ventilation(0x16, 0x00),
    VentilationResidentialHeatRecovery(0x16, 0x01),

    BinarySensor(0x20, 0x00),
    BinarySensorRouting(0x20, 0x01),

    MultilevelSensor(0x21, 0x00),
    MultilevelSensorRouting(0x21, 0x01),

    PulseMeter(0x30, 0x00),
    PulseMeterSimple(0x30, 0x01),

    Meter(0x31, 0x00),
    MeterSimple(0x31, 0x01),

    EntryControl(0x40, 0x00),
    EntryControlDoorLock(0x40, 0x01),
    EntryControlDoorLockAdvanced(0x40, 0x02),
    EntryControlDoorLockSecureKeypad(0x40, 0x03),

    SemiInteroperable(0x50, 0x00),
    SemiInteroperableEnergyProduction(0x50, 0x01),

    AlarmSensor(0xA1, 0x00),
    AlarmSensorRoutingBasic(0xA1, 0x01),
    AlarmSensorRouting(0xA1, 0x02),
    AlarmSensorZensorBasic(0xA1, 0x03),
    AlarmSensorZensor(0xA1, 0x04),
    AlarmSensorZensorAdvanced(0xA1, 0x05),
    AlarmSensorRoutingSmokeBasic(0xA1, 0x06),
    AlarmSensorRoutingSmoke(0xA1, 0x07),
    AlarmSensorRoutingZensorBasic(0xA1, 0x08),
    AlarmSensorRoutingZensor(0xA1, 0x09),
    AlarmSensorRoutingZensorAdvanced(0xA1, 0x0A),

    NonInteroperable(0xFF, 0x00),;

    private int generic;
    private int specific;

    RkZWaveDeviceType(int generic, int specific) {
        this.specific = specific;
        this.generic = generic;
    }

    public static RkZWaveDeviceType getByPattern(int generic, int specific) {

        for (RkZWaveDeviceType value : values()) {
            if (value.getGeneric() == generic && value.getSpecific() == specific)
                return value;
        }

        return Unknown;
    }

    public static RkLibraryDeviceType convertToLibraryFromZWave(RkZWaveDeviceType zwaveType) {
        return RkLibraryDeviceType.valueOf(getByPattern(zwaveType.getGeneric(), 0x00).name());
    }

    public int getGeneric() {
        return generic;
    }

    public int getSpecific() {
        return specific;
    }

}
