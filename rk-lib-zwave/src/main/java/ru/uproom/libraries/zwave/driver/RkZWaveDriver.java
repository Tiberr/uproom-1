package ru.uproom.libraries.zwave.driver;

import libraries.api.RkLibraryDriver;
import libraries.api.RkLibraryManager;
import libraries.auxilliary.LoggingHelper;
import libraries.auxilliary.RunnableClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.uproom.libraries.zwave.devices.RkZWaveDevicePool;
import ru.uproom.libraries.zwave.enums.RkZWaveExtraEnums;
import ru.uproom.libraries.zwave.enums.RkZWaveFunctionID;
import ru.uproom.libraries.zwave.enums.RkZWaveMessageTypes;
import ru.uproom.libraries.zwave.functions.RkZWaveFunctionHandlePool;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

/**
 * Created by osipenko on 11.01.15.
 */
@Service
public class RkZWaveDriver implements RkLibraryDriver {


    //##############################################################################################################
    //######    fields


    private static final Logger LOG = LoggerFactory.getLogger(RkZWaveDriver.class);

    private final Properties properties = new Properties();

    private final RkZWaveSerialPort serialPort = new RkZWaveSerialPort();
    private final List<int[]> receiveMessages = new LinkedList<>();
    private final ReceiveMessagesDispatcher receiveDispatcher = new ReceiveMessagesDispatcher();
    private final List<RkZWaveMessage> sendMessages = new LinkedList<>();
    private final SendMessagesDispatcher sendDispatcher = new SendMessagesDispatcher();
    private final RkZWaveFunctionHandlePool functionHandlePool = new RkZWaveFunctionHandlePool();
    private final CheckLinkQuality checkLinkQuality = new CheckLinkQuality();
    private final RunInitialSequence runInitialSequence = new RunInitialSequence();
    private long sendingMessageLifetime;
    private long timeBeforeResend;
    private int maxNumberOfResendMessage;
    private RkZWaveDevicePool devicePool = new RkZWaveDevicePool();
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
    private long homeId;
    private int controllerId;
    private boolean readyController;
    private boolean readyDriver;
    private long timeBetweenPingSend;
    private long timeBetweenCheckInitSeq;
    private RkLibraryManager libraryManager;


    //##############################################################################################################
    //######    constructors / destructors


    //------------------------------------------------------------------------

    @Override
    public int create() {

        loadProperties();

        timeBetweenPingSend = Long.parseLong(properties.getProperty("time_between_ping_send"));
        checkLinkQuality.timeBetweenPingCheck = Long.parseLong(properties.getProperty("time_between_ping_check"));
        checkLinkQuality.setTimeout(timeBetweenPingSend);
        //new Thread(checkLinkQuality).start();

        functionHandlePool.setDriver(this);
        functionHandlePool.create();

        devicePool.setDriver(this);
        devicePool.create();

        receiveDispatcher.setTimeout(0);
        new Thread(receiveDispatcher).start();
        sendDispatcher.setTimeout(10);
        new Thread(sendDispatcher).start();

        serialPort.setDriver(this);
        serialPort.create();

        return 0;
    }


    //------------------------------------------------------------------------

    @Override
    public void destroy() {

        serialPort.destroy();

        receiveDispatcher.stop();
        sendDispatcher.stop();

    }


    //##############################################################################################################
    //######    getters / setters


    @Override
    public void setLibraryManager(RkLibraryManager libraryManager) {
        this.libraryManager = libraryManager;
    }


    //------------------------------------------------------------------------

    public Properties getProperties() {
        return properties;
    }


    //------------------------------------------------------------------------

    public RkZWaveDevicePool getDevicePool() {
        return devicePool;
    }


    //##############################################################################################################
    //######    methods


    public void restartSerialPort() {
        serialPort.destroy();
        serialPort.create();
    }


    //------------------------------------------------------------------------

    @Override
    public void requestDeviceList() {
        devicePool.requestDeviceList();
    }


    //------------------------------------------------------------------------

    @Override
    public void applyDeviceParameters() {

    }


    //------------------------------------------------------------------------

    private boolean loadProperties() {
        try {
            properties.load(getClass().getResourceAsStream("/rk-lib-zwave.properties"));
        } catch (IOException e) {
            LOG.error("library can not load properties : {}", e.getMessage());
            return false;
        }

        sendingMessageLifetime = Long.parseLong(properties.getProperty("message_life_time"));
        timeBeforeResend = Long.parseLong(properties.getProperty("message_resend_time"));
        maxNumberOfResendMessage = Integer.parseInt(properties.getProperty("message_resend_number"));
        timeBetweenCheckInitSeq = Long.parseLong(properties.getProperty("time_between_check_init_seq"));

        return true;
    }


    //------------------------------------------------------------------------

