package ru.uproom.gate.tindenetlib.commands.server;

import ru.uproom.gate.transport.command.Command;

/**
 * Created by osipenko on 17.03.15.
 */
public interface TindenetServerCommandHandlersFactory {

    public void handleCommand(Command command);

}
