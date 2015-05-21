package ru.uproom.libraries.zwave.enums;

/**
 * Created by osipenko on 14.01.15.
 */
public enum RkZWaveDeviceManufacturer {

    Unknown(0x00, 0x00, 0x00),

    m_2B_Electronics(0x0040, 0x0000, 0x0000),

    m_2GIG_Technologies(0x0098, 0x0000, 0x0000),
    m_2GIG_Technologies_p_CT31_Thermostat(0x0098, 0x1E12, 0x015C),
    m_2GIG_Technologies_p_CT32_Thermostat(0x0098, 0x1E12, 0x015E),
    m_2GIG_Technologies_p_CT100_Thermostat(0x0098, 0x6401, 0x0105),
    m_2GIG_Technologies_p_CT101_Thermostat(0x0098, 0x6401, 0x0107),
    m_2GIG_Technologies_p_CT102_Thermostat(0x0098, 0x6501, 0x000C),

    m_3E_Technologies(0x002A, 0x0000, 0x0000),

    m_A1_Components(0x0022, 0x0000, 0x0000),

    m_ACT(0x0001, 0x0000, 0x0000),
    m_ACT_p_ZCS101_Serial_Interface(0x0001, 0x4349, 0x3130),
    m_ACT_p_ZIR000_PIR_Motion_Sensor(0x0001, 0x4952, 0x3030),
    m_ACT_p_ZIR010_PIR_Motion_Sensor(0x0001, 0x4952, 0x3330),
    m_ACT_p_ZIR011_PIR_Motion_Sensor(0x0001, 0x4952, 0x3130),
    m_ACT_p_ZDP100_Plugin_Lamp_Module(0x0001, 0x4450, 0x3030),
    m_ACT_p_ZDW103_Wall_Dimmer_Module(0x0001, 0x4457, 0x3033),
    m_ACT_p_ZDW230_Wall_Dimmer_Module(0x0001, 0x4457, 0x3330),
    m_ACT_p_ZDW232_Wall_Dimmer_Module(0x0001, 0x4457, 0x3332),
    m_ACT_p_ZDM230_Wall_Dimmer_Module(0x0001, 0x444D, 0x3330),
    m_ACT_p_ZRP100_Plugin_Appliance_Module(0x0001, 0x5250, 0x3030),
    m_ACT_p_ZRP110_Exterior_Appliance_Module(0x0001, 0x5250, 0x3130),
    m_ACT_p_ZRW230_Wall_Appliance_Module(0x0001, 0x5257, 0x3330),
    m_ACT_p_LFM20_Relay_Fixture_Module(0x0001, 0x5246, 0x3133),
    m_ACT_p_ZRW103_Wall_Switch_Module(0x0001, 0x5257, 0x3033),
    m_ACT_p_ZRM230_Wall_Appliance_Module(0x0001, 0x524D, 0x3330),
    m_ACT_p_ZTW231_Wall_Transmitter_Module(0x0001, 0x5457, 0x3330),
    m_ACT_p_ZTW232_Wall_Transmitter_Module(0x0001, 0x544D, 0x3330),

    m_Aeon_Labs(0x0086, 0x0000, 0x0000),
    m_Aeon_Labs_p_Z_Stick(0x0086, 0x0001, 0x0001),
    m_Aeon_Labs_p_Z_Stick_S2(0x0086, 0x0001, 0x0002),
    m_Aeon_Labs_p_Minimote(0x0086, 0x0001, 0x0003),
    m_Aeon_Labs_p_Z_Stick_S2_Lite(0x0086, 0x0001, 0x0007),
    m_Aeon_Labs_p_Key_Fob(0x0086, 0x0001, 0x0016),
    m_Aeon_Labs_p_Z_Stick_Gen5(0x0086, 0x0001, 0x005A),
    m_Aeon_Labs_p_Z_Stick_Gen5_Lite(0x0086, 0x0001, 0x005C),
    m_Aeon_Labs_p_Z_Stick_S3(0x0086, 0x0002, 0x0001),
    m_Aeon_Labs_p_Door_Sensor(0x0086, 0x0002, 0x0004),
    m_Aeon_Labs_p_Multi_Sensor(0x0086, 0x0002, 0x0005),
    m_Aeon_Labs_p_Home_Energy_Meter(0x0086, 0x0002, 0x0009),
    m_Aeon_Labs_p_Home_Energy_Meter_G2(0x0086, 0x0002, 0x001C),
    m_Aeon_Labs_p_Door_Sensor_G2(0x0086, 0x0002, 0x001D),
    m_Aeon_Labs_p_Door_Sensor_Recessed(0x0086, 0x0002, 0x0036),
    m_Aeon_Labs_p_Smart_Energy_Switch(0x0086, 0x0003, 0x0006),
    m_Aeon_Labs_p_Smart_Energy_Illuminator(0x0086, 0x0003, 0x0006),
    m_Aeon_Labs_p_220V_Utility_Switch(0x0086, 0x0003, 0x000A),
    m_Aeon_Labs_p_Smart_Energy_Strip(0x0086, 0x0003, 0x000B),
    m_Aeon_Labs_p_Micro_Smart_Energy_Switch(0x0086, 0x0003, 0x000C),
    m_Aeon_Labs_p_Micro_Smart_Energy_Illuminator(0x0086, 0x0003, 0x000D),
    m_Aeon_Labs_p_Micro_Motor_Controller(0x0086, 0x0003, 0x000E),
    m_Aeon_Labs_p_Micro_Double_Switch(0x0086, 0x0003, 0x0011),
    m_Aeon_Labs_p_Micro_Switch_G2(0x0086, 0x0003, 0x001A),
    m_Aeon_Labs_p_Micro_Smart_Energy_Illuminator_G2(0x0086, 0x0003, 0x001B),
    m_Aeon_Labs_p_Micro_Smart_Energy_Switch_G2(0x0086, 0x0003, 0x0012),
    m_Aeon_Labs_p_Micro_Smart_Energy_Dimmer(0x0086, 0x0003, 0x0013),
    m_Aeon_Labs_p_Smart_Energy_Switch_v2(0x0086, 0x0003, 0x004B),
    m_Aeon_Labs_p_ZWave_Repeater(0x0086, 0x0004, 0x0025),
    m_Aeon_Labs_p_DSD31_Siren_Gen5(0x0086, 0x0004, 0x0050),

    //todo : finish this is ungrateful job tomorrow

    ;

    private int manufacturer;
    private int type;
    private int id;

    RkZWaveDeviceManufacturer(int manufacturer, int type, int id) {
        this.manufacturer = manufacturer;
        this.type = type;
        this.id = id;
    }

    public static RkZWaveDeviceManufacturer getByPattern(int manufacturer, int type, int id) {

        for (RkZWaveDeviceManufacturer value : values()) {
            if (value.getManufacturer() == manufacturer
                    && value.getType() == type && value.getId() == id)
                return value;
        }

        return Unknown;
    }

    public int getManufacturer() {
        return manufacturer;
    }

    public int getType() {
        return type;
    }

    public int getId() {
        return id;
    }

}
