package ru.uproom.gate.tindenetlib.commands;

/**
 * Created by osipenko on 14.01.15.
 */
public enum TindenetCommandID {

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

    TindenetCommandID(int code) {
        this.code = code;
    }

    public static TindenetCommandID getByCode(int code) {

        for (TindenetCommandID value : values()) {
            if (value.getCode() == code) return value;
        }

        return Unknown;
    }

    public int getCode() {
        return code;
    }

}
