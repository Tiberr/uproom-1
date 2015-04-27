package ru.uproom.gate.tindenetlib.commands.hub;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.uproom.gate.tindenetlib.devices.TindenetHub;

/**
 * Handler for command which cancel current mode of Z-Wave controller
 * </p>
 * Created by osipenko on 09.09.14.
 */
@TindenetHubCommandHandlerAnnotation(value = TindenetHubCommandID.Ping)
public class TindenetPingHubCommandHandler implements TindenetHubCommandHandler {

    private static final Logger LOG = LoggerFactory.getLogger(TindenetPingHubCommandHandler.class);


    @Override
    public boolean execute(boolean direction, String parameters, TindenetHub hub) {

        hub.getSerialPortDataHandler().handlePing();

        LOG.debug("Receive command Ping");
        return true;
    }


}
