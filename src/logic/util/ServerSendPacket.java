package logic.util;

import java.io.Serializable;

/** Represents a packet sent by the server to the client */
public class ServerSendPacket implements Serializable {
    public int totalPlayers;
    public int clientID;
    public boolean status;
    public ServerTile[][] tiles;
}
