package ru.uproom.gate.tindenetlib.commands.hub;

/**
 * Created by osipenko on 14.01.15.
 */
public enum TindenetHubCommandID {

    Unknown(0),

    Ping(1),
    GetHubID(3),
    SetHubMode(5),

    NewModuleAdded(11),
    ExistModuleRemoved(13),

    IsModuleActive(51),
    GetModuleProtocolVersion(53),
    GetModuleType(55),
    GetModuleParameter(57),
    SetModuleParameter(59);

    private int code;

    TindenetHubCommandID(int code) {
        this.code = code;
    }

    public static TindenetHubCommandID getByCode(int code) {

        for (TindenetHubCommandID value : values()) {
            if (value.getCode() == code) return value;
        }

        return Unknown;
    }

    public int getCode() {
        return code;
    }

    public String getCodeAsString() {
        return String.format("%d", code);
    }

}
