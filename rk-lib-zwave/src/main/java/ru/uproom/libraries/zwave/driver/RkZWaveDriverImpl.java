package ru.uproom.libraries.zwave.driver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.uproom.libraries.auxilliary.LoggingHelper;
import ru.uproom.libraries.auxilliary.RunnableClass;
import ru.uproom.libraries.zwave.commands.RkZWaveCommandClassFactory;
import ru.uproom.libraries.zwave.commands.RkZWaveCommandClassFactoryImpl;
import ru.uproom.libraries.zwave.devices.RkZWaveDevicePool;
import ru.uproom.libraries.zwave.enums.RkZWaveExtraEnums;
import ru.uproom.libraries.zwave.enums.RkZWaveFunctionID;
import ru.uproom.libraries.zwave.enums.RkZWaveMessageTypes;
import ru.uproom.libraries.zwave.functions.RkZWaveFunctionHandlePool;
import ru.uproom.libraries.zwave.functions.RkZWaveFunctionHandlePoolImpl;

import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

/**
 * Created by osipenko on 11.01.15.
 */
@Service
public class RkZWaveDriverImpl implements RkZWaveDriver {


    //##############################################################################################################
    //######    fields


    private static final Logger LOG = LoggerFactory.getLogger(RkZWaveDriver.class);

    private final List<int[]> receiveMessages = new LinkedList<>();
    private final ReceiveMessagesDispatcher receiveDispatcher = new ReceiveMessagesDispatcher();
    private final List<RkZWaveMessage> sendMessages = new LinkedList<>();
    private final SendMessagesDispatcher sendDispatcher = new SendMessagesDispatcher();
    private final RkZWaveSerialPort serialPort = new RkZWaveSerialPortImpl();
    private final RkZWaveFunctionHandlePool functionHandlePool = new RkZWaveFunctionHandlePoolImpl();
    private final RkZWaveCommandClassFactory commandClassFactory = new RkZWaveCommandClassFactoryImpl();
    private long sendingMessageLifetime;
    private long timeBeforeResend;
    private int maxNumberOfResendMessage;
    private RkZWaveDevicePool devicePool;
    private int[] serialApiVersion = new int[2];
    private int manufacturerId;
    private int productType;
    private int productId;
    private int[] apiMask = new int[32];

    private int controllerSerialApiVersion;
    private int controllerCapabilitiesFlags;
    private int serialApiCapabilitiesFlags;

    private String controllerLibraryVersion = "";
    private String controllerLibraryTypeName = "";
    private int controllerLibraryType;

    private int sucNodeId;

    private boolean ready;


    //##############################################################################################################
    //######    constructors / destructors


    @Override
    public void create() {

        sendingMessageLifetime = Long.parseLong(devicePool.getProperties().getProperty("message_life_time"));
        timeBeforeResend = Long.parseLong(devicePool.getProperties().getProperty("message_resend_time"));
        maxNumberOfResendMessage = Integer.parseInt(devicePool.getProperties().getProperty("message_resend_number"));

        functionHandlePool.create();

        receiveDispatcher.setTimeout(0);
        new Thread(receiveDispatcher).start();
        sendDispatcher.setTimeout(10);
        new Thread(sendDispatcher).start();

        serialPort.open();

    }


    @Override
    public void destroy() {

        serialPort.close();
        receiveDispatcher.stop();
        sendDispatcher.stop();

    }


    //##############################################################################################################
    //######    getters / setters


    @Override
    public void setDevicePool(RkZWaveDevicePool devicePool) {
        this.devicePool = this.devicePool;
    }


    @Override
    public RkZWaveCommandClassFactory getCommandClassFactory() {
        return commandClassFactory;
    }


    @Override
    public Properties getProperties() {
        return devicePool.getProperties();
    }


    //##############################################################################################################
    //######    methods


    //------------------------------------------------------------------------
    //  add message to queue

    @Override
    public void addMessageToSendingQueue(RkZWaveMessage message) {
        synchronized (sendMessages) {
            sendMessages.add(message);
        }
        LOG.debug("SEND MESSAGE : {}",
                LoggingHelper.createHexStringFromIntArray(message.asIntArray()));
    }


