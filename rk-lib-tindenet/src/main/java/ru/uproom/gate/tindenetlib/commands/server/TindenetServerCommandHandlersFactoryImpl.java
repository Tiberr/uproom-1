package ru.uproom.gate.tindenetlib.commands.server;

import libraries.auxilliary.ClassesSearcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.uproom.gate.tindenetlib.devices.TindenetHub;
import ru.uproom.gate.transport.command.Command;
import ru.uproom.gate.transport.command.CommandType;

import javax.annotation.PostConstruct;
import java.util.EnumMap;
import java.util.Map;

/**
 * Created by osipenko on 17.03.15.
 */

@Service
public class TindenetServerCommandHandlersFactoryImpl implements TindenetServerCommandHandlersFactory {


    //##############################################################################################################
    //######    fields


    private static final Logger LOG = LoggerFactory.getLogger(TindenetServerCommandHandlersFactoryImpl.class);

    private final Map<CommandType, TindenetServerCommandHandler> commandHandlers =
            new EnumMap<>(CommandType.class);

    @Autowired
    private TindenetHub hub;


    //##############################################################################################################
    //######    constructors / destructors


    @PostConstruct
    public void init() {
        registerCommandHandlers();
    }


    //##############################################################################################################
    //######    methods


    //---------------------------------------------------------------------

    private void registerCommandHandlers() {
        for (Class<?> handler : ClassesSearcher.getAnnotatedClasses(
                TindenetServerCommandHandlerAnnotation.class
        )) {
            TindenetServerCommandHandlerAnnotation annotation =
                    handler.getAnnotation(TindenetServerCommandHandlerAnnotation.class);
            if (annotation == null) continue;
            TindenetServerCommandHandler commandHandler =
                    (TindenetServerCommandHandler) ClassesSearcher.instantiate(handler);
            commandHandlers.put(annotation.value(), commandHandler);
        }

    }


    //---------------------------------------------------------------------

    @Override
    public void handleCommand(Command command) {

        TindenetServerCommandHandler commandHandler = commandHandlers.get(command.getType());
        if (commandHandler != null)
            commandHandler.execute(command, hub);

    }

}
