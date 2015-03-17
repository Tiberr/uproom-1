package ru.uproom.gate.tindenetlib.commands;

/**
 * marker interface for classes of command handling
 * </p>
 * Created by osipenko on 30.08.14.
 */
public interface TindenetCommandHandler {

    // direction : false = request, true = answer
    public boolean execute(boolean direction, String parameters);

}
