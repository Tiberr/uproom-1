package ru.uproom.libraries.zwave.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.uproom.libraries.zwave.devices.RkZWaveDevice;
import ru.uproom.libraries.zwave.devices.RkZWaveDeviceParameter;
import ru.uproom.libraries.zwave.driver.RkZWaveMessage;
import ru.uproom.libraries.zwave.enums.RkZWaveCommandClassNames;
import ru.uproom.libraries.zwave.enums.RkZWaveFunctionID;
import ru.uproom.libraries.zwave.enums.RkZWaveMessageTypes;

import java.util.HashSet;
import java.util.Set;

/**
 * z-wave command class
 * <p/>
 * Created by osipenko on 10.09.14.
 */
@RkZWaveCommandClassesAnnotation(value = RkZWaveCommandClassNames.MultiInstance)
public class RkZWaveMultiInstanceCommandClass extends RkZWaveCommandClass {


    private static final Logger LOG =
            LoggerFactory.getLogger(RkZWaveMultiInstanceCommandClass.class);
    private final Set<RkZWaveCommandClassNames> endPointCommandClasses = new HashSet<>();
    private final int genericClass[] = new int[]{
            0x21,        // Multilevel Sensor
            0x20,        // Binary Sensor
            0x31,        // Meter
            0x08,        // Thermostat
            0x11,        // Multilevel Switch
            0x10,        // Binary Switch
            0x12,        // Remote Switch
            0xa1,        // Alarm Sensor
            0x16,        // Ventilation
            0x30,        // Pulse Meter
            0x40,        // Entry Control
            0x13,        // Toggle Switch
            0x03,        // AV Control Point
            0x04,        // Display
            0x00        // End of list
    };
    private boolean numberOfEndPointsCanChange;
    private boolean endPointsAreSameClass;
    private int numEndPoints;
    private int endPointsFindIndex;
    private int numEndPointsFound;
    private int numEndPointsHint;
    private int endPointMap;


    //-----------------------------------------------------------------------------------------------------------

    @Override
    public int createParameterList(RkZWaveDevice device, int instance) {
        int parametersNumber = 0;
        String parameterNames = "";

        RkZWaveCommandClassNames annotation =
                (RkZWaveCommandClassNames) getClass().getAnnotation(RkZWaveCommandClassesAnnotation.class).value();
        LOG.debug("ADD COMMAND CLASS : {}, implement {} parameter(s) ({}) ", new Object[]{
                annotation.name(),
                parametersNumber,
                parameterNames
        });

        return parametersNumber;
    }


    //-----------------------------------------------------------------------------------------------------------

    @Override
    public void messageHandler(RkZWaveDevice device, int[] data, int instance) {

        switch (data[0]) {

            case 0x05:
                handleMultiInstanceReport(device, data);
                break;
            case 0x06:
                handleMultiInstanceEncap(device, data);
                break;
            case 0x08:
                handleMultiChannelEndPointReport(device, data);
                break;
            case 0x0A:
                handleMultiChannelCapabilityReport(device, data);
                break;
            case 0x0C:
                handleMultiChannelEndPointFindReport(device, data);
                break;
            case 0x0D:
                handleMultiChannelEncap(device, data);
                break;

            default:

        }
    }


    //-----------------------------------------------------------------------------------------------------------

    @Override
    public void requestDeviceState(RkZWaveDevice device, int instance) {
        super.requestDeviceState(device, instance);
    }


    //-----------------------------------------------------------------------------------------------------------

    @Override
    public void requestDeviceParameter(RkZWaveDevice device, int instance) {

        RkZWaveMessage message = new RkZWaveMessage(
                RkZWaveMessageTypes.Request,
                RkZWaveFunctionID.SEND_DATA,
                null, false
        );
        int[] params = new int[5];
        params[0] = device.getDeviceId();
        params[1] = 0x02;
        params[2] = getId();
        params[3] = 0x02; // command GET
        params[4] = 0x00; // transmit options (?)
        message.setParameters(params);
        device.getDevicePool().getDriver().addMessageToSendingQueue(message);

    }


    //-----------------------------------------------------------------------------------------------------------

    @Override
    public void setDeviceParameter(RkZWaveDeviceParameter parameter, String value) {

        switch (parameter.getZWaveName()) {
            case Switch:
                setSwitchDeviceParameter(parameter, value);
                break;
            default:
        }
    }


    //-----------------------------------------------------------------------------------------------------------

    public void setSwitchDeviceParameter(RkZWaveDeviceParameter parameter, String value) {

        if (!value.equalsIgnoreCase("true") && !value.equalsIgnoreCase("false")) return;

        RkZWaveMessage message = new RkZWaveMessage(
                RkZWaveMessageTypes.Request,
                RkZWaveFunctionID.SEND_DATA,
                null, false
        );
        int[] params = new int[6];
        params[0] = parameter.getDevice().getDeviceId();
        params[1] = 0x03;
        params[2] = getId();
        params[3] = 0x01; // command SET
        params[4] = value.equalsIgnoreCase("true") ? 0xFF : 0x00;
        params[5] = 0x00; // transmit options (?)
        message.setParameters(params);
        parameter.getDevice().getDevicePool().getDriver().addMessageToSendingQueue(message);

    }