    public void addMessageToSendingQueue(RkZWaveMessage message) {
        synchronized (sendMessages) {
            sendMessages.add(message);
        }
        LOG.debug("SEND MESSAGE : {}",
                LoggingHelper.createHexStringFromIntArray(message.asIntArray(), true));
    }


    //------------------------------------------------------------------------

    public void addMessageToReceivingQueue(int[] data) {

        synchronized (receiveMessages) {
            receiveMessages.add(data);
        }
        LOG.debug("RECEIVE MESSAGE : {}", LoggingHelper.createHexStringFromIntArray(data, true));
        synchronized (receiveDispatcher) {
            receiveDispatcher.notify();
        }

    }


    //------------------------------------------------------------------------

    public void currentRequestReceived(RkZWaveFunctionID functionID) {
        RkZWaveMessage message;

        synchronized (sendMessages) {
            message = sendMessages.get(0);
        }
        if (message == null) return;
        if (message.getFunctionID() == functionID) {
            message.setHaveAnswer(true);
            runInitialSequence.setInitSequenceStep(functionID);
        }

    }


    //------------------------------------------------------------------------

    public void receiveAcknowledge() {
        RkZWaveMessage message;

        synchronized (sendMessages) {
            message = sendMessages.get(0);
        }
        if (message != null) message.setAcknowledge(true);

        LOG.debug("ACKNOWLEDGE");
    }


    //------------------------------------------------------------------------

    public void receiveCancel() {
        LOG.debug("CANCEL");
    }


    //------------------------------------------------------------------------

    public void receiveNotAcknowledge() {
        RkZWaveMessage message;

        synchronized (sendMessages) {
            message = sendMessages.get(0);
        }
        if (message != null) message.setResend(true);

        LOG.debug("NOT ACKNOWLEDGE");
    }


    //------------------------------------------------------------------------

    public void applyPortState(boolean open) {
//        if (open) initSequence();
        if (open) {
            runInitialSequence.setTimeout(timeBetweenCheckInitSeq);
            new Thread(runInitialSequence).start();
            runInitialSequence.setInitSequenceStep(RkZWaveFunctionID.UNKNOWN);
        }
    }


    //------------------------------------------------------------------------

    public void setReadyController(boolean ready) {
        if (!readyController && ready)
            devicePool.setControllerReady(true);
        else ; // driver was reset
        readyController = ready;
    }


    //------------------------------------------------------------------------

    public void setReadyDriver(boolean ready) {
        libraryManager.eventLibraryReady(true);
        readyController = ready;
    }


    //------------------------------------------------------------------------

    public void setSucNodeId(int nodeId) {
        sucNodeId = nodeId;
    }


    //------------------------------------------------------------------------

    public void setControllerCapabilitiesFlag(int flags) {
        controllerCapabilitiesFlags = flags;
    }


    //------------------------------------------------------------------------

    public void setControllerLibraryVersion(String version) {
        this.controllerLibraryVersion = version;
    }


    //------------------------------------------------------------------------

    public void setSerialApiVersion(int lower, int upper) {
        serialApiVersion[0] = lower;
        serialApiVersion[1] = upper;
    }


    //------------------------------------------------------------------------

    public void setControllerProductInfo(int manufacturerId, int productType, int productId) {
        this.manufacturerId = manufacturerId;
        this.productType = productType;
        this.productId = productId;
    }


    //------------------------------------------------------------------------

    public void setControllerApiMask(int[] apiMask) {
        this.apiMask = apiMask;
    }


    //------------------------------------------------------------------------

    public void setControllerSerialApiInfo(int version, int flags) {
        controllerSerialApiVersion = version;
        serialApiCapabilitiesFlags = flags;
    }


    //##############################################################################################################
    //######    inner classes


    private class ReceiveMessagesDispatcher extends RunnableClass {

        @Override
        public void body() {
            super.body();

            int[] function;
            RkZWaveMessage request = null;
            while (receiveMessages.size() > 0) {

                synchronized (receiveMessages) {
                    function = receiveMessages.remove(0);
                }
                if (function == null) continue;

                try {
                    synchronized (sendMessages) {
                        request = sendMessages.get(0);
                    }
                } catch (IndexOutOfBoundsException e) {
                    LOG.debug("we have not REQUEST for this FUNCTION");
                }

                functionHandlePool.execute(request, function);
                checkLinkQuality.pingReceived();
            }

        }

    }


