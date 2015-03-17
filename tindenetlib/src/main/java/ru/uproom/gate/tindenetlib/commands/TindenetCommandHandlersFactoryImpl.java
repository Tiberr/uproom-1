package ru.uproom.gate.tindenetlib.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.uproom.gate.transport.domain.ClassesSearcher;

import javax.annotation.PostConstruct;
import java.util.EnumMap;
import java.util.Map;

/**
 * Created by osipenko on 17.03.15.
 */

@Service
public class TindenetCommandHandlersFactoryImpl implements TindenetCommandHandlersFactory {


    //##############################################################################################################
    //######    fields


    private static final Logger LOG = LoggerFactory.getLogger(TindenetCommandHandlersFactoryImpl.class);

    private static final Map<TindenetCommandID, TindenetCommandHandler> commandHandlers =
            new EnumMap<>(TindenetCommandID.class);


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
                TindenetCommandHandlerAnnotation.class
        )) {
            TindenetCommandHandlerAnnotation annotation =
                    handler.getAnnotation(TindenetCommandHandlerAnnotation.class);
            if (annotation == null) continue;
            TindenetCommandHandler commandHandler = (TindenetCommandHandler) ClassesSearcher.instantiate(handler);
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

        boolean parity = ((commandCode & 0x1) == 0x1);
        commandHandlers.get(TindenetCommandID.getByCode(commandCode)).execute(parity, parameters);

    }

}
