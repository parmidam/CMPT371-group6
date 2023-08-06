package logic.util;

import java.io.Serializable;

/** Represents a packet sent by the server to the client */
public class ServerSendPacket implements Serializable {
    /** This is required for de-serialization on a different computer */
    private static final long serialVersionUID = 96475812L;

    public int totalPlayers;
    public int clientID;
    public boolean status;
    public ServerTile[][] tiles;
}
