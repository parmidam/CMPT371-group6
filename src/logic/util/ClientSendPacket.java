package logic.util;

import java.io.Serializable;

import javafx.util.Pair;

/** Represents a packet sent by the client to the server */
public class ClientSendPacket implements Serializable {
    public String type;
    public Pair<Integer,Integer> point;
}
