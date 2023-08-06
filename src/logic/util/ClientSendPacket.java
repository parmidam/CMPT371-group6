package logic.util;

import java.io.Serializable;

import javafx.util.Pair;

/** Represents a packet sent by the client to the server */
public class ClientSendPacket implements Serializable {
    /** This is required for de-serialization on a different computer */
    private static final long serialVersionUID = 54426863L;

    public String type;
    public Pair<Integer,Integer> point;
}