    //------------------------------------------------------------------------
    //  get data from serial port

    @Override
    public void addMessageToReceivingQueue(int[] data) {

        synchronized (receiveMessages) {
            receiveMessages.add(data);
        }
        LOG.debug("RECEIVE MESSAGE : {}", LoggingHelper.createHexStringFromIntArray(data));
        synchronized (receiveDispatcher) {
            receiveDispatcher.notify();
        }

    }


    //------------------------------------------------------------------------
    //  have a answer from controller on our current request

    @Override
    public void currentRequestReceived(RkZWaveFunctionID functionID) {
        RkZWaveMessage message;

        synchronized (sendMessages) {
            message = sendMessages.get(0);
        }
        if (message == null) return;
        if (message.getFunctionID() == functionID)
            message.setHaveAnswer(true);

    }


    //------------------------------------------------------------------------

    @Override
    public void receiveAcknowledge() {
        RkZWaveMessage message;

        synchronized (sendMessages) {
            message = sendMessages.get(0);
        }
        if (message != null) message.setAcknowledge(true);

        LOG.debug("ACKNOWLEDGE");
    }


    //------------------------------------------------------------------------

    @Override
    public void receiveCancel() {
        LOG.debug("CANCEL");
    }


    //------------------------------------------------------------------------

    @Override
    public void receiveNotAcknowledge() {
        RkZWaveMessage message;

        synchronized (sendMessages) {
            message = sendMessages.get(0);
        }
        if (message != null) message.setResend(true);

        LOG.debug("NOT ACKNOWLEDGE");
    }


    //------------------------------------------------------------------------

    @Override
    public void applyPortState(boolean open) {
        if (open) initSequence();
    }


    //------------------------------------------------------------------------

    @Override
    public void setDriverReady(boolean ready) {
        if (!this.ready && ready)
            devicePool.setDriverReady(true);
        else ; // driver was reset
        this.ready = ready;
    }


    //------------------------------------------------------------------------
    //  initial sequence for connecting z-wave stick

    public void initSequence() {

        addMessageToSendingQueue(new RkZWaveMessage(
                RkZWaveMessageTypes.Request, RkZWaveFunctionID.GET_VERSION, true));
        addMessageToSendingQueue(new RkZWaveMessage(
                RkZWaveMessageTypes.Request, RkZWaveFunctionID.MEMORY_GET_ID, true));
        addMessageToSendingQueue(new RkZWaveMessage(
                RkZWaveMessageTypes.Request, RkZWaveFunctionID.GET_CONTROLLER_CAPABILITIES, true));
        addMessageToSendingQueue(new RkZWaveMessage(
                RkZWaveMessageTypes.Request, RkZWaveFunctionID.SERIAL_API_GET_CAPABILITIES, true));
        addMessageToSendingQueue(new RkZWaveMessage(
                RkZWaveMessageTypes.Request, RkZWaveFunctionID.GET_SUC_NODE_ID, true));

        if (isBridgeController())
            addMessageToSendingQueue(new RkZWaveMessage(
                    RkZWaveMessageTypes.Request, RkZWaveFunctionID.GET_VIRTUAL_NODES, false));
        else if (isApiCallSupported(RkZWaveFunctionID.GET_RANDOM))
            addMessageToSendingQueue(new RkZWaveMessage(
                    RkZWaveMessageTypes.Request, RkZWaveFunctionID.GET_RANDOM, false));
        addMessageToSendingQueue(new RkZWaveMessage(
                RkZWaveMessageTypes.Request, RkZWaveFunctionID.SERIAL_API_GET_INIT_DATA, false));

        // todo : think about timeouts
        if (!isBridgeController()) {
            RkZWaveMessage message = new RkZWaveMessage(
                    RkZWaveMessageTypes.Request, RkZWaveFunctionID.SERIAL_API_SET_TIMEOUTS, false);
            int[] data = new int[2];
            data[0] = (byte) (RkZWaveExtraEnums.ACK_TIMEOUT / 10);
            data[1] = (byte) RkZWaveExtraEnums.BYTE_TIMEOUT;
            message.setParameters(data);
            addMessageToSendingQueue(message);
        }

        RkZWaveMessage message = new RkZWaveMessage(
                RkZWaveMessageTypes.Request, RkZWaveFunctionID.SERIAL_API_APPLY_NODE_INFORMATION, false);
        int[] data = new int[4];
        data[0] = RkZWaveExtraEnums.APPLICATION_NODEINFO_LISTENING;
        data[1] = 0x02;
        data[1] = 0x01;
        data[1] = 0x01;
        message.setParameters(data);
        addMessageToSendingQueue(message);
    }


