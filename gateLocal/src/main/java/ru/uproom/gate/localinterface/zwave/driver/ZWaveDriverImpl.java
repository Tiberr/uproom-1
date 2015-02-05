package ru.uproom.gate.localinterface.zwave.driver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.uproom.gate.localinterface.zwave.devices.ZWaveDevicePool;
import ru.uproom.gate.localinterface.zwave.enums.ZWaveExtraEnums;
import ru.uproom.gate.localinterface.zwave.enums.ZWaveFunctionID;
import ru.uproom.gate.localinterface.zwave.enums.ZWaveMessageTypes;

/**
 * Created by osipenko on 15.01.15.
 */

@Service
public class ZWaveDriverImpl implements ZWaveDriver {


    //##############################################################################################################
    //######    fields

    private static final Logger LOG = LoggerFactory.getLogger(ZWaveDriverImpl.class);

    @Autowired
    private ZWaveDevicePool devicePool;
    @Autowired
    private ZWaveSerialDataHandler serialDataHandler;

    private boolean ready;
    private boolean initSequenceFinished;
    private boolean portOpen;

    private byte[] serialApiVersion = new byte[2];
    private int manufacturerId;
    private int productType;
    private int productId;
    private byte[] apiMask = new byte[32];

    private byte controllerSerialApiVersion;
    private byte controllerCapabilitiesFlags;
    private byte serialApiCapabilitiesFlags;

    private String controllerLibraryVersion = "";
    private String controllerLibraryTypeName = "";
    private byte controllerLibraryType;

    private byte sucNodeId;


    //##############################################################################################################
    //######    methods


    public void initSequence() {

        serialDataHandler.addMessageToSendingQueue(
                new ZWaveMessage(ZWaveMessageTypes.Request, ZWaveFunctionID.GET_VERSION, true));
        serialDataHandler.addMessageToSendingQueue(
                new ZWaveMessage(ZWaveMessageTypes.Request, ZWaveFunctionID.MEMORY_GET_ID, true));
        serialDataHandler.addMessageToSendingQueue(
                new ZWaveMessage(ZWaveMessageTypes.Request, ZWaveFunctionID.GET_CONTROLLER_CAPABILITIES, true));
        serialDataHandler.addMessageToSendingQueue(
                new ZWaveMessage(ZWaveMessageTypes.Request, ZWaveFunctionID.SERIAL_API_GET_CAPABILITIES, true));
        serialDataHandler.addMessageToSendingQueue(
                new ZWaveMessage(ZWaveMessageTypes.Request, ZWaveFunctionID.GET_SUC_NODE_ID, true));

        if (isBridgeController())
            serialDataHandler.addMessageToSendingQueue(
                    new ZWaveMessage(ZWaveMessageTypes.Request, ZWaveFunctionID.GET_VIRTUAL_NODES, false));
        else if (isApiCallSupported(ZWaveFunctionID.GET_RANDOM))
            serialDataHandler.addMessageToSendingQueue(
                    new ZWaveMessage(ZWaveMessageTypes.Request, ZWaveFunctionID.GET_RANDOM, false));
        serialDataHandler.addMessageToSendingQueue(
                new ZWaveMessage(ZWaveMessageTypes.Request, ZWaveFunctionID.SERIAL_API_GET_INIT_DATA, false));

        // todo : think about timeouts
        if (!isBridgeController()) {
            ZWaveMessage message = new ZWaveMessage(
                    ZWaveMessageTypes.Request, ZWaveFunctionID.SERIAL_API_SET_TIMEOUTS, false);
            message.setParameters(new byte[]{
                    (byte) (ZWaveExtraEnums.ACK_TIMEOUT / 10),
                    (byte) ZWaveExtraEnums.BYTE_TIMEOUT
            });
            serialDataHandler.addMessageToSendingQueue(message);
        }

        ZWaveMessage message = new ZWaveMessage(
                ZWaveMessageTypes.Request, ZWaveFunctionID.SERIAL_API_APPLY_NODE_INFORMATION, false);
        message.setParameters(new byte[]{
                ZWaveExtraEnums.APPLICATION_NODEINFO_LISTENING,
                0x02,
                0x01,
                0x01
        });
        serialDataHandler.addMessageToSendingQueue(message);

    }


    @Override
    public ZWaveSerialDataHandler getSerialDataHandler() {
        return serialDataHandler;
    }


    @Override
    public void setPortState(boolean open) {
        if (open && !portOpen) {
            initSequence();
        }
        portOpen = open;
    }


    @Override
    public void setDriverReady(boolean ready) {
        if (!this.ready && ready) ; // driver initialized successfully
        else ; // driver was reset
        this.ready = ready;
    }


    @Override
    public void setControllerSerialApiInfo(byte version, byte flags) {
        controllerSerialApiVersion = version;
        serialApiCapabilitiesFlags = flags;
    }


    @Override
    public void setControllerCapabilitiesFlag(byte flags) {
        controllerCapabilitiesFlags = flags;
    }


    @Override
    public void setSerialApiVersion(byte lower, byte upper) {
        serialApiVersion[0] = lower;
        serialApiVersion[1] = upper;
    }


    @Override
    public void setControllerProductInfo(int manufacturerId, int productType, int productId) {
        this.manufacturerId = manufacturerId;
        this.productType = productType;
        this.productId = productId;
    }


    @Override
    public void setControllerApiMask(byte[] apiMask) {
        this.apiMask = apiMask;
    }


    @Override
    public void setControllerLibraryVersion(String version) {
        this.controllerLibraryVersion = version;
    }


    @Override
    public void setControllerLibraryType(byte type) {
        controllerLibraryType = type;
        if (type < ZWaveExtraEnums.libraryTypeNames.length)
            controllerLibraryTypeName = ZWaveExtraEnums.libraryTypeNames[type];
    }

    @Override
    public void setSucNodeId(byte nodeId) {
        sucNodeId = nodeId;
    }


    @Override
    public boolean isBridgeController() {
        return (controllerLibraryType == 7);
    }


    @Override
    public boolean isApiCallSupported(ZWaveFunctionID functionID) {
        int code = functionID.getCode();
        return (apiMask[(code - 1) >> 3] & (1 << ((code - 1) & 0x07))) != 0;
    }


    @Override
    public void deviceMapProcessing(byte[] deviceMap) {

        for (int i = 0; i < deviceMap.length; i++) {
            for (int j = 0; j < 8; j++) {
                int deviceId = (i * 8) + (j + 1);
                int deviceBit = (int) deviceMap[i] & (0x01 << j);
                if (deviceBit != 0) {
                    devicePool.addNewDevice(deviceId);
                } else {
                    devicePool.removeExistingDevice(deviceId);
                }
            }
        }

        devicePool.applyDeviceSet(true);

    }

    @Override
    public void currentRequestReceived(ZWaveFunctionID functionID) {
        serialDataHandler.currentRequestReceived(functionID);
    }

}