    //------------------------------------------------------------------------

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
        }


        private void addMessage(RkZWaveMessage message) {
            message.reload();
            addMessageToSendingQueue(message);
        }


        @Override
        protected void body() {
            super.body();

            RkZWaveMessage message;
            while (sendMessages.size() > 0) {

                synchronized (sendMessages) {
                    message = sendMessages.get(0);
                }

                if (checkSending(message)) {
                    serialPort.sendRequest(message.asIntArray());
                    checkLinkQuality.setStartCheck(true);
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
                    if (message.isMustSendAgain())
                        addMessage(message);

                }

            }
        }

    }


    //------------------------------------------------------------------------

    private class CheckLinkQuality extends RunnableClass {

        private boolean startCheck;

        private long lastReceiveAnswerTime;
        private long timeBetweenPingCheck;


        public void setStartCheck(boolean startCheck) {
            this.startCheck = startCheck;
        }


        public boolean checkPingTime() {
            return (System.currentTimeMillis() - lastReceiveAnswerTime) < timeBetweenPingCheck;
        }


        public void pingReceived() {
            lastReceiveAnswerTime = System.currentTimeMillis();
        }


        private void createNewPingMessage() {

            synchronized (sendMessages) {
                if (sendMessages.isEmpty()) {
                    RkZWaveMessage ping = new RkZWaveMessage(
                            RkZWaveMessageTypes.Request,
                            RkZWaveFunctionID.IS_FAILED_NODE_ID,
                            null,
                            false);
                    int[] data = new int[1];
                    data[0] = 0x01;
                    ping.setParameters(data);
                    addMessageToSendingQueue(ping);
                }
            }
        }


        @Override
        protected void body() {
            super.body();

            if (!startCheck) {
                if (!serialPort.isSerialPortOpen())
                    restartSerialPort();
                return;
            }

            if (checkPingTime()) {
                createNewPingMessage();
            } else {
                restartSerialPort();
                setStartCheck(false);
            }
        }
    }


    //------------------------------------------------------------------------
    // init sequence named after Z-Wave.Me (worked better then initSequence from OpenZWave)
    // todo : must create sequence same as this for each device

    private class RunInitialSequence extends RunnableClass {

        private RkZWaveFunctionID initSequenceStep = RkZWaveFunctionID.UNKNOWN;

        public void setInitSequenceStep(RkZWaveFunctionID stepId) {
            boolean quit = false;

            switch (initSequenceStep) {
                case UNKNOWN:
                    if (stepId == RkZWaveFunctionID.UNKNOWN)
                        initSequenceStep = RkZWaveFunctionID.SERIAL_API_GET_CAPABILITIES;
                    break;
                case SERIAL_API_GET_CAPABILITIES:
                    if (stepId == RkZWaveFunctionID.SERIAL_API_GET_CAPABILITIES)
                        initSequenceStep = RkZWaveFunctionID.SERIAL_API_SET_TIMEOUTS;
                    break;
                case SERIAL_API_SET_TIMEOUTS:
                    if (stepId == RkZWaveFunctionID.SERIAL_API_SET_TIMEOUTS)
                        initSequenceStep = RkZWaveFunctionID.MEMORY_GET_ID;
                    break;
                case MEMORY_GET_ID:
                    if (stepId == RkZWaveFunctionID.MEMORY_GET_ID)
                        initSequenceStep = RkZWaveFunctionID.GET_CONTROLLER_CAPABILITIES;
                    break;
                case GET_CONTROLLER_CAPABILITIES:
                    if (stepId == RkZWaveFunctionID.GET_CONTROLLER_CAPABILITIES)
                        initSequenceStep = RkZWaveFunctionID.GET_VERSION;
                    break;
                case GET_VERSION:
                    if (stepId == RkZWaveFunctionID.GET_VERSION)
                        initSequenceStep = RkZWaveFunctionID.GET_SUC_NODE_ID;
                    break;
                case GET_SUC_NODE_ID:
                    if (stepId == RkZWaveFunctionID.GET_SUC_NODE_ID)
                        initSequenceStep = RkZWaveFunctionID.SERIAL_API_GET_INIT_DATA;
                    break;
                case SERIAL_API_GET_INIT_DATA:
                    if (stepId == RkZWaveFunctionID.SERIAL_API_GET_INIT_DATA) {
                        initSequenceStep = RkZWaveFunctionID.UNKNOWN;
                        quit = true;
                    }
                    break;
            }

            if (quit) stop();
            else
                synchronized (this) {
                    notify();
                }
        }

        @Override
        protected void body() {
            super.body();
            if (initSequenceStep == RkZWaveFunctionID.UNKNOWN) return;

            RkZWaveMessage message =
                    new RkZWaveMessage(RkZWaveMessageTypes.Request, initSequenceStep, null, true);
            if (initSequenceStep == RkZWaveFunctionID.SERIAL_API_SET_TIMEOUTS) {
                int[] data = new int[2];
                data[0] = RkZWaveExtraEnums.ACK_TIMEOUT / 10;
                data[1] = RkZWaveExtraEnums.BYTE_TIMEOUT;
                message.setParameters(data);
            }
            addMessageToSendingQueue(message);
        }
    }

}
