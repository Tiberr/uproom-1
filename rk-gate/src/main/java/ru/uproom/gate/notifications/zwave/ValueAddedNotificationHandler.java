package ru.uproom.gate.notifications.zwave;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zwave4j.Manager;
import org.zwave4j.Notification;
import org.zwave4j.NotificationType;
import ru.uproom.gate.devices.GateDevicesSet;
import ru.uproom.gate.devices.zwave.ZWaveDeviceParametersNames;
import ru.uproom.gate.devices.zwave.ZWaveValueIndexFactory;
import ru.uproom.gate.notifications.NotificationHandler;

/**
 * Created by osipenko on 15.09.14.
 */

@ZwaveNotificationHandlerAnnotation(value = NotificationType.VALUE_ADDED)
public class ValueAddedNotificationHandler implements NotificationHandler {

    private static final Logger LOG = LoggerFactory.getLogger(ValueAddedNotificationHandler.class);

    @Override
    public boolean execute(Notification notification, GateDevicesSet home) {

        int paramIndex = ZWaveValueIndexFactory.createIndex(notification.getValueId());
        String paramZName = Manager.get().getValueLabel(notification.getValueId());
        ZWaveDeviceParametersNames paramName =
                ZWaveDeviceParametersNames.byZWaveUID(paramIndex, paramZName);

        home.addGateDeviceParameter(notification.getNodeId(), paramName, notification.getValueId());

        LOG.debug("z-wave notification : {}; node : {}; label : {}; id : {}", new Object[]{
                notification.getType(),
                notification.getNodeId(),
                paramZName,
                paramIndex
        });

        return true;
    }
}
