package ru.uproom.gate.transport.command;

/**
 * Created with IntelliJ IDEA.
 * User: zhulant
 * Date: 8/29/14
 * Time: 12:48 PM
 */
public class HandshakeCommand extends Command {
    private static final long serialVersionUID = 1650503184269712454L;
    private int gateId;

    public HandshakeCommand(int gateId) {
        super(CommandType.Handshake);
        this.gateId = gateId;
    }

    public int getGateId() {
        return gateId;
    }
}
