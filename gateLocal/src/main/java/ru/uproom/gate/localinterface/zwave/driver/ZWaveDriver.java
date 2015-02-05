package ru.uproom.gate.localinterface.zwave.driver;

import ru.uproom.gate.localinterface.zwave.enums.ZWaveFunctionID;

/**
 * Created by osipenko on 15.01.15.
 */
public interface ZWaveDriver {

    public ZWaveSerialDataHandler getSerialDataHandler();

    public void setPortState(boolean open);

    public void setDriverReady(boolean ready);

    public void setControllerSerialApiInfo(byte version, byte flags);

    public void setControllerCapabilitiesFlag(byte flags);

    public void setSerialApiVersion(byte lower, byte upper);

    public void setControllerProductInfo(int manufacturerId, int productType, int productId);

    public void setControllerApiMask(byte[] apiMask);

    public void setControllerLibraryVersion(String version);

    public void setControllerLibraryType(byte type);

    public void setSucNodeId(byte nodeId);

    public boolean isBridgeController();

    public boolean isApiCallSupported(ZWaveFunctionID functionID);

    public void deviceMapProcessing(byte[] nodeMap);

    public void currentRequestReceived(ZWaveFunctionID functionID);

}
