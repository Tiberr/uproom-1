package ru.uproom.gate.tindenetlib.commands.hub;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.uproom.gate.tindenetlib.devices.TindenetHub;
import ru.uproom.libraries.auxilliary.ClassesSearcher;

import javax.annotation.PostConstruct;
import java.util.EnumMap;
import java.util.Map;

/**
 * Created by osipenko on 17.03.15.
 */

@Service
public class TindenetHubCommandHandlersFactoryImpl implements TindenetHubCommandHandlersFactory {


    //##############################################################################################################
    //######    fields


    private static final Logger LOG = LoggerFactory.getLogger(TindenetHubCommandHandlersFactoryImpl.class);

    private static final Map<TindenetHubCommandID, TindenetHubCommandHandler> commandHandlers =
            new EnumMap<>(TindenetHubCommandID.class);

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
                TindenetHubCommandHandlerAnnotation.class
        )) {
            TindenetHubCommandHandlerAnnotation annotation =
                    handler.getAnnotation(TindenetHubCommandHandlerAnnotation.class);
            if (annotation == null) continue;
            TindenetHubCommandHandler commandHandler = (TindenetHubCommandHandler) ClassesSearcher.instantiate(handler);
            commandHandlers.put(annotation.value(), commandHandler);
        }

    }


    //---------------------------------------------------------------------

    @Override
    public void handleCommand(String command) {
        int commandCode;
        String parameters;

        int pos = command.indexOf(" ");
        try {
            if (pos > 0) {
                commandCode = Integer.parseInt(command.substring(0, pos));
                parameters = command.substring(pos + 1);
            } else {
                commandCode = Integer.parseInt(command);
                parameters = "";
            }
        } catch (NumberFormatException e) {
            LOG.error("Command code is not a number : {}", new Object[]{command});
            return;
        }

        boolean direction = ((commandCode & 0x1) == 0x1); // parity
        if (!direction) commandCode--;

        TindenetHubCommandID id = TindenetHubCommandID.getByCode(commandCode);
        if (id == TindenetHubCommandID.Unknown) return;

        commandHandlers.get(TindenetHubCommandID.getByCode(commandCode)).execute(direction, parameters, hub);

    }

}