    //-----------------------------------------------------------------------------------------------------------

    public void requestInstance(RkZWaveDevice device, RkZWaveCommandClassNames commandClassName) {

        RkZWaveMessage message = new RkZWaveMessage(
                RkZWaveMessageTypes.Request,
                RkZWaveFunctionID.SEND_DATA,
                null, true
        );

        // multi instance
        if (getVersion() == 1) {

            int[] params = new int[6];
            params[0] = device.getDeviceId();
            params[1] = 0x03;
            params[2] = getId();
            params[3] = 0x04; // command MULTI INSTANCE GET
            params[4] = commandClassName.getCode();
            params[5] = 0x00; // transmit options (?)
            message.setParameters(params);
            device.getDevicePool().getDriver().addMessageToSendingQueue(message);

        }

        // multi channel
        else {

            int[] params = new int[5];
            params[0] = device.getDeviceId();
            params[1] = 0x02;
            params[2] = getId();
            params[3] = 0x07; // command MULTI CHANNEL END POINT GET
            params[4] = 0x00; // transmit options (?)
            message.setParameters(params);
            device.getDevicePool().getDriver().addMessageToSendingQueue(message);

        }

    }


    //-----------------------------------------------------------------------------------------------------------

    private void handleMultiInstanceReport(RkZWaveDevice device, int[] data) {

        RkZWaveCommandClassNames commandClassName = RkZWaveCommandClassNames.getByCode(data[1]);
        device.applyCommandClassInstances(commandClassName, data[2]);
    }


    //-----------------------------------------------------------------------------------------------------------

    private void handleMultiInstanceEncap(RkZWaveDevice device, int[] data) {

        int instance = data[1];
        if (getVersion() > 1)
            instance &= 0x7F;

        RkZWaveCommandClassNames commandClassName = RkZWaveCommandClassNames.getByCode(data[2]);

        int[] bytes = new int[data.length - 3];
        System.arraycopy(data, 3, bytes, 0, bytes.length);
        device.applyDeviceParametersFromByteArray(commandClassName, bytes, instance);
    }


    //-----------------------------------------------------------------------------------------------------------

    private void handleMultiChannelEndPointReport(RkZWaveDevice device, int[] data) {

        numberOfEndPointsCanChange = ((data[1] & 0x80) != 0);
        endPointsAreSameClass = ((data[1] & 0x40) != 0);
        if (numEndPointsHint != 0)
            numEndPoints = numEndPointsHint;

        int len = numEndPoints;
        if (endPointsAreSameClass)
            len = 1;

        for (int i = 1; i <= len; i++) {

            RkZWaveMessage message = new RkZWaveMessage(
                    RkZWaveMessageTypes.Request,
                    RkZWaveFunctionID.SEND_DATA,
                    null, true
            );
            int[] params = new int[6];
            params[0] = (byte) device.getDeviceId();
            params[1] = 0x03;
            params[2] = getId();
            params[3] = 0x09; // command MULTI CHANNEL CAPABILITY GET
            params[4] = i;
            params[5] = 0x00; // transmit options (?)
            message.setParameters(params);
            device.getDevicePool().getDriver().addMessageToSendingQueue(message);
        }
    }


    //-----------------------------------------------------------------------------------------------------------

    private void handleMultiChannelCapabilityReport(RkZWaveDevice device, int[] data) {

        int endPoint = data[1] & 0x7F;
        boolean dynamic = ((data[1] & 0x80) != 0);

        endPointCommandClasses.clear();
        for (int i = 0; i < (data.length - 5); ++i) {

            int commandClassId = data[i + 4];
            if (commandClassId == 0xEF)
                break;

            endPointCommandClasses.add(RkZWaveCommandClassNames.getByCode(commandClassId));
            if (device.getCommandClassById(commandClassId) == null) {
                device.addCommandClass(commandClassId);
            }

        }

        RkZWaveBasicCommandClass basicClass =
                (RkZWaveBasicCommandClass) device.getCommandClassByName(RkZWaveCommandClassNames.Basic);
        if (endPointsAreSameClass) {
            int len;
            if (endPointMap == 0x00) // MultiInstanceMapAll
            {
                endPoint = 0;
                len = numEndPoints + 1;
            } else {
                endPoint = 1;
                len = numEndPoints;
            }

            // Create all the command classes for all the endpoints
            for (int i = 1; i <= len; i++) {

                for (RkZWaveCommandClassNames endPointCommandClass : endPointCommandClasses) {

                    RkZWaveCommandClass commandClass = device.getCommandClassByName(endPointCommandClass);
                    commandClass.createInstance(device, i);
                    if (endPointMap != 0 || i != 1)
                        commandClass.setInstanceEndPoint(i, endPoint);

                    if (basicClass != null && basicClass.getMapping() == commandClass.getName()) {
                        basicClass.createInstance(device, i);
                        if (endPointMap != 0 || i != 1)
                            basicClass.setInstanceEndPoint(i, endPoint);
                    }
                }
                endPoint++;

            }
        }

        // Endpoints are different
        else {

            for (RkZWaveCommandClassNames endPointCommandClass : endPointCommandClasses) {

                RkZWaveCommandClass commandClass = device.getCommandClassByName(endPointCommandClass);
                if (commandClass != null) {

                    // Find the next free instance of this class
                    int i;
                    for (i = 1; i <= 127; i++) {

                        // Include the non-endpoint instance
                        if (endPointMap == 0) {
                            if (!commandClass.getInstances().isBit(i)) break;
                        }
                        // Reuse non-endpoint instances first time we see it
                        else if (i == 1 && commandClass.getInstances().isBit(i)
                                && commandClass.getInstanceEndPoint(i) == 0) break;
                            // Find the next free instance
                        else if (!commandClass.getInstances().isBit(i)) break;

                    }
                    commandClass.createInstance(device, i);
                    commandClass.setInstanceEndPoint(i, endPoint);

                    // If we support the BASIC command class and it is mapped to a command class
                    // assigned to this end point, make sure the BASIC command class is also associated
                    // with this end point.
                    if (basicClass != null && basicClass.getMapping() == commandClass.getName()) {
                        basicClass.createInstance(device, i);
                        basicClass.setInstanceEndPoint(i, endPoint);
                    }
                }
            }
        }

    }


