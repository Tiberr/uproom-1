package ru.uproom.libraries.zwave.driver;

import ru.uproom.libraries.zwave.commands.RkZWaveCommandClassFactory;
import ru.uproom.libraries.zwave.devices.RkZWaveDevicePool;
import ru.uproom.libraries.zwave.enums.RkZWaveFunctionID;

import java.util.Properties;

/**
 * Created by osipenko on 11.01.15.
 */
public interface RkZWaveDriver {

    public void create();

    public void destroy();

    public Properties getProperties();

    public void setDevicePool(RkZWaveDevicePool devicePool);

    public RkZWaveCommandClassFactory getCommandClassFactory();

    public void addMessageToSendingQueue(RkZWaveMessage message);

    public void addMessageToReceivingQueue(int[] data);

    public void currentRequestReceived(RkZWaveFunctionID functionID);

    public void receiveAcknowledge();

    public void receiveCancel();

    public void receiveNotAcknowledge();

    public void applyPortState(boolean open);

    public void setDriverReady(boolean ready);

    public void setSucNodeId(int nodeId);

    public void setControllerCapabilitiesFlag(int flags);

    public void setControllerLibraryVersion(String version);

    public void setSerialApiVersion(int lower, int upper);

    public void setControllerProductInfo(int manufacturerId, int productType, int productId);

    public void setControllerApiMask(int[] apiMask);

    public void setControllerSerialApiInfo(int version, int flags);

}
