package ru.uproom.gate.tindenetlib.commands.server;

import ru.uproom.gate.tindenetlib.devices.TindenetHub;
import ru.uproom.gate.transport.command.Command;

/**
 * marker interface for classes of command handling
 * </p>
 * Created by osipenko on 30.08.14.
 */
public interface TindenetServerCommandHandler {

    public boolean execute(Command command, TindenetHub hub);

}