    //-----------------------------------------------------------------------------------------------------------

    private void handleMultiChannelEndPointFindReport(RkZWaveDevice device, int[] data) {

        int numEndPoints = data.length - 5;
        for (int i = 0; i < numEndPoints; ++i) {
            int endPoint = data[i + 4] & 0x7f;

            // Use the stored command class list to set up the endpoint.
            if (endPointsAreSameClass) {
                for (RkZWaveCommandClassNames commandClassName : endPointCommandClasses) {
                    RkZWaveCommandClass commandClass = device.getCommandClassByName(commandClassName);
                    if (commandClass != null) commandClass.createInstance(device, (byte) endPoint);
                }
            }

            // Endpoints are different, so request the capabilities
            else {
                RkZWaveMessage message = new RkZWaveMessage(
                        RkZWaveMessageTypes.Request,
                        RkZWaveFunctionID.SEND_DATA,
                        null, true
                );
                int[] params = new int[6];
                params[0] = device.getDeviceId();
                params[1] = 0x03;
                params[2] = getId();
                params[3] = 0x09; // command MULTI CHANNEL CAPABILITY GET
                params[4] = endPoint;
                params[5] = 0x00; // transmit options (?)
                message.setParameters(params);
                device.getDevicePool().getDriver().addMessageToSendingQueue(message);
            }
        }

        numEndPointsFound += numEndPoints;
        if (endPointsAreSameClass) {

            // No more reports to follow this one, so we can continue the search.
            if (data[1] == 0x00) {

                // We have not yet found all the endpoints, so move to the next generic class request
                if (numEndPointsFound < numEndPoints) {
                    ++endPointsFindIndex;

                    if (genericClass[endPointsFindIndex] > 0) {
                        RkZWaveMessage message = new RkZWaveMessage(
                                RkZWaveMessageTypes.Request,
                                RkZWaveFunctionID.SEND_DATA,
                                null, true
                        );
                        int[] params = new int[7];
                        params[0] = device.getDeviceId();
                        params[1] = 0x04;
                        params[2] = getId();
                        params[3] = 0x0B; // command MULTI CHANNEL END POINT FIND
                        params[4] = genericClass[endPointsFindIndex];
                        params[5] = 0xFF; // generic device class
                        params[6] = 0x00; // transmit options (?)
                        message.setParameters(params);
                        device.getDevicePool().getDriver().addMessageToSendingQueue(message);
                    }
                }
            }
        }
    }


    //-----------------------------------------------------------------------------------------------------------

    private void handleMultiChannelEncap(RkZWaveDevice device, int[] data) {

        int endPoint = data[1] & 0x7f;
        RkZWaveCommandClassNames commandClassName = RkZWaveCommandClassNames.getByCode(data[3]);
        RkZWaveCommandClass commandClass = device.getCommandClassByName(commandClassName);

        if (commandClass != null) {
            int instance = commandClass.getInstance(endPoint);
            if (instance == 0)
                LOG.warn("Cannot find endpoint map to instance for Command Class ({}) endpoint = {}",
                        new Object[]{commandClassName.name(), endPoint});
            else {
                LOG.debug("Received a MultiChannelEncap from node ({}), endpoint = {} for Command Class ({})",
                        new Object[]{device.getDeviceId(), endPoint, commandClassName.name()});
                int[] bytes = new int[data.length - 4];
                System.arraycopy(data, 4, bytes, 0, bytes.length);
                commandClass.messageHandler(device, bytes, instance);
            }
        }
    }

}

