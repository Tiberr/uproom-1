package ru.uproom.gate.tindenetlib;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import ru.uproom.gate.tindenetlib.commands.server.TindenetServerCommandHandlersFactory;
import ru.uproom.gate.tindenetlib.driver.TindenetSerialPort;
import ru.uproom.gate.transport.command.Command;
import ru.uproom.gate.transport.dto.DeviceDTO;
import ru.uproom.libraries.RkLibraryEventListener;
import ru.uproom.libraries.RkLibraryManager;
import ru.uproom.libraries.auxilliary.RunnableClass;
import ru.uproom.libraries.events.RkLibraryEvent;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by osipenko on 19.03.15.
 */
public class TindenetManager implements RkLibraryManager {


    //##############################################################################################################
    //######    fields


    private static final Logger LOG = LoggerFactory.getLogger(TindenetManager.class);

    private final List<RkLibraryEventListener> eventListeners = new ArrayList<>();
    private final List<RkLibraryEvent> commandsFromLib = new LinkedList<>();
    private final List<Command> commandsToLib = new LinkedList<>();
    private final CommandsFromLibDispatcher fromLibDispatcher = new CommandsFromLibDispatcher();
    private final CommandsToLibDispatcher toLibDispatcher = new CommandsToLibDispatcher();
    private TindenetServerCommandHandlersFactory serverCommandHandlers;


    //##############################################################################################################
    //######    constructors / destructors

    public int init() {

        LOG.info("TindeNet Library starting...");

        // spring initialization
        ClassPathXmlApplicationContext ctx;
        try {
            ctx = new ClassPathXmlApplicationContext("applicationContext.xml");
        } catch (BeansException e) {
            LOG.error("TindeNet Library NOT STARTED : {}", e.getMessage());
            return 1;
        }
        LOG.info("TindeNet Library started successfully. ");

        TindenetSerialPort serialPort;
        try {
            serialPort = (TindenetSerialPort) ctx.getBean("tindenetSerialPortImpl");
            serialPort.open();
        } catch (BeansException e) {
            LOG.error("Spring not found bean 'tindenetSerialPortImpl'");
            return 2;
        }

        try {
            serverCommandHandlers =
                    (TindenetServerCommandHandlersFactory) ctx.getBean("tindenetServerCommandHandlersFactoryImpl");
        } catch (BeansException e) {
            LOG.error("Spring not found bean 'tindenetServerCommandHandlersFactoryImpl'");
            return 3;
        }

        new Thread(fromLibDispatcher).start();
        new Thread(toLibDispatcher).start();

        return 0;
    }


    public void stop() {
        fromLibDispatcher.stop();
        toLibDispatcher.stop();
    }


    //##############################################################################################################
    //######    methods


    //------------------------------------------------------------------------
    // event handler is a up level class

    public void addEventHandler(RkLibraryEventListener eventHandler) {
        eventListeners.add(eventHandler);
    }


    //------------------------------------------------------------------------
    // event handler is a up level class

    public void removeEventHandler(RkLibraryEventListener eventHandler) {
        eventListeners.remove(eventHandler);
    }


    //------------------------------------------------------------------------
    // event is a message from library to up level class

    public void addCommandFromLibrary(RkLibraryEvent event) {
        synchronized (commandsFromLib) {
            commandsFromLib.add(event);
        }
        synchronized (fromLibDispatcher) {
            fromLibDispatcher.notify();
        }
    }


    //------------------------------------------------------------------------
    // event is a message from up level class to library

    public void addCommandToLibrary(Command command) {
        synchronized (commandsToLib) {
            commandsToLib.add(command);
        }
        synchronized (toLibDispatcher) {
            toLibDispatcher.notify();
        }
    }

    @Override
    public int create() {
        return 0;
    }

    @Override
    public void destroy() {

    }

    @Override
    public void addEventListener(RkLibraryEventListener listener) {

    }

    @Override
    public void removeEventListener(RkLibraryEventListener listener) {

    }

    @Override
    public void requestDeviceParameters(DeviceDTO device) {

    }

    @Override
    public void applyDeviceParameters(DeviceDTO device) {

    }


    //##############################################################################################################
    //######    inner classes


    //------------------------------------------------------------------------
    //  get messages from library

    private class CommandsFromLibDispatcher extends RunnableClass {

        @Override
        public void body() {

            RkLibraryEvent event;
            while (commandsFromLib.size() > 0) {
                try {
                    synchronized (commandsFromLib) {
                        event = commandsFromLib.remove(0);
                    }
                } catch (IndexOutOfBoundsException e) {
                    continue;
                }
                for (RkLibraryEventListener eventHandler : eventListeners) {
                    eventHandler.handleEvent(event);
                }
            }

        }

    }


    //------------------------------------------------------------------------
    //  send messages to library

    private class CommandsToLibDispatcher extends RunnableClass {

        @Override
        public void body() {

            Command command = null;
            while (commandsToLib.size() > 0) {
                try {
                    synchronized (commandsToLib) {
                        command = commandsToLib.remove(0);
                    }
                } catch (IndexOutOfBoundsException e) {
                    continue;
                }
                serverCommandHandlers.handleCommand(command);

            }

        }

    }


}
