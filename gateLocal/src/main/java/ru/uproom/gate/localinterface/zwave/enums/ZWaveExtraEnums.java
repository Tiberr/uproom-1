package ru.uproom.gate.localinterface.zwave.enums;

/**
 * Created by osipenko on 19.01.15.
 */
public class ZWaveExtraEnums {

    public static final byte NODES_BITFIELD_LENGTH_IN_BYTES = 29;

    public static final int ACK_TIMEOUT = 1000;
    public static final int BYTE_TIMEOUT = 150;

    public static final byte APPLICATION_NODEINFO_LISTENING = 0x01;

    public static final byte END_OF_LIST_SUPPORTED_COMMAND_CLASS_MARK = (byte) 0xEF;

    public static final String[] libraryTypeNames = {
            "Unknown",
            "Static Controller",
            "Controller",
            "Enhanced Slave",
            "Slave",
            "Installer",
            "Routing Slave",
            "Bridge Controller",
            "Device Under Test"
    };

    public static final String[] controllerCommandNames = {
            "None",
            "Add Device",
            "Create New Primary",
            "Receive Configuration",
            "Remove Device",
            "Remove Failed Node",
            "Has Node Failed",
            "Replace Failed Node",
            "Transfer Primary Role",
            "Request Network Update",
            "Request Node Neighbor Update",
            "Assign Return Route",
            "Delete All Return Routes",
            "Send Node Information",
            "Replication Send",
            "Create Button",
            "Delete Button"
    };

}