    public boolean isBridgeController() {
        return (controllerLibraryType == 7);
    }


    public boolean isApiCallSupported(RkZWaveFunctionID functionID) {
        int code = functionID.getCode();
        return (apiMask[(code - 1) >> 3] & (1 << ((code - 1) & 0x07))) != 0;
    }


    //------------------------------------------------------------------------

    @Override
    public void setSucNodeId(int nodeId) {
        sucNodeId = nodeId;
    }


    //------------------------------------------------------------------------

    @Override
    public void setControllerCapabilitiesFlag(int flags) {
        controllerCapabilitiesFlags = flags;
    }


    //------------------------------------------------------------------------

    @Override
    public void setControllerLibraryVersion(String version) {
        this.controllerLibraryVersion = version;
    }


    //------------------------------------------------------------------------

    @Override
    public void setSerialApiVersion(int lower, int upper) {
        serialApiVersion[0] = lower;
        serialApiVersion[1] = upper;
    }


    //------------------------------------------------------------------------

    @Override
    public void setControllerProductInfo(int manufacturerId, int productType, int productId) {
        this.manufacturerId = manufacturerId;
        this.productType = productType;
        this.productId = productId;
    }


    //------------------------------------------------------------------------

    @Override
    public void setControllerApiMask(int[] apiMask) {
        this.apiMask = apiMask;
    }


    //------------------------------------------------------------------------

    @Override
    public void setControllerSerialApiInfo(int version, int flags) {
        controllerSerialApiVersion = version;
        serialApiCapabilitiesFlags = flags;
    }


    //##############################################################################################################
    //######    inner classes


    //------------------------------------------------------------------------
    //  get messages from serial port

    private class ReceiveMessagesDispatcher extends RunnableClass {

        @Override
        public void body() {

            int[] function;
            RkZWaveMessage request = null;
            while (receiveMessages.size() > 0) {
                synchronized (receiveMessages) {
                    function = receiveMessages.remove(0);
                }
                try {
                    synchronized (sendMessages) {
                        request = sendMessages.get(0);
                    }
                } catch (IndexOutOfBoundsException e) {
                    LOG.debug("we have not REQUEST for this FUNCTION");
                }
                functionHandlePool.execute(request, function);
            }

        }

    }


    //------------------------------------------------------------------------
    //  send messages to serial port

    private class SendMessagesDispatcher extends RunnableClass {

        int numberOfResend;

        private boolean checkSending(RkZWaveMessage message) {

            if (message.isResend()) {
                message.setResend(false);
                numberOfResend++;
                return true;
            } else if (!message.isSending()) {
                message.setSending(true);
                numberOfResend = 1;
                return true;
            }

            return false;
        }

        private void removeMessage(RkZWaveMessage message) {
            synchronized (sendMessages) {
                sendMessages.remove(message);
            }
            message.setAcknowledge(false);
        }


        @Override
        protected void body() {

            RkZWaveMessage message;
            while (sendMessages.size() > 0) {

                synchronized (sendMessages) {
                    message = sendMessages.get(0);
                }

                if (checkSending(message)) {
                    serialPort.sendRequest(message.asIntArray());
                    continue;
                }

                if (message.checkUpLifeTime(sendingMessageLifetime)) {
                    removeMessage(message);
                    continue;
                }

                if (!message.isAcknowledge()) {

                    if (message.checkUpResendTime(timeBeforeResend) &&
                            !message.checkUpResendNumber(maxNumberOfResendMessage)) {
                        message.setResend(true);
                    }

                } else {

                    if (!message.isNeedWaitAnswer() || message.isHaveAnswer())
                        removeMessage(message);

                }

            }
        }

    }


}
