package logic.util;

import java.io.Serializable;

public class ServerSendPacket implements Serializable {

    public int totalPlayers;
    public int id;
    public boolean status;
    public ServerTile[][] map;

}
