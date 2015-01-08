package ru.uproom.gate.transport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.uproom.gate.commands.GateCommander;
import ru.uproom.gate.transport.command.Command;
import ru.uproom.gate.transport.command.CommandType;
import ru.uproom.gate.transport.command.HandshakeCommand;
import ru.uproom.gate.transport.domain.DelayTimer;

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
    private ConnectionHandler connectionHandler;


    //##############################################################################################################
    //######    constructors


    //------------------------------------------------------------------------
    //  initialization / reinitialization

    @PostConstruct
    public void init() {

        new Thread(connectionChecker).start();

        createConnect();
    }


    //##############################################################################################################
    //######    getters & setters


    //##############################################################################################################
    //######    inner classes


    //------------------------------------------------------------------------
    //  check connection

    public void backPingToServer(Command ping) {

        synchronized (connectionChecker) {
            connectionChecker.toggleCheck(true);
            connectionChecker.notify();
        }
        sendCommand(ping);

    }


    //------------------------------------------------------------------------
    //  keep connection

    public void createConnect() {
        connectionHandler = new ConnectionHandler();
        new Thread(connectionHandler).start();
    }


    //##############################################################################################################
    //######    methods


    //------------------------------------------------------------------------
    //  ping go back to server

    @PreDestroy
    public void stop() {

        synchronized (connectionChecker) {
            connectionChecker.closeCheck();
            connectionChecker.notify();
        }

        stopLink(false);
    }


    //------------------------------------------------------------------------
    //  create connection

    public void stopLink(boolean restart) {

        synchronized (connectionChecker) {
            connectionChecker.toggleCheck(false);
            connectionChecker.notify();
        }
        connectionHandler.stopHandle();

        if (restart) {
            DelayTimer.sleep(periodBetweenAttempts);
            createConnect();
        }

    }


    //------------------------------------------------------------------------
    //  close connection

    @Override
    public void sendCommand(Command command) {
        connectionHandler.sendCommand(command);
    }


    //------------------------------------------------------------------------
    //  close/restart connection

    private class ConnectionChecker implements Runnable {

        private boolean stopped;
        private boolean check;
        private long currentTime;


        // ---- close checker ----
        public void closeCheck() {
            stopped = true;
        }


        // ---- checking on/off ----
        public void toggleCheck(boolean check) {
            if (!this.check && check) {
                LOG.info("gate HAVE a ping command from server ({}) - LINK SET ON", new Object[]{
                        host
                });
            }
            if (this.check && !check) {
                LOG.info("gate LOST a ping command from server ({}) - LINK SET OFF", new Object[]{
                        host
                });
            }
            this.check = check;
        }


        // ---- wait handler ----
        private void waitForNotify(long period) {
            try {
                wait(period);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


        // ---- checking quality of communication ----
        @Override
        public void run() {

            while (!stopped) {
                synchronized (this) {
                    if (check) {
                        currentTime = System.currentTimeMillis();
                        waitForNotify(periodWaitPing);
                        if (stopped) continue;
                        long dif = System.currentTimeMillis() - currentTime;
                        if (dif >= periodWaitPing) {
                            toggleCheck(false);
                            stopLink(true);
                        }
                    } else waitForNotify(1000);
                }
            }

        }
    }


    //------------------------------------------------------------------------
    //  send command to all interesting servers

    private class ConnectionHandler implements Runnable {


        private Socket socket;
        private ObjectInputStream input;
        private ObjectOutputStream output;

        private boolean stopped;
        private boolean needStop = true;


        // ---- stop handle and close handler ----
        public void stopHandle() {

            stopped = true;
            this.needStop = false;

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
        public void run() {

            stopped = !open();

            Command command = null;
            while (!stopped) {

                command = receiveCommandFromServer();
                if (command != null) {
                    handleCommand(command);
                } else stopped = true;

            }

            if (needStop) stopLink(true);

        }

    }

}
