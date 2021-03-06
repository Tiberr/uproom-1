package ru.uproom.libraries.zwave.enums;

/**
 * Created by osipenko on 14.01.15.
 */
public enum RkZWaveFunctionID {

    UNKNOWN(0x00),
    SERIAL_API_GET_INIT_DATA(0x02),
    SERIAL_API_APPLY_NODE_INFORMATION(0x03),
    APPLICATION_COMMAND(0x04),
    GET_CONTROLLER_CAPABILITIES(0x05),
    SERIAL_API_SET_TIMEOUTS(0x06),
    SERIAL_API_GET_CAPABILITIES(0x07),
    SERIAL_API_SOFT_RESET(0x08),
    SEND_NODE_INFORMATION(0x12),
    SEND_DATA(0x13),
    GET_VERSION(0x15),
    R_F_POWER_LEVEL_SET(0x17),
    GET_RANDOM(0x1C),
    MEMORY_GET_ID(0x20),
    MEMORY_GET_BYTE(0x21),
    READ_MEMORY(0x23),
    SET_LEARN_NODE_STATE(0x40),
    GET_NODE_PROTOCOL_INFO(0x41),
    SET_DEFAULT(0x42),
    NEW_CONTROLLER(0x43),
    REPLICATION_COMMAND_COMPLETE(0x44),
    REPLICATION_SEND_DATA(0x45),
    ASSIGN_RETURN_ROUTE(0x46),
    DELETE_RETURN_ROUTE(0x47),
    REQUEST_NODE_NEIGHBOR_UPDATE(0x48),
    APPLICATION_UPDATE(0x49),
    ADD_NODE_TO_NETWORK(0x4A),
    REMOVE_NODE_FROM_NETWORK(0x4B),
    CREATE_NEW_PRIMARY(0x4C),
    CONTROLLER_CHANGE(0x4D),
    SET_LEARN_MODE(0x50),
    ASSIGN_SUC_RETURN_ROUTE(0x51),
    ENABLE_SUC(0x52),
    REQUEST_NETWORK_UPDATE(0x53),
    SET_SUC_NODE_ID(0x54),
    DELETE_SUC_RETURN_ROUTE(0x55),
    GET_SUC_NODE_ID(0x56),
    REQUEST_NODE_NEIGHBOR_UPDATE_OPTIONS(0x5A),
    REQUEST_NODE_INFO(0x60),
    REMOVE_FAILED_NODE_ID(0x61),
    IS_FAILED_NODE_ID(0x62),
    REPLACE_FAILED_NODE(0x63),
    GET_ROUTING_INFO(0x80),
    SERIAL_API_SLAVE_NODE_INFO(0xA0),
    APPLICATION_SLAVE_COMMAND_HANDLER(0xA1),
    SEND_SLAVE_NODE_INFO(0xA2),
    SEND_SLAVE_DATA(0xA3),
    SET_SLAVE_LEARN_MODE(0xA4),
    GET_VIRTUAL_NODES(0xA5),
    IS_VIRTUAL_NODE(0xA6),
    SET_PROMISCUOUS_MODE(0xD0),
    PROMISCUOUS_APPLICATION_COMMAND_HANDLER(0xD1);

    private int code;

    RkZWaveFunctionID(int code) {
        this.code = code;
    }

    public static RkZWaveFunctionID getByCode(int code) {

        for (RkZWaveFunctionID value : values()) {
            if (value.getCode() == code) return value;
        }

        return UNKNOWN;
    }

    public int getCode() {
        return code;
    }

}
