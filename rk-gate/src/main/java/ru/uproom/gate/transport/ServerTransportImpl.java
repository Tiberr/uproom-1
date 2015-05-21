package ru.uproom.gate.transport;

import libraries.auxilliary.RunnableClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.uproom.gate.commands.GateCommander;
import ru.uproom.gate.transport.command.Command;
import ru.uproom.gate.transport.command.CommandType;
import ru.uproom.gate.transport.command.HandshakeCommand;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * class with functionality of changing data with server
 * <p/>
 * Created by osipenko on 08.08.14.
 */
@Service
public class ServerTransportImpl implements ServerTransport {


    //##############################################################################################################
    //######    fields


    private static final Logger LOG = LoggerFactory.getLogger(ServerTransportImpl.class);

    private final ConnectionChecker connectionChecker = new ConnectionChecker();
    private ConnectionHandler connectionHandler = new ConnectionHandler();

    @Value("${cloud_host}")
    private String host;
    @Value("${cloud_port}")
    private int port;
    @Value("${local_port}")
    private int localPort;
    @Value("${connection_attempts}")
    private int times = 0;
    @Value("${period_between_attempts}")
    private long periodBetweenAttempts = 0;
    @Value("${gateId}")
    private int gateId;
    @Value("${period_wait_ping}")
    private int periodWaitPing;

    @Autowired
    private GateCommander commander;


    //##############################################################################################################
    //######    constructors


    //------------------------------------------------------------------------
    //  connecting initialization

    @PostConstruct
    public void init() {

        connectionChecker.setTimeout(periodWaitPing);
        new Thread(connectionChecker).start();

        connectionHandler.setTimeout(periodBetweenAttempts);
        new Thread(connectionHandler).start();
    }


    //------------------------------------------------------------------------
    //  stop all connecting

    @PreDestroy
    public void stop() {

        stopLink(false);
    }


    //##############################################################################################################
    //######    methods


    //------------------------------------------------------------------------
    //   stop connection

    public void stopLink(boolean restart) {

        connectionChecker.toggleCheck(false);
        if (!restart) {
            connectionChecker.stop();
            connectionHandler.stop();
        }
        connectionHandler.closeLink();

    }


    //------------------------------------------------------------------------
    //  handle for command PING

    public void backPingToServer(Command ping) {

        connectionChecker.toggleCheck(true);
        sendCommand(ping);
    }


    //------------------------------------------------------------------------
    //  send command to server

    @Override
    public void sendCommand(Command command) {
        connectionHandler.sendCommand(command);
    }


    //##############################################################################################################
    //######    inner classes


    //------------------------------------------------------------------------
    //  check connection

    private class ConnectionChecker extends RunnableClass {

        private boolean check;
        private long lastPingMoment;


        // ---- checking on/off ----
        public void toggleCheck(boolean check) {

            if (check) lastPingMoment = System.currentTimeMillis();

            if (!this.check && check) {
                LOG.info("gate HAVE a ping command from server ({}) - LINK SET ON", new Object[]{
                        host
                });
                synchronized (this) {
                    notify();
                }
            }
            if (this.check && !check) {
                LOG.info("gate LOST a ping command from server ({}) - LINK SET OFF", new Object[]{
                        host
                });
            }
            this.check = check;
        }


        // ---- checking quality of communication ----
        @Override
        public void body() {

            if (check) {
                long dif = System.currentTimeMillis() - lastPingMoment;
                if (dif >= periodWaitPing) {
                    toggleCheck(false);
                    stopLink(true);
                }
            }

        }
    }


    //------------------------------------------------------------------------
    //  keep connection

    private class ConnectionHandler extends RunnableClass {


        private Socket socket;
        private ObjectInputStream input;
        private ObjectOutputStream output;

        private boolean needStopLink = true;

        public void closeLink() {
            destroy();
        }

        // ---- create new socket and get it ----
        public boolean createSocket() {
            try {
                socket = new Socket(host, port);
                return true;
            } catch (UnknownHostException e) {
                LOG.error("[UnknownHostException] host : {} - {}", new Object[]{
                        host,
                        e.getMessage()
                });
            } catch (IOException e) {
                LOG.error("[IOException] host : {} - {}", new Object[]{
                        host,
                        e.getMessage()
                });
            }
            return false;
        }


        // ---- create new output stream and get it ----
        public boolean createOutputStream() {
            try {
                output = new ObjectOutputStream(socket.getOutputStream());
                return true;
            } catch (UnknownHostException e) {
                LOG.error("[UnknownHostException] host : {} - {}", new Object[]{
                        host,
                        e.getMessage()
                });
            } catch (IOException e) {
                LOG.error("[IOException] host : {} - {}", new Object[]{
                        host,
                        e.getMessage()
                });
            }
            return false;
        }


        // ---- create new input stream and get it ----
        public boolean createInputStream() {
            try {
                input = new ObjectInputStream(socket.getInputStream());
                return true;
            } catch (IOException e) {
                LOG.error(e.getMessage());
            }
            return false;
        }


        // ---- open connection ----
        public boolean open() {
            if (!createSocket()) return false;
            if (!createOutputStream()) return false;
            sendCommand(new HandshakeCommand(gateId));
            return createInputStream();
        }


        // ---- receive command from server input stream ----
        public Command receiveCommandFromServer() {
            Command command = null;
            try {
                if (input != null)
                    command = (Command) input.readObject();
            } catch (IOException | ClassNotFoundException e) {
                LOG.error("host : {} - {}", new Object[]{
                        host,
                        e.getMessage()
                });
            }
            return command;
        }


        // ---- separation command stream for further processing ----
        public void handleCommand(Command command) {
            if (command.getType() != CommandType.Ping) {
                LOG.debug("host : {} - receive command : {}", new Object[]{
                        host,
                        command.getType().name()
                });
                if (commander != null) commander.execute(command);
            } else {
                backPingToServer(command);
            }
        }


        // ---- send command to server output stream ----
        public void sendCommand(Command command) {
            try {
                if (output != null) output.writeObject(command);
                switch (command.getType()) {
                    case Ping:

                        break;

                    case Handshake:
                        LOG.debug("host : {} - Done handshake with server ( Gate ID = {} )", new Object[]{
                                host,
                                ((HandshakeCommand) command).getGateId()
                        });
                        break;

                    default:
                        LOG.debug("host : {} - Send command to server : {}", new Object[]{
                                host,
                                command.getType().name()
                        });
                }
            } catch (IOException e) {
                LOG.error("[IOException] - host : {} - {}", new Object[]{
                        host,
                        e.getMessage()
                });
            }
        }


        // ---- handle command input stream ----
        @Override
        protected void body() {

            needStopLink = false;
            if (!open()) return;

            while (!needStopLink) {

                Command command = receiveCommandFromServer();
                if (command != null) {
                    handleCommand(command);
                } else needStopLink = true;
            }
        }


        // ---- stop handle and close handler ----
        @Override
        protected void destroy() {

            try {
                if (input != null) input.close();
                if (output != null) output.close();
                if (socket != null) socket.close();
            } catch (IOException e) {
                LOG.error("[IOException] - host : {} - {}", new Object[]{
                        host,
                        e.getMessage()
                });
            }
            socket = null;
            input = null;
            output = null;

        }

    }

}
