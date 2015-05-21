package ru.uproom.gate.commands;

import libraries.auxilliary.ClassesSearcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.uproom.gate.devices.GateDevicesSet;
import ru.uproom.gate.notifications.zwave.NotificationWatcherImpl;
import ru.uproom.gate.transport.command.Command;
import ru.uproom.gate.transport.command.CommandType;

import javax.annotation.PostConstruct;
import java.util.EnumMap;
import java.util.Map;


/**
 * Main object for handling server commands
 * </p>
 * Created by osipenko on 05.08.14.
 */
@Service
public class GateCommanderImpl implements GateCommander {


    //##############################################################################################################
    //######    fields


    private static final Logger LOG = LoggerFactory.getLogger(NotificationWatcherImpl.class);

    private Map<CommandType, CommandHandler> commandHandlers =
            new EnumMap<CommandType, CommandHandler>(CommandType.class);

    @Autowired
    private GateDevicesSet home;


    //##############################################################################################################
    //######    constructors


    @PostConstruct
    public void create() {

        getCommandHandlersFromPath();
    }


    //##############################################################################################################
    //######    getters / setters


    //##############################################################################################################
    //######    methods-


    //------------------------------------------------------------------------

    private boolean getCommandHandlersFromPath() {

        for (Class<?> handler : ClassesSearcher.getAnnotatedClasses(
                CommandHandlerAnnotation.class
        )) {
            CommandHandlerAnnotation annotation =
                    handler.getAnnotation(CommandHandlerAnnotation.class);
            if (annotation == null) continue;
            commandHandlers.put(
                    annotation.value(),
                    (CommandHandler) ClassesSearcher.instantiate(handler)
            );
        }

        return commandHandlers.isEmpty();
    }


    //------------------------------------------------------------------------

    @Override
    public boolean execute(Command command) {

        CommandHandler handler = commandHandlers.get(command.getType());
        if (handler == null) {
            LOG.error("Handler for command '{}' not found", command.getType());
            return false;
        }
        if (command.getType() == CommandType.Ping) handler.execute(command, home);
        else if (home.isReady()) handler.execute(command, home);
        else return false;

        return true;
    }

}
