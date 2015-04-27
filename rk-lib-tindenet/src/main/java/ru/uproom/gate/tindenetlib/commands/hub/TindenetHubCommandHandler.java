package ru.uproom.gate.tindenetlib.commands.hub;

import ru.uproom.gate.tindenetlib.devices.TindenetHub;

/**
 * marker interface for classes of command handling
 * </p>
 * Created by osipenko on 30.08.14.
 */
public interface TindenetHubCommandHandler {

    // direction : false = request, true = answer
    public boolean execute(boolean direction, String parameters, TindenetHub hub);

}
